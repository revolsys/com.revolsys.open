package com.revolsys.geometry.cs;

public class Degree extends AngularUnit {

  public Degree(final String name, final AngularUnit baseUnit, final double conversionFactor,
    final Authority authority, final boolean deprecated) {
    super(name, baseUnit, conversionFactor, authority, deprecated);
  }

  @Override
  public double toDegrees(final double value) {
    return value;
  }
}
