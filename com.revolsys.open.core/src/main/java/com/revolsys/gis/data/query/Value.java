package com.revolsys.gis.data.query;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;

import com.revolsys.converter.string.StringConverterRegistry;
import com.revolsys.gis.data.model.Attribute;
import com.revolsys.gis.data.model.types.DataType;
import com.revolsys.gis.model.data.equals.EqualsRegistry;
import com.revolsys.jdbc.attribute.JdbcAttribute;

public class Value extends AbstractCondition {
  private Object value;

  private JdbcAttribute jdbcAttribute;

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

  public void convert(final Attribute attribute) {
    if (attribute instanceof JdbcAttribute) {
      this.jdbcAttribute = (JdbcAttribute)attribute;
    }
    convert(attribute.getType());
  }

  public void convert(final DataType dataType) {
    if (value != null) {
      final Object newValue = StringConverterRegistry.toObject(dataType, value);
      final Class<?> typeClass = dataType.getJavaClass();
      if (newValue == null || !typeClass.isAssignableFrom(newValue.getClass())) {
        throw new IllegalArgumentException(value + " is not a valid "
          + typeClass);
      } else {
        this.value = newValue;
      }
    }
  }

  @Override
  public boolean equals(final Object obj) {
    if (obj instanceof Value) {
      final Value value = (Value)obj;
      return EqualsRegistry.equal(value.getValue(), this.getValue());
    } else {
      return false;
    }
  }

  public Object getValue() {
    return value;
  }

  public void setValue(final Object value) {
    this.value = value;
  }

  @Override
  public String toString() {
    if (value instanceof String) {
      final String string = (String)value;
      return "'" + string.replaceAll("'", "''") + "'";
    } else if (value instanceof Date) {
      final Date date = (Date)value;
      return "{d '" + date + "'}";
    } else if (value instanceof Time) {
      final Time time = (Time)value;
      return "{t '" + time + "'}";
    } else if (value instanceof Timestamp) {
      final Timestamp time = (Timestamp)value;
      return "{ts '" + time + "'}";
    } else {
      return StringConverterRegistry.toString(value);
    }
  }

}
