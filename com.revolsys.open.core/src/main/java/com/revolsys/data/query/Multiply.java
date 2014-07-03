package com.revolsys.data.query;

import java.math.BigDecimal;
import java.util.Map;

import com.revolsys.converter.string.StringConverterRegistry;

public class Multiply extends BinaryArithmatic {

  public Multiply(final QueryValue left, final QueryValue right) {
    super(left, "*", right);
  }

  @Override
  public Multiply clone() {
    return (Multiply)super.clone();
  }

  @Override
  public boolean equals(final Object obj) {
    if (obj instanceof Multiply) {
      return super.equals(obj);
    }
    return false;
  }

  @Override
  public <V> V getValue(final Map<String, Object> record) {
    final Object leftValue = getLeft().getValue(record);
    final Object rightValue = getRight().getValue(record);
    if (leftValue instanceof Number && rightValue instanceof Number) {
      final BigDecimal number1 = StringConverterRegistry.toObject(
        BigDecimal.class, leftValue);
      final BigDecimal number2 = StringConverterRegistry.toObject(
        BigDecimal.class, rightValue);
      final BigDecimal result = number1.multiply(number2);
      return StringConverterRegistry.toObject(leftValue.getClass(), result);
    }
    return null;
  }
}
