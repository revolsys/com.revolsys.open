package com.revolsys.swing.parallel;

import java.awt.Image;
import java.io.File;
import java.lang.Thread.UncaughtExceptionHandler;
import java.sql.Timestamp;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.impl.Log4jLogEvent;
import org.apache.logging.log4j.message.SimpleMessage;
import org.jeometry.common.logging.Logs;

import com.revolsys.swing.desktop.DesktopInitializer;
import com.revolsys.swing.logging.ListLog4jAppender;
import com.revolsys.swing.logging.LoggingEventPanel;
import com.revolsys.util.Property;
import com.revolsys.util.Strings;

public class BaseMain implements UncaughtExceptionHandler {

  public static void run(final Class<? extends BaseMain> mainClass, final String[] args) {
    try {
      final BaseMain main = mainClass.newInstance();
      main.processArguments(args);
      main.run();
    } catch (final Throwable e) {
      Logs.error(mainClass, e);
    }
  }

  protected Set<File> initialFiles = new LinkedHashSet<>();

  private String lookAndFeelName;

  private final String name;

  public BaseMain(final String name) {
    this.name = name;
    Thread.setDefaultUncaughtExceptionHandler(this);
  }

  public String getLookAndFeelName() {
    return this.lookAndFeelName;
  }

  public void logError(final Throwable e) {
    final LogEvent event = Log4jLogEvent.newBuilder()//
      .setLoggerName(getClass().getName()) //
      .setLevel(Level.ERROR) //
      .setMessage(new SimpleMessage("Unable to start application".toString())) //
      .setThrown(e)
      .build();

    final Timestamp timestamp = new Timestamp(System.currentTimeMillis());
    final String threadName = Thread.currentThread().getName();
    final String stackTrace = Strings.toString("\n", e.getStackTrace());
    LoggingEventPanel.showDialog(null, timestamp, Level.ERROR, getClass().getName(),
      "Unable to start application", threadName, stackTrace);
    Logs.error(this, "Unable to start application " + this.name, e);
  }

  protected void preRunDo() throws Throwable {

  }

  public void processArguments(final String[] args) {
  }

  public void run() {
    try {
      preRunDo();
      Invoke.later(() -> {
        try {
          runDo();
        } catch (final Throwable e) {
          logError(e);
        }
      });
    } catch (final Throwable e) {
      logError(e);
    }
  }

  protected void runDo() throws Throwable {
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

  @Override
  public void uncaughtException(final Thread t, final Throwable e) {
    final Class<? extends BaseMain> logClass = getClass();
    String message = e.getMessage();
    if (!Property.hasValue(message)) {
      if (e instanceof NullPointerException) {
        message = "Null pointer";
      } else {
        message = "Unknown error";
      }
    }
    Logs.error(logClass, message, e);
    final Logger rootLogger = (Logger)LogManager.getRootLogger();
    for (final Appender appender : rootLogger.getAppenders().values()) {
      if (appender instanceof ListLog4jAppender) {
        return;
      }
    }
    final Logger logger = (Logger)LogManager.getLogger(logClass);
    final String name = logger.getClass().getName();
    final LogEvent event = Log4jLogEvent.newBuilder()//
      .setLoggerName(name) //
      .setLevel(Level.ERROR) //
      .setMessage(new SimpleMessage(message)) //
      .setThrown(e)
      .build();

    LoggingEventPanel.showDialog(null, event);
  }
}
