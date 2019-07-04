package com.revolsys.swing.menu;

import javax.swing.Action;
import javax.swing.JMenu;

public class I18nMenu extends JMenu {
  private static final long serialVersionUID = 1L;

  private CharSequence label;

  public I18nMenu() {
  }

  public I18nMenu(final Action action) {
    super(action);
  }

  public I18nMenu(final CharSequence label) {
    this.label = label;
  }

  public I18nMenu(final CharSequence label, final boolean tearOff) {
    super(label.toString(), tearOff);
    this.label = label;
  }

  @Override
  public String getText() {
    if (this.label == null) {
      return super.getText();
    } else {
      return this.label.toString();
    }
  }
}
