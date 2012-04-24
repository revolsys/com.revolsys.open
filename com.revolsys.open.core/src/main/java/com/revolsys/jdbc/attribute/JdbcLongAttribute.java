package com.revolsys.jdbc.attribute;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import javax.xml.namespace.QName;

import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.types.DataTypes;

public class JdbcLongAttribute extends JdbcAttribute {
  public JdbcLongAttribute(final String name, final int sqlType,
    final int length, final boolean required,
    final Map<String, Object> properties) {
    super(name, DataTypes.LONG, sqlType, length, 0, required, properties);
  }

  @Override
  public JdbcLongAttribute clone() {
    return new JdbcLongAttribute(getName(), getSqlType(), getLength(),
      isRequired(), getProperties());
  }

  @Override
  public int setAttributeValueFromResultSet(
    final ResultSet resultSet,
    final int columnIndex,
    final DataObject object) throws SQLException {
    final long longValue = resultSet.getLong(columnIndex);
    if (!resultSet.wasNull()) {
      object.setValue(getIndex(), Long.valueOf(longValue));
    }
    return columnIndex + 1;
  }

  @Override
  public int setPreparedStatementValue(
    final PreparedStatement statement,
    final int parameterIndex,
    final Object value) throws SQLException {
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
