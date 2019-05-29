package com.revolsys.jdbc.field;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Map;

import org.jeometry.common.data.type.DataTypes;

import com.revolsys.record.Record;
import com.revolsys.util.Dates;
import com.revolsys.util.Property;

public class JdbcTimestampFieldDefinition extends JdbcFieldDefinition {
  public JdbcTimestampFieldDefinition(final String dbName, final String name, final int sqlType,
    final boolean required, final String description, final Map<String, Object> properties) {
    super(dbName, name, DataTypes.TIMESTAMP, sqlType, 0, 0, required, description, properties);
  }

  @Override
  public JdbcTimestampFieldDefinition clone() {
    return new JdbcTimestampFieldDefinition(getDbName(), getName(), getSqlType(), isRequired(),
      getDescription(), getProperties());
  }

  @Override
  public int setFieldValueFromResultSet(final ResultSet resultSet, final int columnIndex,
    final Record record) throws SQLException {
    final Timestamp value = resultSet.getTimestamp(columnIndex);
    setValue(record, value);
    return columnIndex + 1;
  }

  @Override
  public int setPreparedStatementValue(final PreparedStatement statement, final int parameterIndex,
    final Object value) throws SQLException {
    if (Property.isEmpty(value)) {
      final int sqlType = getSqlType();
      statement.setNull(parameterIndex, sqlType);
    } else {
      final Timestamp timestamp = Dates.getTimestamp(value);
      statement.setTimestamp(parameterIndex, timestamp);
    }
    return parameterIndex + 1;
  }
}
