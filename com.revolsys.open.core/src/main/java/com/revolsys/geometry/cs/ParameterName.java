package com.revolsys.geometry.cs;

import java.security.MessageDigest;
import java.util.Collection;
import java.util.Map;

import com.revolsys.geometry.cs.unit.UnitOfMeasure;
import com.revolsys.util.Md5;

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

  default void updateDigest(final MessageDigest digest) {
    final String name = getName();
    Md5.update(digest, name.toLowerCase());
  }
}
