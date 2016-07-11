package com.revolsys.record.property;

public class ValueRecordDefinitionProperty extends AbstractRecordDefinitionProperty {
  private String propertyName;

  private Object value;

  public ValueRecordDefinitionProperty() {
  }

  @Override
  public String getPropertyName() {
    return this.propertyName;
  }

  public Object getValue() {
    return this.value;
  }

  public void setPropertyName(final String propertyName) {
    this.propertyName = propertyName;
  }

  public void setValue(final Object value) {
    this.value = value;
  }
}
