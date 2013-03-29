package com.revolsys.swing.component;

import javax.swing.JLabel;

import com.revolsys.converter.string.StringConverterRegistry;

public class JLabelWithObject extends JLabel {

  private Object object;

  public Object getObject() {
    return object;
  }

  public void setObject(Object object) {
    this.object = object;
    setText(StringConverterRegistry.toString(object));
  }

}
