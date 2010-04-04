package com.revolsys.gis.data.model;

import javax.xml.namespace.QName;

public class SimpleProperty extends AbstractDataObjectMetaDataProperty {
  private QName propertyName;

  private Object value;

  public SimpleProperty() {
  }

  public SimpleProperty(
    final QName propertyName,
    final Object value) {
    this.propertyName = propertyName;
    this.value = value;
  }

  @Override
  public SimpleProperty clone() {
    return new SimpleProperty(propertyName, value);
  }

  public QName getPropertyName() {
    return propertyName;
  }

  public <T> T getValue() {
    return (T)value;
  }

  public void setPropertyName(
    final QName propertyName) {
    this.propertyName = propertyName;
  }

  public void setValue(
    final Object value) {
    this.value = value;
  }
}
