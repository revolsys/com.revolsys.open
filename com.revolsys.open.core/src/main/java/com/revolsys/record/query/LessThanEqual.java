package com.revolsys.record.query;

import com.revolsys.record.Record;
import com.revolsys.util.CompareUtil;

public class LessThanEqual extends BinaryCondition {

  public LessThanEqual(final QueryValue left, final QueryValue right) {
    super(left, "<=", right);
  }

  @Override
  public LessThanEqual clone() {
    return (LessThanEqual)super.clone();
  }

  @Override
  public boolean test(final Record record) {
    final QueryValue left = getLeft();
    final Object value1 = left.getValue(record);

    final QueryValue right = getRight();
    final Object value2 = right.getValue(record);

    return CompareUtil.compare(value1, value2) <= 0;
  }

}
