package com.revolsys.gis.postgresql.type;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import com.revolsys.datatype.DataType;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.jdbc.field.JdbcFieldDefinition;
import com.revolsys.record.Record;
import com.revolsys.record.property.FieldProperties;

public class PostgreSQLGeometryJdbcFieldDefinition extends JdbcFieldDefinition {
  private final int axisCount;

  private final GeometryFactory geometryFactory;

  private final int srid;

  public PostgreSQLGeometryJdbcFieldDefinition(final String dbName, final String name,
    final DataType dataType, final boolean required, final String description,
    final Map<String, Object> properties, final int srid, final int axisCount,
    final GeometryFactory geometryFactory) {
    super(dbName, name, dataType, -1, 0, 0, required, description, properties);
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

  public Object getInsertUpdateValue(final Object object) throws SQLException {
    if (object == null) {
      return null;
    } else if (object instanceof Geometry) {
      final Geometry geometry = (Geometry)object;
      if (geometry.isEmpty()) {
        return geometry;
      } else {
        final DataType dataType = getDataType();
        return new PostgreSQLGeometryWrapper(dataType, this.geometryFactory, geometry);
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
    final Object postgresValue = resultSet.getObject(columnIndex);
    final Object value = toJava(postgresValue);
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
      final Geometry geometry = geometryType.getGeometry(this.geometryFactory);
      return geometry;
    } else {
      return object;
    }
  }

  public Object toJdbc(final Object object) throws SQLException {
    if (object instanceof Geometry) {
      final Geometry geometry = (Geometry)object;
      final DataType dataType = getDataType();
      return new PostgreSQLGeometryWrapper(dataType, this.geometryFactory, geometry);
    } else if (object instanceof BoundingBox) {
      BoundingBox boundingBox = (BoundingBox)object;
      boundingBox = boundingBox.convert(this.geometryFactory);
      return new PostgreSQLBoundingBoxWrapper(boundingBox);
    } else {
      return object;
    }
  }

}
