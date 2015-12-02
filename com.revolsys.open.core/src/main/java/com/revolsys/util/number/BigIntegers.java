package com.revolsys.util.number;

import java.math.BigInteger;

import com.revolsys.datatype.AbstractDataType;
import com.revolsys.datatype.DataTypes;

public class BigIntegers extends AbstractDataType {
  public static String toString(final BigInteger number) {
    return number.toString();
  }

  public static BigInteger toValid(final Object value) {
    if (value == null) {
      return null;
    } else if (value instanceof BigInteger) {
      final BigInteger number = (BigInteger)value;
      return number;
    } else {
      final String string = DataTypes.toString(value);
      return new BigInteger(string);
    }
  }

  public BigIntegers() {
    super("integer", BigInteger.class, false);
  }

  @Override
  protected boolean equalsNotNull(final Object value1, final Object value2) {
    return equalsNotNull(value1, value2);
  }

  @Override
  protected Object toObjectDo(final Object value) {
    final String string = DataTypes.toString(value);
    final BigInteger integer = new BigInteger(string);
    return integer;
  }

  @Override
  protected String toStringDo(final Object value) {
    return ((BigInteger)value).toString();
  }
}
