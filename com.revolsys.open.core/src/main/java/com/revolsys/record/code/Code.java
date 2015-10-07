package com.revolsys.record.code;

import java.util.Collections;
import java.util.List;

import com.revolsys.identifier.Identifier;
import com.revolsys.util.Describable;

public interface Code extends Describable, Identifier {
  String getCode();

  @Override
  default List<Object> getValues() {
    return Collections.<Object> singletonList(getCode());
  }
}
