package com.revolsys.gis.esri.gdb.xml.model;

import javax.xml.namespace.QName;

import com.revolsys.gis.cs.GeometryFactory;
import com.revolsys.gis.data.model.Attribute;
import com.revolsys.gis.data.model.AttributeProperties;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.gis.data.model.types.DataType;
import com.revolsys.gis.data.model.types.DataTypes;
import com.revolsys.gis.esri.gdb.xml.EsriGeodatabaseXmlConstants;
import com.revolsys.gis.esri.gdb.xml.type.EsriGeodatabaseXmlFieldType;
import com.revolsys.gis.esri.gdb.xml.type.EsriGeodatabaseXmlFieldTypeRegistry;

public class EsriXmlDataObjectMetaDataUtil implements
  EsriGeodatabaseXmlConstants {
  private static final String DE_TABLE_PROPERTY = EsriXmlDataObjectMetaDataUtil.class
    + ".DETable";

  public static final EsriGeodatabaseXmlFieldTypeRegistry FIELD_TYPES = EsriGeodatabaseXmlFieldTypeRegistry.INSTANCE;

  public static DETable getDETable(DataObjectMetaData metaData) {
     DETable table = metaData.getProperty(DE_TABLE_PROPERTY);
    if (table == null) {
      Attribute geometryAttribute = metaData.getGeometryAttribute();
      boolean hasGeometry = false;
      DataType geometryDataType = null;
      String shapeType = null;
      if (geometryAttribute != null) {
        geometryDataType = geometryAttribute.getType();
        if (FIELD_TYPES.getFieldType(geometryDataType) != null) {
          hasGeometry = true;
          if (geometryDataType.equals(DataTypes.POINT)) {
            shapeType = GEOMETRY_TYPE_POINT;
          } else if (geometryDataType.equals(DataTypes.MULTI_POINT)) {
            shapeType = GEOMETRY_TYPE_MULTI_POINT;
          } else if (geometryDataType.equals(DataTypes.LINE_STRING)) {
            shapeType = GEOMETRY_TYPE_POLYLINE;
          } else if (geometryDataType.equals(DataTypes.MULTI_LINE_STRING)) {
            shapeType = GEOMETRY_TYPE_POLYLINE;
          } else if (geometryDataType.equals(DataTypes.POLYGON)) {
            shapeType = GEOMETRY_TYPE_POLYGON;
          } else if (geometryDataType.equals(DataTypes.MULTI_POLYGON)) {
            shapeType = GEOMETRY_TYPE_MULTI_PATCH;
          } else {
            throw new IllegalArgumentException(
              "Unable to detectn geometry type");
          }
        }
      }

      final QName typeName = metaData.getName();
      final String name = typeName.getLocalPart();
      if (hasGeometry) {
        DEFeatureClass featureClass = new DEFeatureClass();
        table = featureClass;
        featureClass.setShapeType(shapeType);
        featureClass.setShapeFieldName(geometryAttribute.getName());

        GeometryFactory geometryFactory = metaData.getGeometryFactory();
        final SpatialReference spatialReference = new SpatialReference(
          geometryFactory);
        featureClass.setSpatialReference(spatialReference);
        featureClass.setHasZ(geometryFactory.hasM());
        featureClass.setHasZ(geometryFactory.hasZ());
        final EnvelopeN envelope = new EnvelopeN(geometryFactory);
        featureClass.setExtent(envelope);
      } else {
        table = new DETable();
      }

      table.setTypeName(typeName);
      Attribute idAttribute = metaData.getIdAttribute();
      if (idAttribute != null) {
        table.setHasOID(true);
        table.setOIDFieldName(idAttribute.getName());

      }

      for (Attribute attribute : metaData.getAttributes()) {
        addField(shapeType, table, attribute);
      }

      table.setAliasName(name);
    }
    return table;
  }

  private static void addField(String shapeType, DETable table,
    Attribute attribute) {
    final String fieldName = attribute.getName();
    if (fieldName.equals("OBJECTID")) {
      Field field = new Field();
      field.setName(fieldName);
      field.setType(FIELD_TYPE_OBJECT_ID);
      field.setIsNullable(false);
      field.setRequired(true);
      field.setLength(4);
      field.setEditable(false);
      table.addField(field);
    } else {
      final DataType dataType = attribute.getType();
      final EsriGeodatabaseXmlFieldType fieldType = FIELD_TYPES.getFieldType(dataType);
      if (fieldType == null) {
        throw new RuntimeException("Data type not supported " + dataType);
      } else {
        Field field = new Field();
        field.setName(fieldName);
        field.setType(fieldType.getEsriFieldTypeName());
        field.setIsNullable(!attribute.isRequired());
        field.setRequired(attribute.isRequired());
        int length = fieldType.getFixedLength();
        if (length < 0) {
          length = attribute.getLength();
        }
        field.setLength(length);
        final int precision;
        if (fieldType.isUsePrecision()) {
          precision = attribute.getLength();
        } else {
          precision = 0;
        }
        field.setPrecision(precision);
        final int scale = attribute.getScale();
        field.setScale(scale);
        GeometryFactory geometryFactory = attribute.getProperty(AttributeProperties.GEOMETRY_FACTORY);
        if (geometryFactory != null) {
          GeometryDef geometryDef = new GeometryDef(shapeType, geometryFactory);
          field.setGeometryDef(geometryDef);
        }
        table.addField(field);
      }
    }
  }
}
