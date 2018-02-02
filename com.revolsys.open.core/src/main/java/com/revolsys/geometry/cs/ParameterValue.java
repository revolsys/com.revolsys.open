package com.revolsys.geometry.cs;

public interface ParameterValue {

  <V> V getOriginalValue();

  <V> V getValue();
}
