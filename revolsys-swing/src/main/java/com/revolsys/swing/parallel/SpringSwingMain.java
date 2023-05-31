package com.revolsys.swing.parallel;

import java.awt.Image;
import java.io.File;
import java.lang.Thread.UncaughtExceptionHandler;
import java.time.Instant;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

import org.jeometry.common.logging.Logs;
import org.springframework.beans.factory.InitializingBean;

import com.revolsys.log.LogbackUtil;
import com.revolsys.reactor.scheduler.SwingUiScheduler;
import com.revolsys.swing.desktop.DesktopInitializer;
import com.revolsys.swing.logging.ListLoggingAppender;
import com.revolsys.swing.logging.LoggingEventPanel;
import com.revolsys.util.Property;
import com.revolsys.util.Strings;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.classic.spi.ThrowableProxy;
import ch.qos.logback.core.Appender;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

public class SpringSwingMain implements UncaughtExceptionHandler, InitializingBean {

  protected Set<File> initialFiles = new LinkedHashSet<>();

  private String lookAndFeelName;

  private final String name;

  public SpringSwingMain(final String name) {
    this.name = name;
    Thread.setDefaultUncaughtExceptionHandler(this);
  }

  @Override
  public void afterPropertiesSet() {
    new Thread(() -> {
      final CountDownLatch latch = new CountDownLatch(1);
      Mono.just(true)//
        .subscribeOn(Schedulers.boundedElastic())//
        .flatMap(this::swingBefore)
        .publishOn(SwingUiScheduler.INSTANCE)
        .flatMap(this::swingInit)
        .flatMap(this::swingAfter)
        .publishOn(Schedulers.boundedElastic())//
        .flatMap(this::appBefore)
        .publishOn(SwingUiScheduler.INSTANCE)
        .flatMap(this::appSwingInit)
        .doOnError(this::logError)
        .doAfterTerminate(() -> latch.countDown())
        .subscribe();
      try {
        latch.await();
      } catch (final InterruptedException e) {
      }
    }).start();
  }

  protected Mono<Boolean> appBefore(final boolean success) {
    return Mono.just(true);
  }

  protected Mono<Boolean> appSwingInit(final boolean success) {
    return Mono.just(true);
  }

  public void logError(final Throwable e) {
    try {
      final Instant timestamp = Instant.now();
      final String threadName = Thread.currentThread().getName();
      final String stackTrace = e.getMessage() + "\n" + Strings.toString("\n", e.getStackTrace());
      LoggingEventPanel.showDialog(timestamp, Level.ERROR, getClass().getName(),
        "Unable to start application:" + e.getMessage(), threadName, stackTrace);
    } finally {
      Logs.error(this, "Unable to start application " + this.name, e);
    }
  }

  public void setLookAndFeelName(final String lookAndFeelName) {
    this.lookAndFeelName = lookAndFeelName;
  }

  protected void setMacDockIcon(final Image image) {
    try {
      DesktopInitializer.initialize(image, this.initialFiles);
    } catch (final Throwable t) {
      t.printStackTrace();
    }
  }

  protected Mono<Boolean> swingAfter(final boolean success) {
    return Mono.just(true);
  }

  protected Mono<Boolean> swingBefore(final boolean success) {
    return Mono.just(true);
  }

  private Mono<Boolean> swingInit(final boolean success) {
    try {
      boolean lookSet = false;
      if (Property.hasValue(this.lookAndFeelName)) {
        final LookAndFeelInfo[] installedLookAndFeels = UIManager.getInstalledLookAndFeels();
        for (final LookAndFeelInfo lookAndFeelInfo : installedLookAndFeels) {
          final String name = lookAndFeelInfo.getName();
          if (this.lookAndFeelName.equals(name)) {
            try {
              final String className = lookAndFeelInfo.getClassName();
              UIManager.setLookAndFeel(className);
              lookSet = true;
            } catch (final Throwable e) {
            }
          }
        }
      }
      if (!lookSet) {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
      }
      JFrame.setDefaultLookAndFeelDecorated(true);
      JDialog.setDefaultLookAndFeelDecorated(true);
      ToolTipManager.sharedInstance().setInitialDelay(100);
      return Mono.just(true);
    } catch (final Throwable e) {
      return Mono.error(e);
    }
  }

  @Override
  public void uncaughtException(final Thread t, final Throwable e) {
    final Class<? extends SpringSwingMain> logClass = getClass();
    String message = e.getMessage();
    if (!Property.hasValue(message)) {
      if (e instanceof NullPointerException) {
        message = "Null pointer";
      } else {
        message = "Unknown error";
      }
    }
    Logs.error(logClass, message, e);
    final Logger rootLogger = LogbackUtil.getRootLogger();
    for (final Iterator<Appender<ILoggingEvent>> iterator = rootLogger
      .iteratorForAppenders(); iterator.hasNext();) {
      final Appender<ILoggingEvent> appender = iterator.next();
      if (appender instanceof ListLoggingAppender) {
        return;
      }
    }
    final Logger logger = LogbackUtil.getLogger(logClass);
    final String name = logger.getClass().getName();
    final LoggingEvent event = new LoggingEvent();
    event.setLoggerName(name);
    event.setLevel(Level.INFO);
    event.setMessage(message.toString());
    event.setThrowableProxy(new ThrowableProxy(e));

    LoggingEventPanel.showDialog(event);
  }
}
