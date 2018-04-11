package com.revolsys.geometry.cs.unit;

import com.revolsys.geometry.cs.Authority;
import com.revolsys.geometry.cs.projection.CoordinatesOperationPoint;

public class Degree extends AngularUnit {

  public Degree(final String name, final AngularUnit baseUnit, final double conversionFactor,
    final Authority authority, final boolean deprecated) {
    super(name, baseUnit, conversionFactor, authority, deprecated);
  }

  @Override
  public void fromRadians(final CoordinatesOperationPoint point) {
    point.x = Math.toDegrees(point.x);
    point.y = Math.toDegrees(point.y);
  }

  @Override
  public double fromRadians(final double value) {
    return Math.toDegrees(value);
  }

  @Override
  public double toDegrees(final double value) {
    return value;
  }

  @Override
  public double toNormal(final double value) {
    return value;
  }

  @Override
  public void toRadians(final CoordinatesOperationPoint point) {
    point.x = Math.toRadians(point.x);
    point.y = Math.toRadians(point.y);
  }

  @Override
  public double toRadians(final double value) {
    return Math.toRadians(value);
  }
}
