package com.revolsys.data.query;

import java.sql.PreparedStatement;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.revolsys.converter.string.StringConverterRegistry;
import com.revolsys.data.equals.EqualsRegistry;
import com.revolsys.data.io.RecordStore;
import com.revolsys.data.record.schema.Attribute;

public class In extends Condition {

  private QueryValue left;

  private CollectionValue values;

  public In(final Attribute attribute, final Collection<? extends Object> values) {
    this(attribute.getName(), new CollectionValue(attribute, values));
  }

  public In(final QueryValue left, final CollectionValue values) {
    this.left = left;
    if (left instanceof Column) {
      final Column column = (Column)left;
      final Attribute attribute = column.getAttribute();
      if (attribute != null) {
        values.setAttribute(attribute);
      }
    }
    this.values = values;
  }

  public In(final String name, final Collection<? extends Object> values) {
    this(new Column(name), new CollectionValue(values));
  }

  public In(final String name, final CollectionValue values) {
    this(new Column(name), values);
  }

  public In(final String name, final Object... values) {
    this(name, Arrays.asList(values));
  }

  @Override
  public boolean accept(final Map<String, Object> record) {
    final QueryValue left = getLeft();
    final Object value = left.getValue(record);

    final CollectionValue right = getValues();
    final List<Object> allowedValues = right.getValues();

    for (final Object allowedValue : allowedValues) {
      if (EqualsRegistry.equal(value, allowedValue)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public void appendDefaultSql(Query query,
    final RecordStore dataStore, final StringBuffer buffer) {
    if (left == null) {
      buffer.append("NULL");
    } else {
      left.appendSql(query, dataStore, buffer);
    }
    buffer.append(" ");
    buffer.append(" IN ");
    values.appendSql(query, dataStore, buffer);
  }

  @Override
  public int appendParameters(int index, final PreparedStatement statement) {
    if (left != null) {
      index = left.appendParameters(index, statement);
    }
    if (values != null) {
      index = values.appendParameters(index, statement);
    }
    return index;
  }

  @Override
  public In clone() {
    final In clone = (In)super.clone();
    clone.left = left.clone();
    clone.values = values.clone();
    return clone;
  }

  @Override
  public boolean equals(final Object obj) {
    if (obj instanceof In) {
      final In in = (In)obj;
      if (EqualsRegistry.equal(in.getLeft(), this.getLeft())) {
        if (EqualsRegistry.equal(in.getValues(), this.getValues())) {
          return true;
        }
      }
    }
    return false;
  }

  @SuppressWarnings("unchecked")
  public <V extends QueryValue> V getLeft() {
    return (V)left;
  }

  @Override
  public List<QueryValue> getQueryValues() {
    return Arrays.asList(left, values);
  }

  public CollectionValue getValues() {
    return values;
  }

  @Override
  public String toString() {
    return StringConverterRegistry.toString(left) + " IN "
      + StringConverterRegistry.toString(values);
  }
}
