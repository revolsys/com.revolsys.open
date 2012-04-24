package com.revolsys.jdbc.attribute;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import javax.xml.namespace.QName;

import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.types.DataTypes;

public class JdbcIntegerAttribute extends JdbcAttribute {
  public JdbcIntegerAttribute(final String name, final int sqlType,
    final int length, final boolean required,
    final Map<String, Object> properties) {
    super(name, DataTypes.INT, sqlType, length, 0, required, properties);
  }

  @Override
  public JdbcIntegerAttribute clone() {
    return new JdbcIntegerAttribute(getName(), getSqlType(), getLength(),
      isRequired(), getProperties());
  }

  @Override
  public int setAttributeValueFromResultSet(
    final ResultSet resultSet,
    final int columnIndex,
    final DataObject object) throws SQLException {
    final int value = resultSet.getInt(columnIndex);
    if (!resultSet.wasNull()) {
      object.setValue(getIndex(), Integer.valueOf(value));
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
      int numberValue;
      if (value instanceof Number) {
        final Number number = (Number)value;
        numberValue = number.intValue();
      } else {
        numberValue = Integer.parseInt(value.toString());
      }
      statement.setInt(parameterIndex, numberValue);
    }
    return parameterIndex + 1;
  }
}
