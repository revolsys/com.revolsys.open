package com.revolsys.swing.menu;

import java.awt.Component;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JMenuItem;

import org.jeometry.common.exception.Exceptions;

import com.revolsys.swing.action.AbstractAction;
import com.revolsys.swing.action.RunnableAction;
import com.revolsys.swing.action.enablecheck.EnableCheck;
import com.revolsys.swing.component.ComponentFactory;

public class ActionMainMenuItemFactory implements ComponentFactory<JMenuItem> {

  private AbstractAction action;

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
      final ActionMainMenuItemFactory clone = (ActionMainMenuItemFactory)super.clone();
      this.action = this.action.clone();
      return clone;
    } catch (final CloneNotSupportedException e) {
      return Exceptions.throwUncheckedException(e);
    }
  }

  @Override
  public void close(final Component component) {

  }

  public AbstractAction getAction() {
    return this.action;
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
  public JMenuItem newComponent() {
    if (this.checkBoxSelectedCheck == null) {
      return this.action.newMenuItem();
    } else {
      final CheckBoxMenuItem menuItem = this.action.newCheckboxMenuItem();
      menuItem.setSelectedCheck(this.checkBoxSelectedCheck);
      return menuItem;
    }
  }

  @Override
  public String toString() {
    return this.action.toString();
  }
}
