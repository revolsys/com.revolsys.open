package com.revolsys.io.esri.gdb.xml.model;

public class PropertySetProperty {
  private String key;

  private Object value;

  public String getKey() {
    return key;
  }

  public Object getValue() {
    return value;
  }

  public void setKey(final String key) {
    this.key = key;
  }

  public void setValue(final Object value) {
    this.value = value;
  }

  @Override
  public String toString() {
    return key + "=" + value;
  }
}
