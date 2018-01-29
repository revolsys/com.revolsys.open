package com.revolsys.geometry.cs;

import com.revolsys.geometry.cs.unit.UnitOfMeasure;
import com.revolsys.util.number.Doubles;

public class ParameterValueNumber extends Number implements ParameterValue {
  private final double unitValue;

  private final UnitOfMeasure unit;

  private final double value;

  public ParameterValueNumber(final UnitOfMeasure unit, final double unitValue) {
    this.unit = unit;
    this.unitValue = unitValue;
    if (unit == null) {
      this.value = unitValue;
    } else {
      this.value = unit.toNormal(unitValue);
    }
  }

  @Override
  public double doubleValue() {
    return this.value;
  }

  @Override
  public float floatValue() {
    return (float)this.value;
  }

  public UnitOfMeasure getUnit() {
    return this.unit;
  }

  public double getUnitValue() {
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
    return Doubles.toString(this.value);
  }
}
