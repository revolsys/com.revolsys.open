package com.revolsys.swing.table;

import org.jdesktop.swingx.renderer.StringValue;
import org.jeometry.common.data.type.DataTypes;

public class StringConverterValue implements StringValue {

  private static final long serialVersionUID = 1L;

  @Override
  public String getString(final Object value) {
    if (value == null) {
      return "-";
    } else {
      return DataTypes.toString(value);
    }
  }
}
