package com.revolsys.gis.cs;

import java.io.Serializable;

import com.revolsys.gis.model.data.equals.EqualsRegistry;
import com.vividsolutions.jts.geom.Envelope;

public class Area implements Serializable {
  /**
   * 
   */
  private static final long serialVersionUID = 2662773652065582230L;

  private final Authority authority;

  private final boolean deprecated;

  private final Envelope latLonBounds;

  private final String name;

  public Area(final String name, final Envelope latLonBounds,
    final Authority authority, final boolean deprecated) {
    this.name = name;
    this.latLonBounds = latLonBounds;
    this.authority = authority;
    this.deprecated = deprecated;
  }

  public Authority getAuthority() {
    return authority;
  }

  public Envelope getLatLonBounds() {
    return latLonBounds;
  }

  public String getName() {
    return name;
  }

  public boolean isDeprecated() {
    return deprecated;
  }

  @Override
  public int hashCode() {
    return latLonBounds.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof Area) {
      Area area = (Area)obj;
      if (!EqualsRegistry.equal(latLonBounds, area.latLonBounds)) {
        return false;
      } else {
        return true;
      }
    } else {
      return false;
    }
  }

  @Override
  public String toString() {
    return name;
  }
}
