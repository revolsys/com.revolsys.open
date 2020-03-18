package com.revolsys.swing.i18n;

import javax.swing.Icon;
import javax.swing.JPanel;

public class NamedJPanel extends JPanel {
  /**
   *
   */
  private static final long serialVersionUID = 226865239947837466L;

  private Icon icon;

  private CharSequence name;

  public NamedJPanel(final CharSequence name, final Icon icon) {
    setName(name);
    this.icon = icon;
  }

  public Icon getIcon() {
    return this.icon;
  }

  @Override
  public String getName() {
    if (this.name != null) {
      return this.name.toString();
    } else {
      return super.getName();
    }
  }

  public void setIcon(final Icon icon) {
    this.icon = icon;
  }

  public void setName(final CharSequence name) {
    this.name = name;
  }

  @Override
  public void setName(final String name) {
    if (this.name != null) {
      this.name = null;
    } else {
      super.setName(name);
    }
  }
}
