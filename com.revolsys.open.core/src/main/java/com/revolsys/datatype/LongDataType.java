package com.revolsys.datatype;

public class LongDataType extends AbstractDataType {

  public LongDataType() {
    super("long", Long.class, false);
  }

  @Override
  protected boolean equalsNotNull(final Object value1, final Object value2) {
    return (long)value1 == (long)value2;
  }

  @Override
  protected Object toObjectDo(final Object value) {
    if (value instanceof Number) {
      final Number number = (Number)value;
      return number.longValue();
    }
    final String string = DataTypes.toString(value);
    return Long.valueOf(string);
  }

  @Override
  protected String toStringDo(final Object value) {
    return String.valueOf((long)value);
  }

}
