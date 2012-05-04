package com.revolsys.jdbc.attribute;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.types.DataTypes;

public class JdbcStringAttribute extends JdbcAttribute {
  public JdbcStringAttribute(final String name, final int sqlType,
    final int length, final boolean required,
    final Map<String, Object> properties) {
    super(name, DataTypes.STRING, sqlType, length, 0, required, properties);
  }

  @Override
  public JdbcStringAttribute clone() {
    return new JdbcStringAttribute(getName(), getSqlType(), getLength(),
      isRequired(), getProperties());
  }

  @Override
  public int setAttributeValueFromResultSet(
    final ResultSet resultSet,
    final int columnIndex,
    final DataObject object) throws SQLException {
    final String value = resultSet.getString(columnIndex);
    object.setValue(getIndex(), value);
    return columnIndex + 1;
  }

  @Override
  public int setPreparedStatementValue(
    final PreparedStatement statement,
    final int parameterIndex,
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
