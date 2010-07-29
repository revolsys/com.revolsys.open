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
    addSimpleType(DataTypes.ANY_URI, FIELD_TYPE_STRING);
    addSimpleType(DataTypes.BASE64_BINARY, null);
    addSimpleType(DataTypes.BOOLEAN, FIELD_TYPE_STRING);
    addSimpleType(DataTypes.BYTE, FIELD_TYPE_SMALL_INTEGER);
    addSimpleType(DataTypes.DATE, FIELD_TYPE_DATE);
    addSimpleType(DataTypes.DATE_TIME, FIELD_TYPE_DATE);
    addSimpleType(DataTypes.DECIMAL, FIELD_TYPE_DOUBLE);
    addSimpleType(DataTypes.DOUBLE, FIELD_TYPE_DOUBLE);
    addSimpleType(DataTypes.FLOAT, FIELD_TYPE_SINGLE);
    addSimpleType(DataTypes.INT, FIELD_TYPE_INTEGER);
    addSimpleType(DataTypes.INTEGER, FIELD_TYPE_INTEGER);
    addSimpleType(DataTypes.LONG, FIELD_TYPE_INTEGER);
    addSimpleType(DataTypes.QNAME, FIELD_TYPE_STRING);
    addSimpleType(DataTypes.SHORT, FIELD_TYPE_SMALL_INTEGER);
    addSimpleType(DataTypes.STRING, FIELD_TYPE_STRING);
  }

  public void addSimpleType(
    final DataType dataType,
    final String esriFieldType) {
    final SimpleEsriGeodatabaseXmlFieldType fieldType = new SimpleEsriGeodatabaseXmlFieldType(
      esriFieldType, dataType);
    addFieldType(fieldType);
  }

  public void addFieldType(
    final SimpleEsriGeodatabaseXmlFieldType fieldType) {
    final DataType dataType = fieldType.getDataType();
    typeMapping.put(dataType, fieldType);
  }

  public EsriGeodatabaseXmlFieldType getFieldType(
    DataType dataType) {
    return typeMapping.get(dataType);
  }
}
