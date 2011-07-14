package com.revolsys.gis.esri.gdb.xml.type;

import java.util.HashMap;
import java.util.Map;

import com.revolsys.gis.data.model.types.DataType;
import com.revolsys.gis.data.model.types.DataTypes;
import com.revolsys.gis.esri.gdb.xml.EsriGeodatabaseXmlConstants;

public class EsriGeodatabaseXmlFieldTypeRegistry implements
  EsriGeodatabaseXmlConstants {

  public static final EsriGeodatabaseXmlFieldTypeRegistry INSTANCE = new EsriGeodatabaseXmlFieldTypeRegistry();

  private Map<DataType, EsriGeodatabaseXmlFieldType> typeMapping = new HashMap<DataType, EsriGeodatabaseXmlFieldType>();

  public EsriGeodatabaseXmlFieldTypeRegistry() {
    addFieldType(new SimpleFieldType(FIELD_TYPE_STRING, DataTypes.ANY_URI,
      false));
    addFieldType(new SimpleFieldType(null, DataTypes.BASE64_BINARY, false));
    addFieldType(new SimpleFieldType(FIELD_TYPE_STRING, DataTypes.BOOLEAN,
      false));
    addFieldType(new SimpleFieldType(FIELD_TYPE_SMALL_INTEGER, DataTypes.BYTE,
      "xs:short", true, 2));
    addFieldType(new SimpleFieldType(FIELD_TYPE_DATE, DataTypes.DATE, false, 36));
    addFieldType(new SimpleFieldType(FIELD_TYPE_DATE, DataTypes.DATE_TIME,
      true, 36));
    addFieldType(new SimpleFieldType(FIELD_TYPE_DOUBLE, DataTypes.DECIMAL,
      "xs:double", true, 8));
    addFieldType(new SimpleFieldType(FIELD_TYPE_DOUBLE, DataTypes.DOUBLE, true,
      8));
    addFieldType(new SimpleFieldType(FIELD_TYPE_SINGLE, DataTypes.FLOAT,
      "xs:double", true, 4));
    addFieldType(new SimpleFieldType(FIELD_TYPE_INTEGER, DataTypes.INT, true, 8));
    addFieldType(new SimpleFieldType(FIELD_TYPE_INTEGER, DataTypes.INTEGER,
      "xs:int", true, 8));
    addFieldType(new SimpleFieldType(FIELD_TYPE_INTEGER, DataTypes.LONG,
      "xs:int", true, 8));
    addFieldType(new SimpleFieldType(FIELD_TYPE_STRING, DataTypes.QNAME,
      "xs:string", true, -1));
    addFieldType(new SimpleFieldType(FIELD_TYPE_SMALL_INTEGER, DataTypes.SHORT,
      true, 2));
    addFieldType(new SimpleFieldType(FIELD_TYPE_STRING, DataTypes.STRING, false));
    addFieldType(new XmlGeometryFieldType(FIELD_TYPE_GEOMETRY,
      DataTypes.GEOMETRY));
    addFieldType(new XmlGeometryFieldType(FIELD_TYPE_GEOMETRY, DataTypes.POINT));
    addFieldType(new XmlGeometryFieldType(FIELD_TYPE_GEOMETRY,
      DataTypes.MULTI_POINT));
    addFieldType(new XmlGeometryFieldType(FIELD_TYPE_GEOMETRY,
      DataTypes.LINE_STRING));
    addFieldType(new XmlGeometryFieldType(FIELD_TYPE_GEOMETRY,
      DataTypes.MULTI_LINE_STRING));
    addFieldType(new XmlGeometryFieldType(FIELD_TYPE_GEOMETRY,
      DataTypes.POLYGON));
    addFieldType(new XmlGeometryFieldType(FIELD_TYPE_GEOMETRY,
      DataTypes.MULTI_POLYGON));
  }

  public void addFieldType(final EsriGeodatabaseXmlFieldType fieldType) {
    final DataType dataType = fieldType.getDataType();
    addFieldType(dataType, fieldType);
  }

  public void addFieldType(final DataType dataType,
    final EsriGeodatabaseXmlFieldType fieldType) {
    typeMapping.put(dataType, fieldType);
  }

  public EsriGeodatabaseXmlFieldType getFieldType(DataType dataType) {
    return typeMapping.get(dataType);
  }
}
