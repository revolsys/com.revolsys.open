package com.revolsys.gis.model.data.equals;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import com.revolsys.gis.jts.JtsGeometryUtil;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.GeometryCollection;

public class Geometry3DExactEquals implements Equals<Geometry> {
  public static final Set<String> USER_DATA_EXCLUDE = new TreeSet<String>();

  public static void addExclude(final String name) {
    USER_DATA_EXCLUDE.add(name);
  }

  private EqualsRegistry equalsRegistry;

  @Override
  public boolean equals(final Geometry geometry1, final Geometry geometry2,
    final Collection<String> exclude) {
    if (geometry1.getNumGeometries() != geometry2.getNumGeometries()) {
      return false;
    }
    final boolean userDataEquals = !exclude.contains("userData");
    for (int j = 0; j < geometry1.getNumGeometries(); j++) {
      final Geometry geometryPart1 = geometry1.getGeometryN(j);
      final Geometry geometryPart2 = geometry2.getGeometryN(j);
      if (!JtsGeometryUtil.equalsExact3D(geometryPart1, geometryPart2)) {
        return false;
      }
      if (userDataEquals) {
        if (!userDataEquals(geometryPart1, geometryPart2, exclude)) {
          return false;
        }
      }
    }
    if (geometry1 instanceof GeometryCollection) {
      if (userDataEquals) {
        return userDataEquals(geometry1, geometry2, exclude);
      } else {
        return true;
      }
    } else {
      return true;
    }
  }

  @Override
  public void setEqualsRegistry(final EqualsRegistry equalsRegistry) {
    this.equalsRegistry = equalsRegistry;
  }

  @SuppressWarnings("rawtypes")
  public boolean userDataEquals(final Geometry geometry1,
    final Geometry geometry2, Collection<String> exclude) {
    Object userData1 = geometry1.getUserData();
    Object userData2 = geometry2.getUserData();
    if (userData1 == null) {
      if (userData2 == null) {
        return true;
      } else if (userData2 instanceof Map) {
        final Map map = (Map)userData2;
        if (map.isEmpty()) {
          return true;
        } else {
          userData1 = Collections.emptyMap();
        }
      } else {
        return false;
      }
    } else if (userData2 == null) {
      if (userData1 instanceof Map) {
        final Map map = (Map)userData1;
        if (map.isEmpty()) {
          return true;
        } else {
          userData2 = Collections.emptyMap();
        }
      } else {
        return false;
      }
    }
    if (exclude.isEmpty()) {
      exclude = USER_DATA_EXCLUDE;
    } else {
      exclude = new TreeSet<String>(exclude);
      exclude.addAll(USER_DATA_EXCLUDE);
    }
    return equalsRegistry.equals(userData1, userData2, exclude);
  }

}
