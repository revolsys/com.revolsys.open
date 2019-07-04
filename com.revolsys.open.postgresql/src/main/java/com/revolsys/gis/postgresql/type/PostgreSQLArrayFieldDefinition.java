package com.revolsys.gis.postgresql.type;

import java.sql.Array;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.jeometry.common.data.type.CollectionDataType;
import org.jeometry.common.data.type.DataType;

import com.revolsys.jdbc.field.JdbcFieldDefinition;

public class PostgreSQLArrayFieldDefinition extends JdbcFieldDefinition {

  private final DataType elementDataType;

  private final JdbcFieldDefinition elementField;

  public PostgreSQLArrayFieldDefinition(final String dbName, final String name,
    final CollectionDataType dataType, final int sqlType, final int length, final int scale,
    final boolean required, final String description, final JdbcFieldDefinition elementField,
    final Map<String, Object> properties) {
    super(dbName, name, dataType, sqlType, length, scale, required, description, properties);
    this.elementDataType = dataType.getContentType();
    this.elementField = elementField;
  }

  @Override
  public Object getValueFromResultSet(final ResultSet resultSet, final int columnIndex,
    final boolean internStrings) throws SQLException {
    final Object value = resultSet.getObject(columnIndex);
    if (value instanceof Array) {
      final Array array = (Array)value;
      final List<Object> values = new ArrayList<>();
      final ResultSet arrayResultSet = array.getResultSet();
      while (arrayResultSet.next()) {
        final Object elementValue = this.elementField.getValueFromResultSet(arrayResultSet, 2,
          internStrings);
        values.add(elementValue);
      }
      return values;
    }
    return value;
  }

  @Override
  public int setInsertPreparedStatementValue(final PreparedStatement statement,
    final int parameterIndex, final Object value) throws SQLException {
    statement.setObject(parameterIndex, value);
    return parameterIndex + 1;
  }

  @Override
  public int setPreparedStatementValue(final PreparedStatement statement, final int parameterIndex,
    final Object value) throws SQLException {
    return setInsertPreparedStatementValue(statement, parameterIndex, value);
  }

}
