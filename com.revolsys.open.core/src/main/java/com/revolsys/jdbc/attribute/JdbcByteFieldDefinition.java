package com.revolsys.jdbc.attribute;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import com.revolsys.data.record.Record;
import com.revolsys.data.types.DataTypes;

public class JdbcByteFieldDefinition extends JdbcFieldDefinition {
  public JdbcByteFieldDefinition(final String dbName, final String name,
    final int sqlType, final int length, final boolean required,
    final String description, final Map<String, Object> properties) {
    super(dbName, name, DataTypes.BYTE, sqlType, length, 0, required,
      description, properties);
  }

  @Override
  public JdbcByteFieldDefinition clone() {
    return new JdbcByteFieldDefinition(getDbName(), getName(), getSqlType(),
      getLength(), isRequired(), getDescription(), getProperties());
  }

  @Override
  public int setFieldValueFromResultSet(final ResultSet resultSet,
    final int columnIndex, final Record object) throws SQLException {
    final byte byteValue = resultSet.getByte(columnIndex);
    if (!resultSet.wasNull()) {
      setValue(object, Byte.valueOf(byteValue));
    }
    return columnIndex + 1;
  }

  @Override
  public int setPreparedStatementValue(final PreparedStatement statement,
    final int parameterIndex, final Object value) throws SQLException {
    if (value == null) {
      statement.setNull(parameterIndex, getSqlType());
    } else {
      byte numberValue;
      if (value instanceof Number) {
        final Number number = (Number)value;
        numberValue = number.byteValue();
      } else {
        numberValue = Byte.parseByte(value.toString());
      }
      statement.setByte(parameterIndex, numberValue);
    }
    return parameterIndex + 1;
  }
}
