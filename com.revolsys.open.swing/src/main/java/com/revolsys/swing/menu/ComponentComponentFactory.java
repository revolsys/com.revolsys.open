package com.revolsys.swing.menu;

import java.awt.Component;

import javax.swing.Icon;

import com.revolsys.swing.component.ComponentFactory;

public class ComponentComponentFactory implements ComponentFactory<Component> {
  private Component component;

  public ComponentComponentFactory(Component component) {
    this.component = component;
  }

  @Override
  public Component createComponent() {
    return component;
  }

  @Override
  public void close(Component component) {
  }

  @Override
  public Icon getIcon() {
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
