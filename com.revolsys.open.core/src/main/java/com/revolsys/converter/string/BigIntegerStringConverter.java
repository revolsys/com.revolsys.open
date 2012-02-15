package com.revolsys.converter.string;

import java.math.BigInteger;

import org.springframework.util.StringUtils;

public class BigIntegerStringConverter implements StringConverter<BigInteger> {
  public Class<BigInteger> getConvertedClass() {
    return BigInteger.class;
  }

  public boolean requiresQuotes() {
    return false;
  }

  public BigInteger toObject(final Object value) {
    if (value instanceof BigInteger) {
      final BigInteger number = (BigInteger)value;
      return number;
    } else if (value == null) {
      return null;
    } else {
      return toObject(value.toString());
    }
  }

  public BigInteger toObject(final String string) {
    if (StringUtils.hasText(string)) {
      return new BigInteger(string);
    } else {
      return null;
    }
  }

  public String toString(final BigInteger number) {
    return number.toString();
  }

}
