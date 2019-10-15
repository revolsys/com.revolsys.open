package com.revolsys.swing.component;

import java.awt.Window;

import javax.swing.JDialog;

import org.jeometry.common.logging.Logs;

import com.revolsys.swing.Dialogs;
import com.revolsys.swing.WindowManager;

public class BaseDialog extends JDialog {
  private static final long serialVersionUID = 1L;

  public BaseDialog() {
    super();
  }

  public BaseDialog(final String title, final ModalityType modalityType) {
    this(Dialogs.getWindow(), title, modalityType);
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
