package com.revolsys.gis.postgresql;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.postgis.Geometry;
import org.postgis.LineString;
import org.postgis.LinearRing;
import org.postgis.MultiLineString;
import org.postgis.MultiPoint;
import org.postgis.MultiPolygon;
import org.postgis.PGgeometry;
import org.postgis.Point;
import org.postgis.Polygon;
import org.postgresql.geometric.PGbox;

import com.revolsys.gis.data.model.AttributeProperties;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.types.DataType;
import com.revolsys.gis.data.model.types.DataTypes;
import com.revolsys.jdbc.attribute.JdbcAttribute;
import com.revolsys.jts.geom.BoundingBox;
import com.revolsys.jts.geom.GeometryFactory;

public class PostgreSQLGeometryJdbcAttribute extends JdbcAttribute {
  private final GeometryFactory geometryFactory;

  private final int srid;

  private final int axisCount;

  public PostgreSQLGeometryJdbcAttribute(final String dbName,
    final String name, final DataType type, final boolean required,
    final String description, final Map<String, Object> properties,
    final int srid, final int axisCount, final GeometryFactory geometryFactory) {
    super(dbName, name, type, -1, 0, 0, required, description, properties);
    this.srid = srid;
    this.geometryFactory = geometryFactory;
    setProperty(AttributeProperties.GEOMETRY_FACTORY, geometryFactory);
    this.axisCount = axisCount;
  }

  @Override
  public JdbcAttribute clone() {
    return new PostgreSQLGeometryJdbcAttribute(getDbName(), getName(),
      getType(), isRequired(), getDescription(), getProperties(), srid,
      axisCount, geometryFactory);
  }

  public Object getInsertUpdateValue(Object object) throws SQLException {
    if (object == null) {
      return null;
    } else {
      if (object instanceof com.revolsys.jts.geom.Geometry) {
        final com.revolsys.jts.geom.Geometry geometry = (com.revolsys.jts.geom.Geometry)object;
        object = geometry.convert(geometryFactory);
      }
      Geometry geometry = null;

      final DataType type = getType();
      if (type == DataTypes.POINT) {
        geometry = toPgPoint(object);
      } else if (type == DataTypes.LINE_STRING) {
        geometry = toPgLineString(object);
      } else if (type == DataTypes.POLYGON) {
        geometry = toPgPolygon(object);
      } else if (type == DataTypes.MULTI_POINT) {
        geometry = toPgMultiPoint((com.revolsys.jts.geom.Geometry)object);
      } else if (type == DataTypes.MULTI_LINE_STRING) {
        geometry = toPgMultiLineString((com.revolsys.jts.geom.Geometry)object);
      } else if (type == DataTypes.MULTI_POLYGON) {
        geometry = toPgMultiPolygon((com.revolsys.jts.geom.Geometry)object);
      } else if (object instanceof com.revolsys.jts.geom.Point) {
        final com.revolsys.jts.geom.Point point = (com.revolsys.jts.geom.Point)object;
        geometry = toPgPoint(point);
      } else if (object instanceof Point) {
        final Point coordinates = (Point)object;
        final com.revolsys.jts.geom.Point point = geometryFactory.point(coordinates);
        geometry = toPgPoint(point);
      } else if (object instanceof com.revolsys.jts.geom.MultiPoint) {
        final com.revolsys.jts.geom.MultiPoint point = (com.revolsys.jts.geom.MultiPoint)object;
        geometry = toPgMultiPoint(point);
      } else if (object instanceof com.revolsys.jts.geom.LineString) {
        final com.revolsys.jts.geom.LineString lineString = (com.revolsys.jts.geom.LineString)object;
        geometry = toPgLineString(lineString);
      } else if (object instanceof com.revolsys.jts.geom.MultiLineString) {
        final com.revolsys.jts.geom.MultiLineString lineString = (com.revolsys.jts.geom.MultiLineString)object;
        geometry = toPgMultiLineString(lineString);
      } else if (object instanceof com.revolsys.jts.geom.Polygon) {
        final com.revolsys.jts.geom.Polygon polygon = (com.revolsys.jts.geom.Polygon)object;
        geometry = toPgPolygon(polygon);
      } else if (object instanceof com.revolsys.jts.geom.MultiPolygon) {
        final com.revolsys.jts.geom.MultiPolygon polygon = (com.revolsys.jts.geom.MultiPolygon)object;
        geometry = toPgMultiPolygon(polygon);
      } else {
        return object;
      }
      return new PGgeometry(geometry);
    }
  }

