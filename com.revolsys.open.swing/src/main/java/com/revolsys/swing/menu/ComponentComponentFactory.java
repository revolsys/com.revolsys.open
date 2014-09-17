package com.revolsys.swing.menu;

import java.awt.Component;

import javax.swing.Icon;

import com.revolsys.swing.component.ComponentFactory;
import com.revolsys.util.ExceptionUtil;

public class ComponentComponentFactory implements ComponentFactory<Component> {
  private final Component component;

  public ComponentComponentFactory(final Component component) {
    this.component = component;
  }

  @Override
  public ComponentComponentFactory clone() {
    try {
      return (ComponentComponentFactory)super.clone();
    } catch (final CloneNotSupportedException e) {
      return ExceptionUtil.throwUncheckedException(e);
    }
  }

  @Override
  public void close(final Component component) {
  }

  @Override
  public Component createComponent() {
    return this.component;
  }

  @Override
  public Icon getIcon() {
    return null;
  }

  @Override
  public String getIconName() {
    return null;
  }

  @Override
  public String getName() {
    return null;
  }

  @Override
  public String getToolTip() {
    return null;
  }

}
