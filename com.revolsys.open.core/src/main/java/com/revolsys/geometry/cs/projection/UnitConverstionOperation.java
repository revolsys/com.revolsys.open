package com.revolsys.geometry.cs.projection;

import javax.measure.Unit;
import javax.measure.UnitConverter;

public class UnitConverstionOperation implements CoordinatesOperation {
  private int axisCount = 0;

  private final UnitConverter converter;

  private final Unit sourceUnit;

  private final Unit targetUnit;

  public UnitConverstionOperation(final Unit sourceUnit, final Unit targetUnit) {
    this(sourceUnit, targetUnit, 2);
  }

  public UnitConverstionOperation(final Unit sourceUnit, final Unit targetUnit,
    final int axisCount) {
    this.sourceUnit = sourceUnit;
    this.targetUnit = targetUnit;
    this.axisCount = axisCount;
    this.converter = sourceUnit.getConverterTo(targetUnit);
  }

  @Override
  public void perform(final int sourceAxisCount, final double[] sourceCoordinates,
    final int targetAxisCount, final double[] targetCoordinates) {
    final int numPoints = sourceCoordinates.length / sourceAxisCount;
    final int axisCount = this.axisCount;
    final UnitConverter converter = this.converter;
    for (int vertexIndex = 0; vertexIndex < numPoints; vertexIndex++) {
      for (int axisIndex = 0; axisIndex < targetAxisCount; axisIndex++) {
        double value;
        if (axisIndex < sourceAxisCount) {
          value = sourceCoordinates[vertexIndex * sourceAxisCount + axisIndex];
          if (axisIndex < axisCount) {
            value = converter.convert(value);
          }
        } else {
          value = Double.NaN;
        }
        targetCoordinates[vertexIndex * targetAxisCount + axisIndex] = value;
      }
    }
  }

  @Override
  public String toString() {
    return this.sourceUnit + "->" + this.targetUnit;
  }
}