  @Override
  public int setAttributeValueFromResultSet(final ResultSet resultSet,
    final int columnIndex, final DataObject object) throws SQLException {
    final Object oracleValue = resultSet.getObject(columnIndex);
    final Object value = toJava(oracleValue);
    object.setValue(getIndex(), value);
    return columnIndex + 1;
  }

  @Override
  public int setInsertPreparedStatementValue(final PreparedStatement statement,
    final int parameterIndex, final DataObject object) throws SQLException {
    final String name = getName();
    final Object value = object.getValue(name);
    final Object jdbcValue = getInsertUpdateValue(value);
    statement.setObject(parameterIndex, jdbcValue);
    return parameterIndex + 1;
  }

  @Override
  public int setPreparedStatementValue(final PreparedStatement statement,
    final int parameterIndex, final Object value) throws SQLException {
    final Object jdbcValue = toJdbc(value);
    statement.setObject(parameterIndex, jdbcValue);
    return parameterIndex + 1;
  }

  public Object toJava(final Object object) throws SQLException {
    if (object instanceof PGgeometry) {
      final PGgeometry pgGeometry = (PGgeometry)object;
      final Geometry geometry = pgGeometry.getGeometry();
      final int type = geometry.getType();
      if (type == Geometry.POINT) {
        return toJtsPoint(geometryFactory, (Point)geometry);
      } else if (type == Geometry.LINESTRING) {
        return toJtsLineString(geometryFactory, (LineString)geometry);
      } else if (type == Geometry.POLYGON) {
        return toJtsPolygon(geometryFactory, (Polygon)geometry);
      } else if (type == Geometry.MULTIPOINT) {
        return toJtsMultiPoint(geometryFactory, (MultiPoint)geometry);
      } else if (type == Geometry.MULTILINESTRING) {
        return toJtsMultiLineString(geometryFactory, (MultiLineString)geometry);
      } else if (type == Geometry.MULTIPOLYGON) {
        return toJtsMultiPolygon(geometryFactory, (MultiPolygon)geometry);
      } else {
        throw new RuntimeException("Unsopported postgis geometry type " + type);
      }
    } else {
      return null;
    }
  }

