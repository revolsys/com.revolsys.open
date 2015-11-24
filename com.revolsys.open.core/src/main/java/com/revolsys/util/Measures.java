package com.revolsys.util;

import javax.measure.Measure;
import javax.measure.unit.NonSI;
import javax.measure.unit.Unit;

import com.revolsys.datatype.DataTypes;

public interface Measures {
  static Measure<?> newMeasure(final Object value) {
    if (value == null) {
      return null;
    } else if (value instanceof Measure) {
      return (Measure<?>)value;
    } else if (value instanceof Number) {
      final Number number = (Number)value;
      return Measure.valueOf(number.doubleValue(), NonSI.PIXEL);
    } else {
      final String string = DataTypes.toString(value);
      return newMeasure(string);
    }
  }

  static Measure<?> newMeasure(final String string) {
    if (Property.hasValue(string)) {
      final Measure<?> measure = Measure.valueOf(string);
      final Number value = measure.getValue();
      final Unit<?> unit = measure.getUnit();
      return Measure.valueOf(value.doubleValue(), unit);
    } else {
      return null;
    }
  }

  static String toString(final Object value) {
    if (value == null) {
      return null;
    } else {
      final Measure<?> measure = newMeasure(value);
      final double doubleValue = measure.getValue().doubleValue();
      if (Double.isInfinite(doubleValue) || Double.isNaN(doubleValue)) {
        return String.valueOf(doubleValue) + " " + measure.getUnit();
      } else {
        return measure.toString();
      }
    }
  }
}
