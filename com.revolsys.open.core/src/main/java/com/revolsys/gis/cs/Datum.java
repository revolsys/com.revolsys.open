package com.revolsys.gis.cs;

import java.io.Serializable;

import com.revolsys.gis.model.data.equals.EqualsRegistry;

public class Datum implements Serializable {
  /**
   * 
   */
  private static final long serialVersionUID = 4603557237435332398L;

  private final Authority authority;

  private boolean deprecated;

  private final String name;

  private final Spheroid spheroid;

  private ToWgs84 toWgs84;

  private PrimeMeridian primeMeridian;

  public PrimeMeridian getPrimeMeridian() {
    return primeMeridian;
  }

  public Datum(final String name, final Spheroid spheroid,
    final Authority authority) {
    this.name = name;
    this.spheroid = spheroid;
    this.authority = authority;
  }

  public Datum(final String name, final Spheroid spheroid,
    final ToWgs84 toWgs84, final Authority authority) {
    this.name = name;
    this.spheroid = spheroid;
    this.toWgs84 = toWgs84;
    this.authority = authority;
  }

  public Datum(String name, Spheroid spheroid, PrimeMeridian primeMeridian,
    Authority authority, boolean deprecated) {
    this.name = name;
    this.spheroid = spheroid;
    this.primeMeridian = primeMeridian;
    this.authority = authority;
    this.deprecated = deprecated;
  }

  @Override
  public boolean equals(final Object object) {
    if (object instanceof Datum) {
      final Datum datum = (Datum)object;
      if (EqualsRegistry.equal(authority, datum.getAuthority())) {
        return true;
      } else if (name == null) {
        if (datum.name != null) {
          return false;
        }
      } else if (!name.equalsIgnoreCase(datum.name)) {
        return false;
      } else if (!EqualsRegistry.equal(spheroid, datum.spheroid)) {
        return false;
      } else {
        return true;
      }
    }
    return false;
  }

  public Authority getAuthority() {
    return authority;
  }

  public String getName() {
    return name;
  }

  public Spheroid getSpheroid() {
    return spheroid;
  }

  public ToWgs84 getToWgs84() {
    return toWgs84;
  }

  @Override
  public int hashCode() {
    if (spheroid != null) {
      return spheroid.hashCode();
    } else {
      return 1;
    }
  }

  public boolean isDeprecated() {
    return deprecated;
  }

  @Override
  public String toString() {
    return name;
  }

  public boolean equalsExact(Datum datum) {
    if (!EqualsRegistry.equal(authority, datum.authority)) {
      return false;
    } else if (deprecated != datum.deprecated) {
      return false;
    } else if (!EqualsRegistry.equal(name, datum.name)) {
      return false;
    } else if (!primeMeridian.equalsExact(primeMeridian)) {
      return false;
    } else if (!spheroid.equalsExact(datum.spheroid)) {
      return false;
    } else {
      return true;
    }
  }
}
