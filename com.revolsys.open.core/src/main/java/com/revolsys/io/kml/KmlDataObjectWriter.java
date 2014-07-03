package com.revolsys.io.kml;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.util.StringUtils;

import com.revolsys.converter.string.BooleanStringConverter;
import com.revolsys.data.record.Record;
import com.revolsys.data.record.schema.RecordDefinition;
import com.revolsys.io.AbstractWriter;
import com.revolsys.io.IoConstants;
import com.revolsys.jts.geom.BoundingBox;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.jts.geom.Point;

public class KmlDataObjectWriter extends AbstractWriter<Record> implements
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
    open();
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
  public void open() {
    if (!opened) {
      writeHeader();
    }
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
    return "KML Writer";
  }

  @Override
  public void write(final Record object) {
    open();
    writer.startTag(PLACEMARK);
    final RecordDefinition metaData = object.getMetaData();
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
    writeLookAt(object.getGeometryValue());
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
        if (value != null
          || BooleanStringConverter.isTrue(getProperty(Kml22Constants.WRITE_NULLS_PROPERTY))) {
          if (!hasValues) {
            hasValues = true;
            writer.startTag(EXTENDED_DATA);
          }
          writer.startTag(DATA);
          writer.attribute(NAME, attributeName);
          writer.element(VALUE, value);
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
          geometry = metaData.getGeometryFactory().geometry(geometries);
        }
      }
      if (geometry != null) {
        GeometryFactory geometryFactory = getProperty(IoConstants.GEOMETRY_FACTORY);
        if (geometryFactory == null) {
          geometryFactory = geometry.getGeometryFactory();
        }
        int axisCount = geometryFactory.getAxisCount();
        writer.writeGeometry(geometry, axisCount);
      }
    }
    writer.endTag();
  }

  private void writeHeader() {
    opened = true;
    writer.startDocument("UTF-8", "1.0");

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
      final Point point = getProperty(LOOK_AT_POINT_PROPERTY);
      if (point != null) {
        Number range = getProperty(LOOK_AT_RANGE_PROPERTY);
        if (range == null) {
          range = 1000;
        }
        writeLookAt(point, range.longValue());
      }
      final String style = getProperty(STYLE_PROPERTY);
      if (StringUtils.hasText(style)) {
        writer.write(style);
      }

    }
  }

  private void writeLookAt(final Geometry geometry) {
    if (geometry != null) {
      final GeometryFactory geometryFactory = GeometryFactory.wgs84();
      final Geometry projectedGeometry = geometry.convert(geometryFactory);
      final BoundingBox boundingBox = projectedGeometry.getBoundingBox();
      final Point centre = geometryFactory.point(boundingBox.getCentreX(),
        boundingBox.getCentreY());

      final Number configRange = getProperty(LOOK_AT_RANGE_PROPERTY);
      final long range;
      if (configRange == null) {
        range = KmlXmlWriter.getLookAtRange(boundingBox);
      } else {
        range = configRange.longValue();
      }
      writeLookAt(centre, range);
    }
  }

  public void writeLookAt(Point point, long range) {
    final Number minRange = getProperty(Kml22Constants.LOOK_AT_MIN_RANGE_PROPERTY);
    if (minRange != null) {
      if (range < minRange.doubleValue()) {
        range = minRange.longValue();
      }
    }
    final Number maxRange = getProperty(Kml22Constants.LOOK_AT_MAX_RANGE_PROPERTY);
    if (maxRange != null) {
      if (range > maxRange.doubleValue()) {
        range = maxRange.longValue();
      }
    }

    writer.startTag(LOOK_AT);
    point = (Point)point.convert(GeometryFactory.wgs84());
    writer.element(LONGITUDE, point.getX());
    writer.element(LATITUDE, point.getY());
    writer.element(ALTITUDE, 0);
    writer.element(HEADING, 0);
    writer.element(TILT, 0);
    writer.element(RANGE, range);
    writer.endTag(LOOK_AT);
  }
}
