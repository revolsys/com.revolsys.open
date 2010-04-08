package com.revolsys.jump.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import com.revolsys.gis.data.model.Attribute;
import com.revolsys.gis.data.model.DataObjectMetaDataImpl;
import com.revolsys.gis.data.model.types.DataType;
import com.revolsys.gis.data.model.types.DataTypes;
import com.vividsolutions.jump.feature.AttributeType;
import com.vividsolutions.jump.feature.FeatureSchema;

public class FeatureSchemaClassDefinition extends DataObjectMetaDataImpl {
  private static final Map<AttributeType, DataType> ATTRIBUTE_DATA_TYPE_MAP = new HashMap<AttributeType, DataType>();

  static {
    ATTRIBUTE_DATA_TYPE_MAP.put(AttributeType.DATE, DataTypes.DATE_TIME);
    ATTRIBUTE_DATA_TYPE_MAP.put(AttributeType.DOUBLE, DataTypes.DOUBLE);
    ATTRIBUTE_DATA_TYPE_MAP.put(AttributeType.GEOMETRY, DataTypes.GEOMETRY);
    ATTRIBUTE_DATA_TYPE_MAP.put(AttributeType.INTEGER, DataTypes.INTEGER);
    ATTRIBUTE_DATA_TYPE_MAP.put(AttributeType.OBJECT, DataTypes.OBJECT);
    ATTRIBUTE_DATA_TYPE_MAP.put(AttributeType.STRING, DataTypes.STRING);
  }

  private static List<Attribute> getAttributes(
    final FeatureSchema schema) {
    List<Attribute> attributes = new ArrayList<Attribute>();
    for (int i = 0; i < schema.getAttributeCount(); i++) {
      String attrName = schema.getAttributeName(i);
      AttributeType attributeType = schema.getAttributeType(i);
      DataType dataType = ATTRIBUTE_DATA_TYPE_MAP.get(attributeType);
      attributes.add(new Attribute(attrName, dataType, false));
    }
    return attributes;
  }

  public FeatureSchemaClassDefinition(
    final FeatureSchema schema,
    final String name) {
    super(QName.valueOf(name), getAttributes(schema));
  }
}
