package com.revolsys.io.geojson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.springframework.core.io.Resource;

import com.revolsys.collection.AbstractIterator;
import com.revolsys.gis.model.coordinates.list.DoubleCoordinatesList;
import com.revolsys.io.FileUtil;
import com.revolsys.io.IoConstants;
import com.revolsys.io.json.JsonParser;
import com.revolsys.io.json.JsonParser.EventType;
import com.revolsys.jts.geom.PointList;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.jts.geom.LineString;
import com.revolsys.jts.geom.Point;
import com.revolsys.jts.geom.Polygon;

public class GeoJsonGeometryIterator extends AbstractIterator<Geometry>
  implements GeoJsonConstants {

  private com.revolsys.jts.geom.GeometryFactory geometryFactory;

  private JsonParser in;

  public GeoJsonGeometryIterator(final Resource resource) throws IOException {
    this.in = new JsonParser(resource);
  }

  @Override
  protected void doClose() {
    FileUtil.closeSilent(in);
    geometryFactory = null;
    in = null;
  }

  @Override
  protected void doInit() {
    geometryFactory = getProperty(IoConstants.GEOMETRY_FACTORY);
    if (geometryFactory == null) {
      geometryFactory = GeometryFactory.floating3(4326);
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
    do {
      final String attributeName = JsonParser.skipToAttribute(in);
      if (TYPE.equals(attributeName)) {
        in.next();
        final String geometryType = in.getValue();
        if (GEOMETRY_TYPE_NAMES.contains(geometryType)) {
          return readGeometry();
        }
      } else if (CRS.equals(attributeName)) {
        in.next();
        geometryFactory = readCoordinateSystem();

      }
    } while (in.getEvent() != EventType.endDocument);
    throw new NoSuchElementException();
  }

  private PointList readCoordinatesList(final boolean cogo,
    final boolean ring) {
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
    return new DoubleCoordinatesList(axisCount, coordinates);
  }

  private int readCoordinatesList(final List<Double> coordinates) {
    int axisCount = 0;
    if (in.getEvent() == EventType.startArray || in.hasNext()
      && in.next() == EventType.startArray) {
      EventType event = in.next();
      if (event != EventType.endArray) {
        do {
          axisCount = Math.max(axisCount,
            readCoordinatesListCoordinates(coordinates));
          event = in.next();
        } while (event == EventType.comma);
      }
      if (event != EventType.endArray) {
        throw new IllegalStateException("Exepecting end array, not: " + event);
      }
    } else {
      throw new IllegalStateException("Exepecting start array, not: "
        + in.getEvent());
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
    if (in.getEvent() == EventType.startArray || in.hasNext()
      && in.next() == EventType.startArray) {
      EventType event = in.getEvent();
      do {
        final Object value = JsonParser.getValue(in);
        if (value instanceof EventType) {
          event = (EventType)value;
        } else if (value instanceof Number) {
          values.add(((Number)value).doubleValue());
          numAxis++;
          event = in.next();
        } else {
          throw new IllegalArgumentException("Expecting number, not: " + value);
        }
      } while (event == EventType.comma);
      if (event != EventType.endArray) {
        throw new IllegalStateException("Exepecting end array, not: " + event);
      }

      return numAxis;
    } else {
      throw new IllegalStateException("Exepecting start array, not: "
        + in.getEvent());
    }
  }

  private List<PointList> readCoordinatesListList(final boolean cogo,
    final boolean ring) {
    if (in.getEvent() == EventType.startArray || in.hasNext()
      && in.next() == EventType.startArray) {
      EventType event = in.next();
      final List<PointList> coordinatesLists = new ArrayList<PointList>();
      if (event != EventType.endArray) {
        do {
          coordinatesLists.add(readCoordinatesList(cogo, ring));
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

  private List<List<PointList>> readCoordinatesListListList(
    final boolean cogo) {
    if (in.getEvent() == EventType.startArray || in.hasNext()
      && in.next() == EventType.startArray) {
      EventType event = in.next();
      final List<List<PointList>> coordinatesLists = new ArrayList<List<PointList>>();
      if (event != EventType.endArray) {
        do {
          coordinatesLists.add(readCoordinatesListList(cogo, true));
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

  private com.revolsys.jts.geom.GeometryFactory readCoordinateSystem() {
    GeometryFactory factory = geometryFactory;
    do {
      final String attributeName = JsonParser.skipToNextAttribute(in);
      if (PROPERTIES.equals(attributeName)) {
        final Map<String, Object> properties = JsonParser.getMap(in);
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
    } while (in.getEvent() != EventType.endObject
      && in.getEvent() != EventType.endDocument);
    return factory;
  }

  private Geometry readGeometry() {
    final String geometryType = in.getValue();
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
    GeometryFactory factory = geometryFactory;
    do {
      final String attributeName = JsonParser.skipToNextAttribute(in);
      if (GEOMETRIES.equals(attributeName)) {
        geometries = readGeometryList();
      } else if (CRS.equals(attributeName)) {
        factory = readCoordinateSystem();
      }
    } while (in.getEvent() != EventType.endObject
      && in.getEvent() != EventType.endDocument);

    return factory.geometry(geometries);
  }

  private List<Geometry> readGeometryList() {
    if (in.getEvent() == EventType.startArray || in.hasNext()
      && in.next() == EventType.startArray) {
      EventType event = in.next();
      final List<Geometry> geometries = new ArrayList<Geometry>();
      if (event != EventType.endArray) {
        do {
          final Geometry geometry = getNext();
          geometries.add(geometry);
          event = in.next();
        } while (event == EventType.comma);
      }
      if (event != EventType.endArray) {
        throw new IllegalStateException("Exepecting end array, not: " + event);
      }
      return geometries;
    } else {
      throw new IllegalStateException("Exepecting start array, not: "
        + in.getEvent());
    }
  }

  private LineString readLineString(final boolean cogo) {
    PointList points = null;
    GeometryFactory factory = geometryFactory;
    do {
      final String attributeName = JsonParser.skipToNextAttribute(in);
      if (COORDINATES.equals(attributeName)) {
        points = readCoordinatesList(cogo, false);
      } else if (CRS.equals(attributeName)) {
        factory = readCoordinateSystem();
      }
    } while (in.getEvent() != EventType.endObject
      && in.getEvent() != EventType.endDocument);

    if (points == null) {
      return factory.lineString();
    } else {
      final int axisCount = points.getAxisCount();
      final GeometryFactory geometryFactory = factory.convertAxisCount(axisCount);
      return geometryFactory.lineString(points);
    }
  }

  private Geometry readMultiLineString(final boolean cogo) {
    List<PointList> lineStrings = null;
    GeometryFactory factory = geometryFactory;
    do {
      final String attributeName = JsonParser.skipToNextAttribute(in);
      if (COORDINATES.equals(attributeName)) {
        lineStrings = readCoordinatesListList(cogo, false);
      } else if (CRS.equals(attributeName)) {
        factory = readCoordinateSystem();
      }
    } while (in.getEvent() != EventType.endObject
      && in.getEvent() != EventType.endDocument);
    int axisCount = 2;
    for (final PointList points : lineStrings) {
      axisCount = Math.max(axisCount, points.getAxisCount());
    }
    factory = factory.convertAxisCount(axisCount);
    return factory.multiLineString(lineStrings);
  }

  private Geometry readMultiPoint() {
    List<PointList> pointsList = null;
    GeometryFactory factory = geometryFactory;
    do {
      final String attributeName = JsonParser.skipToNextAttribute(in);
      if (COORDINATES.equals(attributeName)) {
        pointsList = readPointCoordinatesListList();
      } else if (CRS.equals(attributeName)) {
        factory = readCoordinateSystem();
      }
    } while (in.getEvent() != EventType.endObject
      && in.getEvent() != EventType.endDocument);
    int axisCount = 2;
    for (final PointList points : pointsList) {
      axisCount = Math.max(axisCount, points.getAxisCount());
    }
    factory = factory.convertAxisCount(axisCount);
    return factory.multiPoint(pointsList);
  }

  private Geometry readMultiPolygon(final boolean cogo) {
    final List<Polygon> polygons = new ArrayList<Polygon>();
    List<List<PointList>> polygonRings = null;
    GeometryFactory factory = geometryFactory;
    do {
      final String attributeName = JsonParser.skipToNextAttribute(in);
      if (COORDINATES.equals(attributeName)) {
        polygonRings = readCoordinatesListListList(cogo);
      } else if (CRS.equals(attributeName)) {
        factory = readCoordinateSystem();
      }
    } while (in.getEvent() != EventType.endObject
      && in.getEvent() != EventType.endDocument);
    int axisCount = 2;
    if (polygonRings != null) {
      for (final List<PointList> rings : polygonRings) {
        for (final PointList points : rings) {
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
    PointList coordinates = null;
    GeometryFactory factory = geometryFactory;
    do {
      final String attributeName = JsonParser.skipToNextAttribute(in);
      if (COORDINATES.equals(attributeName)) {
        coordinates = readPointCoordinatesList();
      } else if (CRS.equals(attributeName)) {
        factory = readCoordinateSystem();
      }
    } while (in.getEvent() != EventType.endObject
      && in.getEvent() != EventType.endDocument);
    if (coordinates == null) {
      return factory.point();
    } else {
      final int axisCount = coordinates.getAxisCount();
      final GeometryFactory geometryFactory = factory.convertAxisCount(axisCount);
      return geometryFactory.point(coordinates);
    }
  }

  private PointList readPointCoordinatesList() {
    final double[] values = JsonParser.getDoubleArray(in);
    if (values == null) {
      return null;
    } else {
      return new DoubleCoordinatesList(values.length, values);
    }
  }

  private List<PointList> readPointCoordinatesListList() {
    if (in.getEvent() == EventType.startArray || in.hasNext()
      && in.next() == EventType.startArray) {
      EventType event = in.next();
      final List<PointList> coordinatesLists = new ArrayList<PointList>();
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

  private Polygon readPolygon(final boolean cogo) {
    List<PointList> rings = null;
    GeometryFactory factory = geometryFactory;
    do {
      final String attributeName = JsonParser.skipToNextAttribute(in);
      if (COORDINATES.equals(attributeName)) {
        rings = readCoordinatesListList(cogo, true);
      } else if (CRS.equals(attributeName)) {
        factory = readCoordinateSystem();
      }
    } while (in.getEvent() != EventType.endObject
      && in.getEvent() != EventType.endDocument);
    int axisCount = 2;
    for (final PointList points : rings) {
      axisCount = Math.max(axisCount, points.getAxisCount());
    }
    factory = factory.convertAxisCount(axisCount);
    return factory.polygon(rings);
  }
}
