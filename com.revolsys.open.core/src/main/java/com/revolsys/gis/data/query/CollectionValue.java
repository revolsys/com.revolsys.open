package com.revolsys.gis.data.query;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.revolsys.gis.data.model.Attribute;
import com.revolsys.jdbc.attribute.JdbcAttribute;

public class CollectionValue extends AbstractCondition {
  private final List<Object> values;

  private final JdbcAttribute jdbcAttribute;

  public CollectionValue(final Attribute attribute,
    final Collection<? extends Object> values) {
    this.values = new ArrayList<Object>(values);
    if (attribute instanceof JdbcAttribute) {
      jdbcAttribute = (JdbcAttribute)attribute;
    } else {
      jdbcAttribute = JdbcAttribute.createAttribute(this.values.get(0));
    }
  }

  public CollectionValue(final Collection<? extends Object> values) {
    this(null, values);
  }

  @Override
  public int appendParameters(int index, final PreparedStatement statement) {
    for (final Object value : values) {
      try {
        index = jdbcAttribute.setPreparedStatementValue(statement, index, value);
      } catch (final SQLException e) {
        throw new RuntimeException("Unable to set value: " + value, e);
      }
    }
    return index;
  }

  @Override
  public void appendSql(final StringBuffer buffer) {
    buffer.append('(');
    for (int i = 0; i < values.size(); i++) {
      if (i > 0) {
        buffer.append(", ");
      }
      buffer.append('?');
    }
    buffer.append(')');
  }

  @Override
  public CollectionValue clone() {
    return new CollectionValue(jdbcAttribute, values);
  }

  public List<Object> getValues() {
    return values;
  }

  @Override
  public String toString() {
    return values.toString();
  }
}
