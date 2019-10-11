package com.revolsys.swing.component;

import java.awt.Dialog;
import java.awt.Frame;
import java.awt.GraphicsConfiguration;
import java.awt.Window;

import javax.swing.JDialog;

import org.jeometry.common.logging.Logs;

import com.revolsys.swing.SwingUtil;
import com.revolsys.swing.WindowManager;

public class BaseDialog extends JDialog {
  private static final long serialVersionUID = 1L;

  static Window windowParent() {
    final Window activeWindow = SwingUtil.windowActive();
    if (activeWindow == null) {
      final JDialog window = new JDialog(activeWindow);
      window.setAlwaysOnTop(true);
      window.requestFocus();
      return window;
    } else {
      activeWindow.toFront();
      return activeWindow;
    }
  }

  public BaseDialog() {
    super();
  }

  public BaseDialog(final Dialog owner) {
    super(owner);
  }

  public BaseDialog(final Dialog owner, final boolean modal) {
    super(owner, modal);
  }

  public BaseDialog(final Dialog owner, final String title) {
    super(owner, title);
  }

  public BaseDialog(final Dialog owner, final String title, final boolean modal) {
    super(owner, title, modal);
  }

  public BaseDialog(final Dialog owner, final String title, final boolean modal,
    final GraphicsConfiguration gc) {
    super(owner, title, modal, gc);
  }

  public BaseDialog(final Frame owner) {
    super(owner);
  }

  public BaseDialog(final Frame owner, final boolean modal) {
    super(owner, modal);
  }

  public BaseDialog(final Frame owner, final String title) {
    super(owner, title);
  }

  public BaseDialog(final Frame owner, final String title, final boolean modal) {
    super(owner, title, modal);
  }

  public BaseDialog(final Frame owner, final String title, final boolean modal,
    final GraphicsConfiguration gc) {
    super(owner, title, modal, gc);
  }

  public BaseDialog(final String title, final ModalityType modalityType) {
    this(windowParent(), title, modalityType);
  }

  public BaseDialog(final Window owner) {
    super(owner);
  }

  public BaseDialog(final Window owner, final ModalityType modalityType) {
    super(owner, modalityType);
  }

  public BaseDialog(final Window owner, final String title) {
    super(owner, title);
  }

  public BaseDialog(final Window owner, final String title, final ModalityType modalityType) {
    super(owner, title, modalityType);
  }

  public BaseDialog(final Window owner, final String title, final ModalityType modalityType,
    final GraphicsConfiguration gc) {
    super(owner, title, modalityType, gc);
  }

  @Override
  public void dispose() {
    WindowManager.removeWindow(this);
    try {
      super.dispose();
    } catch (final Throwable e) {
      Logs.debug(this, e);
    }
  }

  @Override
  public void setTitle(final String title) {
    super.setTitle(title);
    WindowManager.updateWindowTitle(this);
  }

  @Override
  public void setVisible(final boolean visible) {
    if (visible) {
      WindowManager.addWindow(this);
    } else {
      WindowManager.removeWindow(this);
    }
    super.setVisible(visible);
  }
}
