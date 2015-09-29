package com.revolsys.record.io.format.geojson;

import java.io.BufferedWriter;
import java.io.Writer;

import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryCollection;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.LineString;
import com.revolsys.geometry.model.MultiLineString;
import com.revolsys.geometry.model.MultiPoint;
import com.revolsys.geometry.model.MultiPolygon;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.Polygon;
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

  public GeoJsonRecordWriter(final Writer out) {
    this(out, false);
  }

  public GeoJsonRecordWriter(final Writer out, final boolean cogo) {
    this.out = new JsonWriter(new BufferedWriter(out));
    this.out.setIndent(true);
    this.cogo = cogo;
  }

  /**
   * Closes the underlying reader.
   */
  @Override
  public void close() {
    if (this.out != null) {
      try {
        writeFooter();
      } finally {
        this.out.close();
        this.out = null;
      }
    }
  }

  private void coordinate(final Point coordinates) {
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

  private void coordinates(final LineString line) {
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

  public void coordinates(final Point point) {
    coordinate(point);
  }

  public void coordinates(final Polygon polygon) {
    this.out.startList(false);
    this.out.indent();

    final LineString exteriorRing = polygon.getShell();
    coordinates(exteriorRing);
    for (int i = 0; i < polygon.getHoleCount(); i++) {
      final LineString interiorRing = polygon.getHole(i);
      this.out.endAttribute();
      this.out.indent();
      coordinates(interiorRing);
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
    } else if (geometry instanceof MultiPoint) {
      final MultiPoint multiPoint = (MultiPoint)geometry;
      multiPoint(multiPoint);
    } else if (geometry instanceof MultiLineString) {
      final MultiLineString multiLine = (MultiLineString)geometry;
      multiLineString(multiLine);
    } else if (geometry instanceof MultiPolygon) {
      final MultiPolygon multiPolygon = (MultiPolygon)geometry;
      multiPolygon(multiPolygon);
    } else if (geometry instanceof GeometryCollection) {
      final GeometryCollection geometryCollection = (GeometryCollection)geometry;
      geometryCollection(geometryCollection);
    }
    this.out.endObject();
  }

  private void geometryCollection(final GeometryCollection geometryCollection) {
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
      coordinates(line);
    }
  }

  private void multiLineString(final MultiLineString multiLineString) {
    if (this.cogo) {
      type(CogoJson.COGO_LINE_STRING);
    } else {
      type(GeoJson.MULTI_LINE_STRING);
    }

    this.out.endAttribute();
    this.out.label(GeoJson.COORDINATES);
    this.out.startList();
    this.out.indent();
    final int numGeometries = multiLineString.getGeometryCount();
    if (numGeometries > 0) {
      coordinates((LineString)multiLineString.getGeometry(0));
      for (int i = 1; i < numGeometries; i++) {
        final LineString lineString = (LineString)multiLineString.getGeometry(i);
        this.out.endAttribute();
        this.out.indent();
        coordinates(lineString);
      }
    }
    this.out.endList();
  }

  private void multiPoint(final MultiPoint multiPoint) {
    type(GeoJson.MULTI_POINT);

    this.out.endAttribute();
    this.out.label(GeoJson.COORDINATES);
    this.out.startList();
    this.out.indent();
    final int numGeometries = multiPoint.getGeometryCount();
    if (numGeometries > 0) {
      coordinates((Point)multiPoint.getGeometry(0));
      for (int i = 1; i < numGeometries; i++) {
        final Point point = (Point)multiPoint.getGeometry(i);
        this.out.endAttribute();
        this.out.indent();
        coordinates(point);
      }
    }
    this.out.endList();
  }

  private void multiPolygon(final MultiPolygon multiPolygon) {
    if (this.cogo) {
      type(CogoJson.COGO_MULTI_POLYGON);
    } else {
      type(GeoJson.MULTI_POLYGON);
    }

    this.out.endAttribute();
    this.out.label(GeoJson.COORDINATES);
    this.out.startList();
    this.out.indent();
    final int numGeometries = multiPolygon.getGeometryCount();
    if (numGeometries > 0) {
      coordinates((Polygon)multiPolygon.getGeometry(0));
      for (int i = 1; i < numGeometries; i++) {
        final Polygon polygon = (Polygon)multiPolygon.getGeometry(i);
        this.out.endAttribute();
        this.out.indent();
        coordinates(polygon);
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
      coordinates(point);
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
      coordinates(polygon);
    }
  }

  private void srid(final int srid) {
    final String urn = GeoJson.URN_OGC_DEF_CRS_EPSG + srid;
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

  private void type(final String type) {
    this.out.label(GeoJson.TYPE);
    this.out.value(type);
  }

  @Override
  public void write(final Record object) {
    if (this.initialized) {
      this.out.endAttribute();
    } else {
      writeHeader();
      this.initialized = true;
    }
    this.out.startObject();
    type(GeoJson.FEATURE);
    Geometry mainGeometry = object.getGeometry();
    final GeometryFactory geometryFactory = getProperty(IoConstants.GEOMETRY_FACTORY);
    if (geometryFactory != null) {
      mainGeometry = mainGeometry.convert(geometryFactory);
    }
    writeSrid(mainGeometry);
    final RecordDefinition recordDefinition = object.getRecordDefinition();
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
    final int numAttributes = recordDefinition.getFieldCount();
    boolean hasValue = false;
    for (int i = 0; i < numAttributes; i++) {
      if (i != geometryIndex) {
        final Object value = object.getValue(i);
        if (isValueWritable(value)) {
          if (hasValue) {
            this.out.endAttribute();
          } else {
            hasValue = true;
          }
          final FieldDefinition attribute = recordDefinition.getField(i);
          final String name = attribute.getName();
          this.out.label(name);
          if (value instanceof Geometry) {
            final Geometry geometry = (Geometry)value;
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
      this.out.endList();
      this.out.endObject();
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
      this.out.endAttribute();
      this.out.label(GeoJson.FEATURES);
      this.out.startList();
    }
  }

  private int writeSrid() {
    final GeometryFactory geometryFactory = getProperty(IoConstants.GEOMETRY_FACTORY);
    return writeSrid(geometryFactory);
  }

  private void writeSrid(final Geometry geometry) {
    if (geometry != null) {
      final GeometryFactory geometryFactory = geometry.getGeometryFactory();
      writeSrid(geometryFactory);
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
