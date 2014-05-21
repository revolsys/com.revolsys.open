package com.revolsys.jtstest.function;

import com.revolsys.gis.model.coordinates.list.CoordinatesListUtil;
import com.revolsys.jts.algorithm.CGAlgorithms;
import com.revolsys.jts.algorithm.CGAlgorithmsDD;
import com.revolsys.jts.algorithm.RobustLineIntersector;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.Point;

public class CGAlgorithmFunctions {
  public static int orientationIndex(final Geometry segment,
    final Geometry ptGeom) {
    if (segment.getVertexCount() != 2 || ptGeom.getVertexCount() != 1) {
      throw new IllegalArgumentException(
        "A must have two points and B must have one");
    }
    final Point[] segPt = CoordinatesListUtil.getCoordinateArray(segment);

    final Point p = ptGeom.getPoint();
    final int index = CGAlgorithmsDD.orientationIndex(segPt[0], segPt[1], p);
    return index;
  }

  public static int orientationIndexDD(final Geometry segment,
    final Geometry ptGeom) {
    if (segment.getVertexCount() != 2 || ptGeom.getVertexCount() != 1) {
      throw new IllegalArgumentException(
        "A must have two points and B must have one");
    }
    final Point[] segPt = CoordinatesListUtil.getCoordinateArray(segment);

    final Point p = ptGeom.getPoint();
    final int index = CGAlgorithmsDD.orientationIndex(segPt[0], segPt[1], p);
    return index;
  }

  public static Geometry segmentIntersection(final Geometry g1,
    final Geometry g2) {
    final Point[] pt1 = CoordinatesListUtil.getCoordinateArray(g1);
    final Point[] pt2 = CoordinatesListUtil.getCoordinateArray(g2);
    final RobustLineIntersector ri = new RobustLineIntersector();
    ri.computeIntersection(pt1[0], pt1[1], pt2[0], pt2[1]);
    switch (ri.getIntersectionNum()) {
      case 0:
        // no intersection => return empty point
        return g1.getGeometryFactory().point();
      case 1:
        // return point
        return g1.getGeometryFactory().point(ri.getIntersection(0));
      case 2:
        // return line
        return g1.getGeometryFactory().lineString(new Point[] {
          ri.getIntersection(0), ri.getIntersection(1)
        });
    }
    return null;
  }

  public static Geometry segmentIntersectionDD(final Geometry g1,
    final Geometry g2) {
    final Point[] pt1 = CoordinatesListUtil.getCoordinateArray(g1);
    final Point[] pt2 = CoordinatesListUtil.getCoordinateArray(g2);

    // first check if there actually is an intersection
    final RobustLineIntersector ri = new RobustLineIntersector();
    ri.computeIntersection(pt1[0], pt1[1], pt2[0], pt2[1]);
    if (!ri.hasIntersection()) {
      // no intersection => return empty point
      return g1.getGeometryFactory().point();
    }

    final Point intPt = CGAlgorithmsDD.intersection(pt1[0], pt1[1],
      pt2[0], pt2[1]);
    return g1.getGeometryFactory().point(intPt);
  }

  public static boolean segmentIntersects(final Geometry g1, final Geometry g2) {
    final Point[] pt1 = CoordinatesListUtil.getCoordinateArray(g1);
    final Point[] pt2 = CoordinatesListUtil.getCoordinateArray(g2);
    final RobustLineIntersector ri = new RobustLineIntersector();
    ri.computeIntersection(pt1[0], pt1[1], pt2[0], pt2[1]);
    return ri.hasIntersection();
  }

}
