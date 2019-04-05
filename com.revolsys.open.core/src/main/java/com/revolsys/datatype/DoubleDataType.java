package com.revolsys.datatype;

import org.jeometry.common.number.Doubles;

public class DoubleDataType extends AbstractDataType {

  public DoubleDataType() {
    super("double", Double.class, false);
  }

  @Override
  protected boolean equalsNotNull(final Object value1, final Object value2) {
    if (Double.compare((double)value1, (double)value2) == 0) {
      return true;
    } else {
      return false;
    }
  }

  @Override
  protected Object toObjectDo(final Object value) {
    if (value instanceof Number) {
      final Number number = (Number)value;
      return number.doubleValue();
    } else {
      final String string = DataTypes.toString(value);
      if (string == null || string.length() == 0) {
        return null;
      } else {
        return Double.valueOf(string);
      }
    }
  }

  @Override
  protected String toStringDo(final Object value) {
    return Doubles.toString((double)value);
  }
}
