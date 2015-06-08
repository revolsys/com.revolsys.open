package com.revolsys.data.record.property;

import com.revolsys.data.record.Record;
import com.revolsys.data.record.schema.RecordDefinition;

public class GlobalIdProperty extends AbstractRecordDefinitionProperty {
  static final String PROPERTY_NAME = "http://revolsys.com/gis/globalId";

  public static GlobalIdProperty getProperty(final Record object) {
    final RecordDefinition recordDefinition = object.getRecordDefinition();
    return getProperty(recordDefinition);
  }

  public static GlobalIdProperty getProperty(final RecordDefinition recordDefinition) {
    if (recordDefinition == null) {
      return null;
    } else {
      return recordDefinition.getProperty(PROPERTY_NAME);
    }
  }

  private String fieldName;

  public GlobalIdProperty() {
  }

  public GlobalIdProperty(final String fieldName) {
    this.fieldName = fieldName;
  }

  @Override
  public GlobalIdProperty clone() {
    return (GlobalIdProperty)super.clone();
  }

  public String getFieldName() {
    return this.fieldName;
  }

  @Override
  public String getPropertyName() {
    return PROPERTY_NAME;
  }

  public void setFieldName(final String fieldName) {
    this.fieldName = fieldName;
  }

  @Override
  public void setRecordDefinition(final RecordDefinition recordDefinition) {
    if (this.fieldName == null) {
      this.fieldName = recordDefinition.getIdFieldName();
    }
    super.setRecordDefinition(recordDefinition);
  }

}
