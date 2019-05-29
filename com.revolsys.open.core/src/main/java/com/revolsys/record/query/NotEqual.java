package com.revolsys.record.query;

import org.jeometry.common.data.type.DataType;

import com.revolsys.record.Record;

public class NotEqual extends BinaryCondition {

  public NotEqual(final QueryValue left, final QueryValue right) {
    super(left, "<>", right);
  }

  @Override
  public NotEqual clone() {
    return (NotEqual)super.clone();
  }

  @Override
  public boolean test(final Record record) {
    final QueryValue left = getLeft();
    final Object value1 = left.getValue(record);

    final QueryValue right = getRight();
    final Object value2 = right.getValue(record);

    return !DataType.equal(value1, value2);
  }

}
