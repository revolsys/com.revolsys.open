package com.revolsys.swing.menu;

import java.awt.Component;

import javax.swing.Icon;
import javax.swing.JMenuItem;

import com.revolsys.swing.action.AbstractAction;
import com.revolsys.swing.action.enablecheck.EnableCheck;
import com.revolsys.swing.component.ComponentFactory;

public abstract class AbstractActionMainMenuItemFactory extends AbstractAction
  implements ComponentFactory<JMenuItem> {

  private static final long serialVersionUID = 1L;

  private EnableCheck visibleCheck = EnableCheck.ENABLED;

  private String iconName;

  public AbstractActionMainMenuItemFactory() {
    super();
  }

  public AbstractActionMainMenuItemFactory(final CharSequence name, final String toolTip,
    final Icon icon) {
    super(name, toolTip, icon);
  }

  @Override
  public AbstractActionMainMenuItemFactory clone() {
    return (AbstractActionMainMenuItemFactory)super.clone();
  }

  @Override
  public void close(final Component component) {
  }

  @Override
  public String getIconName() {
    return this.iconName;
  }

  public EnableCheck getVisibleCheck() {
    return this.visibleCheck;
  }

  public boolean isVisble() {
    return this.visibleCheck.isEnabled();
  }

  @Override
  public CheckBoxMenuItem newCheckboxMenuItem() {
    if (isVisble()) {
      return super.newCheckboxMenuItem();
    } else {
      return null;
    }
  }

  @Override
  public JMenuItem newComponent() {
    if (isCheckBox()) {
      return newCheckboxMenuItem();
    } else {
      return newMenuItem();
    }
  }

  @Override
  public JMenuItem newMenuItem() {
    if (isVisble()) {
      return super.newMenuItem();
    } else {
      return null;
    }
  }

  @Override
  public AbstractActionMainMenuItemFactory setIconName(final String iconName) {
    this.iconName = iconName;
    return (AbstractActionMainMenuItemFactory)super.setIconName(iconName);
  }

  public AbstractActionMainMenuItemFactory setVisibleCheck(final EnableCheck visibleCheck) {
    if (visibleCheck == null) {
      this.visibleCheck = EnableCheck.ENABLED;
    } else {
      this.visibleCheck = visibleCheck;
    }
    return this;
  }
}
