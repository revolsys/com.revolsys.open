package com.revolsys.jdbc.attribute;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.types.DataTypes;

public class JdbcBooleanAttribute extends JdbcAttribute {
  public JdbcBooleanAttribute(final String name, final int sqlType,
    final int length, final boolean required, final String description,
    final Map<String, Object> properties) {
    super(name, DataTypes.BOOLEAN, sqlType, length, 0, required, description,
      properties);
  }

  @Override
  public JdbcBooleanAttribute clone() {
    return new JdbcBooleanAttribute(getName(), getSqlType(), getLength(),
      isRequired(), getDescription(), getProperties());
  }

  @Override
  public int setAttributeValueFromResultSet(final ResultSet resultSet,
    final int columnIndex, final DataObject object) throws SQLException {
    final boolean booleanValue = resultSet.getBoolean(columnIndex);
    if (!resultSet.wasNull()) {
      object.setValue(getIndex(), booleanValue);
    }
    return columnIndex + 1;
  }

  @Override
  public int setPreparedStatementValue(final PreparedStatement statement,
    final int parameterIndex, final Object value) throws SQLException {
    if (value == null) {
      statement.setNull(parameterIndex, getSqlType());
    } else {
      boolean booleanValue;
      if (value instanceof Boolean) {
        booleanValue = (Boolean)value;
      } else if (value instanceof Number) {
        final Number number = (Number)value;
        booleanValue = number.intValue() == 1;
      } else {
        final String stringValue = value.toString();
        if (stringValue.equals("1") || Boolean.parseBoolean(stringValue)) {
          booleanValue = true;
        } else {
          booleanValue = false;
        }
      }
      statement.setBoolean(parameterIndex, booleanValue);
    }
    return parameterIndex + 1;
  }
}
