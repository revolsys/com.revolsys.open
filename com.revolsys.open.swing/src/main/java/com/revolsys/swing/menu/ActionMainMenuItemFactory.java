package com.revolsys.swing.menu;

import java.awt.Component;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;

import com.revolsys.swing.component.ComponentFactory;

public class ActionMainMenuItemFactory implements ComponentFactory<JMenuItem> {

  private boolean checkBox;

  private final Action action;

  public ActionMainMenuItemFactory(final Action action) {
    this.action = action;
  }

  @Override
  public void close(final Component component) {

  }

  @Override
  public JMenuItem createComponent() {
    if (checkBox) {
      return new JCheckBoxMenuItem(action);
    } else {
      return new JMenuItem(action);
    }
  }

  @Override
  public final Icon getIcon() {
    return (Icon)action.getValue(Action.SMALL_ICON);
  }

  @Override
  public String getName() {
    return (String)action.getValue(Action.NAME);
  }

  @Override
  public String getToolTip() {
    return (String)action.getValue(Action.SHORT_DESCRIPTION);
  }

  public boolean isCheckBox() {
    return checkBox;
  }

}
