package com.revolsys.gis.jdbc.attribute;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import javax.xml.namespace.QName;

import com.revolsys.gis.data.model.Attribute;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.types.DataType;

public class JdbcAttribute extends Attribute {
  private int sqlType;

  public JdbcAttribute() {
  }

  public JdbcAttribute(
    final String name,
    final DataType type,
    final int sqlType,
    final int length,
    final int scale,
    final boolean required,
    final Map<QName, Object> properties) {
    super(name, type, length, scale, required, properties);
    this.sqlType = sqlType;
  }

  @Override
  protected JdbcAttribute clone() {
    return new JdbcAttribute(getName(), getType(), getSqlType(), getLength(),
      getScale(), isRequired(), getProperties());
  }

  public void addInsertStatementPlaceHolder(
    final StringBuffer sql,
    final boolean generateKeys) {
    addStatementPlaceHolder(sql);
  }

  public void addSelectStatementPlaceHolder(
    final StringBuffer sql) {
    addStatementPlaceHolder(sql);
  }

  public void addStatementPlaceHolder(
    final StringBuffer sql) {
    sql.append('?');
  }

  public int getSqlType() {
    return sqlType;
  }

  public int setAttributeValueFromResultSet(
    final ResultSet resultSet,
    final int columnIndex,
    final DataObject object)
    throws SQLException {
    final Object value = resultSet.getObject(columnIndex);
    object.setValue(getIndex(), value);
    return columnIndex + 1;
  }

  public  int setInsertPreparedStatementValue(
    final PreparedStatement statement,
    final int parameterIndex,
    final DataObject object)
    throws SQLException {
    final String name = getName();
    final Object value = object.getValue(name);
    return setPreparedStatementValue(statement, parameterIndex, value);
  }

  public int setPreparedStatementValue(
    final PreparedStatement statement,
    final int parameterIndex,
    final Object value)
    throws SQLException {
    final int sqlType = getSqlType();
    statement.setObject(parameterIndex, value);
    return parameterIndex + 1;
  }

  public void setSqlType(
    final int sqlType) {
    this.sqlType = sqlType;
  }
}
