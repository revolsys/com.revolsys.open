package com.revolsys.converter.string;

import java.math.BigInteger;

import com.revolsys.util.Property;

public class BigIntegerStringConverter extends AbstractNumberStringConverter<BigInteger> {
  public BigIntegerStringConverter() {
    super();
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
  public BigInteger objectToObject(final Object value) {
    if (value instanceof BigInteger) {
      final BigInteger number = (BigInteger)value;
      return number;
    } else if (value == null) {
      return null;
    } else {
      return stringToObject(value.toString());
    }
  }

  @Override
  public BigInteger stringToObject(final String string) {
    if (Property.hasValue(string)) {
      return new BigInteger(string);
    } else {
      return null;
    }
  }
}
