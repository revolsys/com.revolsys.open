package com.revolsys.swing.table.lambda;

import org.jdesktop.swingx.renderer.StringValue;

public interface LambdaStringValue<R> extends StringValue {

  @Override
  @SuppressWarnings("unchecked")
  default String getString(final Object value) {
    if (value == null) {
      return null;
    } else {
      return toString((R)value);
    }
  }

  String toString(R value);

}
