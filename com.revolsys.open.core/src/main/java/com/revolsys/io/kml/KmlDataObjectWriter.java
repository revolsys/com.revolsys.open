package com.revolsys.io.kml;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.util.StringUtils;

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

  private String defaultStyleUrl;

  private String styleUrl;

  public KmlDataObjectWriter(final java.io.Writer out) {
    this.writer = new KmlXmlWriter(out);
  }

  @Override
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

  @Override
  public void flush() {
    writer.flush();
  }

  @Override
  public void setProperty(final String name, final Object value) {
    super.setProperty(name, value);
    if (Kml22Constants.STYLE_URL_PROPERTY.equals(name)) {
      String styleUrl;
      if (value == null) {
        styleUrl = null;
      } else {
        styleUrl = value.toString();
      }
      if (StringUtils.hasText(styleUrl)) {
        if (StringUtils.hasText(defaultStyleUrl)) {
          this.styleUrl = styleUrl;
        } else {
          defaultStyleUrl = styleUrl;
        }
      } else {
        this.styleUrl = defaultStyleUrl;
      }
    }
  }

  @Override
  public String toString() {
    return null;
  }

  @Override
  public void write(final DataObject object) {
    if (!opened) {
      writeHeader();
    }
    writer.startTag(PLACEMARK);
    final DataObjectMetaData metaData = object.getMetaData();
    final int geometryIndex = metaData.getGeometryAttributeIndex();
    final int idIndex = metaData.getIdAttributeIndex();

    final String nameAttribute = getProperty(PLACEMARK_NAME_ATTRIBUTE_PROPERTY);
    String name = null;
    if (nameAttribute != null) {
      name = object.getValue(nameAttribute);
    }
    if (name == null && idIndex != -1) {
      final Object id = object.getValue(idIndex);
      final String typeName = metaData.getTypeName();
      name = typeName + " " + id;
    }
    if (name != null) {
      writer.element(NAME, name);
    }
    final String snippet = getProperty(SNIPPET_PROPERTY);
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
    if (StringUtils.hasText(styleUrl)) {
      writer.element(STYLE_URL, styleUrl);
    } else if (StringUtils.hasText(defaultStyleUrl)) {
      writer.element(STYLE_URL, defaultStyleUrl);
    }
    boolean hasValues = false;
    for (int i = 0; i < metaData.getAttributeCount(); i++) {
      if (i != geometryIndex) {
        final String attributeName = metaData.getAttributeName(i);
        final Object value = object.getValue(i);
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
    final List<Integer> geometryAttributeIndexes = metaData.getGeometryAttributeIndexes();
    if (!geometryAttributeIndexes.isEmpty()) {
      Geometry geometry = null;
      if (geometryAttributeIndexes.size() == 1) {
        geometry = object.getValue(geometryAttributeIndexes.get(0));
      } else {
        final List<Geometry> geometries = new ArrayList<Geometry>();
        for (final Integer geometryAttributeIndex : geometryAttributeIndexes) {
          final Geometry part = object.getValue(geometryAttributeIndex);
          if (part != null) {
            geometries.add(part);
          }
        }
        if (!geometries.isEmpty()) {
          geometry = metaData.getGeometryFactory().createGeometry(geometries);
        }
      }
      if (geometry != null) {
        writer.writeGeometry(geometry);
      }
    }
    writer.endTag();
  }

  private void writeHeader() {
    opened = true;
    writer.startDocument();
    writer.startTag(KML);
    if (!Boolean.TRUE.equals(getProperty(IoConstants.SINGLE_OBJECT_PROPERTY))) {
      writer.startTag(DOCUMENT);
      final String name = getProperty(DOCUMENT_NAME_PROPERTY);
      if (name != null) {
        writer.element(NAME, name);
      }
      final String snippet = getProperty(SNIPPET_PROPERTY);
      if (snippet != null) {
        writer.startTag(SNIPPET);
        writer.text(snippet);
        writer.endTag(SNIPPET);
      }
      final String description = getProperty(DOCUMENT_DESCRIPTION_PROPERTY);
      if (description != null) {
        writer.element(DESCRIPTION, description);
      }
      writer.element(OPEN, 1);
      final String style = getProperty(STYLE_PROPERTY);
      if (StringUtils.hasText(style)) {
        writer.write(style);
      }

    }
  }
}
