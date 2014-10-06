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

  private String attributeName;

  public GlobalIdProperty() {
  }

  public GlobalIdProperty(final String attributeName) {
    this.attributeName = attributeName;
  }

  @Override
  public GlobalIdProperty clone() {
    return (GlobalIdProperty)super.clone();
  }

  public String getFieldName() {
    return attributeName;
  }

  @Override
  public String getPropertyName() {
    return PROPERTY_NAME;
  }

  public void setAttributeName(final String attributeName) {
    this.attributeName = attributeName;
  }

  @Override
  public void setRecordDefinition(final RecordDefinition recordDefinition) {
    if (attributeName == null) {
      attributeName = recordDefinition.getIdFieldName();
    }
    super.setRecordDefinition(recordDefinition);
  }

}
