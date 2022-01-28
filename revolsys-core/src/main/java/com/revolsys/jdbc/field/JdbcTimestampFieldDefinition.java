package com.revolsys.jdbc.field;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Map;

import org.jeometry.common.data.type.DataTypes;
import org.jeometry.common.date.Dates;

import com.revolsys.record.query.ColumnIndexes;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.util.Property;

public class JdbcTimestampFieldDefinition extends JdbcFieldDefinition {
  public JdbcTimestampFieldDefinition(final String dbName, final String name, final int sqlType,
    final boolean required, final String description, final Map<String, Object> properties) {
    super(dbName, name, DataTypes.INSTANT, sqlType, 0, 0, required, description, properties);
  }

  @Override
  public JdbcTimestampFieldDefinition clone() {
    final JdbcTimestampFieldDefinition clone = new JdbcTimestampFieldDefinition(getDbName(),
      getName(), getSqlType(), isRequired(), getDescription(), getProperties());
    postClone(clone);
    return clone;
  }

  @Override
  public Object getValueFromResultSet(final RecordDefinition recordDefinition,
    final ResultSet resultSet, final ColumnIndexes indexes, final boolean internStrings)
    throws SQLException {
    final int index = indexes.incrementAndGet();
    final Timestamp timestamp = resultSet.getTimestamp(index);
    if (timestamp == null) {
      return null;
    } else {
      return timestamp.toInstant();
    }
  }

  @Override
  public int setPreparedStatementValue(final PreparedStatement statement, final int parameterIndex,
    final Object value) throws SQLException {
    if (Property.isEmpty(value)) {
      final int sqlType = getSqlType();
      statement.setNull(parameterIndex, sqlType);
    } else if (value instanceof Timestamp) {
      final Timestamp timestamp = (Timestamp)value;
      statement.setTimestamp(parameterIndex, timestamp);
    } else {
      final Timestamp timestamp = Dates.getTimestamp(value);
      statement.setTimestamp(parameterIndex, timestamp);
    }
    return parameterIndex + 1;
  }
}
