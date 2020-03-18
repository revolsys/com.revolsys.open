package com.revolsys.jdbc.field;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import org.jeometry.common.data.type.DataTypes;

public class JdbcShortFieldDefinition extends JdbcFieldDefinition {
  public JdbcShortFieldDefinition(final String dbName, final String name, final int sqlType,
    final boolean required, final String description, final Map<String, Object> properties) {
    super(dbName, name, DataTypes.SHORT, sqlType, 6, 0, required, description, properties);
  }

  @Override
  public JdbcShortFieldDefinition clone() {
    return new JdbcShortFieldDefinition(getDbName(), getName(), getSqlType(), isRequired(),
      getDescription(), getProperties());
  }

  @Override
  public Object getValueFromResultSet(final ResultSet resultSet, final int columnIndex,
    final boolean internStrings) throws SQLException {
    final short value = resultSet.getShort(columnIndex);
    if (resultSet.wasNull()) {
      return null;
    } else {
      return Short.valueOf(value);
    }
  }

  @Override
  public int setPreparedStatementValue(final PreparedStatement statement, final int parameterIndex,
    final Object value) throws SQLException {
    if (value == null) {
      statement.setNull(parameterIndex, getSqlType());
    } else {
      short numberValue;
      if (value instanceof Number) {
        final Number number = (Number)value;
        numberValue = number.shortValue();
      } else {
        numberValue = Short.parseShort(value.toString());
      }
      statement.setShort(parameterIndex, numberValue);
    }
    return parameterIndex + 1;
  }
}
