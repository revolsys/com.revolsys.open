package com.revolsys.gis.postgresql;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import com.revolsys.datatype.DataType;
import com.revolsys.datatype.DataTypes;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryCollection;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.LineString;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.Polygon;
import com.revolsys.gis.postgresql.type.PostgreSQLBoundingBoxWrapper;
import com.revolsys.gis.postgresql.type.PostgreSQLGeometryWrapper;
import com.revolsys.jdbc.field.JdbcFieldDefinition;
import com.revolsys.record.Record;
import com.revolsys.record.property.FieldProperties;

public class PostgreSQLGeometryJdbcFieldDefinition extends JdbcFieldDefinition {
  private final int axisCount;

  private final GeometryFactory geometryFactory;

  private final int srid;

  public PostgreSQLGeometryJdbcFieldDefinition(final String dbName, final String name,
    final DataType type, final boolean required, final String description,
    final Map<String, Object> properties, final int srid, final int axisCount,
    final GeometryFactory geometryFactory) {
    super(dbName, name, type, -1, 0, 0, required, description, properties);
    this.srid = srid;
    this.geometryFactory = geometryFactory;
    setProperty(FieldProperties.GEOMETRY_FACTORY, geometryFactory);
    this.axisCount = axisCount;
  }

  @Override
  public JdbcFieldDefinition clone() {
    return new PostgreSQLGeometryJdbcFieldDefinition(getDbName(), getName(), getDataType(),
      isRequired(), getDescription(), getProperties(), this.srid, this.axisCount,
      this.geometryFactory);
  }

  private Geometry getGeometry(final Geometry geometry,
    final Class<? extends Geometry> expectedClass) {
    if (expectedClass.isAssignableFrom(geometry.getClass())) {
      return geometry;
    } else if (geometry instanceof GeometryCollection) {
      final GeometryCollection geometryCollection = (GeometryCollection)geometry;
      if (geometryCollection.getGeometryCount() == 1) {
        final Geometry firstGeometry = geometryCollection.getGeometry(0);
        if (expectedClass.isAssignableFrom(firstGeometry.getClass())) {
          return geometry;
        } else {
          throw new RuntimeException("GeometryCollection must contain a single " + expectedClass
            + " not a " + firstGeometry.getClass());
        }
      } else {
        throw new RuntimeException("GeometryCollection must only have one geometry not "
          + geometryCollection.getGeometryCount());
      }
    } else {
      throw new RuntimeException("Expecting a " + expectedClass + " not " + geometry.getClass());
    }
  }

  public Object getInsertUpdateValue(final Object object) throws SQLException {
    if (object == null) {
      return null;
    } else if (object instanceof Geometry) {
      Geometry geometry = (Geometry)object;
      geometry = geometry.convert(this.geometryFactory);
      if (geometry.isEmpty()) {
        return geometry;
      } else {
        final DataType type = getDataType();
        if (type == DataTypes.POINT) {
          geometry = getGeometry(geometry, Point.class);
        } else if (type == DataTypes.LINE_STRING) {
          geometry = getGeometry(geometry, LineString.class);
        } else if (type == DataTypes.POLYGON) {
          geometry = getGeometry(geometry, Polygon.class);
        } else if (type == DataTypes.MULTI_POINT) {
          geometry = this.geometryFactory.multiPoint(geometry);
        } else if (type == DataTypes.MULTI_LINE_STRING) {
          geometry = this.geometryFactory.multiLineString(geometry);
        } else if (type == DataTypes.MULTI_POLYGON) {
          geometry = this.geometryFactory.multiPolygon(geometry);
        }
        return new PostgreSQLGeometryWrapper(geometry);
      }
    } else if (object instanceof BoundingBox) {
      BoundingBox boundingBox = (BoundingBox)object;
      boundingBox = boundingBox.convert(this.geometryFactory);
      return new PostgreSQLBoundingBoxWrapper(boundingBox);
    } else {
      return object;
    }
  }

  @Override
  public int setFieldValueFromResultSet(final ResultSet resultSet, final int columnIndex,
    final Record object) throws SQLException {
    final Object oracleValue = resultSet.getObject(columnIndex);
    final Object value = toJava(oracleValue);
    object.setValue(getIndex(), value);
    return columnIndex + 1;
  }

  @Override
  public int setInsertPreparedStatementValue(final PreparedStatement statement,
    final int parameterIndex, final Record object) throws SQLException {
    final String name = getName();
    final Object value = object.getValue(name);
    final Object jdbcValue = getInsertUpdateValue(value);
    statement.setObject(parameterIndex, jdbcValue);
    return parameterIndex + 1;
  }

  @Override
  public int setPreparedStatementValue(final PreparedStatement statement, final int parameterIndex,
    final Object value) throws SQLException {
    final Object jdbcValue = toJdbc(value);
    statement.setObject(parameterIndex, jdbcValue);
    return parameterIndex + 1;
  }

  public Object toJava(final Object object) throws SQLException {
    if (object instanceof PostgreSQLGeometryWrapper) {
      final PostgreSQLGeometryWrapper geometryType = (PostgreSQLGeometryWrapper)object;
      final Geometry geometry = geometryType.getGeometry();
      return geometry.convert(this.geometryFactory);
    } else {
      return object;
    }
  }

  public Object toJdbc(final Object object) throws SQLException {
    if (object instanceof Geometry) {
      Geometry geometry = (Geometry)object;
      geometry = geometry.convert(this.geometryFactory);
      return new PostgreSQLGeometryWrapper(geometry);
    } else if (object instanceof BoundingBox) {
      BoundingBox boundingBox = (BoundingBox)object;
      boundingBox = boundingBox.convert(this.geometryFactory);
      return new PostgreSQLBoundingBoxWrapper(boundingBox);
    } else {
      return object;
    }
  }

}
