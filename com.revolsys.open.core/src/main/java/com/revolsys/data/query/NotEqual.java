package com.revolsys.data.query;

import java.util.Map;

import com.revolsys.data.equals.EqualsRegistry;

public class NotEqual extends BinaryCondition {

  public NotEqual(final QueryValue left, final QueryValue right) {
    super(left, "<>", right);
  }

  @Override
  public boolean test(final Map<String, Object> record) {
    final QueryValue left = getLeft();
    final Object value1 = left.getValue(record);

    final QueryValue right = getRight();
    final Object value2 = right.getValue(record);

    return !EqualsRegistry.equal(value1, value2);
  }

  @Override
  public NotEqual clone() {
    return (NotEqual)super.clone();
  }

}
