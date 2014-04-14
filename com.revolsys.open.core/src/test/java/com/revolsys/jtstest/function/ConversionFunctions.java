package com.revolsys.jtstest.function;

import java.util.ArrayList;
import java.util.List;

import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.GeometryCollection;
import com.revolsys.jts.geom.GeometryCollectionIterator;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.jts.geom.util.LinearComponentExtracter;

public class ConversionFunctions {
  private static void addComponents(final Geometry g, final List atomicGeoms) {
    if (!(g instanceof GeometryCollection)) {
      atomicGeoms.add(g);
      return;
    }

    final GeometryCollectionIterator it = new GeometryCollectionIterator(g);
    while (it.hasNext()) {
      final Geometry gi = (Geometry)it.next();
      if (!(gi instanceof GeometryCollection)) {
        atomicGeoms.add(gi);
      }
    }
  }

  public static Geometry toGeometryCollection(final Geometry g,
    final Geometry g2) {

    final List atomicGeoms = new ArrayList();
    if (g != null) {
      addComponents(g, atomicGeoms);
    }
    if (g2 != null) {
      addComponents(g2, atomicGeoms);
    }
    return g.getGeometryFactory().createGeometryCollection(
      GeometryFactory.toGeometryArray(atomicGeoms));
  }

  public static Geometry toLines(final Geometry g) {
    return g.getGeometryFactory().buildGeometry(
      LinearComponentExtracter.getLines(g));
  }

  public static Geometry toPoints(final Geometry g) {
    return g.getGeometryFactory().createMultiPoint(g.getCoordinateArray());
  }

}
