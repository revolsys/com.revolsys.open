package com.revolsys.geometry.cs.datum;

import com.revolsys.geometry.cs.Area;
import com.revolsys.geometry.cs.Authority;

public class EngineeringDatum extends Datum {
  public EngineeringDatum(final Authority authority, final String name, final Area area,
    final boolean deprecated) {
    super(authority, name, area, deprecated);
  }

  @Override
  public boolean isSame(final Datum datum) {
    if (datum instanceof EngineeringDatum) {
      return isSame((EngineeringDatum)datum);
    } else {
      return false;
    }
  }

  public boolean isSame(final EngineeringDatum engineeringDatum) {
    return super.isSame(engineeringDatum);
  }

}
