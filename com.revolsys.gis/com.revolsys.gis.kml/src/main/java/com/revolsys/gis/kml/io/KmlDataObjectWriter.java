package com.revolsys.gis.kml.io;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;

import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.io.AbstractWriter;
import com.revolsys.io.IoConstants;
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

  private boolean opened;

  public KmlDataObjectWriter(
    final java.io.Writer out) {
    this.writer = new KmlXmlWriter(out);
  }

  private void writeHeader() {
    opened = true;
    writer.startDocument();
    writer.startTag(Kml22Constants.KML);
    if (!Boolean.TRUE.equals(getProperty(IoConstants.SINGLE_OBJECT_PROPERTY))) {
      writer.startTag(Kml22Constants.DOCUMENT);
      writer.element(Kml22Constants.OPEN, 1);
    }
  }

  public void close() {
    if (!opened) {
      writeHeader();
    }
    if (!Boolean.TRUE.equals(getProperty(IoConstants.SINGLE_OBJECT_PROPERTY))) {
      writer.endTag(Kml22Constants.DOCUMENT);
    }
    writer.endTag(Kml22Constants.KML);
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
    if (!opened) {
      writeHeader();
    }
    writer.startTag(Kml22Constants.PLACEMARK);
    final DataObjectMetaData metaData = object.getMetaData();
    int geometryIndex = metaData.getGeometryAttributeIndex();
    int idIndex = metaData.getIdAttributeIndex();
    if (idIndex != -1) {
      final Object id = object.getValue(idIndex);
      final QName name = metaData.getName();
      final String localName = name.getLocalPart();
      writer.element(Kml22Constants.NAME, localName + " " + id);
    }
    String description = getProperty(IoConstants.DESCRIPTION_PROPERTY);
    if (description != null) {
      writer.startTag(Kml22Constants.DESCRIPTION);
      writer.cdata(description);
      writer.endTag(Kml22Constants.DESCRIPTION);
    }
    String styleUrl = getProperty(IoConstants.STYLE_URL_PROPERTY);
    if (styleUrl != null) {
      writer.element(Kml22Constants.STYLE_URL, styleUrl);
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
