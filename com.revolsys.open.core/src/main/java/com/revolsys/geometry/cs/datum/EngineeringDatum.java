package com.revolsys.geometry.cs.datum;

import com.revolsys.geometry.cs.Area;
import com.revolsys.geometry.cs.Authority;

public class EngineeringDatum extends Datum {
  public EngineeringDatum(final Authority authority, final String name, final Area area,
    final boolean deprecated) {
    super(authority, name, area, deprecated);
  }
}
