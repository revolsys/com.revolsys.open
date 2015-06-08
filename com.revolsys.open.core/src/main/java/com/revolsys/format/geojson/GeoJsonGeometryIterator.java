package com.revolsys.format.geojson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.springframework.core.io.Resource;

import com.revolsys.collection.iterator.AbstractIterator;
import com.revolsys.format.json.JsonParser;
import com.revolsys.format.json.JsonParser.EventType;
import com.revolsys.io.FileUtil;
import com.revolsys.io.IoConstants;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.jts.geom.LineString;
import com.revolsys.jts.geom.Point;
import com.revolsys.jts.geom.Polygon;
import com.revolsys.jts.geom.impl.LineStringDouble;

public class GeoJsonGeometryIterator extends AbstractIterator<Geometry> implements GeoJsonConstants {

  private GeometryFactory geometryFactory;

  private JsonParser in;

  public GeoJsonGeometryIterator(final Resource resource) throws IOException {
    this.in = new JsonParser(resource);
  }

  @Override
  protected void doClose() {
    FileUtil.closeSilent(this.in);
    this.geometryFactory = null;
    this.in = null;
  }

  @Override
  protected void doInit() {
    this.geometryFactory = getProperty(IoConstants.GEOMETRY_FACTORY);
    if (this.geometryFactory == null) {
      this.geometryFactory = GeometryFactory.floating3(4326);
    }
    if (this.in.hasNext()) {
      this.in.next();
    }
  }

  @Override
  protected void finalize() throws Throwable {
    close();
  }

  @Override
  protected Geometry getNext() throws NoSuchElementException {
    do {
      final String fieldName = JsonParser.skipToAttribute(this.in);
      if (TYPE.equals(fieldName)) {
        this.in.next();
        final String geometryType = this.in.getValue();
        if (GEOMETRY_TYPE_NAMES.contains(geometryType)) {
          return readGeometry();
        }
      } else if (CRS.equals(fieldName)) {
        this.in.next();
        this.geometryFactory = readCoordinateSystem();

      }
    } while (this.in.getEvent() != EventType.endDocument);
    throw new NoSuchElementException();
  }

  private LineString readCoordinatesList(final boolean cogo, final boolean ring) {
    final List<Double> coordinates = new ArrayList<>();
    final int axisCount = readCoordinatesList(coordinates);
    if (cogo) {
      final int vertexCount = coordinates.size() / axisCount;
      if (vertexCount > 0) {
        final double firstX = coordinates.get(0);
        final double firstY = coordinates.get(1);
        double previousX = firstX;
        double previousY = firstY;
        for (int i = 1; i < vertexCount; i++) {
          final double distance = coordinates.get(i * axisCount);
          final double angleDegrees = coordinates.get(i * axisCount + 1);
          final double angle = Math.toRadians((450 - angleDegrees) % 360);
          final double x = previousX + distance * Math.cos(angle);
          final double y = previousY + distance * Math.sin(angle);

          coordinates.set(i * axisCount, x);
          coordinates.set(i * axisCount + 1, y);
          previousX = x;
          previousY = y;
        }
        if (ring) {
          coordinates.set((vertexCount - 1) * axisCount, firstX);
          coordinates.set((vertexCount - 1) * axisCount + 1, firstY);
        }
      }
    }
    return new LineStringDouble(axisCount, coordinates);
  }

  private int readCoordinatesList(final List<Double> coordinates) {
    int axisCount = 0;
    if (this.in.getEvent() == EventType.startArray || this.in.hasNext()
      && this.in.next() == EventType.startArray) {
      EventType event = this.in.next();
      if (event != EventType.endArray) {
        do {
          axisCount = Math.max(axisCount, readCoordinatesListCoordinates(coordinates));
          event = this.in.next();
        } while (event == EventType.comma);
      }
      if (event != EventType.endArray) {
        throw new IllegalStateException("Exepecting end array, not: " + event);
      }
    } else {
      throw new IllegalStateException("Exepecting start array, not: " + this.in.getEvent());
    }
    return axisCount;
  }

