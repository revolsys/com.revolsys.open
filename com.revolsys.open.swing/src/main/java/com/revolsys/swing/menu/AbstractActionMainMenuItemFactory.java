package com.revolsys.swing.menu;

import java.awt.Component;

import javax.swing.Icon;
import javax.swing.JMenuItem;

import com.revolsys.swing.action.AbstractAction;
import com.revolsys.swing.component.ComponentFactory;
import com.revolsys.util.Exceptions;

public abstract class AbstractActionMainMenuItemFactory extends AbstractAction
  implements ComponentFactory<JMenuItem> {

  /**
   *
   */
  private static final long serialVersionUID = 1L;

  public AbstractActionMainMenuItemFactory() {
    super();
  }

  public AbstractActionMainMenuItemFactory(final CharSequence name, final String toolTip,
    final Icon icon) {
    super(name, toolTip, icon);
  }

  @Override
  public AbstractActionMainMenuItemFactory clone() {
    try {
      return (AbstractActionMainMenuItemFactory)super.clone();
    } catch (final CloneNotSupportedException e) {
      return Exceptions.throwUncheckedException(e);
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
