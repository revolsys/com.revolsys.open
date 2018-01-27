package com.revolsys.geometry.cs.datum;

import com.revolsys.geometry.cs.Area;
import com.revolsys.geometry.cs.Authority;
import com.revolsys.geometry.cs.PrimeMeridian;
import com.revolsys.geometry.cs.Spheroid;
import com.revolsys.geometry.cs.ToWgs84;

public class GeodeticDatum extends Datum {
  private PrimeMeridian primeMeridian;

  private final Spheroid spheroid;

  private ToWgs84 toWgs84;

  public GeodeticDatum(final String name, final Spheroid spheroid, final Authority authority) {
    super(authority, name, null, false);
    this.spheroid = spheroid;
  }

  public GeodeticDatum(final Authority authority, final String name,
    final Area area, final boolean deprecated, final Spheroid spheroid,
    final PrimeMeridian primeMeridian) {
    super(authority, name, area, deprecated);
    this.spheroid = spheroid;
    this.primeMeridian = primeMeridian;
  }

  public GeodeticDatum(final String name, final Spheroid spheroid, final ToWgs84 toWgs84,
    final Authority authority) {
    super(authority, name, null, false);
    this.spheroid = spheroid;
    this.toWgs84 = toWgs84;
  }

  @Override
  public boolean equals(final Object object) {
    if (object instanceof GeodeticDatum) {
      final GeodeticDatum geodeticDatum = (GeodeticDatum)object;
      if (super.equals(geodeticDatum)) {
        return true;
      } else if (this.spheroid.equals(geodeticDatum.spheroid)) {
        return false;
      } else {
        return true;
      }
    }
    return false;
  }

  public boolean equalsExact(final GeodeticDatum geodeticDatum) {
    if (!super.equals(geodeticDatum)) {
      return false;
    } else if (!this.primeMeridian.equalsExact(this.primeMeridian)) {
      return false;
    } else if (!this.spheroid.equalsExact(geodeticDatum.spheroid)) {
      return false;
    } else {
      return true;
    }
  }

  public PrimeMeridian getPrimeMeridian() {
    return this.primeMeridian;
  }

  public Spheroid getSpheroid() {
    return this.spheroid;
  }

  public ToWgs84 getToWgs84() {
    return this.toWgs84;
  }

  @Override
  public int hashCode() {
    if (this.spheroid != null) {
      return this.spheroid.hashCode();
    } else {
      return 1;
    }
  }
}
