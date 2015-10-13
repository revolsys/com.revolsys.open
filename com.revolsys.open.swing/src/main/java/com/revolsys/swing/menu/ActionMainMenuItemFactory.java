package com.revolsys.swing.menu;

import java.awt.Component;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JMenuItem;

import com.revolsys.swing.action.AbstractAction;
import com.revolsys.swing.action.RunnableAction;
import com.revolsys.swing.action.enablecheck.EnableCheck;
import com.revolsys.swing.component.ComponentFactory;
import com.revolsys.util.Exceptions;

public class ActionMainMenuItemFactory implements ComponentFactory<JMenuItem> {

  private final AbstractAction action;

  private EnableCheck checkBoxSelectedCheck;

  private String iconName;

  public ActionMainMenuItemFactory(final AbstractAction action) {
    this.action = action;
    if (action instanceof RunnableAction) {
      final RunnableAction runnableAction = (RunnableAction)action;
      this.iconName = runnableAction.getIconName();
    }
  }

  public ActionMainMenuItemFactory(final EnableCheck checkBoxSelectedCheck,
    final AbstractAction action) {
    this(action);
    this.checkBoxSelectedCheck = checkBoxSelectedCheck;
  }

  @Override
  public ActionMainMenuItemFactory clone() {
    try {
      return (ActionMainMenuItemFactory)super.clone();
    } catch (final CloneNotSupportedException e) {
      return Exceptions.throwUncheckedException(e);
    }
  }

  @Override
  public void close(final Component component) {

  }

  @Override
  public JMenuItem createComponent() {
    if (this.checkBoxSelectedCheck == null) {
      return this.action.createMenuItem();
    } else {
      final CheckBoxMenuItem menuItem = this.action.createCheckboxMenuItem();
      menuItem.setSelectedCheck(this.checkBoxSelectedCheck);
      return menuItem;
    }
  }

  @Override
  public final Icon getIcon() {
    return (Icon)this.action.getValue(Action.SMALL_ICON);
  }

  @Override
  public String getIconName() {
    return this.iconName;
  }

  @Override
  public String getName() {
    return (String)this.action.getValue(Action.NAME);
  }

  @Override
  public String getToolTip() {
    return (String)this.action.getValue(Action.SHORT_DESCRIPTION);
  }

  @Override
  public String toString() {
    return this.action.toString();
  }
}
