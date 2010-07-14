package com.revolsys.gis.oracle.esri;

import java.sql.Blob;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Map;

import javax.xml.namespace.QName;

import oracle.sql.SQLName;
import oracle.sql.STRUCT;

import com.revolsys.gis.cs.GeometryFactory;
import com.revolsys.gis.cs.projection.GeometryProjectionUtil;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.types.DataType;
import com.revolsys.gis.jdbc.attribute.JdbcAttribute;
import com.revolsys.gis.jts.CoordinateSequenceUtil;
import com.revolsys.gis.jts.JtsGeometryUtil;
import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

public class ArcSdeOracleStGeometryJdbcAttribute extends JdbcAttribute {

  public static final String COORDINATE_DIMENSION_PROPERTY = "coordinateDimension";

  public static final QName ESRI_SCHEMA_PROPERTY = new QName(
    ArcSdeOracleStGeometryJdbcAttribute.class.getName());

  public static final String ESRI_SRID_PROPERTY = ArcSdeOracleStGeometryJdbcAttribute.class.getName()
    + ".esriSrid";

  private final int dimension;

  private final SpatialReference spatialReference;

  private GeometryFactory geometryFactory;

  public ArcSdeOracleStGeometryJdbcAttribute(
    final String name,
    final DataType type,
    final int length,
    final int scale,
    final boolean required,
    final Map<QName, Object> properties,
    final SpatialReference spatialReference,
    final int dimension) {
    super(name, type, -1, length, scale, required, properties);
    this.spatialReference = spatialReference;
    this.geometryFactory = spatialReference.getGeometryFactory();
    this.dimension = dimension;
  }

  @Override
  public ArcSdeOracleStGeometryJdbcAttribute clone() {
    return new ArcSdeOracleStGeometryJdbcAttribute(getName(), getType(),
      getLength(), getScale(), isRequired(), getProperties(), spatialReference,
      dimension);
  }

