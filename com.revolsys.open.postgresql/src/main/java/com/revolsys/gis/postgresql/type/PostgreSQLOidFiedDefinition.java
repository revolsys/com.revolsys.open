package com.revolsys.gis.postgresql.type;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Collections;

import com.revolsys.datatype.DataTypes;
import com.revolsys.jdbc.field.JdbcFieldDefinition;
import com.revolsys.record.Record;

public class PostgreSQLOidFiedDefinition extends JdbcFieldDefinition {

  public PostgreSQLOidFiedDefinition() {
    super("ctid", "ctid", DataTypes.LONG, Types.OTHER, 0, 0, true, "Row Identifier",
      Collections.emptyMap());
  }

  @Override
  public void addInsertStatementPlaceHolder(final StringBuilder sql, final boolean generateKeys) {
  }

  @Override
  public int setFieldValueFromResultSet(final ResultSet resultSet, final int columnIndex,
    final Record record) throws SQLException {
    Object value = resultSet.getObject(columnIndex);
    if (value instanceof PostgreSQLTidWrapper) {
      final PostgreSQLTidWrapper wrapper = (PostgreSQLTidWrapper)value;
      value = wrapper.getTid();
    }
    setValue(record, value);
    return columnIndex + 1;
  }

  @Override
  public int setInsertPreparedStatementValue(final PreparedStatement statement,
    final int parameterIndex, final Record record) throws SQLException {
    return parameterIndex;
  }

  @Override
  public int setPreparedStatementValue(final PreparedStatement statement, final int parameterIndex,
    Object value) throws SQLException {
    value = new PostgreSQLTidWrapper(value);
    statement.setObject(parameterIndex, value);
    return parameterIndex + 1;
  }
}
