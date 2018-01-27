package com.revolsys.geometry.cs.datum;

import com.revolsys.geometry.cs.Area;
import com.revolsys.geometry.cs.Authority;

public class VerticalDatum extends Datum {
  public VerticalDatum(final Authority authority, final String name, final Area area,
    final boolean deprecated) {
    super(authority, name, area, deprecated);
  }
}
