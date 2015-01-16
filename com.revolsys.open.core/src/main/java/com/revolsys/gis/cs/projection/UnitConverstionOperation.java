package com.revolsys.gis.cs.projection;

import javax.measure.converter.UnitConverter;
import javax.measure.unit.Unit;

public class UnitConverstionOperation implements CoordinatesOperation {
  private final UnitConverter converter;

  private final Unit sourceUnit;

  private final Unit targetUnit;

  private int axisCount = 0;

  public UnitConverstionOperation(final Unit sourceUnit, final Unit targetUnit) {
    this.sourceUnit = sourceUnit;
    this.targetUnit = targetUnit;
    this.converter = sourceUnit.getConverterTo(targetUnit);
  }

  public UnitConverstionOperation(final Unit sourceUnit, final Unit targetUnit,
    final int axisCount) {
    this.sourceUnit = sourceUnit;
    this.targetUnit = targetUnit;
    this.axisCount = axisCount;
    this.converter = sourceUnit.getConverterTo(targetUnit);
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
            value = this.converter.convert(value);
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
    return this.sourceUnit + "->" + this.targetUnit;
  }
}
