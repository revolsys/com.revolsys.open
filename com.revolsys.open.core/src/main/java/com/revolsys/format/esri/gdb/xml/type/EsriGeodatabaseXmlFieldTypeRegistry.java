package com.revolsys.format.esri.gdb.xml.type;

import java.util.HashMap;
import java.util.Map;

import com.revolsys.data.types.DataType;
import com.revolsys.data.types.DataTypes;
import com.revolsys.format.esri.gdb.xml.EsriGeodatabaseXmlConstants;
import com.revolsys.format.esri.gdb.xml.model.enums.FieldType;

public class EsriGeodatabaseXmlFieldTypeRegistry implements EsriGeodatabaseXmlConstants {

  public static final EsriGeodatabaseXmlFieldTypeRegistry INSTANCE = new EsriGeodatabaseXmlFieldTypeRegistry();

  private final Map<DataType, EsriGeodatabaseXmlFieldType> typeMapping = new HashMap<DataType, EsriGeodatabaseXmlFieldType>();

  private final Map<FieldType, DataType> esriToDataType = new HashMap<FieldType, DataType>();

  public EsriGeodatabaseXmlFieldTypeRegistry() {
    addFieldType(new SimpleFieldType(FieldType.esriFieldTypeOID, DataTypes.INT, false));
    addFieldType(new SimpleFieldType(FieldType.esriFieldTypeGlobalID, DataTypes.STRING, false));
    addFieldType(new SimpleFieldType(FieldType.esriFieldTypeGUID, DataTypes.STRING, false));
    addFieldType(new SimpleFieldType(FieldType.esriFieldTypeString, DataTypes.ANY_URI, false));
    addFieldType(new SimpleFieldType(null, DataTypes.BASE64_BINARY, false));
    addFieldType(new SimpleFieldType(FieldType.esriFieldTypeString, DataTypes.BOOLEAN, false));
    addFieldType(new SimpleFieldType(FieldType.esriFieldTypeSmallInteger, DataTypes.BYTE,
      "xs:short", false, 2));
    addFieldType(new SimpleFieldType(FieldType.esriFieldTypeDate, DataTypes.DATE, false, 36));
    addFieldType(new SimpleFieldType(FieldType.esriFieldTypeDate, DataTypes.DATE_TIME, false, 36));
    addFieldType(new SimpleFieldType(FieldType.esriFieldTypeDouble, DataTypes.DECIMAL, "xs:double",
      false, 8));
    addFieldType(new SimpleFieldType(FieldType.esriFieldTypeDouble, DataTypes.DOUBLE, false, 8));
    addFieldType(new SimpleFieldType(FieldType.esriFieldTypeSingle, DataTypes.FLOAT, "xs:double",
      false, 4));
    addFieldType(new SimpleFieldType(FieldType.esriFieldTypeInteger, DataTypes.INTEGER, "xs:int",
      false, 4));
    addFieldType(new SimpleFieldType(FieldType.esriFieldTypeInteger, DataTypes.LONG, "xs:int",
      false, 8));
    addFieldType(new SimpleFieldType(FieldType.esriFieldTypeInteger, DataTypes.INT, false, 4));
    addFieldType(new SimpleFieldType(FieldType.esriFieldTypeString, DataTypes.QNAME, "xs:string",
      true, -1));
    addFieldType(new SimpleFieldType(FieldType.esriFieldTypeSmallInteger, DataTypes.SHORT, false, 2));
    addFieldType(new SimpleFieldType(FieldType.esriFieldTypeString, DataTypes.STRING, false));
    addFieldType(new XmlGeometryFieldType(FieldType.esriFieldTypeGeometry, DataTypes.POINT));
    addFieldType(new XmlGeometryFieldType(FieldType.esriFieldTypeGeometry, DataTypes.MULTI_POINT));
    addFieldType(new XmlGeometryFieldType(FieldType.esriFieldTypeGeometry, DataTypes.LINE_STRING));
    addFieldType(new XmlGeometryFieldType(FieldType.esriFieldTypeGeometry, DataTypes.LINEAR_RING));
    addFieldType(new XmlGeometryFieldType(FieldType.esriFieldTypeGeometry,
      DataTypes.MULTI_LINE_STRING));
    addFieldType(new XmlGeometryFieldType(FieldType.esriFieldTypeGeometry, DataTypes.POLYGON));
    addFieldType(new XmlGeometryFieldType(FieldType.esriFieldTypeGeometry, DataTypes.MULTI_POLYGON));
    addFieldType(new XmlGeometryFieldType(FieldType.esriFieldTypeGeometry, DataTypes.GEOMETRY));
  }

  public void addFieldType(final DataType dataType, final EsriGeodatabaseXmlFieldType fieldType) {
    this.typeMapping.put(dataType, fieldType);
    this.esriToDataType.put(fieldType.getEsriFieldType(), dataType);
  }

  public void addFieldType(final EsriGeodatabaseXmlFieldType fieldType) {
    final DataType dataType = fieldType.getDataType();
    addFieldType(dataType, fieldType);
  }

  public DataType getDataType(final FieldType fieldType) {
    return this.esriToDataType.get(fieldType);
  }

  public EsriGeodatabaseXmlFieldType getFieldType(final DataType dataType) {
    return this.typeMapping.get(dataType);
  }
}
