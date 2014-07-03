package com.revolsys.data.record.property;

public class ValueMetaDataProperty extends AbstractRecordDefinitionProperty {

  private Object value;

  private String propertyName;

  @Override
  public String getPropertyName() {
    return propertyName;
  }

  public Object getValue() {
    return value;
  }

  public void setPropertyName(final String propertyName) {
    this.propertyName = propertyName;
  }

  public void setValue(final Object value) {
    this.value = value;
  }
}
