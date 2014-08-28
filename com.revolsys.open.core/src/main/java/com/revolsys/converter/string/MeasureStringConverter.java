package com.revolsys.converter.string;

import javax.measure.Measure;
import javax.measure.unit.NonSI;
import javax.measure.unit.Unit;

import com.revolsys.util.Property;

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
      return Measure.valueOf(number.doubleValue(), NonSI.PIXEL);
    } else {
      return toObject(value.toString());
    }
  }

  @Override
  public Measure toObject(final String string) {
    if (Property.hasValue(string)) {
      final Measure<?> measure = Measure.valueOf(string);
      final Number value = measure.getValue();
      final Unit<?> unit = measure.getUnit();
      return Measure.valueOf(value.doubleValue(), unit);
    } else {
      return null;
    }
  }

  @Override
  public String toString(final Object value) {
    if (value == null) {
      return null;
    } else if (value instanceof Measure) {
      final Measure<?> measure = (Measure<?>)value;
      final double doubleValue = measure.getValue().doubleValue();
      if (Double.isInfinite(doubleValue) || Double.isNaN(doubleValue)) {
        return String.valueOf(doubleValue) + " " + measure.getUnit();
      } else {
        return measure.toString();
      }
    } else {
      return value.toString();
    }
  }
}
