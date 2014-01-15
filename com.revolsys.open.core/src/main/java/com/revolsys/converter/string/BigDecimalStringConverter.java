package com.revolsys.converter.string;

import java.math.BigDecimal;

import org.springframework.util.StringUtils;

public class BigDecimalStringConverter extends
  AbstractNumberStringConverter<BigDecimal> {

  public static boolean isNumber(final Object value) {
    if (value instanceof Number) {
      return true;
    } else {
      try {
        final Object number = StringConverterRegistry.toObject(
          BigDecimal.class, value);
        if (number instanceof Number) {
          return true;
        } else {
          return false;
        }

      } catch (final Throwable t) {
        return false;
      }
    }
  }

  public BigDecimalStringConverter() {
    super(getDecimalFormat());
  }

  @Override
  public Class<BigDecimal> getConvertedClass() {
    return BigDecimal.class;
  }

  @Override
  public boolean requiresQuotes() {
    return false;
  }

  @Override
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

  @Override
  public BigDecimal toObject(final String string) {
    if (StringUtils.hasText(string)) {
      return new BigDecimal(string);
    } else {
      return null;
    }
  }

}
