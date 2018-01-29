package com.revolsys.geometry.cs;

import java.util.Collection;
import java.util.Map;

import com.revolsys.geometry.cs.unit.UnitOfMeasure;

public class SingleParameterName implements ParameterName {

  private final int id;

  private final String name;

  private final UnitOfMeasure unitOfMeasure;

  public SingleParameterName(final int id, final String name) {
    this(id, name, null);
  }

  public SingleParameterName(final int id, final String name, final UnitOfMeasure unitOfMeasure) {
    this.id = id;
    this.name = name;
    this.unitOfMeasure = unitOfMeasure;
    if (id > 0) {
      if (!ParameterNames._PARAMETER_BY_NAME.containsKey(name)) {
        ParameterNames._PARAMETER_BY_NAME.put(name, this);
      }
    }
  }

  public SingleParameterName(final String name) {
    this(0, name);
  }

  public SingleParameterName(final String name, final UnitOfMeasure unitOfMeasure) {
    this(0, name, unitOfMeasure);
  }

  @Override
  public void addNames(final Collection<ParameterName> names) {
    names.add(this);
  }

  @Override
  public boolean equals(final Object object) {
    if (object == this) {
      return true;
    } else if (object instanceof SingleParameterName) {
      final SingleParameterName name = (SingleParameterName)object;
      if (this.id == name.id) {
        return true;
      } else if (this.name.equalsIgnoreCase(name.name)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public String getName() {
    return this.name;
  }

  @Override
  public UnitOfMeasure getUnitOfMeasure() {
    return this.unitOfMeasure;
  }

  @Override
  public ParameterValue getValue(final UnitOfMeasure measure,
    final Map<ParameterName, Double> values) {
    Object value = values.get(this);
    if (value == null) {
      value = values.get(this);
    }
    if (value == null) {
      return null;
    } else {
      return newParameterValue(measure, (Double)value);
    }
  }

  @Override
  public int hashCode() {
    return this.name.hashCode();
  }

  @Override
  public String toString() {
    return this.name;
  }
}
