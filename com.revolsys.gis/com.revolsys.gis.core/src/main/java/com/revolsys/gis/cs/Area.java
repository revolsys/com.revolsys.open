package com.revolsys.gis.cs;

import com.vividsolutions.jts.geom.Envelope;

public class Area {
  private final Authority authority;

  private final boolean deprecated;

  private final Envelope latLonBounds;

  private final String name;

  public Area(
    final String name,
    final Envelope latLonBounds,
    final Authority authority,
    final boolean deprecated) {
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

}
