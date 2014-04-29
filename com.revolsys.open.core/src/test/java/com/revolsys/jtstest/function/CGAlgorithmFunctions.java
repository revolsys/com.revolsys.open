package com.revolsys.jtstest.function;

import com.revolsys.jts.algorithm.CGAlgorithms;
import com.revolsys.jts.algorithm.CGAlgorithmsDD;
import com.revolsys.jts.algorithm.RobustLineIntersector;
import com.revolsys.jts.geom.Coordinates;
import com.revolsys.jts.geom.Geometry;

public class CGAlgorithmFunctions {
  public static int orientationIndex(final Geometry segment,
    final Geometry ptGeom) {
    if (segment.getVertexCount() != 2 || ptGeom.getVertexCount() != 1) {
      throw new IllegalArgumentException(
        "A must have two points and B must have one");
    }
    final Coordinates[] segPt = segment.getCoordinateArray();

    final Coordinates p = ptGeom.getCoordinate();
    final int index = CGAlgorithms.orientationIndex(segPt[0], segPt[1], p);
    return index;
  }

  public static int orientationIndexDD(final Geometry segment,
    final Geometry ptGeom) {
    if (segment.getVertexCount() != 2 || ptGeom.getVertexCount() != 1) {
      throw new IllegalArgumentException(
        "A must have two points and B must have one");
    }
    final Coordinates[] segPt = segment.getCoordinateArray();

    final Coordinates p = ptGeom.getCoordinate();
    final int index = CGAlgorithmsDD.orientationIndex(segPt[0], segPt[1], p);
    return index;
  }

  public static Geometry segmentIntersection(final Geometry g1,
    final Geometry g2) {
    final Coordinates[] pt1 = g1.getCoordinateArray();
    final Coordinates[] pt2 = g2.getCoordinateArray();
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
        return g1.getGeometryFactory().lineString(new Coordinates[] {
          ri.getIntersection(0), ri.getIntersection(1)
        });
    }
    return null;
  }

  public static Geometry segmentIntersectionDD(final Geometry g1,
    final Geometry g2) {
    final Coordinates[] pt1 = g1.getCoordinateArray();
    final Coordinates[] pt2 = g2.getCoordinateArray();

    // first check if there actually is an intersection
    final RobustLineIntersector ri = new RobustLineIntersector();
    ri.computeIntersection(pt1[0], pt1[1], pt2[0], pt2[1]);
    if (!ri.hasIntersection()) {
      // no intersection => return empty point
      return g1.getGeometryFactory().point();
    }

    final Coordinates intPt = CGAlgorithmsDD.intersection(pt1[0], pt1[1],
      pt2[0], pt2[1]);
    return g1.getGeometryFactory().point(intPt);
  }

  public static boolean segmentIntersects(final Geometry g1, final Geometry g2) {
    final Coordinates[] pt1 = g1.getCoordinateArray();
    final Coordinates[] pt2 = g2.getCoordinateArray();
    final RobustLineIntersector ri = new RobustLineIntersector();
    ri.computeIntersection(pt1[0], pt1[1], pt2[0], pt2[1]);
    return ri.hasIntersection();
  }

}
