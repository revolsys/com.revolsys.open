package com.revolsys.gis.data.query;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.revolsys.converter.string.StringConverterRegistry;
import com.revolsys.gis.data.model.Attribute;
import com.revolsys.jdbc.attribute.JdbcAttribute;

public class Value extends AbstractCondition {
  private Object value;

  private final JdbcAttribute jdbcAttribute;

  public Value(final Attribute attribute, final Object value) {
    if (attribute instanceof JdbcAttribute) {
      jdbcAttribute = (JdbcAttribute)attribute;
    } else {
      jdbcAttribute = JdbcAttribute.createAttribute(value);
    }
    this.value = value;
  }

  public Value(final Object value) {
    this(JdbcAttribute.createAttribute(value), value);
  }

  @Override
  public int appendParameters(final int index, final PreparedStatement statement) {
    try {
      return jdbcAttribute.setPreparedStatementValue(statement, index, value);
    } catch (final SQLException e) {
      throw new RuntimeException("Unable to set value: " + value, e);
    }
  }

  @Override
  public void appendSql(final StringBuffer buffer) {
    buffer.append('?');
  }

  @Override
  public Value clone() {
    return new Value(jdbcAttribute, value);
  }

  public Object getValue() {
    return value;
  }

  public void setValue(final Object value) {
    this.value = value;
  }

  @Override
  public String toString() {
    return StringConverterRegistry.toString(value);
  }
}
