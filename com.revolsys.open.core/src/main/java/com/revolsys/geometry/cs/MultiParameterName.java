package com.revolsys.geometry.cs;

import java.util.Collection;
import java.util.Map;

import com.revolsys.geometry.cs.unit.UnitOfMeasure;

public class MultiParameterName implements ParameterName {

  private final ParameterName[] parameterNames;

  private Double defaultValue;

  public MultiParameterName(final Double defaultValue, final ParameterName... parameterNames) {
    this.defaultValue = defaultValue;
    this.parameterNames = parameterNames;
  }

  public MultiParameterName(final ParameterName... parameterNames) {
    this.parameterNames = parameterNames;
  }

  @Override
  public void addNames(final Collection<ParameterName> names) {
    for (final ParameterName parameterName : this.parameterNames) {
      parameterName.addNames(names);
    }
  }

  @Override
  public boolean equals(final Object object) {
    if (object instanceof SingleParameterName) {
      final SingleParameterName singleName = (SingleParameterName)object;
      for (final ParameterName parameterName : this.parameterNames) {
        if (singleName.equals(parameterName)) {
          return true;
        }
      }
    } else if (object instanceof MultiParameterName) {
      final MultiParameterName multiName = (MultiParameterName)object;
      for (final ParameterName parameterName : multiName.parameterNames) {
        if (equals(parameterName)) {
          return true;
        }
      }
    }
    return false;
  }

  @Override
  public ParameterValue getDefaultValue() {
    final Double value = this.defaultValue;
    if (value == null) {
      return null;
    } else {
      return newParameterValue(value);
    }
  }

  @Override
  public int getId() {
    return this.parameterNames[0].getId();
  }

  @Override
  public String getName() {
    return this.parameterNames[0].getName();
  }

  @Override
  public UnitOfMeasure getUnitOfMeasure() {
    return this.parameterNames[0].getUnitOfMeasure();
  }

  @Override
  public ParameterValue getValue(final UnitOfMeasure measure,
    final Map<ParameterName, Double> values) {
    for (final ParameterName parameterName : this.parameterNames) {
      final ParameterValue value = parameterName.getValue(measure, values);
      if (value != null) {
        return value;
      }
    }
    return null;
  }

  @Override
  public int hashCode() {
    return getName().hashCode();
  }

  @Override
  public String toString() {
    return getName();
  }
}
