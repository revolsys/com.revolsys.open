package com.revolsys.gis.cs.projection;

import javax.measure.converter.UnitConverter;
import javax.measure.unit.Unit;

import com.revolsys.jts.geom.Coordinates;

public class UnitConverstionOperation implements CoordinatesOperation {
  private final UnitConverter converter;

  private final Unit sourceUnit;

  private final Unit targetUnit;

  private int axisCount = 0;

  public UnitConverstionOperation(final Unit sourceUnit, final Unit targetUnit) {
    this.sourceUnit = sourceUnit;
    this.targetUnit = targetUnit;
    converter = sourceUnit.getConverterTo(targetUnit);
  }

  public UnitConverstionOperation(final Unit sourceUnit, final Unit targetUnit,
    final int axisCount) {
    this.sourceUnit = sourceUnit;
    this.targetUnit = targetUnit;
    this.axisCount = axisCount;
    converter = sourceUnit.getConverterTo(targetUnit);
  }

  @Override
  public void perform(final Coordinates from, final Coordinates to) {
    final int axisCount = Math.min(from.getAxisCount(), to.getAxisCount());

    for (int i = 0; i < axisCount; i++) {
      final double value = from.getValue(i);
      if (i < this.axisCount) {
        final double convertedValue = converter.convert(value);
        to.setValue(i, convertedValue);
      } else {
        to.setValue(i, value);
      }
    }

  }

  @Override
  public void perform(final int sourceAxisCount,
    final double[] sourceCoordinates, final int targetAxisCount,
    final double[] targetCoordinates) {
    final int numPoints = sourceCoordinates.length / sourceAxisCount;
    for (int vertexIndex = 0; vertexIndex < numPoints; vertexIndex++) {
      for (int axisIndex = 0; axisIndex < targetAxisCount; axisIndex++) {
        double value;
        if (axisIndex < sourceAxisCount) {
          value = sourceCoordinates[vertexIndex * sourceAxisCount + axisIndex];
          if (axisIndex < this.axisCount) {
            value = converter.convert(value);
          }
        } else {
          value = Double.NaN;
        }
        targetCoordinates[vertexIndex * sourceAxisCount + axisIndex] = value;
      }
    }
  }

  @Override
  public String toString() {
    return sourceUnit + "->" + targetUnit;
  }
}
