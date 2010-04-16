package com.revolsys.gis.kml.io;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.io.AbstractWriter;
import com.vividsolutions.jts.geom.Geometry;

public class KmlDataObjectWriter extends AbstractWriter<DataObject> {
  private static final Map<Class<?>, String> TYPE_MAP = new HashMap<Class<?>, String>();

  static {
    TYPE_MAP.put(Double.class, "decimal");
    TYPE_MAP.put(Integer.class, "decimal");
    TYPE_MAP.put(BigDecimal.class, "decimal");
    TYPE_MAP.put(java.sql.Date.class, "dateTime");
    TYPE_MAP.put(java.util.Date.class, "dateTime");
    TYPE_MAP.put(String.class, "string");
    TYPE_MAP.put(Geometry.class, "wktGeometry");

  }

  private final KmlXmlWriter writer;

  public KmlDataObjectWriter(
    final java.io.Writer out) {
    this.writer = new KmlXmlWriter(out);
    writer.startDocument();
    writer.startTag(Kml22Constants.KML);
    writer.startTag(Kml22Constants.DOCUMENT);
  }

  public void close() {
    writer.endTag();
    writer.endTag();
    writer.endDocument();
    writer.close();
  }

  public String toString() {
    return null;
  }

  public void flush() {
    writer.flush();
  }

  public void write(
    final DataObject object) {
    writer.startTag(Kml22Constants.PLACEMARK);
    final DataObjectMetaData metaData = object.getMetaData();
    int geometryIndex = metaData.getGeometryAttributeIndex();
    int idIndex = metaData.getIdAttributeIndex();
    if (idIndex != -1) {
      final Object id = object.getValue(idIndex);
      writer.element(Kml22Constants.NAME, metaData.getName() + " " + id);
    }
    boolean hasValues = false;

    for (int i = 0; i < metaData.getAttributeCount(); i++) {
      if (i != geometryIndex) {
        final String name = metaData.getAttributeName(i);
        Object value = object.getValue(i);
        if (value != null) {
          if (!hasValues) {
            hasValues = true;
            writer.startTag(Kml22Constants.EXTENDED_DATA);
          }
          writer.startTag(Kml22Constants.DATA);
          writer.attribute(Kml22Constants.NAME, name);
          writer.text(value);
          writer.endTag(Kml22Constants.DATA);
        }
      }
    }
    if (hasValues) {
      writer.endTag(Kml22Constants.EXTENDED_DATA);
    }
    if (geometryIndex != -1) {
      final Geometry geometry = object.getValue(geometryIndex);
      writer.writeGeometry(geometry);
    }
    writer.endTag();
  }

}
