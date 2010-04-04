package com.revolsys.jump.ui.swing;

import java.util.EventObject;

@SuppressWarnings("serial")
public class ValueChangeEvent extends EventObject {

  private Object value;

  public ValueChangeEvent(final Object source, final Object value) {
    super(source);
    this.value = value;
  }

  /**
   * @return the value
   */
  public Object getValue() {
    return value;
  }

}
