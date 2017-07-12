package com.revolsys.record.io.format.geojson;

import java.io.BufferedWriter;
import java.io.Writer;

import com.revolsys.geometry.model.ClockDirection;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.LineString;
import com.revolsys.geometry.model.Lineal;
import com.revolsys.geometry.model.LinearRing;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.Polygon;
import com.revolsys.geometry.model.Polygonal;
import com.revolsys.geometry.model.Punctual;
import com.revolsys.io.AbstractRecordWriter;
import com.revolsys.io.IoConstants;
import com.revolsys.math.Angle;
import com.revolsys.record.Record;
import com.revolsys.record.io.format.cogojson.CogoJson;
import com.revolsys.record.io.format.json.JsonWriter;
import com.revolsys.record.schema.FieldDefinition;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.util.MathUtil;

public class GeoJsonRecordWriter extends AbstractRecordWriter {

  private final boolean cogo;

  boolean initialized = false;

  /** The writer */
  private JsonWriter out;

  private boolean singleObject;

  private int srid = -1;

  private RecordDefinition recordDefinition;

  private boolean allowCustomCoordinateSystem = true;

  public GeoJsonRecordWriter(final Writer out, final boolean cogo) {
    this.out = new JsonWriter(new BufferedWriter(out));
    this.out.setIndent(true);
    this.cogo = cogo;
  }

  public GeoJsonRecordWriter(final Writer out, final RecordDefinition recordDefinition) {
    this(out, false);
    this.recordDefinition = recordDefinition;
    setProperty(IoConstants.GEOMETRY_FACTORY, recordDefinition.getGeometryFactory());
  }

  /**
   * Closes the underlying reader.
   */
  @Override
  public void close() {
    if (this.out != null) {
      try {
        if (!this.initialized) {
          writeHeader();
        }
        writeFooter();
      } finally {
        this.out.close();
        this.out = null;
      }
    }
  }

  private void coordinatePoint(final Point coordinates) {
    this.out.print('[');
    for (int axisIndex = 0; axisIndex < coordinates.getAxisCount(); axisIndex++) {
      if (axisIndex > 0) {
        this.out.print(',');
      }
      final double value = coordinates.getCoordinate(axisIndex);
      this.out.value(value);
    }
    this.out.print(']');
  }

  private void coordinatesLineString(final LineString line) {
    this.out.startList(false);
    this.out.indent();
    for (int i = 0; i < line.getVertexCount(); i++) {
      if (i > 0) {
        this.out.endAttribute();
        this.out.indent();
      }
      double x = line.getX(i);
      double y = line.getY(i);

      if (this.cogo && i > 0) {
        final double currentX = x;
        final double previousX = line.getX(i - 1);
        final double previousY = line.getY(i - 1);
        x = MathUtil.distance(previousX, previousY, currentX, y);
        y = Angle.angleNorthDegrees(previousX, previousY, currentX, y);
      }

      this.out.print('[');
      this.out.value(x);
      this.out.print(',');
      this.out.value(y);

      for (int axisIndex = 2; axisIndex < line.getAxisCount(); axisIndex++) {
        this.out.print(',');
        final double value = line.getCoordinate(i, axisIndex);
        this.out.value(value);
      }
      this.out.print(']');
    }
    this.out.endList();
  }

  private void coordinatesPoint(final Point point) {
    coordinatePoint(point);
  }

  private void coordinatesPolygon(final Polygon polygon) {
    this.out.startList(false);
    this.out.indent();

    final LineString shell = polygon.getShell();
    coordinatesLineString(shell.toCounterClockwise());
    for (final LinearRing hole : polygon.holes()) {
      this.out.endAttribute();
      this.out.indent();
      coordinatesLineString(hole.toClockwise());
    }

    this.out.endList();
  }

  @Override
  public void flush() {
    this.out.flush();
  }

  private void geometry(final Geometry geometry) {
    this.out.startObject();
    if (geometry instanceof Point) {
      final Point point = (Point)geometry;
      point(point);
    } else if (geometry instanceof LineString) {
      final LineString line = (LineString)geometry;
      line(line);
    } else if (geometry instanceof Polygon) {
      final Polygon polygon = (Polygon)geometry;
      polygon(polygon);
    } else if (geometry instanceof Punctual) {
      final Punctual punctual = (Punctual)geometry;
      multiPoint(punctual);
    } else if (geometry instanceof Lineal) {
      final Lineal lineal = (Lineal)geometry;
      multiLineString(lineal);
    } else if (geometry instanceof Polygonal) {
      final Polygonal polygonal = (Polygonal)geometry;
      multiPolygon(polygonal);
    } else if (geometry.isGeometryCollection()) {
      geometryCollection(geometry);
    }
    this.out.endObject();
  }

