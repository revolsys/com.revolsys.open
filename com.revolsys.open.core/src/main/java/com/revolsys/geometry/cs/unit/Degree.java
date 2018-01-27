package com.revolsys.geometry.cs.unit;

import com.revolsys.geometry.cs.Authority;

public class Degree extends AngularUnit {

  public Degree(final String name, final AngularUnit baseUnit, final double conversionFactor,
    final Authority authority, final boolean deprecated) {
    super(name, baseUnit, conversionFactor, authority, deprecated);
  }

  @Override
  public double toDegrees(final double value) {
    return value;
  }

  @Override
  public double toNormal(final double value) {
    return value;
  }
}
