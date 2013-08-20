package com.revolsys.gis.cs;

import java.io.Serializable;

import com.revolsys.gis.model.data.equals.EqualsRegistry;

public class Projection implements Serializable {
  /**
   * 
   */
  private static final long serialVersionUID = 6199958151692874551L;

  private Authority authority;

  private final String name;

  public Projection(final String name) {
    this.name = name;
  }

  public Projection(final String name, final Authority authority) {
    this.name = name;
    this.authority = authority;
  }

  @Override
  public boolean equals(final Object obj) {
    if (obj instanceof Projection) {
      final Projection projection = (Projection)obj;
      if (EqualsRegistry.equal(authority, projection.authority)) {
        return true;
      } else {
        return name.equals(projection.getName());
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

  @Override
  public int hashCode() {
    return name.hashCode();
  }

  @Override
  public String toString() {
    return name;
  }
}
