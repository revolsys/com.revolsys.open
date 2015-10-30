package com.revolsys.swing.component;

import java.awt.Component;

import javax.swing.Icon;

public interface ComponentFactory<T extends Component> extends Cloneable {

  ComponentFactory<?> clone();

  void close(Component component);

  Icon getIcon();

  String getIconName();

  String getName();

  String getToolTip();

  T newComponent();
}
