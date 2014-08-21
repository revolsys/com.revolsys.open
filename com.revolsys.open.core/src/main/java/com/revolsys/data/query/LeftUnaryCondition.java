package com.revolsys.data.query;

import java.sql.PreparedStatement;
import java.util.Collections;
import java.util.List;

import com.revolsys.data.equals.EqualsRegistry;
import com.revolsys.data.record.schema.RecordStore;

public class LeftUnaryCondition extends Condition {

  private QueryValue value;

  private final String operator;

  public LeftUnaryCondition(final String operator, final QueryValue value) {
    this.operator = operator;
    this.value = value;
  }

  @Override
  public void appendDefaultSql(Query query,
    final RecordStore recordStore, final StringBuffer buffer) {
    buffer.append(operator);
    buffer.append(" ");
    value.appendSql(query, recordStore, buffer);
  }

  @Override
  public int appendParameters(final int index, final PreparedStatement statement) {
    return value.appendParameters(index, statement);
  }

  @Override
  public LeftUnaryCondition clone() {
    final LeftUnaryCondition clone = (LeftUnaryCondition)super.clone();
    clone.value = value.clone();
    return clone;
  }

  @Override
  public boolean equals(final Object obj) {
    if (obj instanceof LeftUnaryCondition) {
      final LeftUnaryCondition value = (LeftUnaryCondition)obj;
      if (EqualsRegistry.equal(value.getQueryValue(), this.getQueryValue())) {
        if (EqualsRegistry.equal(value.getOperator(), this.getOperator())) {
          return true;
        }
      }
    }
    return false;
  }

  public String getOperator() {
    return operator;
  }

  @SuppressWarnings("unchecked")
  public <V extends QueryValue> V getQueryValue() {
    return (V)value;
  }

  @Override
  public List<QueryValue> getQueryValues() {
    return Collections.singletonList(value);
  }

  @Override
  public String toString() {
    return operator + " " + value;
  }
}
