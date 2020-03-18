package com.revolsys.swing.component;

import java.awt.Desktop;
import java.awt.HeadlessException;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.JFrame;
import javax.swing.JMenuBar;

import org.jeometry.common.logging.Logs;

import com.revolsys.swing.WindowManager;
import com.revolsys.swing.parallel.Invoke;

public class BaseFrame extends JFrame implements WindowListener {
  private static final long serialVersionUID = 1L;

  private JMenuBar menuBar;

  public BaseFrame() throws HeadlessException {
    this(true);
  }

  public BaseFrame(final boolean initialize) throws HeadlessException {
    if (initialize) {
      initUi();
    }
  }

  public BaseFrame(final String title) throws HeadlessException {
    this(title, true);
  }

  public BaseFrame(final String title, final boolean initialize) throws HeadlessException {
    super(title);
    if (initialize) {
      initUi();
    }
  }

  protected void close() {
    setJMenuBar(null);
    removeWindowListener(this);
    WindowManager.removeWindow(this);
  }

  @Override
  public void dispose() {
    close();
    try {
      super.dispose();
    } catch (final IllegalStateException e) {
      Logs.debug(this, e);
    }
  }

  protected void initUi() {
    setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
    addWindowListener(this);
    this.menuBar = newMenuBar();
    updateMenuBar();
  }

  protected JMenuBar newMenuBar() {
    final JMenuBar menuBar = new JMenuBar();
    setJMenuBar(menuBar);
    WindowManager.addMenu(menuBar);
    return menuBar;
  }

  @Override
  public void setVisible(final boolean visible) {
    Invoke.later(() -> {
      if (visible) {
        WindowManager.addWindow(this);
      } else {
        WindowManager.removeWindow(this);
      }
      final boolean oldVisible = isVisible();
      super.setVisible(visible);
      if (!visible && oldVisible) {
        close();
      }
    });
  }

  private void updateMenuBar() {
    final Desktop desktop = Desktop.getDesktop();
    if (desktop.isSupported(Desktop.Action.APP_MENU_BAR)) {
      desktop.setDefaultMenuBar(this.menuBar);
    }
  }

  @Override
  public void windowActivated(final WindowEvent e) {
    updateMenuBar();
  }

  @Override
  public void windowClosed(final WindowEvent e) {
    dispose();
  }

  @Override
  public void windowClosing(final WindowEvent e) {
    setVisible(false);
  }

  @Override
  public void windowDeactivated(final WindowEvent e) {
  }

  @Override
  public void windowDeiconified(final WindowEvent e) {
  }

  @Override
  public void windowIconified(final WindowEvent e) {
  }

  @Override
  public void windowOpened(final WindowEvent e) {
  }

}
