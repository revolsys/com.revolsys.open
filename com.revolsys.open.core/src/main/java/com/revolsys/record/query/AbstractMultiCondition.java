package com.revolsys.record.query;

import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.revolsys.datatype.DataType;
import com.revolsys.record.schema.RecordStore;

public abstract class AbstractMultiCondition extends Condition {

  private final String operator;

  private List<QueryValue> values = new ArrayList<>();

  public AbstractMultiCondition(final String operator,
    final Iterable<? extends Condition> conditions) {
    this.operator = operator;
    if (conditions != null) {
      for (final Condition condition : conditions) {
        add(condition);
      }
    }
  }

  public boolean add(final Condition condition) {
    if (condition == null) {
      return false;
    } else {
      return this.values.add(condition);
    }
  }

  public void add(final String sql) {
    final SqlCondition value = new SqlCondition(sql);
    add(value);
  }

  @Override
  public void appendDefaultSql(final Query query, final RecordStore recordStore,
    final StringBuilder buffer) {
    buffer.append("(");
    boolean first = true;

    for (final QueryValue value : getQueryValues()) {
      if (first) {
        first = false;
      } else {
        buffer.append(" ");
        buffer.append(this.operator);
        buffer.append(" ");
      }
      if (value == null) {
        buffer.append("NULL");
      } else {
        value.appendSql(query, recordStore, buffer);
      }
    }
    buffer.append(")");
  }

  @Override
  public int appendParameters(int index, final PreparedStatement statement) {
    for (final QueryValue value : getQueryValues()) {
      if (value != null) {
        index = value.appendParameters(index, statement);
      }
    }
    return index;
  }

  public void clear() {
    this.values.clear();
  }

  @Override
  public AbstractMultiCondition clone() {
    final AbstractMultiCondition clone = (AbstractMultiCondition)super.clone();
    clone.values = QueryValue.cloneQueryValues(this.values);
    return clone;
  }

  @Override
  public boolean equals(final Object obj) {
    if (obj instanceof AbstractMultiCondition) {
      final AbstractMultiCondition value = (AbstractMultiCondition)obj;
      if (DataType.equal(getOperator(), value.getOperator())) {
        final List<QueryValue> values1 = getQueryValues();
        final List<QueryValue> values2 = value.getQueryValues();
        if (values1.size() == values2.size()) {
          for (int i = 0; i < values1.size(); i++) {
            final QueryValue value1 = values1.get(i);
            final QueryValue value2 = values2.get(i);
            if (!DataType.equal(value1, value2)) {
              return false;
            }
          }
          return true;
        }
      }
    }
    return false;
  }

  public String getOperator() {
    return this.operator;
  }

  @Override
  public List<QueryValue> getQueryValues() {
    return Collections.<QueryValue> unmodifiableList(this.values);

  }

  @Override
  public boolean isEmpty() {
    return this.values.isEmpty();
  }

  @Override
  public String toString() {
    final StringBuilder string = new StringBuilder();
    boolean first = true;
    for (final QueryValue value : getQueryValues()) {
      if (first) {
        first = false;
      } else {
        string.append(' ');
        string.append(this.operator);
        string.append(' ');
      }
      if (value instanceof Or && !(this instanceof Or)) {
        string.append('(');
        string.append(value);
        string.append(')');

      } else {
        string.append(value);
      }
    }
    return string.toString();
  }
}
