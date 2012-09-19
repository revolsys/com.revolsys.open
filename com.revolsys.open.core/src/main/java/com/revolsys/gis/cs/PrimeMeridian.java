package com.revolsys.gis.cs;

import java.io.Serializable;

import com.revolsys.gis.model.data.equals.EqualsRegistry;

public class PrimeMeridian implements Serializable {
  /**
   * 
   */
  private static final long serialVersionUID = -2580130961723669616L;

  private final Authority authority;

  private boolean deprecated;

  private final double longitude;

  private final String name;

  public PrimeMeridian(final String name, final double longitude,
    final Authority authority) {
    this.name = name;
    this.longitude = longitude;
    this.authority = authority;
  }

  public PrimeMeridian(final String name, final double longitude,
    final Authority authority, final boolean deprecated) {
    this.name = name;
    this.longitude = longitude;
    this.authority = authority;
    this.deprecated = deprecated;
  }

  @Override
  public boolean equals(final Object object) {
    if (object == null) {
      return false;
    } else if (object == this) {
      return true;
    } else if (object instanceof PrimeMeridian) {
      final PrimeMeridian primeMeridian = (PrimeMeridian)object;
      return longitude == primeMeridian.longitude;
    }
    return false;
  }

  public Authority getAuthority() {
    return authority;
  }

  public double getLongitude() {
    return longitude;
  }

  public String getName() {
    return name;
  }

  @Override
  public int hashCode() {
    final long temp = Double.doubleToLongBits(longitude);
    return (int)(temp ^ (temp >>> 32));
  }

  public boolean isDeprecated() {
    return deprecated;
  }

  @Override
  public String toString() {
    return name;
  }

  public boolean equalsExact(PrimeMeridian primeMeridian) {
    if (!EqualsRegistry.equal(authority, primeMeridian.authority)) {
      return false;
    } else if (deprecated != primeMeridian.deprecated) {
      return false;
    } else if (longitude != longitude) {
      return false;
    } else if (!EqualsRegistry.equal(name, primeMeridian.name)) {
      return false;
    } else {
      return true;
    }
  }
}
