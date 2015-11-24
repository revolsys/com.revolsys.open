package com.revolsys.util.number;

import java.math.BigInteger;

import com.revolsys.datatype.DataTypes;

public interface BigIntegers {
  static BigInteger toValid(final Object value) {
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
}
