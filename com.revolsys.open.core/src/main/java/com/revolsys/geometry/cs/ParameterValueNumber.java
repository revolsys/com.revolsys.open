package com.revolsys.geometry.cs;

import java.security.MessageDigest;

import com.revolsys.geometry.cs.unit.UnitOfMeasure;
import com.revolsys.util.Debug;
import com.revolsys.util.Md5;
import com.revolsys.util.number.Doubles;

public class ParameterValueNumber extends Number implements ParameterValue {
  private final double unitValue;

  private final UnitOfMeasure unit;

  private final double value;

  public ParameterValueNumber(final double unitValue) {
    this(null, unitValue);
  }

  public ParameterValueNumber(final UnitOfMeasure unit, final double unitValue) {
    this.unit = unit;
    this.unitValue = unitValue;
    if (unit == null) {
      this.value = unitValue;
    } else {
      this.value = unit.toNormal(unitValue);
    }
    if (this.value == -12600) {
      Debug.noOp();
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

  @SuppressWarnings("unchecked")
  @Override
  public <V> V getOriginalValue() {
    return (V)(Double)this.unitValue;
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
  public boolean isSame(final ParameterValue parameterValue) {
    if (parameterValue instanceof ParameterValueNumber) {
      final ParameterValueNumber numberValue = (ParameterValueNumber)parameterValue;
      return this.value == numberValue.value;
    }
    return false;
  }

  @Override
  public long longValue() {
    return (long)this.value;
  }

  @Override
  public String toString() {
    return Doubles.toString(this.value);
  }

  @Override
  public void updateDigest(final MessageDigest digest) {
    Md5.update(digest, Math.floor(1e6 * this.value));
  }
}
