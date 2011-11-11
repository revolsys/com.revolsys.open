package com.revolsys.converter.string;

import java.math.BigInteger;

import org.springframework.util.StringUtils;

public class BigIntegerStringConverter implements StringConverter<BigInteger> {
  public boolean requiresQuotes() {
    return false;
  }

  public String toString(BigInteger number) {
    return number.toString();
  }

  public BigInteger toObject(Object value) {
    if (value instanceof BigInteger) {
      BigInteger number = (BigInteger)value;
      return number;
    } else if (value == null) {
      return null;
    } else {
      return toObject(value.toString());
    }
  }

  public BigInteger toObject(String string) {
    if (StringUtils.hasText(string)) {
      return new BigInteger(string);
    } else {
      return null;
    }
  }

}