  public Object toJdbc(final Object object) throws SQLException {
    Geometry geometry = null;
    if (object instanceof com.revolsys.jts.geom.Geometry) {
      final com.revolsys.jts.geom.Geometry rsGeometry = ((com.revolsys.jts.geom.Geometry)object).convert(geometryFactory);
      if (rsGeometry instanceof com.revolsys.jts.geom.Point) {
        final com.revolsys.jts.geom.Point point = (com.revolsys.jts.geom.Point)rsGeometry;
        geometry = toPgPoint(point);
      } else if (rsGeometry instanceof com.revolsys.jts.geom.LineString) {
        final com.revolsys.jts.geom.LineString lineString = (com.revolsys.jts.geom.LineString)rsGeometry;
        geometry = toPgLineString(lineString);
      } else if (rsGeometry instanceof com.revolsys.jts.geom.Polygon) {
        final com.revolsys.jts.geom.Polygon polygon = (com.revolsys.jts.geom.Polygon)rsGeometry;
        geometry = toPgPolygon(polygon);
      } else if (rsGeometry instanceof com.revolsys.jts.geom.MultiPoint) {
        final com.revolsys.jts.geom.MultiPoint multiPoint = (com.revolsys.jts.geom.MultiPoint)rsGeometry;
        geometry = toPgMultiPoint(multiPoint);
      } else if (rsGeometry instanceof com.revolsys.jts.geom.MultiLineString) {
        final com.revolsys.jts.geom.MultiLineString lineString = (com.revolsys.jts.geom.MultiLineString)rsGeometry;
        geometry = toPgMultiLineString(lineString);
      } else if (rsGeometry instanceof com.revolsys.jts.geom.MultiPolygon) {
        final com.revolsys.jts.geom.MultiPolygon multiPolygon = (com.revolsys.jts.geom.MultiPolygon)rsGeometry;
        geometry = toPgMultiPolygon(multiPolygon);
      }
    } else if (object instanceof BoundingBox) {
      BoundingBox boundingBox = (BoundingBox)object;
      boundingBox = boundingBox.convert(geometryFactory, 2);
      final double minX = boundingBox.getMinX();
      final double minY = boundingBox.getMinY();
      final double maxX = boundingBox.getMaxX();
      final double maxY = boundingBox.getMaxY();
      return new PGbox(minX, minY, maxX, maxY);
    } else {
      return object;
    }
    return new PGgeometry(geometry);
  }

  private com.revolsys.jts.geom.LineString toJtsLineString(
    final GeometryFactory factory, final LineString lineString) {
    final Point[] points = lineString.getPoints();
    final double[] coordinates = new double[points.length * axisCount];
    for (int i = 0; i < points.length; i++) {
      final Point point = points[i];
      coordinates[i * axisCount] = point.x;
      coordinates[i * axisCount + 1] = point.y;
      if (axisCount > 2) {
        coordinates[i * axisCount + 2] = point.z;
        if (axisCount > 3) {
          coordinates[i * axisCount + 3] = point.m;
        }
      }
    }
    return factory.lineString(axisCount, coordinates);
  }

  private com.revolsys.jts.geom.Geometry toJtsMultiLineString(
    final GeometryFactory factory, final MultiLineString multiLine) {
    final LineString[] lines = multiLine.getLines();
    if (lines.length == 1) {
      return toJtsLineString(factory, lines[0]);
    } else {
      final com.revolsys.jts.geom.LineString[] lineStrings = new com.revolsys.jts.geom.LineString[lines.length];
      for (int i = 0; i < lines.length; i++) {
        final LineString line = lines[i];
        lineStrings[i] = toJtsLineString(factory, line);
      }
      return factory.multiLineString(lineStrings);
    }
  }

  private com.revolsys.jts.geom.Geometry toJtsMultiPoint(
    final GeometryFactory factory, final MultiPoint multiPoint) {
    final List<com.revolsys.jts.geom.Point> points = new ArrayList<com.revolsys.jts.geom.Point>();
    for (final Point point : multiPoint.getPoints()) {
      final com.revolsys.jts.geom.Point jtsPoint = toJtsPoint(factory, point);
      points.add(jtsPoint);
    }
    if (points.size() == 1) {
      return points.get(0);
    } else {
      return factory.multiPoint(points);
    }
  }

  private com.revolsys.jts.geom.Geometry toJtsMultiPolygon(
    final GeometryFactory factory, final MultiPolygon multiPolygon) {
    final List<com.revolsys.jts.geom.Polygon> polygons = new ArrayList<com.revolsys.jts.geom.Polygon>();
    for (final Polygon polygon : multiPolygon.getPolygons()) {
      final com.revolsys.jts.geom.Polygon jtsPolygon = toJtsPolygon(factory,
        polygon);
      polygons.add(jtsPolygon);
    }
    if (polygons.size() == 1) {
      return polygons.get(0);
    } else {
      return factory.multiPolygon(polygons);
    }
  }