  @Override
  public void addStatementPlaceHolder(
    final StringBuffer sql) {
    sql.append("SDE.ST_GEOMETRY(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
  }

  @Override
  public void addColumnName(
    StringBuffer sql,
    String tablePrefix) {
    sql.append(tablePrefix);
    sql.append(".GEOMETRY.ENTITY, ");
    sql.append(tablePrefix);
    sql.append(".GEOMETRY.NUMPTS, ");
    sql.append(tablePrefix);
    sql.append(".GEOMETRY.POINTS");
  }

  @Override
  public int setAttributeValueFromResultSet(
    final ResultSet resultSet,
    final int columnIndex,
    final DataObject object)
    throws SQLException {
    final Geometry geometry;

    final int entity = resultSet.getInt(columnIndex);
    if (!resultSet.wasNull()) {
      final int numPoints = resultSet.getInt(columnIndex + 1);
      final byte[] pointData = resultSet.getBytes(columnIndex+2);

      final Double xOffset = spatialReference.getXOffset();
      final Double yOffset = spatialReference.getYOffset();
      final Double xyScale = spatialReference.getXyScale();
      final Double zScale = spatialReference.getZScale();
      final Double zOffset = spatialReference.getZOffset();
      final Double mScale = spatialReference.getMScale();
      final Double mOffset = spatialReference.getMOffset();

//       final byte[] pointData = points.getBytes(1, (int)points.length());
      final GeometryFactory geometryFactory = spatialReference.getGeometryFactory();
      switch (entity) {
        case ArcSdeConstants.ST_GEOMETRY_POINT:
          final CoordinateSequence pointCoordinates = PackedCoordinateUtil.getCoordinateSequence(
            numPoints, xOffset, yOffset, xyScale, zOffset, zScale, mOffset,
            mScale, pointData);
          geometry = geometryFactory.createPoint(pointCoordinates);
        break;
        case ArcSdeConstants.ST_GEOMETRY_LINESTRING:
          final CoordinateSequence lineCoordinates = PackedCoordinateUtil.getCoordinateSequence(
            numPoints, xOffset, yOffset, xyScale, zOffset, zScale, mOffset,
            mScale, pointData);
          geometry = geometryFactory.createLineString(lineCoordinates);
        break;
        case ArcSdeConstants.ST_GEOMETRY_POLYGON:
          final CoordinateSequence polygonCoordinates = PackedCoordinateUtil.getCoordinateSequence(
            numPoints, xOffset, yOffset, xyScale, zOffset, zScale, mOffset,
            mScale, pointData);
          // TODO holes
          geometry = geometryFactory.createPolygon(
            geometryFactory.createLinearRing(polygonCoordinates), null);
        break;
        // TODO multi geometries
        default:
          throw new IllegalArgumentException(
            "Unknown ST_GEOMETRY entity type: " + entity);
      }
      object.setValue(getIndex(), geometry);
    }
    return columnIndex + 3;
  }

  @Override
  public int setPreparedStatementValue(
    final PreparedStatement statement,
    final int parameterIndex,
    final Object value)
    throws SQLException {
    int index = parameterIndex;

    if (value instanceof Geometry) {
      Geometry geometry = (Geometry)value;
      geometry = GeometryProjectionUtil.perform(geometry, geometryFactory);
      final int numPoints = geometry.getNumPoints();

      final int sdeSrid = spatialReference.getEsriSrid();
      final Double xOffset = spatialReference.getXOffset();
      final Double yOffset = spatialReference.getYOffset();
      final Double xyScale = spatialReference.getXyScale();
      final Double zScale = spatialReference.getZScale();
      final Double zOffset = spatialReference.getZOffset();
      final Double mScale = spatialReference.getMScale();
      final Double mOffset = spatialReference.getMOffset();

      final Envelope envelope = geometry.getEnvelopeInternal();
      final double minX = envelope.getMinX();
      final double minY = envelope.getMinY();
      final double maxX = envelope.getMaxX();
      final double maxY = envelope.getMaxY();
      final double area = geometry.getArea();
      final double length = geometry.getLength();
      int entityType = 0;

      final boolean hasZ = dimension > 2 && zOffset != null && zScale != null;
      final boolean hasM = dimension > 3 && mOffset != null && mScale != null;

      byte[] data;
      CoordinateSequence coordinates;
      if (value instanceof Point) {
        final Point point = (Point)value;
        coordinates = point.getCoordinateSequence();
        data = PackedCoordinateUtil.getPackedBytes(xOffset, yOffset, xyScale,
          hasZ, zOffset, zScale, hasM, mOffset, mScale, coordinates);
        entityType = ArcSdeConstants.ST_GEOMETRY_POINT;
      } else if (value instanceof LineString) {
        final LineString line = (LineString)value;
        coordinates = line.getCoordinateSequence();
        data = PackedCoordinateUtil.getPackedBytes(xOffset, yOffset, xyScale,
          hasZ, zOffset, zScale, hasM, mOffset, mScale, coordinates);
        entityType = ArcSdeConstants.ST_GEOMETRY_LINESTRING;
      } else if (value instanceof Polygon) {
        final Polygon polygon = (Polygon)value;
        final LineString exteriorRing = polygon.getExteriorRing();
        coordinates = exteriorRing.getCoordinateSequence();
        if (!JtsGeometryUtil.isCCW(coordinates)) {
          coordinates = CoordinateSequenceUtil.reverse(coordinates);
        }
        data = PackedCoordinateUtil.getPackedBytes(xOffset, yOffset, xyScale,
          hasZ, zOffset, zScale, hasM, mOffset, mScale, coordinates);
        entityType = ArcSdeConstants.ST_GEOMETRY_POLYGON;
      } else {
        throw new IllegalArgumentException("Cannot convert: "
          + value.getClass());
      }

      statement.setInt(index++, entityType);
      statement.setInt(index++, numPoints);
      statement.setFloat(index++, (float)minX);
      statement.setFloat(index++, (float)minY);
      statement.setFloat(index++, (float)maxX);
      statement.setFloat(index++, (float)maxY);
      if (hasZ) {
        final double[] zRange = JtsGeometryUtil.getZRange(coordinates);
        double minZ = zRange[0];
        double maxZ = zRange[1];
        if (minZ == Double.MAX_VALUE && maxZ == Double.MIN_VALUE) {
          minZ = 0;
          maxZ = 0;
        }
        statement.setFloat(index++, (float)minZ);
        statement.setFloat(index++, (float)maxZ);
      } else {
        statement.setNull(index++, Types.FLOAT);
        statement.setNull(index++, Types.FLOAT);
      }
      if (hasM) {
        final double[] mRange = JtsGeometryUtil.getMRange(coordinates);
        double minM = mRange[0];
        double maxM = mRange[1];
        if (minM == Double.MAX_VALUE && maxM == Double.MIN_VALUE) {
          minM = 0;
          maxM = 0;
        }
        statement.setFloat(index++, (float)minM);
        statement.setFloat(index++, (float)maxM);
      } else {
        statement.setNull(index++, Types.FLOAT);
        statement.setNull(index++, Types.FLOAT);
      }
      statement.setFloat(index++, (float)area);
      statement.setFloat(index++, (float)length);
      statement.setInt(index++, sdeSrid);
      statement.setBytes(index++, data);
    } else {
      throw new IllegalArgumentException("Geometry cannot be null");
    }
    return index;
  }

  public Object toJava(
    final Object object)
    throws SQLException {
    if (object instanceof STRUCT) {
      final STRUCT struct = (STRUCT)object;
      final SQLName sqlTypeName = struct.getDescriptor().getSQLName();
      if (sqlTypeName.getName().equals("SDE.ST_GEOMETRY")) {

      }
    }
    return object;

  }
}
