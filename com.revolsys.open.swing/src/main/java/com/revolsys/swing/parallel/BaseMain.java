package com.revolsys.swing.parallel;

import java.awt.Image;
import java.lang.Thread.UncaughtExceptionHandler;
import java.lang.reflect.Method;
import java.util.Enumeration;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;

import org.apache.commons.beanutils.MethodUtils;
import org.apache.log4j.Appender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggingEvent;

import com.revolsys.swing.logging.ListLog4jAppender;
import com.revolsys.swing.logging.LoggingEventPanel;
import com.revolsys.util.ExceptionUtil;

public class BaseMain implements UncaughtExceptionHandler {

  public static void run(final Class<? extends BaseMain> mainClass,
    final String[] args) {
    try {
      final BaseMain main = mainClass.newInstance();
      main.processArguments(args);
      main.run();
    } catch (final Throwable e) {
      ExceptionUtil.log(mainClass, e);
    }
  }

  protected static void setMacDockIcon(final Image image) {
    try {
      final Class<?> clazz = Class.forName("com.apple.eawt.Application");
      final Method appMethod = clazz.getMethod("getApplication");
      final Object application = appMethod.invoke(clazz);
      MethodUtils.invokeMethod(application, "setDockIconImage", image);
    } catch (final Throwable t) {
    }
  }

  private final String name;

  private final boolean swing;

  public BaseMain(final String name, final boolean swing) {
    Thread.setDefaultUncaughtExceptionHandler(this);
    this.name = name;
    this.swing = swing;
  }

  public void doPreRun() throws Throwable {

  }

  public void doRun() throws Throwable {
    JFrame.setDefaultLookAndFeelDecorated(true);
    JDialog.setDefaultLookAndFeelDecorated(true);
    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    ToolTipManager.sharedInstance().setInitialDelay(100);
  }

  public void processArguments(final String[] args) {
  }

  public void run() {
    try {
      if (swing) {
        System.setProperty("awt.useSystemAAFontSettings", "lcd");
        System.setProperty("apple.laf.useScreenMenuBar", "true");
        System.setProperty("com.apple.mrj.application.apple.menu.about.name",
          name);
      }
      doPreRun();
      if (swing) {
        SwingWorkerManager.invokeLater(this, "doRun");
      } else {
        doRun();
      }
    } catch (final Throwable e) {
      final Logger logger = Logger.getLogger(getClass());
      final LoggingEvent event = new LoggingEvent(logger.getName(), logger,
        Level.ERROR, "Unable to start application", e);

      LoggingEventPanel.showDialog(null, event);
      ExceptionUtil.log(getClass(), "Unable to start application " + name, e);
    }
  }

  @Override
  public void uncaughtException(final Thread t, final Throwable e) {
    Class<? extends BaseMain> logClass = getClass();
    ExceptionUtil.log(logClass, "Unable to start application " + name, e);
    @SuppressWarnings("unchecked")
    final Enumeration<Appender> allAppenders = Logger.getRootLogger()
      .getAllAppenders();
    while (allAppenders.hasMoreElements()) {
      final Appender appender = allAppenders.nextElement();
      if (appender instanceof ListLog4jAppender) {
        return;
      }
    }
    final Logger logger = Logger.getLogger(logClass);
    final LoggingEvent event = new LoggingEvent(logger.getName(), logger,
      Level.ERROR, "Unable to start application", e);

    LoggingEventPanel.showDialog(null, event);

  }
}