  private com.revolsys.jts.geom.Point toJtsPoint(final GeometryFactory factory,
    final Point point) {
    switch (axisCount) {
      case 3:
        return factory.point(point.x, point.y, point.z);
      case 4:
        return factory.point(point.x, point.y, point.z, point.m);
      default:
        return factory.point(point.x, point.y);
    }
  }

  private com.revolsys.jts.geom.Polygon toJtsPolygon(
    final GeometryFactory factory, final Polygon polygon) {
    final List<com.revolsys.jts.geom.LinearRing> rings = new ArrayList<>();
    for (int ringIndex = 0; ringIndex < polygon.numRings(); ringIndex++) {
      final LinearRing ring = polygon.getRing(ringIndex);
      final Point[] points = ring.getPoints();
      int vertexCount = points.length;
      if (!points[0].equals(points[vertexCount - 1])) {
        vertexCount++;
      }
      final double[] coordinates = new double[vertexCount * axisCount];

      for (int i = 0; i < vertexCount; i++) {
        final Point point = points[i % points.length];
        coordinates[i * axisCount + 0] = point.x;
        coordinates[i * axisCount + 1] = point.y;
        if (axisCount > 2) {
          coordinates[i * axisCount + 2] = point.z;
          if (axisCount > 3) {
            coordinates[i * axisCount + 3] = point.m;
          }
        }
      }
      rings.add(factory.linearRing(axisCount, coordinates));
    }
    return factory.polygon(rings);
  }

  private LinearRing toPgLinearRing(final com.revolsys.jts.geom.LineString ring) {
    final Point[] pgPoints = toPgPoints(ring);
    final LinearRing linearRing = new LinearRing(pgPoints);
    linearRing.setSrid(ring.getSrid());
    return linearRing;
  }

  private LineString toPgLineString(
    final com.revolsys.jts.geom.LineString lineString) {
    final Point[] pgPoints = toPgPoints(lineString);
    final LineString pgLineString = new LineString(pgPoints);
    final int srid = lineString.getSrid();
    pgLineString.setSrid(srid);
    return pgLineString;
  }

  private Geometry toPgLineString(final Object object) {
    if (object instanceof com.revolsys.jts.geom.LineString) {
      final com.revolsys.jts.geom.LineString lineString = (com.revolsys.jts.geom.LineString)object;
      return toPgLineString(lineString);
    } else if (object instanceof com.revolsys.jts.geom.GeometryCollection) {
      final com.revolsys.jts.geom.GeometryCollection geometryCollection = (com.revolsys.jts.geom.GeometryCollection)object;
      if (geometryCollection.getGeometryCount() == 1) {
        final com.revolsys.jts.geom.Geometry firstGeometry = geometryCollection.getGeometry(0);
        if (firstGeometry instanceof com.revolsys.jts.geom.LineString) {
          final com.revolsys.jts.geom.LineString line = (com.revolsys.jts.geom.LineString)firstGeometry;
          return toPgLineString(line);
        } else {
          throw new RuntimeException(
            "GeometryCollection must contain a single LineString not a "
              + firstGeometry.getClass());
        }
      } else {
        throw new RuntimeException("MultiLineString has more than one geometry");
      }
    } else {
      throw new RuntimeException("Expecting a linestring");
    }
  }

  private Geometry toPgMultiLineString(
    final com.revolsys.jts.geom.Geometry geometry) {
    final List<LineString> pgLineStrings = new ArrayList<LineString>();
    for (int i = 0; i < geometry.getGeometryCount(); i++) {
      final com.revolsys.jts.geom.Geometry subGeometry = geometry.getGeometry(i);
      if (subGeometry instanceof com.revolsys.jts.geom.LineString) {
        final com.revolsys.jts.geom.LineString line = (com.revolsys.jts.geom.LineString)subGeometry;
        final LineString pgLineString = toPgLineString(line);
        pgLineStrings.add(pgLineString);
      } else {
        throw new RuntimeException(
          "Geometry must contain only LineStrings not a "
            + subGeometry.getClass());
      }
    }
    return toPgMultiLineString(geometry.getSrid(), pgLineStrings);
  }

