package com.revolsys.geometry.cs.datum;

import com.revolsys.geometry.cs.Area;
import com.revolsys.geometry.cs.Authority;

public class VerticalDatum extends Datum {
  private int datumType;

  public VerticalDatum(final Authority authority, final String name, final Area area,
    final boolean deprecated) {
    super(authority, name, area, deprecated);
  }

  public VerticalDatum(final Authority authority, final String name, final int datumType) {
    super(authority, name, null, false);
    this.datumType = datumType;
  }

  public int getDatumType() {
    return this.datumType;
  }
}
