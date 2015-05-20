package com.revolsys.jdbc.field;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Map;

import com.revolsys.data.record.Record;
import com.revolsys.data.types.DataTypes;

public class JdbcTimestampFieldDefinition extends JdbcFieldDefinition {
  public JdbcTimestampFieldDefinition(final String dbName, final String name,
    final int sqlType, final boolean required, final String description,
    final Map<String, Object> properties) {
    super(dbName, name, DataTypes.DATE_TIME, sqlType, 0, 0, required,
      description, properties);
  }

  @Override
  public JdbcTimestampFieldDefinition clone() {
    return new JdbcTimestampFieldDefinition(getDbName(), getName(), getSqlType(),
      isRequired(), getDescription(), getProperties());
  }

  @Override
  public int setFieldValueFromResultSet(final ResultSet resultSet,
    final int columnIndex, final Record object) throws SQLException {
    final Timestamp value = resultSet.getTimestamp(columnIndex);
    setValue(object, value);
    return columnIndex + 1;
  }

  @Override
  public int setPreparedStatementValue(final PreparedStatement statement,
    final int parameterIndex, final Object value) throws SQLException {
    if (value == null) {
      final int sqlType = getSqlType();
      statement.setNull(parameterIndex, sqlType);
    } else {
      if (value instanceof Timestamp) {
        final Timestamp timestamp = (Timestamp)value;
        statement.setTimestamp(parameterIndex, timestamp);
      } else if (value instanceof java.util.Date) {
        final java.util.Date date = (java.util.Date)value;
        statement.setTimestamp(parameterIndex, new Timestamp(date.getTime()));
      } else {
        final Timestamp date = Timestamp.valueOf(value.toString());
        statement.setTimestamp(parameterIndex, date);
      }
    }
    return parameterIndex + 1;
  }
}
