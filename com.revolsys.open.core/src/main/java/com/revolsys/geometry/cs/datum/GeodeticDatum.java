package com.revolsys.geometry.cs.datum;

import java.security.MessageDigest;

import com.revolsys.geometry.cs.Area;
import com.revolsys.geometry.cs.Authority;
import com.revolsys.geometry.cs.Ellipsoid;
import com.revolsys.geometry.cs.PrimeMeridian;
import com.revolsys.geometry.cs.ToWgs84;

public class GeodeticDatum extends Datum {
  private PrimeMeridian primeMeridian;

  private final Ellipsoid ellipsoid;

  private ToWgs84 toWgs84;

  public GeodeticDatum(final Authority authority, final String name, final Area area,
    final boolean deprecated, final Ellipsoid ellipsoid, final PrimeMeridian primeMeridian) {
    super(authority, name, area, deprecated);
    this.ellipsoid = ellipsoid;
    this.primeMeridian = primeMeridian;
  }

  public GeodeticDatum(final String name, final Ellipsoid ellipsoid, final ToWgs84 toWgs84,
    final Authority authority) {
    super(authority, name, null, false);
    this.ellipsoid = ellipsoid;
    this.toWgs84 = toWgs84;
  }

  @Override
  public boolean equals(final Object object) {
    if (object == null) {
      return false;
    } else if (object == this) {
      return true;
    } else if (object instanceof GeodeticDatum) {
      final GeodeticDatum geodeticDatum = (GeodeticDatum)object;
      if (this.ellipsoid.equals(geodeticDatum.ellipsoid)) {
        return true;
      } else {
        return false;
      }
    }
    return false;
  }

  public boolean equalsExact(final GeodeticDatum geodeticDatum) {
    if (!super.equals(geodeticDatum)) {
      return false;
    } else if (!this.primeMeridian.equalsExact(this.primeMeridian)) {
      return false;
    } else if (!this.ellipsoid.equalsExact(geodeticDatum.ellipsoid)) {
      return false;
    } else {
      return true;
    }
  }

  public Ellipsoid getEllipsoid() {
    return this.ellipsoid;
  }

  public PrimeMeridian getPrimeMeridian() {
    return this.primeMeridian;
  }

  public ToWgs84 getToWgs84() {
    return this.toWgs84;
  }

  @Override
  public int hashCode() {
    if (this.ellipsoid != null) {
      return this.ellipsoid.hashCode();
    } else {
      return 1;
    }
  }

  public void updateDigest(final MessageDigest digest) {
    this.ellipsoid.updateDigest(digest);
  }
}
