package com.revolsys.record.code;

import com.revolsys.datatype.AbstractDataType;

public class CodeDataType extends AbstractDataType {
  public CodeDataType() {
    super("code", Code.class, true);
  }

  @Override
  public boolean equals(final Object value1, final Object value2) {
    final Object code1 = Code.getCode(value1);
    final Object code2 = Code.getCode(value2);
    if (code1 == null) {
      return code2 == null;
    } else {
      return code1.equals(code2);
    }
  }
}
