package com.revolsys.geometry.cs.unit;

import com.revolsys.geometry.cs.Authority;

public class Metre extends LinearUnit {

  public Metre(final String name, final LinearUnit baseUnit, final double conversionFactor,
    final Authority authority, final boolean deprecated) {
    super(name, baseUnit, conversionFactor, authority, deprecated);
  }

  @Override
  public double toNormal(final double value) {
    return value;
  }
}
