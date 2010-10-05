package com.revolsys.gis.geojson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.core.io.Resource;

import com.revolsys.gis.cs.GeometryFactory;
import com.revolsys.gis.cs.epsg.EpsgCoordinateSystems;
import com.revolsys.gis.model.coordinates.list.CoordinatesList;
import com.revolsys.gis.model.coordinates.list.DoubleCoordinatesList;
import com.revolsys.json.JsonParser;
import com.revolsys.json.JsonParser.EventType;
import com.revolsys.json.JsonParserUtil;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

public class GeoJsonGeometryIterator implements Iterator<Geometry>,
  GeoJsonConstants {

  private GeometryFactory geometryFactory = new GeometryFactory(
    EpsgCoordinateSystems.getCoordinateSystem(4326));

  /** The current geometry. */
  private Geometry currentGeometry;

  /** Flag indicating if there are more records to be read. */
  private boolean hasNext = false;

  private JsonParser in;

  public GeoJsonGeometryIterator(
    Resource resource)
    throws IOException {
    this.in = new JsonParser(resource);
    init();
  }

  private void init() {
    if (in.hasNext()) {
      EventType event = in.next();
      if (event == EventType.startDocument) {
        hasNext = true;
        readNextGeometry();
      }
    }
    if (!hasNext) {
      close();
    }
  }

  public void close() {
    in.close();
  }

  @Override
  protected void finalize()
    throws Throwable {
    close();
  }

  public boolean hasNext() {
    return hasNext;
  }

  public Geometry next() {
    if (!hasNext) {
      throw new NoSuchElementException("No more elements");
    } else {
      final Geometry geometry = currentGeometry;
      readNextGeometry();
      return geometry;
    }
  }

  private void readNextGeometry() {
    String geometryType = null;
    do {
      do {
        JsonParserUtil.skipToAttribute(in, TYPE);
        if (in.getEvent() == EventType.endDocument) {
          currentGeometry = null;
          hasNext = false;
          return;
        }
      } while (in.getEvent() != EventType.colon);
      geometryType = JsonParserUtil.getString(in);
    } while (!GEOMETRY_TYPE_NAMES.contains(geometryType));
    currentGeometry = readGeometry();
  }

  private Geometry readGeometry() {
    String geometryType = in.getValue();
    if (geometryType.equals(POINT)) {
      return readPoint();
    } else if (geometryType.equals(LINE_STRING)) {
      return readLineString();
    } else if (geometryType.equals(POLYGON)) {
      return readPolygon();
    } else if (geometryType.equals(MULTI_POINT)) {
      return readMultiPoint();
    } else if (geometryType.equals(MULTI_LINE_STRING)) {
      return readMultiLineString();
    } else if (geometryType.equals(MULTI_POLYGON)) {
      return readMultiPolygon();
    } else if (geometryType.equals(GEOMETRY_COLLECTION)) {
      return readGeometryCollection();
    } else {
      return null;
    }
  }

  private Geometry readGeometryCollection() {
    return null;
  }

  private Geometry readMultiPolygon() {
    List<Polygon> polygons = new ArrayList<Polygon>();
    List<List<CoordinatesList>> polygonRings = null;
    GeometryFactory factory = geometryFactory;
    do {
      String attributeName = JsonParserUtil.skipToNextAttribute(in);
      if (COORDINATES.equals(attributeName)) {
        polygonRings = readCoordinatesListListList();
      }
    } while (in.getEvent() != EventType.endObject
      && in.getEvent() != EventType.endDocument);
    if (polygonRings != null) {
      for (List<CoordinatesList> rings : polygonRings) {
        Polygon polygon = factory.createPolygon(rings);
        polygons.add(polygon);
      }
    }
    return factory.createMultiPolygon(polygons);
  }

  private Geometry readMultiLineString() {
    List<CoordinatesList> lineStrings = null;
    GeometryFactory factory = geometryFactory;
    do {
      String attributeName = JsonParserUtil.skipToNextAttribute(in);
      if (COORDINATES.equals(attributeName)) {
        lineStrings = readCoordinatesListList();
      }
    } while (in.getEvent() != EventType.endObject
      && in.getEvent() != EventType.endDocument);
    return factory.createMultiLineString(lineStrings);
  }

  private Geometry readMultiPoint() {
    List<CoordinatesList> points = null;
    GeometryFactory factory = geometryFactory;
    do {
      String attributeName = JsonParserUtil.skipToNextAttribute(in);
      if (COORDINATES.equals(attributeName)) {
        points = readPointCoordinatesListList();
      }
    } while (in.getEvent() != EventType.endObject
      && in.getEvent() != EventType.endDocument);
    return factory.createMultiPoint(points);
  }

  private Polygon readPolygon() {
    List<CoordinatesList> rings = null;
    GeometryFactory factory = geometryFactory;
    do {
      String attributeName = JsonParserUtil.skipToNextAttribute(in);
      if (COORDINATES.equals(attributeName)) {
        rings = readCoordinatesListList();
      }
    } while (in.getEvent() != EventType.endObject
      && in.getEvent() != EventType.endDocument);
    return factory.createPolygon(rings);
  }

  private LineString readLineString() {
    CoordinatesList points = null;
    GeometryFactory factory = geometryFactory;
    do {
      String attributeName = JsonParserUtil.skipToNextAttribute(in);
      if (COORDINATES.equals(attributeName)) {
        points = readCoordinatesList();
      }
    } while (in.getEvent() != EventType.endObject
      && in.getEvent() != EventType.endDocument);
    final LineString lineString = factory.createLineString(points);
    return lineString;
  }

  private Point readPoint() {
    CoordinatesList coordinates = null;
    GeometryFactory factory = geometryFactory;
    do {
      String attributeName = JsonParserUtil.skipToNextAttribute(in);
      if (COORDINATES.equals(attributeName)) {
        coordinates = readPointCoordinatesList();
      }
    } while (in.getEvent() != EventType.endObject
      && in.getEvent() != EventType.endDocument);
    final Point point = factory.createPoint(coordinates);
    return point;
  }

  private CoordinatesList readPointCoordinatesList() {
    double[] values = JsonParserUtil.getDoubleArray(in);
    return new DoubleCoordinatesList(values.length, values);
  }

  private CoordinatesList readCoordinatesList() {
    if (in.getEvent() == EventType.startArray || in.hasNext()
      && in.next() == EventType.startArray) {
      EventType event = in.next();
      final List<Number> values = new ArrayList<Number>();
      int dimension = 0;
      if (event != EventType.endArray) {
        do {
          dimension = Math.max(dimension,
            readCoordinatesListCoordinates(values));
          event = in.next();
        } while (event == EventType.comma);
      }
      if (event != EventType.endArray) {
        throw new IllegalStateException("Exepecting end array, not: " + event);
      }
      return new DoubleCoordinatesList(dimension, values);
    } else {
      throw new IllegalStateException("Exepecting start array, not: "
        + in.getEvent());
    }
  }

  private List<CoordinatesList> readCoordinatesListList() {
    if (in.getEvent() == EventType.startArray || in.hasNext()
      && in.next() == EventType.startArray) {
      EventType event = in.next();
      final List<CoordinatesList> coordinatesLists = new ArrayList<CoordinatesList>();
      if (event != EventType.endArray) {
        do {
          coordinatesLists.add(readCoordinatesList());
          event = in.next();
        } while (event == EventType.comma);
      }
      if (event != EventType.endArray) {
        throw new IllegalStateException("Exepecting end array, not: " + event);
      }
      return coordinatesLists;
    } else {
      throw new IllegalStateException("Exepecting start array, not: "
        + in.getEvent());
    }
  }

  private List<List<CoordinatesList>> readCoordinatesListListList() {
    if (in.getEvent() == EventType.startArray || in.hasNext()
      && in.next() == EventType.startArray) {
      EventType event = in.next();
      final List<List<CoordinatesList>> coordinatesLists = new ArrayList<List<CoordinatesList>>();
      if (event != EventType.endArray) {
        do {
          coordinatesLists.add(readCoordinatesListList());
          event = in.next();
        } while (event == EventType.comma);
      }
      if (event != EventType.endArray) {
        throw new IllegalStateException("Exepecting end array, not: " + event);
      }
      return coordinatesLists;
    } else {
      throw new IllegalStateException("Exepecting start array, not: "
        + in.getEvent());
    }
  }

  private List<CoordinatesList> readPointCoordinatesListList() {
    if (in.getEvent() == EventType.startArray || in.hasNext()
      && in.next() == EventType.startArray) {
      EventType event = in.next();
      final List<CoordinatesList> coordinatesLists = new ArrayList<CoordinatesList>();
      if (event != EventType.endArray) {
        do {
          coordinatesLists.add(readPointCoordinatesList());
          event = in.next();
        } while (event == EventType.comma);
      }
      if (event != EventType.endArray) {
        throw new IllegalStateException("Exepecting end array, not: " + event);
      }
      return coordinatesLists;
    } else {
      throw new IllegalStateException("Exepecting start array, not: "
        + in.getEvent());
    }
  }

  /**
   * Read one points coordinates and add them to the list of coordinate values.
   * 
   * @param values The list to add the points coordinates to.
   * @return The dimension of the coordinate read.
   */
  private int readCoordinatesListCoordinates(
    final List<Number> values) {
    int dimension = 0;
    if (in.getEvent() == EventType.startArray || in.hasNext()
      && in.next() == EventType.startArray) {
      EventType event = in.getEvent();
      do {
        final Object value = JsonParserUtil.getValue(in);
        if (value instanceof EventType) {
          event = (EventType)value;
        } else if (value instanceof Number) {
          values.add((Number)value);
          dimension++;
          event = in.next();
        } else {
          throw new IllegalArgumentException("Expecting number, not: " + value);
        }
      } while (event == EventType.comma);
      if (event != EventType.endArray) {
        throw new IllegalStateException("Exepecting end array, not: " + event);
      }

      return dimension;
    } else {
      throw new IllegalStateException("Exepecting start array, not: "
        + in.getEvent());
    }
  }

  public void remove() {
    throw new UnsupportedOperationException();
  }
}