  /**
   * Read one points coordinates and add them to the list of coordinate values.
   *
   * @param values The list to add the points coordinates to.
   * @return The dimension of the coordinate read.
   */
  private int readCoordinatesListCoordinates(final List<Double> values) {
    int numAxis = 0;
    if (this.in.getEvent() == EventType.startArray || this.in.hasNext()
      && this.in.next() == EventType.startArray) {
      EventType event = this.in.getEvent();
      do {
        final Object value = JsonParser.getValue(this.in);
        if (value instanceof EventType) {
          event = (EventType)value;
        } else if (value instanceof Number) {
          values.add(((Number)value).doubleValue());
          numAxis++;
          event = this.in.next();
        } else {
          throw new IllegalArgumentException("Expecting number, not: " + value);
        }
      } while (event == EventType.comma);
      if (event != EventType.endArray) {
        throw new IllegalStateException("Exepecting end array, not: " + event);
      }

      return numAxis;
    } else {
      throw new IllegalStateException("Exepecting start array, not: " + this.in.getEvent());
    }
  }

  private List<LineString> readCoordinatesListList(final boolean cogo, final boolean ring) {
    if (this.in.getEvent() == EventType.startArray || this.in.hasNext()
      && this.in.next() == EventType.startArray) {
      EventType event = this.in.next();
      final List<LineString> coordinatesLists = new ArrayList<LineString>();
      if (event != EventType.endArray) {
        do {
          coordinatesLists.add(readCoordinatesList(cogo, ring));
          event = this.in.next();
        } while (event == EventType.comma);
      }
      if (event != EventType.endArray) {
        throw new IllegalStateException("Exepecting end array, not: " + event);
      }
      return coordinatesLists;
    } else {
      throw new IllegalStateException("Exepecting start array, not: " + this.in.getEvent());
    }
  }

  private List<List<LineString>> readCoordinatesListListList(final boolean cogo) {
    if (this.in.getEvent() == EventType.startArray || this.in.hasNext()
      && this.in.next() == EventType.startArray) {
      EventType event = this.in.next();
      final List<List<LineString>> coordinatesLists = new ArrayList<List<LineString>>();
      if (event != EventType.endArray) {
        do {
          coordinatesLists.add(readCoordinatesListList(cogo, true));
          event = this.in.next();
        } while (event == EventType.comma);
      }
      if (event != EventType.endArray) {
        throw new IllegalStateException("Exepecting end array, not: " + event);
      }
      return coordinatesLists;
    } else {
      throw new IllegalStateException("Exepecting start array, not: " + this.in.getEvent());
    }
  }

  private GeometryFactory readCoordinateSystem() {
    GeometryFactory factory = this.geometryFactory;
    do {
      final String fieldName = JsonParser.skipToNextAttribute(this.in);
      if (PROPERTIES.equals(fieldName)) {
        final Map<String, Object> properties = JsonParser.getMap(this.in);
        final String name = (String)properties.get("name");
        if (name != null) {
          if (name.startsWith(URN_OGC_DEF_CRS_EPSG)) {
            final int srid = Integer.parseInt(name.substring(URN_OGC_DEF_CRS_EPSG.length()));
            factory = GeometryFactory.floating3(srid);
          } else if (name.startsWith(EPSG)) {
            final int srid = Integer.parseInt(name.substring(EPSG.length()));
            factory = GeometryFactory.floating3(srid);
          }
        }
      }
    } while (this.in.getEvent() != EventType.endObject
      && this.in.getEvent() != EventType.endDocument);
    return factory;
  }

