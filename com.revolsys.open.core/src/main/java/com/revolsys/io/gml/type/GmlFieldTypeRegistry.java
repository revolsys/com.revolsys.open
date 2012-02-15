package com.revolsys.io.gml.type;

import java.util.HashMap;
import java.util.Map;

import com.revolsys.gis.data.model.types.DataType;
import com.revolsys.gis.data.model.types.DataTypes;

public class GmlFieldTypeRegistry {

  public static final GmlFieldTypeRegistry INSTANCE = new GmlFieldTypeRegistry();

  private final Map<DataType, GmlFieldType> typeMapping = new HashMap<DataType, GmlFieldType>();

  public GmlFieldTypeRegistry() {
    addFieldType(new SimpleFieldType(DataTypes.ANY_URI));
    addFieldType(new SimpleFieldType(DataTypes.BASE64_BINARY));
    addFieldType(new SimpleFieldType(DataTypes.BOOLEAN));
    addFieldType(new SimpleFieldType(DataTypes.BYTE));
    addFieldType(new SimpleFieldType(DataTypes.DATE));
    addFieldType(new SimpleFieldType(DataTypes.DATE_TIME));
    addFieldType(new SimpleFieldType(DataTypes.DECIMAL));
    addFieldType(new SimpleFieldType(DataTypes.DOUBLE));
    addFieldType(new SimpleFieldType(DataTypes.FLOAT));
    addFieldType(new SimpleFieldType(DataTypes.INT));
    addFieldType(new SimpleFieldType(DataTypes.INTEGER));
    addFieldType(new SimpleFieldType(DataTypes.LONG));
    addFieldType(new SimpleFieldType(DataTypes.QNAME));
    addFieldType(new SimpleFieldType(DataTypes.SHORT));
    addFieldType(new SimpleFieldType(DataTypes.STRING));
    addFieldType(new GmlGeometryFieldType(DataTypes.GEOMETRY));
    addFieldType(new GmlGeometryFieldType(DataTypes.POINT));
    addFieldType(new GmlGeometryFieldType(DataTypes.LINE_STRING));
    addFieldType(new GmlGeometryFieldType(DataTypes.POLYGON));
  }

  public void addFieldType(final DataType dataType, final GmlFieldType fieldType) {
    typeMapping.put(dataType, fieldType);
  }

  public void addFieldType(final GmlFieldType fieldType) {
    final DataType dataType = fieldType.getDataType();
    addFieldType(dataType, fieldType);
  }

  public GmlFieldType getFieldType(final DataType dataType) {
    return typeMapping.get(dataType);
  }
}
