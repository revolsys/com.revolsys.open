package com.revolsys.oracle.recordstore.field;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.jeometry.common.jdbc.StringClob;

public class OracleJdbcClobAsStringFieldDefinition extends OracleJdbcClobFieldDefinition {
  public OracleJdbcClobAsStringFieldDefinition(final String dbName, final String name,
    final int sqlType, final boolean required, final String description) {
    super(dbName, name, sqlType, required, description);
  }

  @Override
  public Object getValueFromResultSet(final ResultSet resultSet, final int columnIndex,
    final boolean internStrings) throws SQLException {
    final String string = resultSet.getString(columnIndex);
    return new StringClob(string);
  }

}
