package com.revolsys.swing.menu;

import java.awt.Component;

import javax.swing.Icon;
import javax.swing.JMenuItem;

import com.revolsys.swing.component.ComponentFactory;

public class WrappedMenuFactory implements ComponentFactory<JMenuItem> {
  private final MenuFactory menuFactory;

  private final String name;

  public WrappedMenuFactory(final String name, final MenuFactory menuFactory) {
    this.name = name;
    this.menuFactory = menuFactory;
  }

  @Override
  public ComponentFactory<?> clone() {
    return new WrappedMenuFactory(this.name, this.menuFactory);
  }

  @Override
  public void close(final Component component) {
  }

  @Override
  public JMenuItem createComponent() {
    return this.menuFactory.createJMenu(this.name, false);
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
  public String toString() {
    return this.name;
  }
}