  private Geometry readGeometry() {
    final String geometryType = this.in.getValue();
    if (geometryType.equals(POINT)) {
      return readPoint();
    } else if (geometryType.equals(LINE_STRING)) {
      return readLineString(false);
    } else if (geometryType.equals(POLYGON)) {
      return readPolygon(false);
    } else if (geometryType.equals(MULTI_POINT)) {
      return readMultiPoint();
    } else if (geometryType.equals(MULTI_LINE_STRING)) {
      return readMultiLineString(false);
    } else if (geometryType.equals(MULTI_POLYGON)) {
      return readMultiPolygon(false);
    } else if (geometryType.equals(GEOMETRY_COLLECTION)) {
      return readGeometryCollection();
    } else if (geometryType.equals(COGO_LINE_STRING)) {
      return readLineString(true);
    } else if (geometryType.equals(COGO_POLYGON)) {
      return readPolygon(true);
    } else if (geometryType.equals(COGO_MULTI_LINE_STRING)) {
      return readMultiLineString(true);
    } else if (geometryType.equals(COGO_MULTI_POLYGON)) {
      return readMultiPolygon(true);
    } else {
      return null;
    }
  }

  private Geometry readGeometryCollection() {
    List<Geometry> geometries = new ArrayList<Geometry>();
    GeometryFactory factory = this.geometryFactory;
    do {
      final String fieldName = JsonParser.skipToNextAttribute(this.in);
      if (GEOMETRIES.equals(fieldName)) {
        geometries = readGeometryList();
      } else if (CRS.equals(fieldName)) {
        factory = readCoordinateSystem();
      }
    } while (this.in.getEvent() != EventType.endObject
      && this.in.getEvent() != EventType.endDocument);

    return factory.geometry(geometries);
  }

  private List<Geometry> readGeometryList() {
    if (this.in.getEvent() == EventType.startArray || this.in.hasNext()
      && this.in.next() == EventType.startArray) {
      EventType event = this.in.next();
      final List<Geometry> geometries = new ArrayList<Geometry>();
      if (event != EventType.endArray) {
        do {
          final Geometry geometry = getNext();
          geometries.add(geometry);
          event = this.in.next();
        } while (event == EventType.comma);
      }
      if (event != EventType.endArray) {
        throw new IllegalStateException("Exepecting end array, not: " + event);
      }
      return geometries;
    } else {
      throw new IllegalStateException("Exepecting start array, not: " + this.in.getEvent());
    }
  }

  private LineString readLineString(final boolean cogo) {
    LineString points = null;
    GeometryFactory factory = this.geometryFactory;
    do {
      final String fieldName = JsonParser.skipToNextAttribute(this.in);
      if (COORDINATES.equals(fieldName)) {
        points = readCoordinatesList(cogo, false);
      } else if (CRS.equals(fieldName)) {
        factory = readCoordinateSystem();
      }
    } while (this.in.getEvent() != EventType.endObject
      && this.in.getEvent() != EventType.endDocument);

    if (points == null) {
      return factory.lineString();
    } else {
      final int axisCount = points.getAxisCount();
      final GeometryFactory geometryFactory = factory.convertAxisCount(axisCount);
      return geometryFactory.lineString(points);
    }
  }

  private Geometry readMultiLineString(final boolean cogo) {
    List<LineString> lineStrings = null;
    GeometryFactory factory = this.geometryFactory;
    do {
      final String fieldName = JsonParser.skipToNextAttribute(this.in);
      if (COORDINATES.equals(fieldName)) {
        lineStrings = readCoordinatesListList(cogo, false);
      } else if (CRS.equals(fieldName)) {
        factory = readCoordinateSystem();
      }
    } while (this.in.getEvent() != EventType.endObject
      && this.in.getEvent() != EventType.endDocument);
    int axisCount = 2;
    for (final LineString points : lineStrings) {
      axisCount = Math.max(axisCount, points.getAxisCount());
    }
    factory = factory.convertAxisCount(axisCount);
    return factory.multiLineString(lineStrings);
  }

