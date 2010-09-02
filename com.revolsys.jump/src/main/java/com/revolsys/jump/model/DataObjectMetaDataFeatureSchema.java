package com.revolsys.jump.model;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;

import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.gis.data.model.types.DataType;
import com.revolsys.gis.data.model.types.DataTypes;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jump.feature.AttributeType;
import com.vividsolutions.jump.feature.FeatureSchema;

@SuppressWarnings("serial")
public class DataObjectMetaDataFeatureSchema extends FeatureSchema {
  private DataObjectMetaData metaData;

  private String name;

  public DataObjectMetaDataFeatureSchema(
    final DataObjectMetaData metaData,
    final String geometryAttribute) {
    this.metaData = metaData;
    name = metaData.getName().getLocalPart();
     int i = 0;
    for (String name : metaData.getAttributeNames()) {
      AttributeType attributeType = AttributeType.OBJECT;
      if (geometryAttribute != null && name.equals(geometryAttribute)) {
        attributeType = AttributeType.GEOMETRY;
      } else {
        DataType type = metaData.getAttributeType(i);
        Class<?> typeClass = type.getJavaClass();
        if (typeClass == String.class) {
          attributeType = AttributeType.STRING;
        } else if (typeClass == Double.class || typeClass == Float.class
          || typeClass == BigDecimal.class) {
          attributeType = AttributeType.DOUBLE;
        } else if (typeClass == Byte.class || typeClass == Short.class
          || typeClass == Integer.class || typeClass == Long.class
          || typeClass == BigInteger.class) {
          attributeType = AttributeType.INTEGER;
        } else if (typeClass == Geometry.class) {
          attributeType = AttributeType.GEOMETRY;
        } else if (typeClass == Date.class) {
          attributeType = AttributeType.DATE;
        } else if (type == DataTypes.BOOLEAN) {
          attributeType = AttributeType.STRING;
        } else if (Geometry.class.isAssignableFrom(typeClass)) {
          attributeType = AttributeType.GEOMETRY;
        }
      }
      i++;
      addAttribute(name, attributeType);
    }
  }

  /**
   * @return the metaData
   */
  public DataObjectMetaData getMetaData() {
    return metaData;
  }

  /**
   * @return the name
   */
  public String getName() {
    return name;
  }

  public int hashCode() {
    return name.hashCode();
  }

  public boolean equals(
    final Object other) {
    if (other instanceof DataObjectMetaDataFeatureSchema) {
      DataObjectMetaDataFeatureSchema schema = (DataObjectMetaDataFeatureSchema)other;
      return (name.equals(schema.name));

    }
    return false;
  }

  @Override
  public String toString() {
    return metaData.getName().toString();
  }
}
