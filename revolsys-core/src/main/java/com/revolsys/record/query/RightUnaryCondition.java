package com.revolsys.record.query;

import java.io.IOException;

import org.jeometry.common.data.type.DataType;
import org.jeometry.common.exception.Exceptions;

import com.revolsys.record.schema.RecordStore;

public class RightUnaryCondition extends AbstractUnaryQueryValue implements Condition {

  private final String operator;

  public RightUnaryCondition(final QueryValue value, final String operator) {
    super(value);
    this.operator = operator;
  }

  @Override
  public void appendDefaultSql(final Query query, final RecordStore recordStore,
    final Appendable buffer) {
    super.appendDefaultSql(query, recordStore, buffer);
    try {
      buffer.append(" ");
      buffer.append(this.operator);
    } catch (final IOException e) {
      throw Exceptions.wrap(e);
    }
  }

  @Override
  public RightUnaryCondition clone() {
    return (RightUnaryCondition)super.clone();
  }

  @Override
  public RightUnaryCondition clone(final TableReference oldTable, final TableReference newTable) {
    return (RightUnaryCondition)super.clone(oldTable, newTable);
  }

  @Override
  public boolean equals(final Object obj) {
    if (obj instanceof RightUnaryCondition) {
      final RightUnaryCondition condition = (RightUnaryCondition)obj;
      if (DataType.equal(condition.getValue(), this.getValue())) {
        if (DataType.equal(condition.getOperator(), getOperator())) {
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
  public String toString() {
    return getValue() + " " + getOperator();
  }
}
