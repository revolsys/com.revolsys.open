package com.revolsys.gis.postgis;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import javax.xml.namespace.QName;

import org.postgis.Geometry;
import org.postgis.LineString;
import org.postgis.LinearRing;
import org.postgis.MultiLineString;
import org.postgis.PGgeometry;
import org.postgis.Point;
import org.postgis.Polygon;

import com.revolsys.gis.cs.CoordinateSystem;
import com.revolsys.gis.cs.GeometryFactory;
import com.revolsys.gis.cs.epsg.EpsgCoordinateSystems;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.types.DataType;
import com.revolsys.gis.jdbc.attribute.JdbcAttribute;
import com.revolsys.gis.model.coordinates.list.CoordinatesList;
import com.revolsys.gis.model.coordinates.list.DoubleCoordinatesList;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.PrecisionModel;

public class PostGisGeometryJdbcAttribute extends JdbcAttribute {
  private final GeometryFactory geometryFactory;

  private final int srid;

  public PostGisGeometryJdbcAttribute(
    final String name,
    final DataType type,
    final int length,
    final int scale,
    final boolean required,
    final Map<QName, Object> properties,
    final int srid) {
    super(name, type, -1, length, scale, required, properties);
    this.srid = srid;
    final PrecisionModel precisionModel = new PrecisionModel();
    final CoordinateSystem coordinateSystem = EpsgCoordinateSystems.getCoordinateSystem(srid);
    geometryFactory = new GeometryFactory(coordinateSystem, precisionModel);
  }

  @Override
  protected JdbcAttribute clone() {
    return new PostGisGeometryJdbcAttribute(getName(), getType(), getLength(),
      getScale(), isRequired(), getProperties(), srid);
  }

  @Override
  public int setAttributeValueFromResultSet(
    final ResultSet resultSet,
    final int columnIndex,
    final DataObject object)
    throws SQLException {
    final Object oracleValue = resultSet.getObject(columnIndex);
    final Object value = toJava(oracleValue);
    object.setValue(getIndex(), value);
    return columnIndex + 1;
  }

  @Override
  public int setPreparedStatementValue(
    final PreparedStatement statement,
    final int parameterIndex,
    final Object value)
    throws SQLException {
    final Object jdbcValue = toJdbc(value);
    statement.setObject(parameterIndex, jdbcValue);
    return parameterIndex + 1;
  }

  public Object toJava(
    final Object object)
    throws SQLException {
    if (object instanceof PGgeometry) {
      final PGgeometry pgGeometry = (PGgeometry)object;
      final Geometry geometry = pgGeometry.getGeometry();
      if (geometry.getType() == Geometry.POINT) {
        return toJtsPoint(geometryFactory, (Point)geometry);
      } else if (geometry.getType() == Geometry.LINESTRING) {
        return toJtsLineString(geometryFactory, (LineString)geometry);
      } else if (geometry.getType() == Geometry.POLYGON) {
        return toJtsPolygon(geometryFactory, (Polygon)geometry);
      } else if (geometry.getType() == Geometry.MULTILINESTRING) {
        return toJtsMultiLineString(geometryFactory, (MultiLineString)geometry);
      } else {
        return null;
      }
    } else {
      return null;
    }
  }

  public Object toJdbc(
    final Object object)
    throws SQLException {
    Geometry geometry = null;
    if (object instanceof com.vividsolutions.jts.geom.Point) {
      final com.vividsolutions.jts.geom.Point point = (com.vividsolutions.jts.geom.Point)object;
      geometry = toPgPoint(point);
    } else if (object instanceof com.vividsolutions.jts.geom.LineString) {
      final com.vividsolutions.jts.geom.LineString lineString = (com.vividsolutions.jts.geom.LineString)object;
      geometry = toPgLineString(lineString);
    } else if (object instanceof com.vividsolutions.jts.geom.MultiLineString) {
      final com.vividsolutions.jts.geom.MultiLineString lineString = (com.vividsolutions.jts.geom.MultiLineString)object;
      geometry = toPgMultiLineString(lineString);
    } else if (object instanceof com.vividsolutions.jts.geom.Polygon) {
      final com.vividsolutions.jts.geom.Polygon polygon = (com.vividsolutions.jts.geom.Polygon)object;
      geometry = toPgPolygon(polygon);
    } else {
      return object;
    }
    return new PGgeometry(geometry);
  }

  private com.vividsolutions.jts.geom.LineString toJtsLineString(
    final GeometryFactory factory,
    final LineString lineString) {
    final Point[] points = lineString.getPoints();
    final int dimension = lineString.getDimension();
    final CoordinatesList coordinates = new DoubleCoordinatesList(
      points.length, dimension);
    for (int i = 0; i < points.length; i++) {
      final Point point = points[i];
      coordinates.setValue(i, 0, point.x);
      coordinates.setValue(i, 1, point.y);
      if (dimension > 3) {
        coordinates.setOrdinate(i, 2, point.z);
        if (dimension > 4) {
          coordinates.setOrdinate(i, 3, point.m);
        }
      }
    }
    return factory.createLineString(coordinates);
  }

