package com.revolsys.gis.postgresql.type;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;

import org.postgresql.util.PGobject;

import com.revolsys.jdbc.field.JdbcFieldDefinition;
import com.revolsys.record.io.format.json.Json;

public class PostgreSQLJsonbFieldDefinition extends JdbcFieldDefinition {

  public PostgreSQLJsonbFieldDefinition(final String dbName, final String name,
    final String dataType, final int sqlType, final int length, final int scale,
    final boolean required, final String description, final Map<String, Object> properties) {
    super(dbName, name, Json.JSON_TYPE, sqlType, length, scale, required, description, properties);
  }

  @Override
  public int setPreparedStatementValue(final PreparedStatement statement, final int parameterIndex,
    final Object value) throws SQLException {

    if (value == null) {
      final int sqlType = getSqlType();
      statement.setNull(parameterIndex, sqlType);
    } else {
      final PGobject json = new PGobject();
      json.setType("jsonb");
      json.setValue(value.toString());
      statement.setObject(parameterIndex, json);
    }
    return parameterIndex + 1;

  }

}
