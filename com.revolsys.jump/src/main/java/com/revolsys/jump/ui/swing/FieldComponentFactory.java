package com.revolsys.jump.ui.swing;

import javax.swing.JComponent;

public interface FieldComponentFactory {
  JComponent createComponent();

  JComponent createComponent(ValueChangeListener listener);

  Object getValue(JComponent component);

  void setValue(JComponent component, Object value);
}
