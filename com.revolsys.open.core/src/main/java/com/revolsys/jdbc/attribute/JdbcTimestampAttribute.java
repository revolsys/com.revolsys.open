package com.revolsys.jdbc.attribute;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Map;

import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.types.DataTypes;

public class JdbcTimestampAttribute extends JdbcAttribute {
  public JdbcTimestampAttribute(final String name, final int sqlType,
    final boolean required, final String description,
    final Map<String, Object> properties) {
    super(name, DataTypes.DATE_TIME, sqlType, 0, 0, required, description,
      properties);
  }

  @Override
  public JdbcTimestampAttribute clone() {
    return new JdbcTimestampAttribute(getName(), getSqlType(), isRequired(),
      getDescription(), getProperties());
  }

  @Override
  public int setAttributeValueFromResultSet(final ResultSet resultSet,
    final int columnIndex, final DataObject object) throws SQLException {
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
