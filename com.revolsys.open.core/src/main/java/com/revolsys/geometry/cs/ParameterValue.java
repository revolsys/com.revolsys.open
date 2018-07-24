package com.revolsys.geometry.cs;

import java.security.MessageDigest;

public interface ParameterValue {

  <V> V getOriginalValue();

  <V> V getValue();

  boolean isSame(ParameterValue parameterValue);

  void updateDigest(MessageDigest digest);
}
