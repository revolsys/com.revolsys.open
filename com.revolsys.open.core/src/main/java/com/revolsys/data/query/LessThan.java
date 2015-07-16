package com.revolsys.data.query;

import java.util.Map;

import com.revolsys.util.CompareUtil;

public class LessThan extends BinaryCondition {

  public LessThan(final QueryValue left, final QueryValue right) {
    super(left, "<", right);
  }

  @Override
  public boolean test(final Map<String, Object> record) {
    final QueryValue left = getLeft();
    final Object value1 = left.getValue(record);

    final QueryValue right = getRight();
    final Object value2 = right.getValue(record);

    return CompareUtil.compare(value1, value2) < 0;
  }

  @Override
  public LessThan clone() {
    return (LessThan)super.clone();
  }

}
