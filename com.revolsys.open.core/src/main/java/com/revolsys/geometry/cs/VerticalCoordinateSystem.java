package com.revolsys.geometry.cs;

import com.revolsys.record.code.Code;

public interface VerticalCoordinateSystem extends Code {

  @SuppressWarnings("unchecked")
  @Override
  default <C> C getCode() {
    return (C)(Integer)getCoordinateSystemId();
  }

  int getCoordinateSystemId();

  String getCoordinateSystemName();

  String getDatumName();

  @Override
  default String getDescription() {
    return getCoordinateSystemName();
  }

  @Override
  default Integer getInteger(final int index) {
    if (index == 0) {
      return getCoordinateSystemId();
    } else {
      throw new ArrayIndexOutOfBoundsException(index);
    }
  }
}
