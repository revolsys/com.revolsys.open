package com.revolsys.geometry.cs;

import java.util.Collection;
import java.util.Map;

import com.revolsys.geometry.cs.unit.UnitOfMeasure;

public interface ParameterName extends Comparable<ParameterName> {
  void addNames(Collection<ParameterName> names);

  @Override
  default int compareTo(final ParameterName parameterName) {
    return this.getName().compareTo(parameterName.getName());
  }

  default ParameterValue getDefaultValue() {
    return null;
  }

  int getId();

  String getName();

  UnitOfMeasure getUnitOfMeasure();

  <V> V getValue(Map<ParameterName, Object> parameters);

  default ParameterValueNumber newParameterValue(final double value) {
    final UnitOfMeasure unitOfMeasure = getUnitOfMeasure();
    return new ParameterValueNumber(unitOfMeasure, value);
  }
}