  private Geometry readMultiPoint() {
    List<LineString> pointsList = null;
    GeometryFactory factory = this.geometryFactory;
    do {
      final String fieldName = JsonParser.skipToNextAttribute(this.in);
      if (COORDINATES.equals(fieldName)) {
        pointsList = readPointCoordinatesListList();
      } else if (CRS.equals(fieldName)) {
        factory = readCoordinateSystem();
      }
    } while (this.in.getEvent() != EventType.endObject
      && this.in.getEvent() != EventType.endDocument);
    int axisCount = 2;
    for (final LineString points : pointsList) {
      axisCount = Math.max(axisCount, points.getAxisCount());
    }
    factory = factory.convertAxisCount(axisCount);
    return factory.multiPoint(pointsList);
  }

  private Geometry readMultiPolygon(final boolean cogo) {
    final List<Polygon> polygons = new ArrayList<Polygon>();
    List<List<LineString>> polygonRings = null;
    GeometryFactory factory = this.geometryFactory;
    do {
      final String fieldName = JsonParser.skipToNextAttribute(this.in);
      if (COORDINATES.equals(fieldName)) {
        polygonRings = readCoordinatesListListList(cogo);
      } else if (CRS.equals(fieldName)) {
        factory = readCoordinateSystem();
      }
    } while (this.in.getEvent() != EventType.endObject
      && this.in.getEvent() != EventType.endDocument);
    int axisCount = 2;
    if (polygonRings != null) {
      for (final List<LineString> rings : polygonRings) {
        for (final LineString points : rings) {
          axisCount = Math.max(axisCount, points.getAxisCount());
        }
        factory = factory.convertAxisCount(axisCount);

        final Polygon polygon = factory.polygon(rings);
        polygons.add(polygon);
      }
    }
    return factory.multiPolygon(polygons);
  }

  private Point readPoint() {
    LineString coordinates = null;
    GeometryFactory factory = this.geometryFactory;
    do {
      final String fieldName = JsonParser.skipToNextAttribute(this.in);
      if (COORDINATES.equals(fieldName)) {
        coordinates = readPointCoordinatesList();
      } else if (CRS.equals(fieldName)) {
        factory = readCoordinateSystem();
      }
    } while (this.in.getEvent() != EventType.endObject
      && this.in.getEvent() != EventType.endDocument);
    if (coordinates == null) {
      return factory.point();
    } else {
      final int axisCount = coordinates.getAxisCount();
      final GeometryFactory geometryFactory = factory.convertAxisCount(axisCount);
      return geometryFactory.point(coordinates);
    }
  }

  private LineString readPointCoordinatesList() {
    final double[] values = JsonParser.getDoubleArray(this.in);
    if (values == null) {
      return null;
    } else {
      return new LineStringDouble(values.length, values);
    }
  }

  private List<LineString> readPointCoordinatesListList() {
    if (this.in.getEvent() == EventType.startArray || this.in.hasNext()
      && this.in.next() == EventType.startArray) {
      EventType event = this.in.next();
      final List<LineString> coordinatesLists = new ArrayList<LineString>();
      if (event != EventType.endArray) {
        do {
          coordinatesLists.add(readPointCoordinatesList());
          event = this.in.next();
        } while (event == EventType.comma);
      }
      if (event != EventType.endArray) {
        throw new IllegalStateException("Exepecting end array, not: " + event);
      }
      return coordinatesLists;
    } else {
      throw new IllegalStateException("Exepecting start array, not: " + this.in.getEvent());
    }
  }

  private Polygon readPolygon(final boolean cogo) {
    List<LineString> rings = null;
    GeometryFactory factory = this.geometryFactory;
    do {
      final String fieldName = JsonParser.skipToNextAttribute(this.in);
      if (COORDINATES.equals(fieldName)) {
        rings = readCoordinatesListList(cogo, true);
      } else if (CRS.equals(fieldName)) {
        factory = readCoordinateSystem();
      }
    } while (this.in.getEvent() != EventType.endObject
      && this.in.getEvent() != EventType.endDocument);
    int axisCount = 2;
    for (final LineString points : rings) {
      axisCount = Math.max(axisCount, points.getAxisCount());
    }
    factory = factory.convertAxisCount(axisCount);
    return factory.polygon(rings);
  }
}
