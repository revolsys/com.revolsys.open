package com.revolsys.jtstest.testbuilder.geom;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.revolsys.jts.algorithm.PointLocator;
import com.revolsys.jts.geom.Coordinates;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.jts.geom.LineString;
import com.revolsys.jts.geom.LinearRing;
import com.revolsys.jts.geom.Location;
import com.revolsys.jts.geom.Point;
import com.revolsys.jts.geom.Polygon;
import com.revolsys.jts.geom.Polygonal;

public class GeometryCombiner {
  public static List extractElements(final Geometry geom,
    final boolean skipEmpty) {
    final List elem = new ArrayList();
    if (geom == null) {
      return elem;
    }

    for (int i = 0; i < geom.getGeometryCount(); i++) {
      final Geometry elemGeom = geom.getGeometry(i);
      if (skipEmpty && elemGeom.isEmpty()) {
        continue;
      }
      elem.add(elemGeom);
    }
    return elem;
  }

  private static Polygon findPolygonContaining(final Geometry geom,
    final Coordinates pt) {
    final PointLocator locator = new PointLocator();
    for (int i = 0; i < geom.getGeometryCount(); i++) {
      final Polygon poly = (Polygon)geom.getGeometry(i);
      final Location loc = locator.locate(pt, poly);
      if (loc == Location.INTERIOR) {
        return poly;
      }
    }
    return null;
  }

  public static Geometry replace(final Geometry parent,
    final Geometry original, final Geometry replacement) {
    final List elem = extractElements(parent, false);
    Collections.replaceAll(elem, original, replacement);
    return parent.getGeometryFactory().buildGeometry(elem);
  }

  private final GeometryFactory geomFactory;

  public GeometryCombiner(final GeometryFactory geomFactory) {
    this.geomFactory = geomFactory;
  }

  public Polygon addHole(final Polygon poly, final LinearRing hole) {
    final List<LinearRing> rings = poly.getRings();
    rings.add(hole);
    return geomFactory.polygon(rings);
  }

  public Geometry addLineString(final Geometry orig, final Coordinates[] pts) {
    final LineString line = geomFactory.lineString(pts);
    return combine(orig, line);
  }

  public Geometry addPoint(final Geometry orig, final Coordinates pt) {
    final Point point = geomFactory.point(pt);
    return combine(orig, point);
  }

  public Geometry addPolygonRing(final Geometry orig, final Coordinates[] pts) {
    final LinearRing ring = geomFactory.linearRing(pts);

    if (orig == null) {
      return geomFactory.polygon(ring);
    }
    if (!(orig instanceof Polygonal)) {
      return combine(orig, geomFactory.polygon(ring));
    }
    // add the ring as either a hole or a shell
    final Polygon polyContaining = findPolygonContaining(orig, pts[0]);
    if (polyContaining == null) {
      return combine(orig, geomFactory.polygon(ring));
    }

    // add ring as hole
    final Polygon polyWithHole = addHole(polyContaining, ring);
    return replace(orig, polyContaining, polyWithHole);
  }

  public Geometry combine(final Geometry orig, final Geometry geom) {
    final List origList = extractElements(orig, true);
    final List geomList = extractElements(geom, true);
    origList.addAll(geomList);

    if (origList.size() == 0) {
      // return a clone of the orig geometry
      return orig.clone();
    }
    // return the "simplest possible" geometry
    return geomFactory.buildGeometry(origList);
  }
}
