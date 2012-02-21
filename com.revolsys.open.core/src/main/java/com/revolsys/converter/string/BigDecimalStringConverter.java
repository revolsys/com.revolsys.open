package com.revolsys.converter.string;

import java.math.BigDecimal;

import org.springframework.util.StringUtils;

public class BigDecimalStringConverter extends
  AbstractNumberStringConverter<BigDecimal> {
  public BigDecimalStringConverter() {
    super(DECIMAL_FORMAT);
  }

  public Class<BigDecimal> getConvertedClass() {
    return BigDecimal.class;
  }

  public boolean requiresQuotes() {
    return false;
  }

  public BigDecimal toObject(final Object value) {
    if (value instanceof BigDecimal) {
      final BigDecimal number = (BigDecimal)value;
      return number;
    } else if (value == null) {
      return null;
    } else {
      return toObject(value.toString());
    }
  }

  public BigDecimal toObject(final String string) {
    if (StringUtils.hasText(string)) {
      return new BigDecimal(string);
    } else {
      return null;
    }
  }

}
