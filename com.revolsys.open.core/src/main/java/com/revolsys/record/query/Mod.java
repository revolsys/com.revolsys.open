package com.revolsys.record.query;

import java.math.BigDecimal;
import java.util.Map;

import com.revolsys.converter.string.StringConverterRegistry;

public class Mod extends BinaryArithmatic {

  public Mod(final QueryValue left, final QueryValue right) {
    super(left, "%", right);
  }

  @Override
  public Mod clone() {
    return (Mod)super.clone();
  }

  @Override
  public boolean equals(final Object obj) {
    if (obj instanceof Mod) {
      return super.equals(obj);
    }
    return false;
  }

  @Override
  public <V> V getValue(final Map<String, Object> record) {
    final Object leftValue = getLeft().getValue(record);
    final Object rightValue = getRight().getValue(record);
    if (leftValue instanceof Number && rightValue instanceof Number) {
      final BigDecimal number1 = StringConverterRegistry.toObject(BigDecimal.class, leftValue);
      final BigDecimal number2 = StringConverterRegistry.toObject(BigDecimal.class, rightValue);
      final BigDecimal result = number1.remainder(number2);
      return StringConverterRegistry.toObject(leftValue.getClass(), result);
    }
    return null;
  }
}
