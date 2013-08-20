package com.revolsys.swing.menu;

import java.awt.Component;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;

import com.revolsys.swing.action.enablecheck.EnableCheck;
import com.revolsys.swing.component.ComponentFactory;
import com.revolsys.util.ExceptionUtil;

public class ActionMainMenuItemFactory implements ComponentFactory<JMenuItem> {

  private EnableCheck checkBoxSelectedCheck;

  private final Action action;

  public ActionMainMenuItemFactory(final Action action) {
    this.action = action;
  }

  public ActionMainMenuItemFactory(final EnableCheck checkBoxSelectedCheck,
    final Action action) {
    this(action);
    this.checkBoxSelectedCheck = checkBoxSelectedCheck;
  }

  @Override
  public ActionMainMenuItemFactory clone() {
    try {
      return (ActionMainMenuItemFactory)super.clone();
    } catch (final CloneNotSupportedException e) {
      return ExceptionUtil.throwUncheckedException(e);
    }
  }

  @Override
  public void close(final Component component) {

  }

  @Override
  public JMenuItem createComponent() {
    if (this.checkBoxSelectedCheck != null) {
      final JCheckBoxMenuItem menuItem = new JCheckBoxMenuItem(this.action);
      menuItem.setSelected(this.checkBoxSelectedCheck.isEnabled());
      return menuItem;
    } else {
      return new JMenuItem(this.action);
    }
  }

  @Override
  public final Icon getIcon() {
    return (Icon)this.action.getValue(Action.SMALL_ICON);
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
