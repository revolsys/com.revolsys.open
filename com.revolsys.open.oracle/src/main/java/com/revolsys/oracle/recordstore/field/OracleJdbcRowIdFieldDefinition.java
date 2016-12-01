package com.revolsys.oracle.recordstore.field;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Collections;

import com.revolsys.datatype.DataTypes;
import com.revolsys.jdbc.field.JdbcFieldDefinition;
import com.revolsys.record.Record;

import oracle.sql.ROWID;

public class OracleJdbcRowIdFieldDefinition extends JdbcFieldDefinition {
  public OracleJdbcRowIdFieldDefinition() {
    super("rowid", "ORACLE_ROWID", DataTypes.STRING, Types.ROWID, 18, 0, true, "Row identifier",
      Collections.emptyMap());
  }

  @Override
  public void addColumnName(final StringBuilder sql, final String tablePrefix) {
    super.addColumnName(sql, tablePrefix);
    sql.append(" \"ORACLE_ROWID\"");
  }

  @Override
  public void addInsertStatementPlaceHolder(final StringBuilder sql, final boolean generateKeys) {
  }

  @Override
  public void addStatementPlaceHolder(final StringBuilder sql) {
    sql.append("chartorowid(");
    super.addStatementPlaceHolder(sql);
    sql.append(")");
  }

  @Override
  public int setFieldValueFromResultSet(final ResultSet resultSet, final int columnIndex,
    final Record record) throws SQLException {
    final ROWID rowId = (ROWID)resultSet.getRowId(columnIndex);
    if (rowId == null) {
      setValue(record, null);
    } else {
      setValue(record, rowId.stringValue());
    }
    return columnIndex + 1;
  }

  @Override
  public int setInsertPreparedStatementValue(final PreparedStatement statement,
    final int parameterIndex, final Record record) throws SQLException {
    return parameterIndex;
  }

  @Override
  public int setPreparedStatementValue(final PreparedStatement statement, final int parameterIndex,
    final Object value) throws SQLException {
    if (value == null) {
      final int sqlType = getSqlType();
      statement.setNull(parameterIndex, sqlType);
    } else {
      final String string = value.toString();
      statement.setString(parameterIndex, string);
    }
    return parameterIndex + 1;
  }
}
