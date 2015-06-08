package com.revolsys.jdbc.field;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import com.revolsys.data.record.Record;
import com.revolsys.data.types.DataTypes;

public class JdbcIntegerFieldDefinition extends JdbcFieldDefinition {
  public JdbcIntegerFieldDefinition(final String dbName, final String name, final int sqlType,
    final int length, final boolean required, final String description,
    final Map<String, Object> properties) {
    super(dbName, name, DataTypes.INT, sqlType, length, 0, required, description, properties);
  }

  @Override
  public JdbcIntegerFieldDefinition clone() {
    return new JdbcIntegerFieldDefinition(getDbName(), getName(), getSqlType(), getLength(),
      isRequired(), getDescription(), getProperties());
  }

  @Override
  public int setFieldValueFromResultSet(final ResultSet resultSet, final int columnIndex,
    final Record object) throws SQLException {
    final int value = resultSet.getInt(columnIndex);
    if (!resultSet.wasNull()) {
      setValue(object, Integer.valueOf(value));
    }
    return columnIndex + 1;
  }

  @Override
  public int setPreparedStatementValue(final PreparedStatement statement, final int parameterIndex,
    final Object value) throws SQLException {
    if (value == null) {
      statement.setNull(parameterIndex, getSqlType());
    } else {
      int numberValue;
      if (value instanceof Number) {
        final Number number = (Number)value;
        numberValue = number.intValue();
      } else {
        numberValue = Integer.parseInt(value.toString());
      }
      statement.setInt(parameterIndex, numberValue);
    }
    return parameterIndex + 1;
  }
}
