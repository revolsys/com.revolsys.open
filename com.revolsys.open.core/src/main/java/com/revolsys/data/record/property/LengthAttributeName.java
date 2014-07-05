package com.revolsys.data.record.property;

import org.springframework.util.StringUtils;

import com.revolsys.data.record.Record;
import com.revolsys.data.record.schema.RecordDefinition;
import com.revolsys.jts.geom.LineString;

public class LengthAttributeName extends AbstractRecordDefinitionProperty {
  public static final String PROPERTY_NAME = LengthAttributeName.class.getName()
    + ".propertyName";

  public static LengthAttributeName getProperty(final Record object) {
    final RecordDefinition recordDefinition = object.getRecordDefinition();
    return getProperty(recordDefinition);
  }

  public static LengthAttributeName getProperty(
    final RecordDefinition recordDefinition) {
    LengthAttributeName property = recordDefinition.getProperty(PROPERTY_NAME);
    if (property == null) {
      property = new LengthAttributeName();
      property.setRecordDefinition(recordDefinition);
    }
    return property;
  }

  public static void setObjectLength(final Record object) {
    final LengthAttributeName property = getProperty(object);
    property.setLength(object);
  }

  private String attributeName;

  public LengthAttributeName() {
  }

  public LengthAttributeName(final String attributeName) {
    this.attributeName = attributeName;
  }

  public String getAttributeName() {
    return attributeName;
  }

  @Override
  public String getPropertyName() {
    return PROPERTY_NAME;
  }

  public void setAttributeName(final String attributeName) {
    this.attributeName = attributeName;
  }

  public void setLength(final Record object) {
    if (StringUtils.hasText(attributeName)) {
      final LineString line = object.getGeometryValue();
      final double length = line.getLength();
      object.setValue(attributeName, length);
    }
  }

  @Override
  public String toString() {
    return "LengthAttribute " + attributeName;
  }
}
