package com.revolsys.swing.menu;

import java.awt.Component;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;

import com.revolsys.swing.component.ComponentFactory;

public abstract class AbstractActionMainMenuItemFactory extends AbstractAction
  implements ComponentFactory<JMenuItem> {

  private boolean checkBox;

  @Override
  public void close(Component component) {
  }

  @Override
  public JMenuItem createComponent() {
    if (checkBox) {
      return new JCheckBoxMenuItem(this);
    } else {
      return new JMenuItem(this);
    }
  }

  public final Icon getIcon() {
    return (Icon)getValue(Action.SMALL_ICON);
  }

  public Integer getMnemonic() {
    return (Integer)getValue(Action.MNEMONIC_KEY);
  }

  public String getName() {
    return (String)getValue(Action.NAME);
  }

  public String getToolTip() {
    return (String)getValue(Action.SHORT_DESCRIPTION);
  }

  public boolean isCheckBox() {
    return checkBox;
  }

  protected void setCheckBox(final boolean checkBox) {
    this.checkBox = checkBox;
  }

  protected void setIcon(final Icon icon) {
    putValue(Action.SMALL_ICON, icon);
  }

  protected void setName(final String name) {
    putValue(Action.NAME, name);
  }

  protected void setToolTip(final String toolTip) {
    putValue(Action.SHORT_DESCRIPTION, toolTip);
  }
}
