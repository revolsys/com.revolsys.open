package com.revolsys.gis.data.model.property;

import org.springframework.util.StringUtils;

import com.revolsys.gis.data.model.AbstractDataObjectMetaDataProperty;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.vividsolutions.jts.geom.LineString;

public class LengthAttributeName extends AbstractDataObjectMetaDataProperty {
  public static final String PROPERTY_NAME = LengthAttributeName.class.getName()
    + ".propertyName";

  public static LengthAttributeName getProperty(final DataObject object) {
    final DataObjectMetaData metaData = object.getMetaData();
    return getProperty(metaData);
  }

  public static LengthAttributeName getProperty(
    final DataObjectMetaData metaData) {
    LengthAttributeName property = metaData.getProperty(PROPERTY_NAME);
    if (property == null) {
      property = new LengthAttributeName();
      property.setMetaData(metaData);
    }
    return property;
  }

  public static void setObjectLength(final DataObject object) {
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

  public String getPropertyName() {
    return PROPERTY_NAME;
  }

  public void setAttributeName(final String attributeName) {
    this.attributeName = attributeName;
  }

  public void setLength(final DataObject object) {
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
