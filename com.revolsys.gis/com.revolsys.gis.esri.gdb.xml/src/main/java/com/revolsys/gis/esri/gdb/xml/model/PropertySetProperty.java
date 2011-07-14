package com.revolsys.gis.esri.gdb.xml.model;

public class PropertySetProperty {
  private String key;

  private Object value;

  public String getKey() {
    return key;
  }

  public void setKey(String key) {
    this.key = key;
  }

  public Object getValue() {
    return value;
  }

  public void setValue(Object value) {
    this.value = value;
  }

  @Override
  public String toString() {
    return key + "=" + value;
  }
}
