package com.revolsys.jdbc.field;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import com.revolsys.datatype.DataTypes;
import com.revolsys.record.Record;
import com.revolsys.util.Dates;
import com.revolsys.util.Property;

public class JdbcDateFieldDefinition extends JdbcFieldDefinition {
  public JdbcDateFieldDefinition(final String dbName, final String name, final int sqlType,
    final boolean required, final String description, final Map<String, Object> properties) {
    super(dbName, name, DataTypes.SQL_DATE, sqlType, 0, 0, required, description, properties);
  }

  @Override
  public JdbcDateFieldDefinition clone() {
    return new JdbcDateFieldDefinition(getDbName(), getName(), getSqlType(), isRequired(),
      getDescription(), getProperties());
  }

  @Override
  public int setFieldValueFromResultSet(final ResultSet resultSet, final int columnIndex,
    final Record record) throws SQLException {
    final Date value = resultSet.getDate(columnIndex);
    setValue(record, value);
    return columnIndex + 1;
  }

  @Override
  public int setPreparedStatementValue(final PreparedStatement statement, final int parameterIndex,
    final Object value) throws SQLException {
    if (Property.isEmpty(value)) {
      final int sqlType = getSqlType();
      statement.setNull(parameterIndex, sqlType);
    } else {
      final Date date = Dates.getSqlDate(value);
      statement.setDate(parameterIndex, date);
    }
    return parameterIndex + 1;
  }
}
