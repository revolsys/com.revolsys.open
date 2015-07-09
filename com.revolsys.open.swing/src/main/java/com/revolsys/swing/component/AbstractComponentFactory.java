package com.revolsys.swing.component;

import java.awt.Component;

import javax.swing.Icon;

public abstract class AbstractComponentFactory<T extends Component>
  implements ComponentFactory<T>, Cloneable {

  private Icon icon;

  private String name;

  private String toolTip;

  public AbstractComponentFactory() {
  }

  public AbstractComponentFactory(final Icon icon, final String name, final String toolTip) {
    this.icon = icon;
    this.name = name;
    this.toolTip = toolTip;
  }

  @SuppressWarnings("unchecked")
  @Override
  public AbstractComponentFactory<T> clone() {
    try {
      return (AbstractComponentFactory<T>)super.clone();
    } catch (final CloneNotSupportedException e) {
      return null;
    }
  }

  @Override
  public void close(final Component component) {
  }

  @Override
  public Icon getIcon() {
    return this.icon;
  }

  @Override
  public String getIconName() {
    return null;
  }

  @Override
  public String getName() {
    return this.name;
  }

  @Override
  public String getToolTip() {
    return this.toolTip;
  }

  protected void setIcon(final Icon icon) {
    this.icon = icon;
  }

  protected void setName(final String name) {
    this.name = name;
  }

  protected void setToolTip(final String toolTip) {
    this.toolTip = toolTip;
  }

}
