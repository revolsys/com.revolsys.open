package com.revolsys.geometry.cs;

import java.io.Serializable;

import org.jeometry.common.data.type.DataType;

public class PrimeMeridian implements Serializable {
  /**
   *
   */
  private static final long serialVersionUID = -2580130961723669616L;

  private final Authority authority;

  private boolean deprecated;

  private final double longitude;

  private final String name;

  public PrimeMeridian(final String name, final double longitude, final Authority authority) {
    this.name = name;
    this.longitude = longitude;
    this.authority = authority;
  }

  public PrimeMeridian(final String name, final double longitude, final Authority authority,
    final boolean deprecated) {
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
      return this.longitude == primeMeridian.longitude;
    }
    return false;
  }

  public boolean equalsExact(final PrimeMeridian primeMeridian) {
    if (!DataType.equal(this.authority, primeMeridian.authority)) {
      return false;
    } else if (this.deprecated != primeMeridian.deprecated) {
      return false;
    } else if (this.longitude != this.longitude) {
      return false;
    } else if (!DataType.equal(this.name, primeMeridian.name)) {
      return false;
    } else {
      return true;
    }
  }

  public Authority getAuthority() {
    return this.authority;
  }

  public double getLongitude() {
    return this.longitude;
  }

  public String getName() {
    return this.name;
  }

  @Override
  public int hashCode() {
    final long temp = Double.doubleToLongBits(this.longitude);
    return (int)(temp ^ temp >>> 32);
  }

  public boolean isDeprecated() {
    return this.deprecated;
  }

  @Override
  public String toString() {
    return this.name;
  }
}
