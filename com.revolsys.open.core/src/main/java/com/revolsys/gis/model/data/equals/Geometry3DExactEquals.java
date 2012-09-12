package com.revolsys.gis.model.data.equals;

import java.util.Collection;
import java.util.Map;

import com.revolsys.gis.jts.JtsGeometryUtil;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;

public class Geometry3DExactEquals implements Equals<Geometry> {
  private EqualsRegistry equalsRegistry;

  @Override
  public boolean equals(final Geometry geometry1, final Geometry geometry2,
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
      if (!userDataEquals(geometryPart1, geometryPart2, exclude)) {
        return false;
      }
    }
    if (geometry1 instanceof GeometryCollection) {
      return userDataEquals(geometry1, geometry2, exclude);
    } else {
      return true;
    }
  }

  @Override
  public void setEqualsRegistry(final EqualsRegistry equalsRegistry) {
    this.equalsRegistry = equalsRegistry;
  }

  public boolean userDataEquals(final Geometry geometry1,
    final Geometry geometry2, final Collection<String> exclude) {
    final Object userData1 = geometry1.getUserData();
    final Object userData2 = geometry2.getUserData();
    if (userData1 == null) {
      if (userData2 == null) {
        return true;
      } else if (userData2 instanceof Map) {
        final Map map = (Map)userData2;
        return map.isEmpty();
      }
    } else if (userData2 == null) {
      if (userData1 instanceof Map) {
        final Map map = (Map)userData1;
        return map.isEmpty();
      } else {
        return false;
      }
    }
    return equalsRegistry.equals(userData1, userData2, exclude);
  }

}
