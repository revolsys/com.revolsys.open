package com.revolsys.swing.menu;

import java.awt.Component;

import javax.swing.Icon;

import org.jeometry.common.exception.Exceptions;

import com.revolsys.swing.component.ComponentFactory;

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
      return Exceptions.throwUncheckedException(e);
    }
  }

  @Override
  public void close(final Component component) {
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

  @Override
  public Component newComponent() {
    return this.component;
  }

}
