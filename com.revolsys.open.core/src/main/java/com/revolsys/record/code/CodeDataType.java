package com.revolsys.record.code;

import org.jeometry.common.data.identifier.Code;
import org.jeometry.common.data.type.AbstractDataType;

import com.revolsys.util.Strings;

public class CodeDataType extends AbstractDataType {
  public CodeDataType() {
    super("code", Code.class, true);
  }

  @Override
  public boolean equals(final Object value1, final Object value2) {
    final String code1 = Code.getCode(value1);
    final String code2 = Code.getCode(value2);
    return Strings.equals(code1, code2);
  }
}
