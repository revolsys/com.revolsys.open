package com.revolsys.jdbc.attribute;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import javax.xml.namespace.QName;

import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.types.DataTypes;

public class JdbcByteAttribute extends JdbcAttribute {
  public JdbcByteAttribute(final String name, final int sqlType,
    final int length, final boolean required,
    final Map<QName, Object> properties) {
    super(name, DataTypes.BYTE, sqlType, length, 0, required, properties);
  }

  @Override
  public JdbcByteAttribute clone() {
    return new JdbcByteAttribute(getName(), getSqlType(), getLength(),
      isRequired(), getProperties());
  }

  @Override
  public int setAttributeValueFromResultSet(
    final ResultSet resultSet,
    final int columnIndex,
    final DataObject object) throws SQLException {
    final byte longValue = resultSet.getByte(columnIndex);
    if (!resultSet.wasNull()) {
      object.setValue(getIndex(), Byte.valueOf(longValue));
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
      byte numberValue;
      if (value instanceof Number) {
        final Number number = (Number)value;
        numberValue = number.byteValue();
      } else {
        numberValue = Byte.parseByte(value.toString());
      }
      statement.setByte(parameterIndex, numberValue);
    }
    return parameterIndex + 1;
  }
}
