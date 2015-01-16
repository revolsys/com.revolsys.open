package com.revolsys.swing.menu;

import java.awt.Component;

import javax.swing.JMenuItem;

import com.revolsys.swing.action.AbstractAction;
import com.revolsys.swing.component.ComponentFactory;
import com.revolsys.util.ExceptionUtil;

public abstract class AbstractActionMainMenuItemFactory extends AbstractAction
implements ComponentFactory<JMenuItem> {

  /**
   *
   */
  private static final long serialVersionUID = 1L;

  @Override
  public AbstractActionMainMenuItemFactory clone() {
    try {
      return (AbstractActionMainMenuItemFactory)super.clone();
    } catch (final CloneNotSupportedException e) {
      return ExceptionUtil.throwUncheckedException(e);
    }
  }

  @Override
  public void close(final Component component) {
  }

  @Override
  public JMenuItem createComponent() {
    if (isCheckBox()) {
      return createCheckboxMenuItem();
    } else {
      return createMenuItem();
    }
  }

  @Override
  public String getIconName() {
    return null;
  }
}
