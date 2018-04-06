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
  public void perform(final CoordinatesOperationPoint point) {
    final UnitConverter converter = this.converter;
    point.x = converter.convert(point.x);
    point.y = converter.convert(point.y);
    if (this.axisCount > 2) {
      point.z = converter.convert(point.z);
    }
  }

  @Override
  public String toString() {
    return this.sourceUnit + "->" + this.targetUnit;
  }
}
