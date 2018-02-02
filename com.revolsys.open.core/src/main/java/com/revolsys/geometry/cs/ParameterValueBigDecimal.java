package com.revolsys.geometry.cs;

import java.math.BigDecimal;

public class ParameterValueBigDecimal extends Number implements ParameterValue {
  private final BigDecimal unitValue;

  private final double value;

  public ParameterValueBigDecimal(final BigDecimal unitValue) {
    this.unitValue = unitValue;
    this.value = unitValue.doubleValue();
  }

  public ParameterValueBigDecimal(final String text) {
    this(new BigDecimal(text));
  }

  @Override
  public double doubleValue() {
    return this.value;
  }

  @Override
  public float floatValue() {
    return (float)this.value;
  }

  @SuppressWarnings("unchecked")
  @Override
  public <V> V getOriginalValue() {
    return (V)this.unitValue;
  }

  public BigDecimal getUnitValue() {
    return this.unitValue;
  }

  @SuppressWarnings("unchecked")
  @Override
  public <V> V getValue() {
    return (V)(Double)this.value;
  }

  @Override
  public int intValue() {
    return (int)this.value;
  }

  @Override
  public long longValue() {
    return (long)this.value;
  }

  @Override
  public String toString() {
    return this.unitValue.toString();
  }
}
