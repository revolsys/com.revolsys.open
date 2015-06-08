package com.revolsys.jdbc.field;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import com.revolsys.data.record.Record;
import com.revolsys.data.types.DataTypes;

public class JdbcStringFieldDefinition extends JdbcFieldDefinition {
  public JdbcStringFieldDefinition(final String dbName, final String name, final int sqlType,
    final int length, final boolean required, final String description,
    final Map<String, Object> properties) {
    super(dbName, name, DataTypes.STRING, sqlType, length, 0, required, description, properties);
  }

  @Override
  public JdbcStringFieldDefinition clone() {
    return new JdbcStringFieldDefinition(getDbName(), getName(), getSqlType(), getLength(),
      isRequired(), getDescription(), getProperties());
  }

  @Override
  public int setFieldValueFromResultSet(final ResultSet resultSet, final int columnIndex,
    final Record object) throws SQLException {
    final String value = resultSet.getString(columnIndex);
    setValue(object, value);
    return columnIndex + 1;
  }

  @Override
  public int setPreparedStatementValue(final PreparedStatement statement, final int parameterIndex,
    final Object value) throws SQLException {
    if (value == null) {
      final int sqlType = getSqlType();
      statement.setNull(parameterIndex, sqlType);
    } else {
      statement.setString(parameterIndex, value.toString());
    }
    return parameterIndex + 1;
  }

}
