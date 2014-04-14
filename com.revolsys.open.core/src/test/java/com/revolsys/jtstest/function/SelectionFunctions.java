package com.revolsys.jtstest.function;

import java.util.ArrayList;
import java.util.List;

import com.revolsys.jts.geom.Geometry;

interface GeometryPredicate {
  boolean isTrue(Geometry geom);
}

public class SelectionFunctions {
  public static Geometry areaGreater(final Geometry a, final double minArea) {
    return select(a, new GeometryPredicate() {
      @Override
      public boolean isTrue(final Geometry g) {
        return g.getArea() > minArea;
      }
    });
  }

  public static Geometry areaZero(final Geometry a) {
    return select(a, new GeometryPredicate() {
      @Override
      public boolean isTrue(final Geometry g) {
        return g.getArea() == 0.0;
      }
    });
  }

  public static Geometry coveredBy(final Geometry a, final Geometry mask) {
    return select(a, new GeometryPredicate() {
      @Override
      public boolean isTrue(final Geometry g) {
        return g.coveredBy(mask);
      }
    });
  }

  public static Geometry covers(final Geometry a, final Geometry mask) {
    return select(a, new GeometryPredicate() {
      @Override
      public boolean isTrue(final Geometry g) {
        return g.covers(mask);
      }
    });
  }

  public static Geometry disjoint(final Geometry a, final Geometry mask) {
    final List selected = new ArrayList();
    for (int i = 0; i < a.getNumGeometries(); i++) {
      final Geometry g = a.getGeometry(i);
      if (mask.disjoint(g)) {
        selected.add(g);
      }
    }
    return a.getGeometryFactory().buildGeometry(selected);
  }

  public static Geometry firstNComponents(final Geometry g, final int n) {
    final List comp = new ArrayList();
    for (int i = 0; i < g.getNumGeometries() && i < n; i++) {
      comp.add(g.getGeometry(i));
    }
    return g.getGeometryFactory().buildGeometry(comp);
  }

  public static Geometry intersects(final Geometry a, final Geometry mask) {
    return select(a, new GeometryPredicate() {
      @Override
      public boolean isTrue(final Geometry g) {
        return mask.intersects(g);
      }
    });
  }

  public static Geometry invalid(final Geometry a) {
    final List selected = new ArrayList();
    for (int i = 0; i < a.getNumGeometries(); i++) {
      final Geometry g = a.getGeometry(i);
      if (!g.isValid()) {
        selected.add(g);
      }
    }
    return a.getGeometryFactory().buildGeometry(selected);
  }

  private static Geometry select(final Geometry geom,
    final GeometryPredicate pred) {
    final List selected = new ArrayList();
    for (int i = 0; i < geom.getNumGeometries(); i++) {
      final Geometry g = geom.getGeometry(i);
      if (pred.isTrue(g)) {
        selected.add(g);
      }
    }
    return geom.getGeometryFactory().buildGeometry(selected);

  }

  public static Geometry valid(final Geometry a) {
    final List selected = new ArrayList();
    for (int i = 0; i < a.getNumGeometries(); i++) {
      final Geometry g = a.getGeometry(i);
      if (g.isValid()) {
        selected.add(g);
      }
    }
    return a.getGeometryFactory().buildGeometry(selected);
  }

  public static Geometry within(final Geometry a, final Geometry mask) {
    return select(a, new GeometryPredicate() {
      @Override
      public boolean isTrue(final Geometry g) {
        return g.within(mask);
      }
    });
  }
}
