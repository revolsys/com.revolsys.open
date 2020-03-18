package com.revolsys.swing.i18n;

import java.awt.GraphicsConfiguration;
import java.awt.HeadlessException;

import javax.swing.JFrame;

import com.revolsys.i18n.I18n;

public class I18nJFrame extends JFrame {

  /**
   *
   */
  private static final long serialVersionUID = 205569343155489552L;

  public I18nJFrame() throws HeadlessException {
  }

  public I18nJFrame(final Class<?> clazz, final String title) throws HeadlessException {
    setTitle(title);
  }

  public I18nJFrame(final Class<?> clazz, final String title, final GraphicsConfiguration gc) {
    super(gc);
    setTitle(title);
  }

  public I18nJFrame(final GraphicsConfiguration gc) {
    super(gc);
  }

  public I18nJFrame(final String title) {
    setTitle(title);
  }

  public I18nJFrame(final String title, final GraphicsConfiguration gc) {
    this(gc);
    setTitle(getClass(), title);
  }

  private void setTitle(final Class<?> clazz, final String title) {
    setTitle(I18n.getString(clazz, title));
  }

}
