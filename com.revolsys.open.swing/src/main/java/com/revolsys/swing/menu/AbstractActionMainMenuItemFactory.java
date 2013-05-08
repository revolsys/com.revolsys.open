package com.revolsys.swing.menu;

import java.awt.Component;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;

import com.revolsys.swing.action.AbstractAction;
import com.revolsys.swing.component.ComponentFactory;
import com.revolsys.util.ExceptionUtil;

public abstract class AbstractActionMainMenuItemFactory extends AbstractAction
  implements ComponentFactory<JMenuItem> {

  @Override
  public AbstractActionMainMenuItemFactory clone() {
    try {
      return (AbstractActionMainMenuItemFactory)super.clone();
    } catch (CloneNotSupportedException e) {
      return ExceptionUtil.throwUncheckedException(e);
    }
  }

  @Override
  public void close(Component component) {
  }

  @Override
  public JMenuItem createComponent() {
    if (isCheckBox()) {
      return new JCheckBoxMenuItem(this);
    } else {
      JMenuItem menuItem = new JMenuItem(this);

      return menuItem;
    }
  }

}
