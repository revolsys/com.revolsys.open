package com.revolsys.swing.menu;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JMenuItem;

@SuppressWarnings("serial")
public class I18nMenuItem extends JMenuItem {
  private CharSequence label;

  public I18nMenuItem() {
  }

  public I18nMenuItem(final Action a) {
    super(a);
  }

  public I18nMenuItem(final CharSequence text) {
    super(text.toString());
    this.label = text;
  }

  public I18nMenuItem(final CharSequence text, final Icon icon) {
    super(text.toString(), icon);
  }

  public I18nMenuItem(final CharSequence text, final int mnemonic) {
    super(text.toString(), mnemonic);
  }

  public I18nMenuItem(final Icon icon) {
    super(icon);
  }
}
