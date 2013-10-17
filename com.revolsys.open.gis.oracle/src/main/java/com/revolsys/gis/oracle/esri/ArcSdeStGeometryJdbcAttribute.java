package com.revolsys.gis.oracle.esri;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.sql.Blob;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;
import java.util.Map;

import com.revolsys.gis.cs.BoundingBox;
import com.revolsys.gis.cs.GeometryFactory;
import com.revolsys.gis.cs.projection.GeometryProjectionUtil;
import com.revolsys.gis.data.model.AttributeProperties;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.types.DataType;
import com.revolsys.gis.jts.JtsGeometryUtil;
import com.revolsys.gis.model.coordinates.Coordinates;
import com.revolsys.gis.model.coordinates.list.CoordinatesList;
import com.revolsys.gis.model.coordinates.list.CoordinatesListUtil;
import com.revolsys.jdbc.attribute.JdbcAttribute;
import com.vividsolutions.jts.geom.Geometry;

public class ArcSdeStGeometryJdbcAttribute extends JdbcAttribute {

  private final int dimension;

  private final ArcSdeSpatialReference spatialReference;

  private final GeometryFactory geometryFactory;

  public ArcSdeStGeometryJdbcAttribute(final String name,
    final DataType type, final boolean required,
    final Map<String, Object> properties,
    final ArcSdeSpatialReference spatialReference, final int dimension) {
    super(name, type, -1, 0, 0, required, properties);
    this.spatialReference = spatialReference;
    final GeometryFactory factory = spatialReference.getGeometryFactory();
    this.geometryFactory = GeometryFactory.getFactory(factory.getSRID(),
      dimension, factory.getScaleXY(), factory.getScaleZ());
    this.dimension = dimension;
    setProperty(AttributeProperties.GEOMETRY_FACTORY, geometryFactory);
  }

  @Override
  public void addColumnName(final StringBuffer sql, final String tablePrefix) {
    sql.append(tablePrefix);
    sql.append(".GEOMETRY.ENTITY, ");
    sql.append(tablePrefix);
    sql.append(".GEOMETRY.NUMPTS, ");
    sql.append(tablePrefix);
    sql.append(".GEOMETRY.POINTS");
  }

  @Override
  public void addStatementPlaceHolder(final StringBuffer sql) {
    sql.append("SDE.ST_GEOMETRY(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
  }

  @Override
  public ArcSdeStGeometryJdbcAttribute clone() {
    return new ArcSdeStGeometryJdbcAttribute(getName(), getType(),
      isRequired(), getProperties(), spatialReference, dimension);
  }

  @Override
  public int setAttributeValueFromResultSet(final ResultSet resultSet,
    final int columnIndex, final DataObject object) throws SQLException {
    final int geometryType = resultSet.getInt(columnIndex);
    if (!resultSet.wasNull()) {
      final int numPoints = resultSet.getInt(columnIndex + 1);
      final Blob blob = resultSet.getBlob(columnIndex + 2);
      final InputStream pointsIn = new BufferedInputStream(
        blob.getBinaryStream(), 32000);

      final Double xOffset = spatialReference.getXOffset();
      final Double yOffset = spatialReference.getYOffset();
      final Double xyScale = spatialReference.getXyScale();
      final Double zScale = spatialReference.getZScale();
      final Double zOffset = spatialReference.getZOffset();
      final Double mScale = spatialReference.getMScale();
      final Double mOffset = spatialReference.getMOffset();

      final GeometryFactory geometryFactory = spatialReference.getGeometryFactory();
      final Geometry geometry = PackedCoordinateUtil.getGeometry(pointsIn,
        geometryFactory, geometryType, numPoints, xOffset, yOffset, xyScale,
        zOffset, zScale, mOffset, mScale);
      object.setValue(getIndex(), geometry);
    }
    return columnIndex + 3;
  }

  @Override
  public int setPreparedStatementValue(final PreparedStatement statement,
    final int parameterIndex, Object value) throws SQLException {
    int index = parameterIndex;

    if (value instanceof Coordinates) {
      final Coordinates coordinates = (Coordinates)value;
      value = geometryFactory.createPoint(coordinates);
    }
    if (value instanceof Geometry) {
      Geometry geometry = (Geometry)value;
      geometry = GeometryProjectionUtil.performCopy(geometry, geometryFactory);

      final int sdeSrid = spatialReference.getEsriSrid();
      final Double xOffset = spatialReference.getXOffset();
      final Double yOffset = spatialReference.getYOffset();
      final Double xyScale = spatialReference.getXyScale();
      final Double zScale = spatialReference.getZScale();
      final Double zOffset = spatialReference.getZOffset();
      final Double mScale = spatialReference.getMScale();
      final Double mOffset = spatialReference.getMOffset();

      final BoundingBox envelope = BoundingBox.getBoundingBox(geometry);
      final double minX = envelope.getMinX();
      final double minY = envelope.getMinY();
      final double maxX = envelope.getMaxX();
      final double maxY = envelope.getMaxY();
      final double area = geometry.getArea();
      final double length = geometry.getLength();

      final boolean hasZ = dimension > 2 && zOffset != null && zScale != null;
      final boolean hasM = dimension > 3 && mOffset != null && mScale != null;

      int numPoints = 0;
      byte[] data;

      final List<List<CoordinatesList>> parts = CoordinatesListUtil.getParts(geometry);
      final int entityType = ArcSdeConstants.getStGeometryType(geometry);
      numPoints = PackedCoordinateUtil.getNumPoints(parts);
      data = PackedCoordinateUtil.getPackedBytes(xOffset, yOffset, xyScale,
        hasZ, zOffset, zScale, hasM, mScale, mOffset, parts);

      statement.setInt(index++, entityType);
      statement.setInt(index++, numPoints);
      statement.setFloat(index++, (float)minX);
      statement.setFloat(index++, (float)minY);
      statement.setFloat(index++, (float)maxX);
      statement.setFloat(index++, (float)maxY);
      if (hasZ) {
        final double[] zRange = JtsGeometryUtil.getZRange(geometry);
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
        final double[] mRange = JtsGeometryUtil.getMRange(geometry);
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
}