  private MultiLineString toPgMultiLineString(final int srid,
    final List<LineString> lineStrings) {
    final LineString[] pgLineStrings = new LineString[lineStrings.size()];
    lineStrings.toArray(pgLineStrings);
    final MultiLineString pgMultiLineString = new MultiLineString(pgLineStrings);
    pgMultiLineString.setSrid(srid);
    return pgMultiLineString;
  }

  private Geometry toPgMultiPoint(final com.revolsys.jts.geom.Geometry geometry) {
    final List<Point> pgPoints = new ArrayList<Point>();
    for (int i = 0; i < geometry.getGeometryCount(); i++) {
      final com.revolsys.jts.geom.Geometry subGeometry = geometry.getGeometry(i);
      if (subGeometry instanceof com.revolsys.jts.geom.Point) {
        final com.revolsys.jts.geom.Point point = (com.revolsys.jts.geom.Point)subGeometry;
        final Point pgPoint = toPgPoint(point);
        pgPoints.add(pgPoint);
      } else {
        throw new RuntimeException("Geometry must contain only Point not a "
          + subGeometry.getClass());
      }
    }
    return toPgMultiPoint(geometry.getSrid(), pgPoints);
  }

  private MultiPoint toPgMultiPoint(final int srid, final List<Point> points) {
    final Point[] pgPoints = new Point[points.size()];
    points.toArray(pgPoints);
    final MultiPoint pgMultiPoint = new MultiPoint(pgPoints);
    pgMultiPoint.setSrid(srid);
    return pgMultiPoint;
  }

  private Geometry toPgMultiPolygon(
    final com.revolsys.jts.geom.Geometry geometry) {
    final List<Polygon> pgPolygons = new ArrayList<Polygon>();
    for (int i = 0; i < geometry.getGeometryCount(); i++) {
      final com.revolsys.jts.geom.Geometry subGeometry = geometry.getGeometry(i);
      if (subGeometry instanceof com.revolsys.jts.geom.Polygon) {
        final com.revolsys.jts.geom.Polygon line = (com.revolsys.jts.geom.Polygon)subGeometry;
        final Polygon pgPolygon = toPgPolygon(line);
        pgPolygons.add(pgPolygon);
      } else {
        throw new RuntimeException("Geometry must contain only Polygons not a "
          + subGeometry.getClass());
      }
    }
    return toPgMultiPolygon(geometry.getSrid(), pgPolygons);
  }

  private MultiPolygon toPgMultiPolygon(final int srid,
    final List<Polygon> polygons) {
    final Polygon[] pgPolygons = new Polygon[polygons.size()];
    polygons.toArray(pgPolygons);
    final MultiPolygon pgMultiPolygon = new MultiPolygon(pgPolygons);
    pgMultiPolygon.setSrid(srid);
    return pgMultiPolygon;
  }

  private Point toPgPoint(final com.revolsys.jts.geom.Point point) {
    final int axisCount = point.getAxisCount();

    Point pgPoint;
    final double x = point.getX();
    final double y = point.getY();
    if (axisCount > 2) {
      double z = point.getZ();
      if (Double.isNaN(z)) {
        z = 0;
      }
      pgPoint = new Point(x, y, z);
      if (axisCount > 3) {
        double m = point.getM();
        if (Double.isNaN(m)) {
          m = 0;
        }
        pgPoint.m = m;
      }
    } else {
      pgPoint = new Point(x, y);
    }
    pgPoint.setSrid(point.getSrid());
    return pgPoint;
  }