  private void geometryCollection(final Geometry geometryCollection) {
    type(GeoJson.GEOMETRY_COLLECTION);

    this.out.endAttribute();
    this.out.label(GeoJson.GEOMETRIES);
    this.out.startList();
    final int numGeometries = geometryCollection.getGeometryCount();
    if (numGeometries > 0) {
      geometry(geometryCollection.getGeometry(0));
      for (int i = 1; i < numGeometries; i++) {
        final Geometry geometry = geometryCollection.getGeometry(i);
        this.out.endAttribute();
        geometry(geometry);
      }
    }
    this.out.endList();
  }

  @Override
  public ClockDirection getPolygonRingDirection() {
    return ClockDirection.COUNTER_CLOCKWISE;
  }

  @Override
  public RecordDefinition getRecordDefinition() {
    return this.recordDefinition;
  }

  public boolean isAllowCustomCoordinateSystem() {
    return this.allowCustomCoordinateSystem;
  }

  public boolean isCogo() {
    return this.cogo;
  }

  private void line(final LineString line) {
    if (this.cogo) {
      type(CogoJson.COGO_LINE_STRING);
    } else {
      type(GeoJson.LINE_STRING);
    }
    this.out.endAttribute();
    this.out.label(GeoJson.COORDINATES);
    if (line.isEmpty()) {
      this.out.startList();
      this.out.endList();
    } else {
      coordinatesLineString(line);
    }
  }

  private void multiLineString(final Lineal lineal) {
    if (this.cogo) {
      type(CogoJson.COGO_LINE_STRING);
    } else {
      type(GeoJson.MULTI_LINE_STRING);
    }

    this.out.endAttribute();
    this.out.label(GeoJson.COORDINATES);
    this.out.startList();
    this.out.indent();
    final int numGeometries = lineal.getGeometryCount();
    if (numGeometries > 0) {
      coordinatesLineString((LineString)lineal.getGeometry(0));
      for (int i = 1; i < numGeometries; i++) {
        final LineString lineString = (LineString)lineal.getGeometry(i);
        this.out.endAttribute();
        this.out.indent();
        coordinatesLineString(lineString);
      }
    }
    this.out.endList();
  }

  private void multiPoint(final Punctual punctual) {
    type(GeoJson.MULTI_POINT);

    this.out.endAttribute();
    this.out.label(GeoJson.COORDINATES);
    this.out.startList();
    this.out.indent();
    final int numGeometries = punctual.getGeometryCount();
    if (numGeometries > 0) {
      coordinatesPoint(punctual.getPoint(0));
      for (int i = 1; i < numGeometries; i++) {
        final Point point = punctual.getPoint(i);
        this.out.endAttribute();
        this.out.indent();
        coordinatesPoint(point);
      }
    }
    this.out.endList();
  }

  private void multiPolygon(final Polygonal polygonal) {
    if (this.cogo) {
      type(CogoJson.COGO_MULTI_POLYGON);
    } else {
      type(GeoJson.MULTI_POLYGON);
    }

    this.out.endAttribute();
    this.out.label(GeoJson.COORDINATES);
    this.out.startList();
    this.out.indent();
    final int numGeometries = polygonal.getGeometryCount();
    if (numGeometries > 0) {
      coordinatesPolygon((Polygon)polygonal.getGeometry(0));
      for (int i = 1; i < numGeometries; i++) {
        final Polygon polygon = (Polygon)polygonal.getGeometry(i);
        this.out.endAttribute();
        this.out.indent();
        coordinatesPolygon(polygon);
      }
    }
    this.out.endList();
  }

  private void point(final Point point) {
    type(GeoJson.POINT);
    this.out.endAttribute();
    this.out.label(GeoJson.COORDINATES);
    if (point.isEmpty()) {
      this.out.startList();
      this.out.endList();
    } else {
      coordinatesPoint(point);
    }
  }

  private void polygon(final Polygon polygon) {
    if (this.cogo) {
      type(CogoJson.COGO_POLYGON);
    } else {
      type(GeoJson.POLYGON);
    }

    this.out.endAttribute();
    this.out.label(GeoJson.COORDINATES);
    if (polygon.isEmpty()) {
      this.out.startList();
      this.out.endList();
    } else {
      coordinatesPolygon(polygon);
    }
  }

  public void setAllowCustomCoordinateSystem(final boolean allowCustomCoordinateSystem) {
    this.allowCustomCoordinateSystem = allowCustomCoordinateSystem;
  }

