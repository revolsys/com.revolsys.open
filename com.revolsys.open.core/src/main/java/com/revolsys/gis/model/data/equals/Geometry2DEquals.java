package com.revolsys.gis.model.data.equals;

import java.util.Collection;

import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.GeometryCollection;

public class Geometry2DEquals implements Equals<Geometry> {
  public static final Geometry2DEquals INSTANCE = new Geometry2DEquals();

  private EqualsRegistry equalsRegistry;

  public Geometry2DEquals() {
    this(EqualsInstance.INSTANCE);
  }

  public Geometry2DEquals(final EqualsRegistry equalsRegistry) {
    this.equalsRegistry = equalsRegistry;
  }

  @Override
  public boolean equals(final Geometry geometry1, final Geometry geometry2,
    final Collection<String> exclude) {
    if (geometry1.getGeometryCount() != geometry2.getGeometryCount()) {
      return false;
    }
    for (int j = 0; j < geometry1.getGeometryCount(); j++) {
      final Geometry geometryPart1 = geometry1.getGeometry(j);
      final Geometry geometryPart2 = geometry2.getGeometry(j);
      if (!geometryPart1.equals(geometryPart2)) {
        return false;
      }
      if (!equalsRegistry.equals(geometryPart1.getUserData(),
        geometryPart2.getUserData(), exclude)) {
        return false;
      }
    }
    if (geometry1 instanceof GeometryCollection) {
      final Object userData1 = geometry1.getUserData();
      final Object userData2 = geometry2.getUserData();
      return equalsRegistry.equals(userData1, userData2, exclude);
    } else {
      return true;
    }
  }

  @Override
  public void setEqualsRegistry(final EqualsRegistry equalsRegistry) {
    this.equalsRegistry = equalsRegistry;
  }

}
