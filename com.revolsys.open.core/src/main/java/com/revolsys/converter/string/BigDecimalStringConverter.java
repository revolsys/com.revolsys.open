package com.revolsys.converter.string;

import java.math.BigDecimal;

import org.springframework.util.StringUtils;

public class BigDecimalStringConverter implements StringConverter<BigDecimal> {
  public boolean requiresQuotes() {
    return false;
  }

  public String toString(BigDecimal number) {
    return number.toPlainString();
  }

  public BigDecimal toObject(Object value) {
    if (value instanceof BigDecimal) {
      BigDecimal number = (BigDecimal)value;
      return number;
    } else if (value == null) {
      return null;
    } else {
      return toObject(value.toString());
    }
  }

  public BigDecimal toObject(String string) {
    if (StringUtils.hasText(string)) {
      return new BigDecimal(string);
    } else {
      return null;
    }
  }

}
