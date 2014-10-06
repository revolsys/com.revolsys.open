package com.revolsys.jdbc.attribute;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import com.revolsys.data.record.Record;
import com.revolsys.data.types.DataTypes;

public class JdbcLongFieldDefinition extends JdbcFieldDefinition {
  public JdbcLongFieldDefinition(final String name) {
    super(name, name, DataTypes.LONG, 0, 0, 0, false, null, null);
  }

  public JdbcLongFieldDefinition(final String dbName, final String name,
    final int sqlType, final int length, final boolean required,
    final String description, final Map<String, Object> properties) {
    super(dbName, name, DataTypes.LONG, sqlType, length, 0, required,
      description, properties);
  }

  @Override
  public JdbcLongFieldDefinition clone() {
    return new JdbcLongFieldDefinition(getDbName(), getName(), getSqlType(),
      getLength(), isRequired(), getDescription(), getProperties());
  }

  @Override
  public int setFieldValueFromResultSet(final ResultSet resultSet,
    final int columnIndex, final Record object) throws SQLException {
    final long longValue = resultSet.getLong(columnIndex);
    if (!resultSet.wasNull()) {
      setValue(object, Long.valueOf(longValue));
    }
    return columnIndex + 1;
  }

  @Override
  public int setPreparedStatementValue(final PreparedStatement statement,
    final int parameterIndex, final Object value) throws SQLException {
    if (value == null) {
      statement.setNull(parameterIndex, getSqlType());
    } else {
      long numberValue;
      if (value instanceof Number) {
        final Number number = (Number)value;
        numberValue = number.longValue();
      } else {
        numberValue = Long.parseLong(value.toString());
      }
      statement.setLong(parameterIndex, numberValue);

    }
    return parameterIndex + 1;
  }
}
