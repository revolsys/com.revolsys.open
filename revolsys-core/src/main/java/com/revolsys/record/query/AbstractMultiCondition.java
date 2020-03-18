package com.revolsys.record.query;

import java.sql.PreparedStatement;

import org.jeometry.common.data.type.DataType;

import com.revolsys.record.schema.RecordStore;

public abstract class AbstractMultiCondition extends AbstractMultiQueryValue implements Condition {

  private final String operator;

  public AbstractMultiCondition(final String operator,
    final Iterable<? extends Condition> conditions) {
    super(conditions);
    this.operator = operator;
  }

  public boolean addCondition(final Condition condition) {
    if (condition == null) {
      return false;
    } else {
      return addValue(condition);
    }
  }

  public void addCondition(final String sql) {
    final SqlCondition value = new SqlCondition(sql);
    addCondition(value);
  }

  @Override
  public void appendDefaultSql(final Query query, final RecordStore recordStore,
    final StringBuilder buffer) {
    buffer.append("(");
    boolean first = true;

    for (final QueryValue value : this.values) {
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
    for (final QueryValue value : this.values) {
      if (value != null) {
        index = value.appendParameters(index, statement);
      }
    }
    return index;
  }

  @Override
  public AbstractMultiCondition clone() {
    final AbstractMultiCondition clone = (AbstractMultiCondition)super.clone();
    return clone;
  }

  @Override
  public boolean equals(final Object obj) {
    if (obj instanceof AbstractMultiCondition) {
      final AbstractMultiCondition multiCondition = (AbstractMultiCondition)obj;
      if (DataType.equal(getOperator(), multiCondition.getOperator())) {
        return super.equals(multiCondition);
      }
    }
    return false;
  }

  public String getOperator() {
    return this.operator;
  }

  @Override
  public String toString() {
    final StringBuilder string = new StringBuilder();
    boolean first = true;
    for (final QueryValue value : this.values) {
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
