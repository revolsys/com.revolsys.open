package com.revolsys.gis.cs.projection;

import javax.measure.converter.ConversionException;
import javax.measure.converter.UnitConverter;
import javax.measure.unit.Unit;

import com.revolsys.gis.model.coordinates.Coordinates;

public class UnitConverstionOperation implements CoordinatesOperation {
  private final UnitConverter converter;

  private final Unit<?> sourceUnit;

  private final Unit<?> targetUnit;

  public UnitConverstionOperation(final Unit<?> sourceUnit,
    final Unit<?> targetUnit) {
    this.sourceUnit = sourceUnit;
    this.targetUnit = targetUnit;
    try {
      converter = sourceUnit.getConverterToAny(targetUnit);
    } catch (final ConversionException e) {
      throw new IllegalArgumentException(e);
    }
  }

  public void perform(final Coordinates from, final Coordinates to) {
    final int dimension = Math.min(from.getNumAxis(), to.getNumAxis());
    for (int i = 0; i < dimension; i++) {
      final double value = from.getValue(i);
      final double convertedValue = converter.convert(value);
      to.setValue(i, convertedValue);
    }

  }

  @Override
  public String toString() {
    return sourceUnit + "->" + targetUnit;
  }
}