  private com.vividsolutions.jts.geom.MultiLineString toJtsMultiLineString(
    final GeometryFactory factory,
    final MultiLineString multiLine) {
    final LineString[] lines = multiLine.getLines();
    final com.vividsolutions.jts.geom.LineString[] lineStrings = new com.vividsolutions.jts.geom.LineString[lines.length];
    for (int i = 0; i < lines.length; i++) {
      final LineString line = lines[i];
      lineStrings[i] = toJtsLineString(factory, line);
    }
    return factory.createMultiLineString(lineStrings);
  }

  private com.vividsolutions.jts.geom.Point toJtsPoint(
    final GeometryFactory factory,
    final Point point) {
    final Coordinate coordinate = new Coordinate(point.x, point.y, point.z);
    return factory.createPoint(coordinate);
  }

  private com.vividsolutions.jts.geom.Polygon toJtsPolygon(
    final GeometryFactory factory,
    final Polygon polygon) {
    final LinearRing ring = polygon.getRing(0);
    final Point[] points = ring.getPoints();
    final int dimension = polygon.getDimension();
    final CoordinatesList coordinates = new DoubleCoordinatesList(
      points.length, dimension);
    for (int i = 0; i < points.length; i++) {
      final Point point = points[i];
      coordinates.setValue(i, 0, point.x);
      coordinates.setValue(i, 1, point.y);
      if (dimension > 3) {
        coordinates.setOrdinate(i, 2, point.z);
        if (dimension > 4) {
          coordinates.setOrdinate(i, 3, point.m);
        }
      }
    }
    final com.vividsolutions.jts.geom.LinearRing exteriorRing = factory.createLinearRing(coordinates);
    return factory.createPolygon(exteriorRing, null);
  }

  private LinearRing toPgLinearRing(
    final com.vividsolutions.jts.geom.LineString ring) {
    final CoordinateSequence coordinateSequence = ring.getCoordinateSequence();
    final Point[] points = toPgPoints(coordinateSequence);
    return new LinearRing(points);
  }

  private LineString toPgLineString(
    final com.vividsolutions.jts.geom.LineString lineString) {
    final CoordinateSequence coordinates = lineString.getCoordinateSequence();
    final Point[] points = toPgPoints(coordinates);
    final LineString pgLineString = new LineString(points);
    pgLineString.setSrid(lineString.getSRID());
    return pgLineString;
  }

  private MultiLineString toPgMultiLineString(
    final com.vividsolutions.jts.geom.MultiLineString multiLineString) {
    final LineString[] pgLineStrings = new LineString[multiLineString.getNumGeometries()];
    for (int i = 0; i < multiLineString.getNumGeometries(); i++) {
      final com.vividsolutions.jts.geom.LineString lineString = (com.vividsolutions.jts.geom.LineString)multiLineString.getGeometryN(i);
      pgLineStrings[i] = toPgLineString(lineString);
    }
    final MultiLineString pgMultiLineString = new MultiLineString(pgLineStrings);
    pgMultiLineString.setSrid(multiLineString.getSRID());
    return pgMultiLineString;
  }

  private Point toPgPoint(
    final com.vividsolutions.jts.geom.Point point) {
    final Coordinate coord = point.getCoordinate();
    final Point pgPoint = toPgPoint(coord);
    pgPoint.setSrid(point.getSRID());
    return pgPoint;
  }

  private Point toPgPoint(
    final Coordinate coordinate) {
    if (Double.isNaN(coordinate.z)) {
      return new Point(coordinate.x, coordinate.y);
    } else {
      return new Point(coordinate.x, coordinate.y, coordinate.z);
    }
  }

  private Point[] toPgPoints(
    final CoordinateSequence coordinates) {
    final Point[] points = new Point[coordinates.size()];
    for (int i = 0; i < coordinates.size(); i++) {
      final double y = coordinates.getY(i);
      final double x = coordinates.getX(i);
      if (coordinates.getDimension() == 2) {
        points[i] = new Point(x, y);
      } else {
        final double z = coordinates.getOrdinate(i, 2);
        if (Double.isNaN(z)) {
          points[i] = new Point(x, y);
        } else {
          points[i] = new Point(x, y, z);
        }
      }
    }
    return points;
  }

  private Polygon toPgPolygon(
    final com.vividsolutions.jts.geom.Polygon polygon) {
    final LinearRing[] rings = new LinearRing[1 + polygon.getNumInteriorRing()];
    final com.vividsolutions.jts.geom.LineString exteriorRing = polygon.getExteriorRing();
    rings[0] = toPgLinearRing(exteriorRing);
    for (int i = 0; i < polygon.getNumInteriorRing(); i++) {
      final com.vividsolutions.jts.geom.LineString ring = polygon.getInteriorRingN(i);
      rings[i + 1] = toPgLinearRing(ring);

    }
    final Polygon pgPolygon = new Polygon(rings);
    pgPolygon.setSrid(polygon.getSRID());
    return pgPolygon;
  }
}
