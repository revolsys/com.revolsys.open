package com.revolsys.geometry.cs;

public class ParameterValueString implements ParameterValue {
  private final String value;

  public ParameterValueString(final String value) {
    this.value = value;
  }

  @SuppressWarnings("unchecked")
  @Override
  public <V> V getValue() {
    return (V)this.value;
  }

  @Override
  public String toString() {
    return this.value;
  }
}
