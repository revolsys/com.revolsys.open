package com.revolsys.io.geojson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.springframework.core.io.Resource;

import com.revolsys.collection.AbstractIterator;
import com.revolsys.gis.cs.GeometryFactory;
import com.revolsys.gis.cs.epsg.EpsgCoordinateSystems;
import com.revolsys.gis.model.coordinates.list.CoordinatesList;
import com.revolsys.gis.model.coordinates.list.DoubleCoordinatesList;
import com.revolsys.io.IoConstants;
import com.revolsys.io.json.JsonParser;
import com.revolsys.io.json.JsonParserUtil;
import com.revolsys.io.json.JsonParser.EventType;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

public class GeoJsonGeometryIterator extends AbstractIterator<Geometry>
  implements GeoJsonConstants {

  /** The current geometry. */
  private Geometry currentGeometry;

  private GeometryFactory geometryFactory;

  /** Flag indicating if there are more records to be read. */
  private boolean hasNext = false;

  private final JsonParser in;

  public GeoJsonGeometryIterator(final Resource resource) throws IOException {
    this.in = new JsonParser(resource);
  }

  @Override
  protected void doClose() {
    in.close();
  }

  @Override
  protected void doInit() {
    geometryFactory = getProperty(IoConstants.GEOMETRY_FACTORY);
    if (geometryFactory == null) {
      geometryFactory = GeometryFactory.getFactory(4326);
    }
    if (in.hasNext()) {
      in.next();
    }
  }

  @Override
  protected void finalize() throws Throwable {
    close();
  }

  @Override
  protected Geometry getNext() throws NoSuchElementException {
    String type = null;
    do {
      do {
        JsonParserUtil.skipToAttribute(in, TYPE);
        if (in.getEvent() == EventType.endDocument) {
          throw new NoSuchElementException();
        }
      } while (in.getEvent() != EventType.colon);
      type = JsonParserUtil.getString(in);
      if (CRS.equals(type)) {
        geometryFactory = readCoordinateSystem();
      }
    } while (!GEOMETRY_TYPE_NAMES.contains(type));
    return readGeometry();
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

  /**
   * Read one points coordinates and add them to the list of coordinate values.
   * 
   * @param values The list to add the points coordinates to.
   * @return The dimension of the coordinate read.
   */
  private int readCoordinatesListCoordinates(final List<Number> values) {
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

  private Geometry readGeometry() {
    final String geometryType = in.getValue();
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

  private LineString readLineString() {
    CoordinatesList points = null;
    GeometryFactory factory = geometryFactory;
    do {
      final String attributeName = JsonParserUtil.skipToNextAttribute(in);
      if (COORDINATES.equals(attributeName)) {
        points = readCoordinatesList();
      } else if (CRS.equals(attributeName)) {
        factory = readCoordinateSystem();
      }
    } while (in.getEvent() != EventType.endObject
      && in.getEvent() != EventType.endDocument);
    final LineString lineString = factory.createLineString(points);
    return lineString;
  }

  private GeometryFactory readCoordinateSystem() {
    GeometryFactory factory = geometryFactory;
    do {
      final String attributeName = JsonParserUtil.skipToNextAttribute(in);
      if (PROPERTIES.equals(attributeName)) {
        final Map<String, Object> properties = JsonParserUtil.getMap(in);
        String name = (String)properties.get("name");
        if (name != null) {
          if (name.startsWith(URN_OGC_DEF_CRS_EPSG)) {
            int srid = Integer.parseInt(name.substring(URN_OGC_DEF_CRS_EPSG.length()));
            factory = GeometryFactory.getFactory(srid);
          } else if (name.startsWith(EPSG)) {
            int srid = Integer.parseInt(name.substring(EPSG.length()));
            factory = GeometryFactory.getFactory(srid);
          }
        }
      }
    } while (in.getEvent() != EventType.endObject
      && in.getEvent() != EventType.endDocument);
    return factory;
  }

  private Geometry readMultiLineString() {
    List<CoordinatesList> lineStrings = null;
    GeometryFactory factory = geometryFactory;
    do {
      final String attributeName = JsonParserUtil.skipToNextAttribute(in);
      if (COORDINATES.equals(attributeName)) {
        lineStrings = readCoordinatesListList();
      } else if (CRS.equals(attributeName)) {
        factory = readCoordinateSystem();
      }
    } while (in.getEvent() != EventType.endObject
      && in.getEvent() != EventType.endDocument);
    return factory.createMultiLineString(lineStrings);
  }

  private Geometry readMultiPoint() {
    List<CoordinatesList> points = null;
    GeometryFactory factory = geometryFactory;
    do {
      final String attributeName = JsonParserUtil.skipToNextAttribute(in);
      if (COORDINATES.equals(attributeName)) {
        points = readPointCoordinatesListList();
      } else if (CRS.equals(attributeName)) {
        factory = readCoordinateSystem();
      }
    } while (in.getEvent() != EventType.endObject
      && in.getEvent() != EventType.endDocument);
    return factory.createMultiPoint(points);
  }

  private Geometry readMultiPolygon() {
    final List<Polygon> polygons = new ArrayList<Polygon>();
    List<List<CoordinatesList>> polygonRings = null;
    GeometryFactory factory = geometryFactory;
    do {
      final String attributeName = JsonParserUtil.skipToNextAttribute(in);
      if (COORDINATES.equals(attributeName)) {
        polygonRings = readCoordinatesListListList();
      } else if (CRS.equals(attributeName)) {
        factory = readCoordinateSystem();
      }
    } while (in.getEvent() != EventType.endObject
      && in.getEvent() != EventType.endDocument);
    if (polygonRings != null) {
      for (final List<CoordinatesList> rings : polygonRings) {
        final Polygon polygon = factory.createPolygon(rings);
        polygons.add(polygon);
      }
    }
    return factory.createMultiPolygon(polygons);
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

  private Point readPoint() {
    CoordinatesList coordinates = null;
    GeometryFactory factory = geometryFactory;
    do {
      final String attributeName = JsonParserUtil.skipToNextAttribute(in);
      if (COORDINATES.equals(attributeName)) {
        coordinates = readPointCoordinatesList();
      } else if (CRS.equals(attributeName)) {
        factory = readCoordinateSystem();
      }
    } while (in.getEvent() != EventType.endObject
      && in.getEvent() != EventType.endDocument);
    final Point point = factory.createPoint(coordinates);
    return point;
  }

  private CoordinatesList readPointCoordinatesList() {
    final double[] values = JsonParserUtil.getDoubleArray(in);
    return new DoubleCoordinatesList(values.length, values);
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

  private Polygon readPolygon() {
    List<CoordinatesList> rings = null;
    final GeometryFactory factory = geometryFactory;
    do {
      final String attributeName = JsonParserUtil.skipToNextAttribute(in);
      if (COORDINATES.equals(attributeName)) {
        rings = readCoordinatesListList();
      }
    } while (in.getEvent() != EventType.endObject
      && in.getEvent() != EventType.endDocument);
    return factory.createPolygon(rings);
  }
}
