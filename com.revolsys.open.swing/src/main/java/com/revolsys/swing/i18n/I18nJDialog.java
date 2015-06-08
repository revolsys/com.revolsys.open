package com.revolsys.swing.i18n;

import java.awt.GraphicsConfiguration;
import java.awt.Window;

import javax.swing.JDialog;

import com.revolsys.i18n.I18n;

public class I18nJDialog extends JDialog {

  /**
   *
   */
  private static final long serialVersionUID = -5408536069204690176L;

  public I18nJDialog() {
  }

  public I18nJDialog(final Window owner) {
    super(owner);
  }

  public I18nJDialog(final Window owner, final ModalityType modalityType) {
    super(owner, modalityType);
  }

  public I18nJDialog(final Window owner, final String title) {
    super(owner, title);
    setTitle(title);
  }

  public I18nJDialog(final Window owner, final String title, final ModalityType modalityType) {
    super(owner, title, modalityType);
    setTitle(title);
  }

  public I18nJDialog(final Window owner, final String title, final ModalityType modalityType,
    final GraphicsConfiguration gc) {
    super(owner, title, modalityType, gc);
    setTitle(title);
  }

  public void setTitle(final Class<?> clazz, final String title) {
    final String i18nTitle = I18n.getString(clazz, title);
    super.setTitle(i18nTitle);
  }

  @Override
  public void setTitle(final String title) {
    final Class<?> clazz = getClass();
    setTitle(clazz, title);
  }

}
