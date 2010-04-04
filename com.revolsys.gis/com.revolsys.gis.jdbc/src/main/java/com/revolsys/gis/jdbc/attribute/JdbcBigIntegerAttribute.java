package com.revolsys.gis.jdbc.attribute;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import javax.xml.namespace.QName;

import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.types.DataTypes;

public class JdbcBigIntegerAttribute extends JdbcAttribute {
  public JdbcBigIntegerAttribute(
    final String name,
    final int sqlType,
    final int length,
    final boolean required,
    final Map<QName, Object> properties) {
    super(name, DataTypes.INTEGER, sqlType, length, 0, required, properties);
  }

  @Override
  protected JdbcBigIntegerAttribute clone() {
    return new JdbcBigIntegerAttribute(getName(), getSqlType(), getLength(),
      isRequired(), getProperties());
  }

  @Override
  public int setAttributeValueFromResultSet(
    final ResultSet resultSet,
    final int columnIndex,
    final DataObject object)
    throws SQLException {
    final BigDecimal number = resultSet.getBigDecimal(columnIndex);
    if (number != null) {
      Object value = number;
      final int precision = number.precision();
      if (precision <= 2) {
        value = Byte.valueOf(number.byteValue());
      } else if (precision <= 4) {
        value = Short.valueOf(number.shortValue());
      } else if (precision <= 9) {
        value = Integer.valueOf(number.intValue());
      } else if (precision <= 18) {
        value = Long.valueOf(number.longValue());
      } else {
        value = number.toBigInteger();
      }
      object.setValue(getIndex(), value);
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
      if (value instanceof BigDecimal) {
        final BigDecimal number = (BigDecimal)value;
        statement.setBigDecimal(parameterIndex, number);
      } else if (value instanceof BigInteger) {
        final BigInteger number = (BigInteger)value;
        statement.setBigDecimal(parameterIndex, new BigDecimal(number));
      } else if (value instanceof Number) {
        final Number number = (Number)value;
        statement.setLong(parameterIndex, number.longValue());
      } else {
        final BigDecimal number = new BigDecimal(value.toString());
        statement.setBigDecimal(parameterIndex, number);
      }
    }
    return parameterIndex + 1;
  }
}
