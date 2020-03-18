package com.revolsys.swing.menu;

import java.awt.Component;
import java.util.function.Supplier;

import javax.swing.Icon;
import javax.swing.JMenuItem;

import com.revolsys.swing.component.ComponentFactory;

public class WrappedMenuFactory implements ComponentFactory<JMenuItem> {
  private final Supplier<MenuFactory> menuSupplier;

  private final String name;

  public WrappedMenuFactory(final String name, final Supplier<MenuFactory> menuSupplier) {
    this.name = name;
    this.menuSupplier = menuSupplier;
  }

  @Override
  public ComponentFactory<?> clone() {
    return new WrappedMenuFactory(this.name, this.menuSupplier);
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
  public JMenuItem newComponent() {
    return this.menuSupplier.get().newJMenu(this.name, false);
  }

  @Override
  public String toString() {
    return this.name;
  }
}
