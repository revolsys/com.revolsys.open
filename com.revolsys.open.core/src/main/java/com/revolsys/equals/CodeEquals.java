package com.revolsys.equals;

import java.util.Collection;

import com.revolsys.record.code.Code;
import com.revolsys.util.Strings;

public class CodeEquals implements Equals<Object> {
  public static String getCode(final Object object) {
    if (object == null) {
      return null;
    } else if (object instanceof Code) {
      final Code code = (Code)object;
      return code.getCode();
    } else {
      return object.toString();
    }
  }

  @Override
  public boolean equals(final Object object1, final Object object2,
    final Collection<String> exclude) {
    final String code1 = getCode(object1);
    final String code2 = getCode(object2);
    return Strings.equals(code1, code2);
  }
}
