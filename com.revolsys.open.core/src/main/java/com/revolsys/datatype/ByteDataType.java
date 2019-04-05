package com.revolsys.datatype;

public class ByteDataType extends AbstractDataType {

  public ByteDataType() {
    super("byte", Byte.class, false);
  }

  @Override
  protected boolean equalsNotNull(final Object value1, final Object value2) {
    return (byte)value1 == (byte)value2;
  }

  @Override
  protected Object toObjectDo(final Object value) {
    final String string = DataTypes.toString(value);
    return Byte.valueOf(string);
  }

  @Override
  protected String toStringDo(final Object value) {
    return String.valueOf((byte)value);
  }
}
