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

  ParameterValue getValue(final UnitOfMeasure measure, Map<ParameterName, Double> values);

  default ParameterValueNumber newParameterValue(final double value) {
    final UnitOfMeasure unitOfMeasure = getUnitOfMeasure();
    return new ParameterValueNumber(unitOfMeasure, value);
  }

  default ParameterValue newParameterValue(final UnitOfMeasure measure, final double value) {
    UnitOfMeasure unitOfMeasure = getUnitOfMeasure();
    if (unitOfMeasure != null) {
      if (unitOfMeasure.getType() == measure.getType()) {
        unitOfMeasure = measure;
      }
    }
    return new ParameterValueNumber(unitOfMeasure, value);
  }
}
