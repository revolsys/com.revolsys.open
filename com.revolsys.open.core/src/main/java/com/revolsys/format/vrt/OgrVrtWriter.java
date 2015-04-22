package com.revolsys.format.vrt;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;

import javax.xml.namespace.QName;

import com.revolsys.data.record.schema.FieldDefinition;
import com.revolsys.data.record.schema.RecordDefinition;
import com.revolsys.data.types.DataType;
import com.revolsys.data.types.DataTypes;
import com.revolsys.format.xml.XmlWriter;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.GeometryFactory;

public class OgrVrtWriter {
  public static void write(final File file,
    final RecordDefinition recordDefinition, final String dataSource)
        throws IOException {
    try (
        XmlWriter writer = new XmlWriter(new FileWriter(file))) {
      writer.setIndent(true);
      writer.startDocument("UTF-8", "1.0");
      writer.startTag("OGRVRTDataSource");
      writer.startTag("OGRVRTLayer");
      final String typeName = recordDefinition.getName();
      writer.attribute("name", typeName);
      writer.startTag("SrcDataSource");
      writer.attribute("relativeToVRT", "1");
      writer.text(dataSource);
      writer.endTag("SrcDataSource");

      writer.element(new QName("SrcLayer"), typeName);

      for (final FieldDefinition attribute : recordDefinition.getFields()) {
        final String fieldName = attribute.getName();
        final DataType fieldType = attribute.getType();
        final Class<?> typeClass = attribute.getTypeClass();
        if (Geometry.class.isAssignableFrom(typeClass)) {
          final GeometryFactory geometryFactory = recordDefinition.getGeometryFactory();
          writer.element("GeometryType", "wkb" + fieldType);
          if (geometryFactory != null) {
            writer.element("LayerSRS", "EPSG:" + geometryFactory.getSrid());
          }
          writer.startTag("GeometryField");
          writer.attribute("encoding", "WKT");
          writer.attribute("field", fieldName);
          writer.attribute("name", fieldName);
          writer.attribute("reportSrcColumn", "FALSE");
          writer.element("GeometryType", "wkb" + fieldType);
          if (geometryFactory != null) {
            writer.element("SRS", "EPSG:" + geometryFactory.getSrid());
          }
          writer.endTag("GeometryField");
        } else {
          writer.startTag("Field");
          writer.attribute("name", fieldName);
          String type = "String";
          if (Arrays.asList(DataTypes.BYTE, DataTypes.SHORT, DataTypes.INT,
            DataTypes.LONG, DataTypes.INTEGER).contains(fieldType)) {
            type = "Integer";
          } else if (Arrays.asList(DataTypes.FLOAT, DataTypes.DOUBLE,
            DataTypes.DECIMAL).contains(fieldType)) {
            type = "Real";
          } else if (DataTypes.DATE.equals(type)) {
            type = "Date";
          } else if (DataTypes.DATE_TIME.equals(type)) {
            type = "DateTime";
          } else {
            type = "String";
          }
          writer.attribute("type", type);
          final int length = attribute.getLength();
          if (length > 0) {
            writer.attribute("width", length);
          }
          final int scale = attribute.getScale();
          if (scale > 0) {
            writer.attribute("scale", scale);
          }
          writer.attribute("src", fieldName);
          writer.endTag("Field");
        }
      }
      writer.endTag("OGRVRTLayer");
      writer.endTag("OGRVRTDataSource");
      writer.endDocument();
    }
  }
}
