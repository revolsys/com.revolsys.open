package com.revolsys;

import com.revolsys.awt.SwingWorkerManager;
import com.revolsys.logging.Slf4jUncaughtExceptionHandler;
import com.revolsys.util.ExceptionUtil;

public class BaseMain {
  static {
    Slf4jUncaughtExceptionHandler.init();
  }

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

  public static void startUi(final Class<? extends BaseMain> mainClass,
    final String[] args) {
    try {
      final BaseMain main = mainClass.newInstance();
      main.processArguments(args);
      main.startUi();
    } catch (final Throwable e) {
      ExceptionUtil.log(mainClass, e);
    }
  }

  private final String name;

  public BaseMain(final String name) {
    this.name = name;
  }

  public void doPreStartUi() {

  }

  public void doStartUi() {

  }

  public void processArguments(final String[] args) {
  }

  public void run() {
  }

  public void startUi() {
    System.setProperty("awt.useSystemAAFontSettings", "lcd");
    System.setProperty("apple.laf.useScreenMenuBar", "true");
    System.setProperty("com.apple.mrj.application.apple.menu.about.name", name);
    doPreStartUi();
    SwingWorkerManager.invokeLater(this, "doStartUi");
  }
}
