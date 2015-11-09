package com.revolsys.converter.string;

import com.revolsys.util.Numbers;

public class IntegerStringConverter extends AbstractNumberStringConverter<Integer> {
  public IntegerStringConverter() {
    super();
  }

  @Override
  public Class<Integer> getConvertedClass() {
    return Integer.class;
  }

  @Override
  public boolean requiresQuotes() {
    return false;
  }

  @Override
  public Integer objectToObject(final Object value) {
    return Numbers.toInteger(value);
  }

  @Override
  public Integer stringToObject(final String string) {
    return Numbers.toInteger(string);
  }
}
