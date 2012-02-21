package com.revolsys.converter.string;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;

public abstract class AbstractNumberStringConverter<T> implements
  StringConverter<T> {

  public static final NumberFormat INTEGER_FORMAT = new DecimalFormat("0");

  public static final NumberFormat DECIMAL_FORMAT = new DecimalFormat(
    "0.##########################");

  private NumberFormat format;

  public AbstractNumberStringConverter(NumberFormat format) {
    super();
    this.format = format;
  }

  public String toString(final Object value) {
    if (value == null) {
      return null;
    } else if (value instanceof BigDecimal) {
      BigDecimal number = (BigDecimal)value;
      return number.toPlainString();
    } else if (value instanceof Number) {
      Number number = (Number)value;
      return format.format(number);
    } else {
      return value.toString();
    }
  }

}
