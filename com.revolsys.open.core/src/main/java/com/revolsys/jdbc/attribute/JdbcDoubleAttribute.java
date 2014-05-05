package com.revolsys.jdbc.attribute;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.types.DataTypes;

public class JdbcDoubleAttribute extends JdbcAttribute {
  public JdbcDoubleAttribute(final String name, final int sqlType,
    final int length, final boolean required, final String description,
    final Map<String, Object> properties) {
    super(name, DataTypes.DOUBLE, sqlType, length, 0, required, description,
      properties);
  }

  @Override
  public JdbcDoubleAttribute clone() {
    return new JdbcDoubleAttribute(getName(), getSqlType(), getLength(),
      isRequired(), getDescription(), getProperties());
  }

  @Override
  public int setAttributeValueFromResultSet(final ResultSet resultSet,
    final int columnIndex, final DataObject object) throws SQLException {
    final double longValue = resultSet.getDouble(columnIndex);
    if (!resultSet.wasNull()) {
      setValue(object, Double.valueOf(longValue));
    }
    return columnIndex + 1;
  }

  @Override
  public int setPreparedStatementValue(final PreparedStatement statement,
    final int parameterIndex, final Object value) throws SQLException {
    if (value == null) {
      statement.setNull(parameterIndex, getSqlType());
    } else {
      double numberValue;
      if (value instanceof Number) {
        final Number number = (Number)value;
        numberValue = number.doubleValue();
      } else {
        numberValue = Double.parseDouble(value.toString());
      }
      statement.setDouble(parameterIndex, numberValue);

    }
    return parameterIndex + 1;
  }
}
