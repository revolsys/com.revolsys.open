package com.revolsys.swing.component;

import java.awt.Component;

import javax.swing.Icon;

public interface ComponentFactory<T extends Component> extends Cloneable {

  ComponentFactory<?> clone();

  void close(Component component);

  T createComponent();

  Icon getIcon();

  String getName();

  String getToolTip();
}
