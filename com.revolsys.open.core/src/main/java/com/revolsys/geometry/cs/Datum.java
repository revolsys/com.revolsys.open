package com.revolsys.geometry.cs;

import java.io.Serializable;

import com.revolsys.util.Equals;

public class Datum implements Serializable {
  /**
   *
   */
  private static final long serialVersionUID = 4603557237435332398L;

  private final Authority authority;

  private boolean deprecated;

  private final String name;

  private PrimeMeridian primeMeridian;

  private final Spheroid spheroid;

  private ToWgs84 toWgs84;

  public Datum(final String name, final Spheroid spheroid, final Authority authority) {
    this.name = name;
    this.spheroid = spheroid;
    this.authority = authority;
  }

  public Datum(final String name, final Spheroid spheroid, final PrimeMeridian primeMeridian,
    final Authority authority, final boolean deprecated) {
    this.name = name;
    this.spheroid = spheroid;
    this.primeMeridian = primeMeridian;
    this.authority = authority;
    this.deprecated = deprecated;
  }

  public Datum(final String name, final Spheroid spheroid, final ToWgs84 toWgs84,
    final Authority authority) {
    this.name = name;
    this.spheroid = spheroid;
    this.toWgs84 = toWgs84;
    this.authority = authority;
  }

  @Override
  public boolean equals(final Object object) {
    if (object instanceof Datum) {
      final Datum datum = (Datum)object;
      if (Equals.equals(this.authority, datum.getAuthority())) {
        return true;
      } else if (this.name == null) {
        if (datum.name != null) {
          return false;
        }
        // } else if (!name.equalsIgnoreCase(datum.name)) {
        // return false;
      } else if (!Equals.equals(this.spheroid, datum.spheroid)) {
        return false;
      } else {
        return true;
      }
    }
    return false;
  }

  public boolean equalsExact(final Datum datum) {
    if (!Equals.equals(this.authority, datum.authority)) {
      return false;
    } else if (this.deprecated != datum.deprecated) {
      return false;
    } else if (!Equals.equals(this.name, datum.name)) {
      return false;
    } else if (!this.primeMeridian.equalsExact(this.primeMeridian)) {
      return false;
    } else if (!this.spheroid.equalsExact(datum.spheroid)) {
      return false;
    } else {
      return true;
    }
  }

  public Authority getAuthority() {
    return this.authority;
  }

  public String getName() {
    return this.name;
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

  public boolean isDeprecated() {
    return this.deprecated;
  }

  @Override
  public String toString() {
    return this.name;
  }
}
