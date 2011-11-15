package com.revolsys.gis.model.data.equals;

import java.util.Collection;

import com.revolsys.gis.jts.JtsGeometryUtil;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;

public class Geometry3DExactEquals implements Equals<Geometry> {
  private EqualsRegistry equalsRegistry;

  public boolean equals(
    final Geometry geometry1,
    final Geometry geometry2,
    final Collection<String> exclude) {
    if (geometry1.getNumGeometries() != geometry2.getNumGeometries()) {
      return false;
    }
    for (int j = 0; j < geometry1.getNumGeometries(); j++) {
      final Geometry geometryPart1 = geometry1.getGeometryN(j);
      final Geometry geometryPart2 = geometry2.getGeometryN(j);
      if (!JtsGeometryUtil.equalsExact3D(geometryPart1, geometryPart2)) {
        return false;
      }
      final Object userData1 = geometryPart1.getUserData();
      final Object userData2 = geometryPart2.getUserData();
      if (!equalsRegistry.equals(userData1, userData2, exclude)) {
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

  public void setEqualsRegistry(
    final EqualsRegistry equalsRegistry) {
    this.equalsRegistry = equalsRegistry;
  }

}
