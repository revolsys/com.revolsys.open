package com.revolsys.format.kml;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.revolsys.converter.string.BooleanStringConverter;
import com.revolsys.data.record.Record;
import com.revolsys.data.record.schema.RecordDefinition;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.Point;
import com.revolsys.io.AbstractRecordWriter;
import com.revolsys.io.IoConstants;
import com.revolsys.util.Property;

public class KmlRecordWriter extends AbstractRecordWriter implements Kml22Constants {
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

  private String defaultStyleUrl;

  private boolean opened;

  private String styleUrl;

  private final KmlXmlWriter writer;

  public KmlRecordWriter(final java.io.Writer out) {
    this.writer = new KmlXmlWriter(out);
  }

  @Override
  public void close() {
    open();
    if (!Boolean.TRUE.equals(getProperty(IoConstants.SINGLE_OBJECT_PROPERTY))) {
      this.writer.endTag(DOCUMENT);
    }
    this.writer.endTag(KML);
    this.writer.endDocument();
    this.writer.close();
  }

  @Override
  public void flush() {
    this.writer.flush();
  }

  @Override
  public boolean isWriteNulls() {
    return super.isWriteNulls()
      || BooleanStringConverter.isTrue(getProperty(Kml22Constants.WRITE_NULLS_PROPERTY));
  }

  @Override
  public void open() {
    if (!this.opened) {
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
      if (Property.hasValue(styleUrl)) {
        if (Property.hasValue(this.defaultStyleUrl)) {
          this.styleUrl = styleUrl;
        } else {
          this.defaultStyleUrl = styleUrl;
        }
      } else {
        this.styleUrl = this.defaultStyleUrl;
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
    this.writer.startTag(PLACEMARK);
    final RecordDefinition recordDefinition = object.getRecordDefinition();
    final int geometryIndex = recordDefinition.getGeometryFieldIndex();
    final int idIndex = recordDefinition.getIdFieldIndex();

    final String nameAttribute = getProperty(PLACEMARK_NAME_ATTRIBUTE_PROPERTY);
    String name = null;
    if (nameAttribute != null) {
      name = object.getValue(nameAttribute);
    }
    if (name == null && idIndex != -1) {
      final Object id = object.getValue(idIndex);
      final String typeName = recordDefinition.getName();
      name = typeName + " " + id;
    }
    if (name != null) {
      this.writer.element(NAME, name);
    }
    final String snippet = getProperty(SNIPPET_PROPERTY);
    if (snippet != null) {
      this.writer.startTag(SNIPPET);
      this.writer.text(snippet);
      this.writer.endTag(SNIPPET);
    }
    String description = getProperty(PLACEMARK_DESCRIPTION_PROPERTY);
    if (description == null) {
      description = getProperty(IoConstants.DESCRIPTION_PROPERTY);
    }
    if (description != null) {
      this.writer.startTag(DESCRIPTION);
      this.writer.cdata(description);
      this.writer.endTag(DESCRIPTION);
    }
    writeLookAt(object.getGeometry());
    if (Property.hasValue(this.styleUrl)) {
      this.writer.element(STYLE_URL, this.styleUrl);
    } else if (Property.hasValue(this.defaultStyleUrl)) {
      this.writer.element(STYLE_URL, this.defaultStyleUrl);
    }
    boolean hasValues = false;
    for (int i = 0; i < recordDefinition.getFieldCount(); i++) {
      if (i != geometryIndex) {
        final String fieldName = recordDefinition.getFieldName(i);
        final Object value = object.getValue(i);
        if (isValueWritable(value)) {
          if (!hasValues) {
            hasValues = true;
            this.writer.startTag(EXTENDED_DATA);
          }
          this.writer.startTag(DATA);
          this.writer.attribute(NAME, fieldName);
          this.writer.element(VALUE, value);
          this.writer.endTag(DATA);
        }
      }
    }
    if (hasValues) {
      this.writer.endTag(EXTENDED_DATA);
    }
    final List<Integer> geometryFieldIndexes = recordDefinition.getGeometryFieldIndexes();
    if (!geometryFieldIndexes.isEmpty()) {
      Geometry geometry = null;
      if (geometryFieldIndexes.size() == 1) {
        geometry = object.getValue(geometryFieldIndexes.get(0));
      } else {
        final List<Geometry> geometries = new ArrayList<Geometry>();
        for (final Integer geometryFieldIndex : geometryFieldIndexes) {
          final Geometry part = object.getValue(geometryFieldIndex);
          if (part != null) {
            geometries.add(part);
          }
        }
        if (!geometries.isEmpty()) {
          geometry = recordDefinition.getGeometryFactory().geometry(geometries);
        }
      }
      if (geometry != null) {
        GeometryFactory geometryFactory = getProperty(IoConstants.GEOMETRY_FACTORY);
        if (geometryFactory == null) {
          geometryFactory = geometry.getGeometryFactory();
        }
        final int axisCount = geometryFactory.getAxisCount();
        this.writer.writeGeometry(geometry, axisCount);
      }
    }
    this.writer.endTag();
  }

  private void writeHeader() {
    this.writer.setIndent(isIndent());
    this.opened = true;
    this.writer.startDocument("UTF-8", "1.0");

    this.writer.startTag(KML);
    if (!Boolean.TRUE.equals(getProperty(IoConstants.SINGLE_OBJECT_PROPERTY))) {
      this.writer.startTag(DOCUMENT);
      final String name = getProperty(DOCUMENT_NAME_PROPERTY);
      if (name != null) {
        this.writer.element(NAME, name);
      }
      final String snippet = getProperty(SNIPPET_PROPERTY);
      if (snippet != null) {
        this.writer.startTag(SNIPPET);
        this.writer.text(snippet);
        this.writer.endTag(SNIPPET);
      }
      final String description = getProperty(DOCUMENT_DESCRIPTION_PROPERTY);
      if (description != null) {
        this.writer.element(DESCRIPTION, description);
      }
      this.writer.element(OPEN, 1);
      final Point point = getProperty(LOOK_AT_POINT_PROPERTY);
      if (point != null) {
        Number range = getProperty(LOOK_AT_RANGE_PROPERTY);
        if (range == null) {
          range = 1000;
        }
        writeLookAt(point, range.longValue());
      }
      final String style = getProperty(STYLE_PROPERTY);
      if (Property.hasValue(style)) {
        this.writer.write(style);
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

    this.writer.startTag(LOOK_AT);
    point = (Point)point.convert(GeometryFactory.wgs84());
    this.writer.element(LONGITUDE, point.getX());
    this.writer.element(LATITUDE, point.getY());
    this.writer.element(ALTITUDE, 0);
    this.writer.element(HEADING, 0);
    this.writer.element(TILT, 0);
    this.writer.element(RANGE, range);
    this.writer.endTag(LOOK_AT);
  }
}
