package com.revolsys.jdbc.field;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

public class JdbcBigDecimalFieldDefinition extends JdbcDecimalFieldDefinition {
  public JdbcBigDecimalFieldDefinition(final String dbName, final String name, final int sqlType,
    final int length, final int scale, final boolean required, final String description,
    final Map<String, Object> properties) {
    super(dbName, name, sqlType, length, scale, required, description, properties);
  }

  @Override
  public JdbcBigDecimalFieldDefinition clone() {
    return new JdbcBigDecimalFieldDefinition(getDbName(), getName(), getSqlType(), getLength(),
      getScale(), isRequired(), getDescription(), getProperties());
  }

  @Override
  public Object getValueFromResultSet(final ResultSet resultSet, final int columnIndex,
    final boolean internStrings) throws SQLException {
    return resultSet.getBigDecimal(columnIndex);
  }

}
