package com.revolsys.swing.component;

import java.awt.HeadlessException;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.JFrame;
import javax.swing.JMenuBar;

import com.revolsys.logging.Logs;
import com.revolsys.swing.WindowManager;
import com.revolsys.swing.parallel.Invoke;

public class BaseFrame extends JFrame implements WindowListener {
  private static final long serialVersionUID = 1L;

  public BaseFrame() throws HeadlessException {
    this(true);
  }

  public BaseFrame(final boolean iniaitlize) throws HeadlessException {
    if (iniaitlize) {
      initUi();
    }
  }

  public BaseFrame(final String title) throws HeadlessException {
    this(title, true);
  }

  public BaseFrame(final String title, final boolean iniaitlize) throws HeadlessException {
    super(title);
    if (iniaitlize) {
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
    newMenuBar();
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

  @Override
  public void windowActivated(final WindowEvent e) {
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
