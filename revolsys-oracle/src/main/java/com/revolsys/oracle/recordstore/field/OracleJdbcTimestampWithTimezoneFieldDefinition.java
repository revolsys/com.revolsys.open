package com.revolsys.oracle.recordstore.field;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Collections;

import org.jeometry.common.data.type.DataTypes;
import org.jeometry.common.date.Dates;

import com.revolsys.jdbc.JdbcConnection;
import com.revolsys.jdbc.field.JdbcFieldDefinition;
import com.revolsys.jdbc.io.JdbcRecordStore;

import oracle.jdbc.OracleConnection;
import oracle.sql.TIMESTAMPTZ;

public class OracleJdbcTimestampWithTimezoneFieldDefinition extends JdbcFieldDefinition {
  public OracleJdbcTimestampWithTimezoneFieldDefinition(final String dbName, final String name,
    final int sqlType, final boolean required, final String description) {
    super(dbName, name, DataTypes.INSTANT, sqlType, 0, 0, required, description,
      Collections.<String, Object> emptyMap());
  }

  @Override
  public Object getValueFromResultSet(final ResultSet resultSet, final int columnIndex,
    final boolean internStrings) throws SQLException {
    TIMESTAMPTZ value = (TIMESTAMPTZ)resultSet.getObject(columnIndex);
    if (value == null) {
      return null;
    } else {
      JdbcRecordStore recordStore = getRecordStore();
      try (
        JdbcConnection connection = recordStore.getJdbcConnection()) {
        OracleConnection oracleConnection = connection.unwrap(OracleConnection.class);
        Timestamp timestamp = value.timestampValue(oracleConnection);
        ZoneId zoneId = value.getTimeZone().toZoneId();

        return timestamp.toInstant().atZone(zoneId).toInstant();
      }
    }
  }

  @Override
  public boolean isSortable() {
    return false;
  }

  @Override
  public int setPreparedStatementValue(final PreparedStatement statement, final int parameterIndex,
    final Object value) throws SQLException {
    if (value == null) {
      final int sqlType = getSqlType();
      statement.setNull(parameterIndex, sqlType);
    } else {
      Instant instant = DataTypes.INSTANT.toObject(value);
      JdbcRecordStore recordStore = getRecordStore();
      try (
        JdbcConnection connection = recordStore.getJdbcConnection()) {
        OracleConnection oracleConnection = connection.unwrap(OracleConnection.class);
        Timestamp timestamp = Timestamp.from(instant);
        TIMESTAMPTZ timestampTz = new TIMESTAMPTZ(oracleConnection, timestamp, Dates.UTC);
        statement.setObject(parameterIndex, timestampTz);
      }
    }
    return parameterIndex + 1;
  }

}