  private Geometry toPgPoint(final Object object) {
    if (object instanceof com.revolsys.jts.geom.Point) {
      final com.revolsys.jts.geom.Point point = (com.revolsys.jts.geom.Point)object;
      return toPgPoint(point);
    } else if (object instanceof com.revolsys.jts.geom.GeometryCollection) {
      final com.revolsys.jts.geom.GeometryCollection geometryCollection = (com.revolsys.jts.geom.GeometryCollection)object;
      if (geometryCollection.getGeometryCount() == 1) {
        final com.revolsys.jts.geom.Geometry firstGeometry = geometryCollection.getGeometry(0);
        if (firstGeometry instanceof com.revolsys.jts.geom.Point) {
          final com.revolsys.jts.geom.Point point = (com.revolsys.jts.geom.Point)firstGeometry;
          return toPgPoint(point);
        } else {
          throw new RuntimeException(
            "GeometryCollection must contain a single Point not a "
              + firstGeometry.getClass());
        }
      } else {
        throw new RuntimeException(
          "GeometryCollection has more than one geometry");
      }
    } else {
      throw new RuntimeException("Expecting a point");
    }
  }

  private Point[] toPgPoints(final com.revolsys.jts.geom.LineString line) {
    final Point[] points = new Point[line.getVertexCount()];
    for (int i = 0; i < line.getVertexCount(); i++) {
      Point pgPoint;
      final double y = line.getY(i);
      final double x = line.getX(i);

      if (axisCount > 2) {
        double z = line.getZ(i);
        if (Double.isNaN(z)) {
          z = 0;
        }
        pgPoint = new Point(x, y, z);
        if (axisCount > 3) {
          double m = line.getM(i);
          if (Double.isNaN(m)) {
            m = 0;
          }
          pgPoint.m = m;
        }
      } else {
        pgPoint = new Point(x, y);
      }
      points[i] = pgPoint;
    }
    return points;
  }

  private Polygon toPgPolygon(final com.revolsys.jts.geom.Polygon polygon) {
    final LinearRing[] rings = new LinearRing[1 + polygon.getNumInteriorRing()];
    final com.revolsys.jts.geom.LineString exteriorRing = polygon.getExteriorRing();
    rings[0] = toPgLinearRing(exteriorRing);
    for (int i = 0; i < polygon.getNumInteriorRing(); i++) {
      final com.revolsys.jts.geom.LineString ring = polygon.getInteriorRing(i);
      rings[i + 1] = toPgLinearRing(ring);

    }
    final Polygon pgPolygon = new Polygon(rings);
    pgPolygon.setSrid(polygon.getSrid());
    return pgPolygon;
  }

  private Geometry toPgPolygon(final Object object) {
    if (object instanceof com.revolsys.jts.geom.Polygon) {
      final com.revolsys.jts.geom.Polygon polygon = (com.revolsys.jts.geom.Polygon)object;
      return toPgPolygon(polygon);
    } else if (object instanceof com.revolsys.jts.geom.GeometryCollection) {
      final com.revolsys.jts.geom.GeometryCollection geometryCollection = (com.revolsys.jts.geom.GeometryCollection)object;
      if (geometryCollection.getGeometryCount() == 1) {
        final com.revolsys.jts.geom.Geometry firstGeometry = geometryCollection.getGeometry(0);
        if (firstGeometry instanceof com.revolsys.jts.geom.Polygon) {
          final com.revolsys.jts.geom.Polygon polygon = (com.revolsys.jts.geom.Polygon)firstGeometry;
          return toPgPolygon(polygon);
        } else {
          throw new RuntimeException(
            "GeometryCollection must contain a single Polygon not a "
              + firstGeometry.getClass());
        }
      } else {
        throw new RuntimeException("Expecting a single Polygon not a "
          + object.getClass() + " with more than one geometry");
      }
    } else if (object == null) {
      return null;
    } else {
      throw new RuntimeException("Expecting a polygon");
    }
  }
}
