package com.revolsys.record.code;

import java.util.Collections;
import java.util.List;

import com.revolsys.identifier.Identifier;
import com.revolsys.util.Describable;

public interface Code extends Describable, Identifier {
  static String getCode(final Object value) {
    if (value == null) {
      return null;
    } else if (value instanceof Code) {
      final Code code = (Code)value;
      return code.getCode();
    } else {
      return value.toString();
    }
  }

  String getCode();

  @Override
  default List<Object> getValues() {
    return Collections.<Object> singletonList(getCode());
  }
}
