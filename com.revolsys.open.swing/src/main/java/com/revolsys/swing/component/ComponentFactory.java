package com.revolsys.swing.component;

import java.awt.Component;

import javax.swing.Icon;

public interface ComponentFactory<T extends Component> {

  public abstract void close(Component component);

  public abstract T createComponent();

  public abstract Icon getIcon();

  public abstract String getName();

  public abstract String getToolTip();
}
