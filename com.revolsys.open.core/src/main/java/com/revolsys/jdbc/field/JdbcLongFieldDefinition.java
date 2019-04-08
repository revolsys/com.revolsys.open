package com.revolsys.jdbc.field;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import org.jeometry.common.data.type.DataTypes;

public class JdbcLongFieldDefinition extends JdbcFieldDefinition {
  public JdbcLongFieldDefinition(final String dbName, final String name, final int sqlType,
    final boolean required, final String description, final Map<String, Object> properties) {
    super(dbName, name, DataTypes.LONG, sqlType, 20, 0, required, description, properties);
  }

  @Override
  public JdbcLongFieldDefinition clone() {
    return new JdbcLongFieldDefinition(getDbName(), getName(), getSqlType(), isRequired(),
      getDescription(), getProperties());
  }

  @Override
  public Object getValueFromResultSet(final ResultSet resultSet, final int columnIndex,
    final boolean internStrings) throws SQLException {
    final long value = resultSet.getLong(columnIndex);
    if (resultSet.wasNull()) {
      return null;
    } else {
      return Long.valueOf(value);
    }
  }

  @Override
  public int setPreparedStatementValue(final PreparedStatement statement, final int parameterIndex,
    final Object value) throws SQLException {
    if (value == null) {
      statement.setNull(parameterIndex, getSqlType());
    } else {
      long numberValue;
      if (value instanceof Number) {
        final Number number = (Number)value;
        numberValue = number.longValue();
      } else {
        numberValue = Long.parseLong(value.toString());
      }
      statement.setLong(parameterIndex, numberValue);

    }
    return parameterIndex + 1;
  }
}
