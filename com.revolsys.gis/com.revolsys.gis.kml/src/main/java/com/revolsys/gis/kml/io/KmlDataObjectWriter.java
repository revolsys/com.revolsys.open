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

public class KmlDataObjectWriter extends AbstractWriter<DataObject> implements
  Kml22Constants {
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

  public KmlDataObjectWriter(final java.io.Writer out) {
    this.writer = new KmlXmlWriter(out);
  }

  private void writeHeader() {
    opened = true;
    writer.startDocument();
    writer.startTag(KML);
    if (!Boolean.TRUE.equals(getProperty(IoConstants.SINGLE_OBJECT_PROPERTY))) {
      writer.startTag(DOCUMENT);
      String name = getProperty(DOCUMENT_NAME_PROPERTY);
      if (name != null) {
        writer.element(NAME, name);
      }
      String snippet = getProperty(SNIPPET_PROPERTY);
      if (snippet != null) {
        writer.startTag(SNIPPET);
        writer.text(snippet);
        writer.endTag(SNIPPET);
      }
      String description = getProperty(DOCUMENT_DESCRIPTION_PROPERTY);
      if (description != null) {
        writer.element(DESCRIPTION, description);
      }
      writer.element(OPEN, 1);
    }
  }

  public void close() {
    if (!opened) {
      writeHeader();
    }
    if (!Boolean.TRUE.equals(getProperty(IoConstants.SINGLE_OBJECT_PROPERTY))) {
      writer.endTag(DOCUMENT);
    }
    writer.endTag(KML);
    writer.endDocument();
    writer.close();
  }

  public String toString() {
    return null;
  }

  public void flush() {
    writer.flush();
  }

  public void write(final DataObject object) {
    if (!opened) {
      writeHeader();
    }
    writer.startTag(PLACEMARK);
    final DataObjectMetaData metaData = object.getMetaData();
    int geometryIndex = metaData.getGeometryAttributeIndex();
    int idIndex = metaData.getIdAttributeIndex();

    String nameAttribute = getProperty(PLACEMARK_NAME_ATTRIBUTE_PROPERTY);
    String name = null;
    if (nameAttribute != null) {
      name = object.getValue(nameAttribute);
    }
    if (name == null && idIndex != -1) {
      final Object id = object.getValue(idIndex);
      final QName qName = metaData.getName();
      final String localName = qName.getLocalPart();
      name = localName + " " + id;
    }
    if (name != null) {
      writer.element(NAME, name);
    }
    String snippet = getProperty(SNIPPET_PROPERTY);
    if (snippet != null) {
      writer.startTag(SNIPPET);
      writer.text(snippet);
      writer.endTag(SNIPPET);
    }
    String description = getProperty(PLACEMARK_DESCRIPTION_PROPERTY);
    if (description == null) {
      description = getProperty(IoConstants.DESCRIPTION_PROPERTY);
    }
    if (description != null) {
      writer.startTag(DESCRIPTION);
      writer.cdata(description);
      writer.endTag(DESCRIPTION);
    }
    String styleUrl = getProperty(IoConstants.STYLE_URL_PROPERTY);
    if (styleUrl != null) {
      writer.element(STYLE_URL, styleUrl);
    }
    boolean hasValues = false;
    for (int i = 0; i < metaData.getAttributeCount(); i++) {
      if (i != geometryIndex) {
        final String attributeName = metaData.getAttributeName(i);
        Object value = object.getValue(i);
        if (value != null) {
          if (!hasValues) {
            hasValues = true;
            writer.startTag(EXTENDED_DATA);
          }
          writer.startTag(DATA);
          writer.attribute(NAME, attributeName);
          writer.nillableElement(VALUE, value);
          writer.endTag(DATA);
        }
      }
    }
    if (hasValues) {
      writer.endTag(EXTENDED_DATA);
    }
    if (geometryIndex != -1) {
      final Geometry geometry = object.getValue(geometryIndex);
      writer.writeGeometry(geometry);
    }
    writer.endTag();
  }

}
