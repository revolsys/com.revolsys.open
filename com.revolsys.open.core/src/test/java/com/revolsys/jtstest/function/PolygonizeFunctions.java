package com.revolsys.jtstest.function;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.util.LineStringExtracter;
import com.revolsys.geometry.operation.polygonize.Polygonizer;

public class PolygonizeFunctions {

  public static Geometry polygonize(final Geometry g) {
    final List lines = LineStringExtracter.getLines(g);
    final Polygonizer polygonizer = new Polygonizer();
    polygonizer.add(lines);
    final Collection polys = polygonizer.getPolygons();
    return g.getGeometryFactory().geometryCollection(polys);
  }

  public static Geometry polygonizeAllErrors(final Geometry g) {
    final List lines = LineStringExtracter.getLines(g);
    final Polygonizer polygonizer = new Polygonizer();
    polygonizer.add(lines);
    final List errs = new ArrayList();
    errs.addAll(polygonizer.getDangles());
    errs.addAll(polygonizer.getCutEdges());
    errs.addAll(polygonizer.getInvalidRingLines());
    return g.getGeometryFactory().buildGeometry(errs);
  }

  public static Geometry polygonizeCutEdges(final Geometry g) {
    final List lines = LineStringExtracter.getLines(g);
    final Polygonizer polygonizer = new Polygonizer();
    polygonizer.add(lines);
    final Collection geom = polygonizer.getCutEdges();
    return g.getGeometryFactory().buildGeometry(geom);
  }

  public static Geometry polygonizeDangles(final Geometry g) {
    final List lines = LineStringExtracter.getLines(g);
    final Polygonizer polygonizer = new Polygonizer();
    polygonizer.add(lines);
    final Collection geom = polygonizer.getDangles();
    return g.getGeometryFactory().buildGeometry(geom);
  }

  public static Geometry polygonizeInvalidRingLines(final Geometry g) {
    final List lines = LineStringExtracter.getLines(g);
    final Polygonizer polygonizer = new Polygonizer();
    polygonizer.add(lines);
    final Collection geom = polygonizer.getInvalidRingLines();
    return g.getGeometryFactory().buildGeometry(geom);
  }

}