  private void srid(final int srid) {
    if (isAllowCustomCoordinateSystem()) {
      final String urn;
      if (srid == 4326) {
        urn = "urn:ogc:def:crs:OGC::CRS84";
      } else {
        urn = GeoJson.URN_OGC_DEF_CRS_EPSG + srid;
      }
      this.out.label(GeoJson.CRS);
      this.out.startObject();
      type(GeoJson.NAME);
      this.out.endAttribute();
      this.out.label(GeoJson.PROPERTIES);
      this.out.startObject();
      this.out.label(GeoJson.NAME);
      this.out.value(urn);
      this.out.endObject();
      this.out.endObject();
    }
  }

  private void type(final String type) {
    this.out.label(GeoJson.TYPE);
    this.out.value(type);
  }

  @Override
  public void write(final Record record) {
    if (this.initialized) {
      this.out.endAttribute();
      if (!isIndent()) {
        this.out.newLineForce();
      }
    } else {
      writeHeader();
      this.initialized = true;
    }
    this.out.startObject();
    type(GeoJson.FEATURE);
    Geometry mainGeometry = record.getGeometry();
    final GeometryFactory geometryFactory = getProperty(IoConstants.GEOMETRY_FACTORY);
    if (geometryFactory != null) {
      mainGeometry = mainGeometry.convertGeometry(geometryFactory);
    }
    writeSrid(mainGeometry);
    final RecordDefinition recordDefinition = record.getRecordDefinition();
    final int geometryIndex = recordDefinition.getGeometryFieldIndex();
    boolean geometryWritten = false;
    this.out.endAttribute();
    this.out.label(GeoJson.GEOMETRY);
    if (mainGeometry != null) {
      geometryWritten = true;
      geometry(mainGeometry);
    }
    if (!geometryWritten) {
      this.out.value(null);
    }
    this.out.endAttribute();
    this.out.label(GeoJson.PROPERTIES);
    this.out.startObject();
    boolean hasValue = false;
    for (final FieldDefinition field : recordDefinition.getFields()) {
      final int fieldIndex = field.getIndex();
      if (fieldIndex != geometryIndex) {
        final Object value;
        if (isWriteCodeValues()) {
          value = record.getCodeValue(fieldIndex);
        } else {
          value = record.getValue(fieldIndex);
        }
        if (isValueWritable(value)) {
          if (hasValue) {
            this.out.endAttribute();
          } else {
            hasValue = true;
          }
          final String name = field.getName();
          this.out.label(name);
          if (value instanceof Geometry) {
            Geometry geometry = (Geometry)value;
            if (!this.allowCustomCoordinateSystem) {
              final GeometryFactory wgs84 = geometryFactory.convertSrid(4326);
              geometry = geometry.convertGeometry(wgs84);
            }
            geometry(geometry);
          } else {
            this.out.value(value);
          }
        }
      }
    }
    this.out.endObject();
    this.out.endObject();
  }

  private void writeFooter() {
    if (!this.singleObject) {
      if (!isIndent()) {
        this.out.newLineForce();
      }
      this.out.endList();
      this.out.endObject();
      this.out.newLineForce();
    }
    final String callback = getProperty(IoConstants.JSONP_PROPERTY);
    if (callback != null) {
      this.out.print(");");
    }
  }

  private void writeHeader() {
    this.out.setIndent(isIndent());
    final String callback = getProperty(IoConstants.JSONP_PROPERTY);
    if (callback != null) {
      this.out.print(callback);
      this.out.print('(');
    }
    this.singleObject = Boolean.TRUE.equals(getProperty(IoConstants.SINGLE_OBJECT_PROPERTY));
    if (!this.singleObject) {
      this.out.startObject();
      type(GeoJson.FEATURE_COLLECTION);
      this.srid = writeSrid();
      if (this.allowCustomCoordinateSystem) {
        this.out.endAttribute();
      }
      this.out.label(GeoJson.FEATURES);
      this.out.startList();
      this.out.newLineForce();
    }
  }

  private int writeSrid() {
    final GeometryFactory geometryFactory = getProperty(IoConstants.GEOMETRY_FACTORY);
    return writeSrid(geometryFactory);
  }

  private void writeSrid(final Geometry geometry) {
    if (geometry != null) {
      final GeometryFactory geometryFactory = geometry.getGeometryFactory();
      if (this.recordDefinition == null
        || !geometryFactory.isSameCoordinateSystem(this.recordDefinition.getGeometryFactory())) {
        writeSrid(geometryFactory);
      }
    }
  }

  protected int writeSrid(final GeometryFactory geometryFactory) {
    if (geometryFactory != null) {
      final int srid = geometryFactory.getCoordinateSystemId();
      if (srid != 0 && srid != this.srid) {
        this.out.endAttribute();
        srid(srid);
        return srid;
      }
    }
    return -1;
  }
}
