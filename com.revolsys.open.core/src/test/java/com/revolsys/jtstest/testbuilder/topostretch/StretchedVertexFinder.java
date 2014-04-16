package com.revolsys.jtstest.testbuilder.topostretch;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import com.revolsys.jts.algorithm.CGAlgorithms;
import com.revolsys.jts.geom.BoundingBox;
import com.revolsys.jts.geom.CoordinateArrays;
import com.revolsys.jts.geom.Coordinates;
import com.revolsys.jts.geom.LineSegment;
import com.revolsys.jts.geom.LineString;

class StretchedVertexFinder {
  private static LineSegment distSeg = new LineSegment();

  private static boolean contains(final BoundingBox env, final Coordinates p0,
    final Coordinates p1) {
    if (!env.contains(p0)) {
      return false;
    }
    if (!env.contains(p1)) {
      return false;
    }
    return true;
  }

  private static double distanceToSeg(final Coordinates p,
    final Coordinates p0, final Coordinates p1) {
    distSeg.setP0(p0);
    distSeg.setP1(p1);
    double segDist = distSeg.distance(p);

    // robust calculation of zero distance
    if (CGAlgorithms.computeOrientation(p0, p1, p) == CGAlgorithms.COLLINEAR) {
      segDist = 0.0;
    }

    return segDist;
  }

  public static List findNear(final Collection linestrings,
    final double tolerance, final BoundingBox mask) {
    final StretchedVertexFinder finder = new StretchedVertexFinder(linestrings,
      tolerance, mask);
    return finder.getNearVertices();
  }

  private static int geomPointsLen(final Coordinates[] pts) {
    int n = pts.length;
    // don't process the last point of a ring twice
    if (CoordinateArrays.isRing(pts)) {
      n = pts.length - 1;
    }
    return n;
  }

  private static boolean isPointNearButNotOnSeg(final Coordinates p,
    final Coordinates p0, final Coordinates p1, final double distTol) {
    // don't rely on segment distance algorithm to correctly compute zero
    // distance
    // on segment
    if (CGAlgorithms.computeOrientation(p0, p1, p) == CGAlgorithms.COLLINEAR) {
      return false;
    }

    // compute actual distance
    distSeg.setP0(p0);
    distSeg.setP1(p1);
    final double segDist = distSeg.distance(p);
    if (segDist > distTol) {
      return false;
    }
    return true;
  }

  private final Collection linestrings;

  private double tolerance = 0.0;

  private BoundingBox limitEnv = null;

  private final List nearVerts = new ArrayList();

  public StretchedVertexFinder(final Collection linestrings,
    final double tolerance) {
    this.linestrings = linestrings;
    this.tolerance = tolerance;
  }

  public StretchedVertexFinder(final Collection linestrings,
    final double tolerance, final BoundingBox limitEnv) {
    this(linestrings, tolerance);
    this.limitEnv = limitEnv;
  }

  private void findNearVertex(final Coordinates[] linePts, final int index) {
    for (final Iterator i = linestrings.iterator(); i.hasNext();) {
      final LineString testLine = (LineString)i.next();
      findNearVertex(linePts, index, testLine);
    }
  }

  /**
   * Finds a single near vertex.
   * This is simply the first one found, not necessarily 
   * the nearest.  
   * This choice may sub-optimal, resulting 
   * in odd result geometry.
   * It's not clear that this can be done better, however.
   * If there are several near points, the stretched
   * geometry is likely to be distorted anyway.
   * 
   * @param targetPts
   * @param index
   * @param testLine
   */
  private void findNearVertex(final Coordinates[] targetPts, final int index,
    final LineString testLine) {
    final Coordinates targetPt = targetPts[index];
    final Coordinates[] testPts = testLine.getCoordinateArray();
    // don't process the last point of a ring twice
    final int n = geomPointsLen(testPts);
    for (int i = 0; i < n; i++) {
      final Coordinates testPt = testPts[i];

      StretchedVertex stretchVert = null;

      // is near to vertex?
      final double dist = testPt.distance(targetPt);
      if (dist <= tolerance && dist != 0.0) {
        stretchVert = new StretchedVertex(targetPt, targetPts, index, testPt,
          testPts, i);
      }
      // is near segment?
      else if (i < testPts.length - 1) {
        final Coordinates segEndPt = testPts[i + 1];

        /**
         * Check whether pt is near or equal to other segment endpoint.
         * If near, it will be handled by the near vertex case code.
         * If equal, don't record it at all
         */
        final double distToOther = segEndPt.distance(targetPt);
        if (distToOther <= tolerance) {
          // will be handled as a point-vertex case
          continue;
        }

        // Here we know point is not near the segment endpoints.
        // Check if it is near the segment at all.
        if (isPointNearButNotOnSeg(targetPt, testPt, segEndPt, tolerance)) {
          stretchVert = new StretchedVertex(targetPt, targetPts, i,
            new LineSegment(testPt, testPts[i + 1]));
        }
      }
      if (stretchVert != null) {
        nearVerts.add(stretchVert);
      }
    }
  }

  private void findNearVertices() {
    for (final Iterator i = linestrings.iterator(); i.hasNext();) {
      final LineString line = (LineString)i.next();
      findNearVertices(line);
    }

  }

  private void findNearVertices(final LineString targetLine) {
    final Coordinates[] pts = targetLine.getCoordinateArray();
    // don't process the last point of a ring twice
    final int n = geomPointsLen(pts);
    for (int i = 0; i < n; i++) {
      if (limitEnv.intersects(pts[i])) {
        findNearVertex(pts, i);
      }
    }
  }

  public List getNearVertices() {
    findNearVertices();
    return nearVerts;
  }
}
