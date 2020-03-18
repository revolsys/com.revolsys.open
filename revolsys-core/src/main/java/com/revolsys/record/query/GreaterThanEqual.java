package com.revolsys.record.query;

import org.jeometry.common.compare.CompareUtil;

import com.revolsys.record.Record;

public class GreaterThanEqual extends BinaryCondition {

  public GreaterThanEqual(final QueryValue left, final QueryValue right) {
    super(left, ">=", right);
  }

  @Override
  public GreaterThanEqual clone() {
    return (GreaterThanEqual)super.clone();
  }

  @Override
  public boolean test(final Record record) {
    final QueryValue left = getLeft();
    final Object value1 = left.getValue(record);

    final QueryValue right = getRight();
    final Object value2 = right.getValue(record);

    return CompareUtil.compare(value1, value2) >= 0;
  }

}
