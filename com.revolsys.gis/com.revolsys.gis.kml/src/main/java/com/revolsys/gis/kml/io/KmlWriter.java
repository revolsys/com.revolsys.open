package com.revolsys.gis.kml.io;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import com.revolsys.gis.data.model.DataObject;
import com.revolsys.io.AbstractWriter;
import com.vividsolutions.jts.geom.Geometry;

public class KmlWriter extends AbstractWriter<DataObject> {
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

  public KmlWriter(
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
    final Geometry geometry = object.getGeometryValue();
    writer.writeGeometry(geometry);
    writer.endTag();
  }

}
