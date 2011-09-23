package com.revolsys.gis.jdbc.attribute;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import javax.xml.namespace.QName;

import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.types.DataTypes;

public class JdbcDoubleAttribute extends JdbcAttribute {
  public JdbcDoubleAttribute(
    final String name,
    final int sqlType,
    final int length,
    final boolean required,
    final Map<QName, Object> properties) {
    super(name, DataTypes.DOUBLE, sqlType, length, 0, required, properties);
  }

  @Override
  public JdbcDoubleAttribute clone() {
    return new JdbcDoubleAttribute(getName(), getSqlType(), getLength(), isRequired(), getProperties());
  }
  
  @Override
  public int setAttributeValueFromResultSet(
    final ResultSet resultSet,
    final int columnIndex,
    final DataObject object)
    throws SQLException {
    final double longValue = resultSet.getDouble(columnIndex);
    if (!resultSet.wasNull()) {
      object.setValue(getIndex(), Double.valueOf(longValue));
    }
    return columnIndex + 1;
  }

  @Override
  public int setPreparedStatementValue(
    final PreparedStatement statement,
    final int parameterIndex,
    final Object value)
    throws SQLException {
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
