package com.revolsys.jdbc.field;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import org.jeometry.common.data.type.DataType;

import com.revolsys.record.Record;
import com.revolsys.record.schema.FieldDefinition;

public class JdbcFieldDefinition extends FieldDefinition {
  private String dbName;

  private boolean quoteName = false;

  private int sqlType;

  JdbcFieldDefinition() {
    setName(JdbcFieldDefinitions.UNKNOWN);
  }

  public JdbcFieldDefinition(final String dbName, final String name, final DataType type,
    final int sqlType, final int length, final int scale, final boolean required,
    final String description, final Map<String, Object> properties) {
    super(name, type, length, scale, required, description, properties);
    this.dbName = dbName;
    this.sqlType = sqlType;
  }

  public void addInsertStatementPlaceHolder(final StringBuilder sql, final boolean generateKeys) {
    addStatementPlaceHolder(sql);
  }

  public void addSelectStatementPlaceHolder(final StringBuilder sql) {
    addStatementPlaceHolder(sql);
  }

  public void addStatementPlaceHolder(final StringBuilder sql) {
    sql.append('?');
  }

  @Override
  public void appendColumnName(final StringBuilder sql) {
    appendColumnName(sql, this.quoteName);
  }

  @Override
  public void appendColumnName(final StringBuilder sql, boolean quoteName) {
    quoteName |= this.quoteName;
    if (quoteName) {
      sql.append('"');
    }
    final String dbName = getDbName();
    sql.append(dbName);
    if (quoteName) {
      sql.append('"');
    }
  }

  @Override
  public JdbcFieldDefinition clone() {
    return new JdbcFieldDefinition(this.dbName, getName(), getDataType(), getSqlType(), getLength(),
      getScale(), isRequired(), getDescription(), getProperties());
  }

  public String getDbName() {
    return this.dbName;
  }

  public int getSqlType() {
    return this.sqlType;
  }

  public Object getValueFromResultSet(final ResultSet resultSet, final int columnIndex,
    final boolean internStrings) throws SQLException {
    return resultSet.getObject(columnIndex);
  }

  public boolean isQuoteName() {
    return this.quoteName;
  }

  public int setFieldValueFromResultSet(final ResultSet resultSet, final int columnIndex,
    final Record record, final boolean internStrings) throws SQLException {
    final Object value = getValueFromResultSet(resultSet, columnIndex, internStrings);
    final int index = getIndex();
    record.setValue(index, value);
    return columnIndex + 1;
  }

  public int setInsertPreparedStatementValue(final PreparedStatement statement,
    final int parameterIndex, final Object value) throws SQLException {
    return setPreparedStatementValue(statement, parameterIndex, value);
  }

  public int setInsertPreparedStatementValue(final PreparedStatement statement,
    final int parameterIndex, final Record record) throws SQLException {
    final String name = getName();
    final Object value = record.getValue(name);
    return setInsertPreparedStatementValue(statement, parameterIndex, value);
  }

  public int setPreparedStatementValue(final PreparedStatement statement, final int parameterIndex,
    final Object value) throws SQLException {
    if (value == null) {
      statement.setNull(parameterIndex, this.sqlType);
    } else {
      statement.setObject(parameterIndex, value);
    }
    return parameterIndex + 1;
  }

  public void setQuoteName(final boolean quoteName) {
    this.quoteName = quoteName;
  }

  public void setSqlType(final int sqlType) {
    this.sqlType = sqlType;
  }
}
