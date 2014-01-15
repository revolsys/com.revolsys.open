package com.revolsys.converter.string;

import java.math.BigInteger;

import org.springframework.util.StringUtils;

public class BigIntegerStringConverter extends
  AbstractNumberStringConverter<BigInteger> {
  public BigIntegerStringConverter() {
    super(getIntegerFormat());
  }

  @Override
  public Class<BigInteger> getConvertedClass() {
    return BigInteger.class;
  }

  @Override
  public boolean requiresQuotes() {
    return false;
  }

  @Override
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

  @Override
  public BigInteger toObject(final String string) {
    if (StringUtils.hasText(string)) {
      return new BigInteger(string);
    } else {
      return null;
    }
  }
}
