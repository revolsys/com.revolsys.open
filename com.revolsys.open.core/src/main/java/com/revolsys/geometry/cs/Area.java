package com.revolsys.geometry.cs;

import java.io.Serializable;

import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.impl.BoundingBoxDoubleXY;

public class Area extends BoundingBoxDoubleXY implements Serializable {
  private static final long serialVersionUID = 2662773652065582230L;

  private final Authority authority;

  private final boolean deprecated;

  private final String name;

  public Area(final String name, final double minX, final double minY, final double maxX,
    final double maxY, final Authority authority, final boolean deprecated) {
    super(minX, minY, maxX, maxY);
    this.name = name;
    this.authority = authority;
    this.deprecated = deprecated;
  }

  @Override
  public boolean equals(final Object obj) {
    if (obj instanceof Area) {
      final Area area = (Area)obj;
      if (super.equals(area)) {
        return false;
      } else {
        return true;
      }
    } else {
      return false;
    }
  }

  public Authority getAuthority() {
    return this.authority;
  }

  public String getName() {
    return this.name;
  }

  public boolean isDeprecated() {
    return this.deprecated;
  }

  @Override
  public String toString() {
    return this.name;
  }
}
