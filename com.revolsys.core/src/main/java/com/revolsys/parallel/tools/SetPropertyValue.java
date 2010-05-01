package com.revolsys.parallel.tools;

import javax.annotation.PostConstruct;

public class SetPropertyValue extends AttributeMap {
  private String key;

  private Object value;

  public String getKey() {
    return key;
  }

  public Object getValue() {
    return value;
  }

  @PostConstruct
  public void init() {
    if (key != null) {
      put(key, value);
    }
  }

  public void setKey(
    final String key) {
    this.key = key;
  }

  public void setValue(
    final Object value) {
    this.value = value;
  }

}
