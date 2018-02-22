package com.revolsys.jdbc.field;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import com.revolsys.datatype.DataTypes;

public class JdbcFloatFieldDefinition extends JdbcFieldDefinition {
  public JdbcFloatFieldDefinition(final String dbName, final String name, final int sqlType,
    final boolean required, final String description, final Map<String, Object> properties) {
    super(dbName, name, DataTypes.FLOAT, sqlType, 11, 0, required, description, properties);
  }

  @Override
  public JdbcFloatFieldDefinition clone() {
    return new JdbcFloatFieldDefinition(getDbName(), getName(), getSqlType(), isRequired(),
      getDescription(), getProperties());
  }

  @Override
  public Object getValueFromResultSet(final ResultSet resultSet, final int columnIndex,
    final boolean internStrings) throws SQLException {
    final float value = resultSet.getFloat(columnIndex);
    if (resultSet.wasNull()) {
      return null;
    } else {
      return Float.valueOf(value);
    }
  }

  @Override
  public int setPreparedStatementValue(final PreparedStatement statement, final int parameterIndex,
    final Object value) throws SQLException {
    if (value == null) {
      statement.setNull(parameterIndex, getSqlType());
    } else {
      float numberValue;
      if (value instanceof Number) {
        final Number number = (Number)value;
        numberValue = number.floatValue();
      } else {
        numberValue = Float.parseFloat(value.toString());
      }
      statement.setFloat(parameterIndex, numberValue);
    }
    return parameterIndex + 1;
  }
}
