package com.revolsys.jump.ui.swing;

import java.awt.Component;

import javax.swing.Icon;

import org.openjump.swing.factory.component.ComponentFactory;

public abstract class AbstractComponentFactory<T extends Component> implements
  ComponentFactory<T> {
  private Icon icon;

  private String name;

  private String toolTip;

  public AbstractComponentFactory(final String name, final Icon icon,
    final String toolTip) {
    this.name = name;
    this.icon = icon;
    this.toolTip = toolTip;
  }

  public Icon getIcon() {
    return icon;
  }

  public String getName() {
    return name;
  }

  public String getToolTip() {
    return toolTip;
  }

  public void close(
    Component component) {
   }
}
