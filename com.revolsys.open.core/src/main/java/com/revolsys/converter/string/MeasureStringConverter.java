package com.revolsys.converter.string;

import java.math.BigDecimal;

import javax.measure.Measure;
import javax.measure.unit.NonSI;

import org.springframework.util.StringUtils;

public class MeasureStringConverter implements StringConverter<Measure> {
  @Override
  public Class<Measure> getConvertedClass() {
    return Measure.class;
  }

  @Override
  public boolean requiresQuotes() {
    return true;
  }

  @Override
  public Measure toObject(final Object value) {
    if (value == null) {
      return null;
    } else if (value instanceof Measure) {
      return (Measure)value;
    } else if (value instanceof Number) {
      final Number number = (Number)value;
      return Measure.valueOf(new BigDecimal(number.toString()), NonSI.PIXEL);
    } else {
      return toObject(value.toString());
    }
  }

  @Override
  public Measure toObject(final String string) {
    if (StringUtils.hasText(string)) {
      return Measure.valueOf(string);
    } else {
      return null;
    }
  }

  @Override
  public String toString(final Object value) {
    if (value == null) {
      return null;
    } else {
      return value.toString();
    }
  }

}
