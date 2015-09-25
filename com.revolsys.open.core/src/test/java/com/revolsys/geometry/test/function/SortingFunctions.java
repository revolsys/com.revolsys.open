package com.revolsys.geometry.test.function;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.revolsys.geometry.model.Geometry;

public class SortingFunctions {
  private static class GeometryAreaComparator implements Comparator {
    @Override
    public int compare(final Object o1, final Object o2) {
      final Geometry g1 = (Geometry)o1;
      final Geometry g2 = (Geometry)o2;
      return Double.compare(g1.getArea(), g2.getArea());
    }
  }

  private static class GeometryLengthComparator implements Comparator {
    @Override
    public int compare(final Object o1, final Object o2) {
      final Geometry g1 = (Geometry)o1;
      final Geometry g2 = (Geometry)o2;
      return Double.compare(g1.getLength(), g2.getLength());
    }
  }

  private static List components(final Geometry g) {
    final List comp = new ArrayList();
    for (int i = 0; i < g.getGeometryCount(); i++) {
      comp.add(g.getGeometry(i));
    }
    return comp;
  }

  public static Geometry sortByArea(final Geometry g) {
    final List geoms = components(g);
    Collections.sort(geoms, new GeometryAreaComparator());
    return g.getGeometryFactory().buildGeometry(geoms);
  }

  public static Geometry sortByLength(final Geometry g) {
    final List geoms = components(g);
    Collections.sort(geoms, new GeometryLengthComparator());
    return g.getGeometryFactory().buildGeometry(geoms);
  }
}
