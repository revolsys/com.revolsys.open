package com.revolsys.jtstest.testbuilder.topostretch;

import com.revolsys.jts.algorithm.CGAlgorithms;
import com.revolsys.jts.geom.Coordinate;
import com.revolsys.jts.geom.CoordinateArrays;
import com.revolsys.jts.geom.Coordinates;
import com.revolsys.jts.geom.LineSegment;
import com.revolsys.jts.geom.LineSegmentImpl;
import com.revolsys.jts.math.MathUtil;
import com.revolsys.jts.math.Vector2D;

/**
 * Models a vertex of a Geometry which will be stretched
 * due to being too near other segments and vertices.
 * <p>
 * Currently for simplicity a vertex is assumed to 
 * be near only one segment or other vertex.
 * This is sufficient for most cases.
 * 
 * @author Martin Davis
 *
 */
public class StretchedVertex {
  // TODO: also provide information about the segments around the facet the
  // vertex is near to, to allow smarter adjustment

  private final Coordinates vertexPt;

  private final Coordinates[] parentLine;

  private final int parentIndex;

  private Coordinates nearPt = null;

  private Coordinates[] nearPts = null;

  private int nearIndex = -1;

  private LineSegment nearSeg = null;

  private Coordinates stretchedPt = null;

  private static final double MAX_ARM_NEARNESS_ANG = 20.0 / 180.0 * Math.PI;

  private static final double POINT_LINE_FLATNESS_RATIO = 0.01;

  private static boolean isFlat(final Coordinates p, final Coordinates p1,
    final Coordinates p2) {
    final double dist = CGAlgorithms.distancePointLine(p, p1, p2);
    final double len = p1.distance(p2);
    if (dist / len < POINT_LINE_FLATNESS_RATIO) {
      return true;
    }
    return false;
  }

  private static double maxAngleToBisector(final double ang) {
    final double relAng = ang / 2 - MAX_ARM_NEARNESS_ANG;
    if (relAng < 0) {
      return 0;
    }
    return relAng;
  }

  /**
   * Returns an array of pts such that p0 - p[0] - [p1] is CW.
   * 
   * @param p0
   * @param p1
   * @param p2
   * @return
   */
  private static Vector2D normalizedOffset(final Coordinates p0,
    final Coordinates p1, final Coordinates p2) {
    final Vector2D u1 = Vector2D.create(p0, p1).normalize();
    final Vector2D u2 = Vector2D.create(p0, p2).normalize();
    final Vector2D offset = u1.add(u2).normalize();
    return offset;
  }

  /**
   * Returns an array of pts such that p0 - p[0] - [p1] is CW.
   * 
   * @param p0
   * @param p1
   * @param p2
   * @return
   */
  private static Coordinates[] orientCorner(final Coordinates p0,
    final Coordinates p1, final Coordinates p2) {
    Coordinates[] orient;
    // TODO: not sure if determining orientation is necessary?
    if (CGAlgorithms.CLOCKWISE == CGAlgorithms.orientationIndex(p0, p1, p2)) {
      orient = new Coordinates[] {
        p1, p2
      };
    } else {
      orient = new Coordinates[] {
        p2, p1
      };
    }

    return orient;
  }

  /**
   * 
   * @param pt
   * @param cornerBase the two vertices defining the 
   * @param corner the two vertices defining the arms of the corner, oriented CW
   * @return the quadrant the pt lies in
   */
  private static int quadrant(final Coordinates pt,
    final Coordinates cornerBase, final Coordinates[] corner) {
    if (CGAlgorithms.orientationIndex(cornerBase, corner[0], pt) == CGAlgorithms.CLOCKWISE) {
      if (CGAlgorithms.orientationIndex(cornerBase, corner[1], pt) == CGAlgorithms.COUNTERCLOCKWISE) {
        return 0;
      } else {
        return 3;
      }
    } else {
      if (CGAlgorithms.orientationIndex(cornerBase, corner[1], pt) == CGAlgorithms.COUNTERCLOCKWISE) {
        return 1;
      } else {
        return 2;
      }
    }
  }

  private static Coordinates rotateToQuadrant(final Coordinates v,
    final int quadrant) {
    switch (quadrant) {
      case 0:
        return v;
      case 1:
        return new Coordinate(-v.getY(), v.getX());
      case 2:
        return new Coordinate(-v.getX(), -v.getY());
      case 3:
        return new Coordinate(v.getY(), -v.getX());
    }
    return null;
  }

  /**
   * Creates a vertex which lies near a vertex
   */
  public StretchedVertex(final Coordinates vertexPt,
    final Coordinates[] parentLine, final int parentIndex,
    final Coordinates nearPt, final Coordinates[] nearPts, final int nearIndex) {
    this.vertexPt = vertexPt;
    this.parentLine = parentLine;
    this.parentIndex = parentIndex;
    this.nearPt = nearPt;
    this.nearPts = nearPts;
    this.nearIndex = nearIndex;
  }

  /**
   * Creates a vertex for a point which lies near a line segment
   * @param vertexPt
   * @param parentLine
   * @param parentIndex
   * @param nearSeg
   */
  public StretchedVertex(final Coordinates vertexPt,
    final Coordinates[] parentLine, final int parentIndex,
    final LineSegment nearSeg) {
    this.vertexPt = vertexPt;
    this.parentLine = parentLine;
    this.parentIndex = parentIndex;
    this.nearSeg = nearSeg;
  }

  private Coordinates displaceFromCorner(final Coordinates nearPt,
    final Coordinates p1, final Coordinates p2, final double dist) {
    final Coordinates[] corner = orientCorner(nearPt, p1, p2);
    // compute perpendicular bisector of p1-p2
    final Vector2D u1 = Vector2D.create(nearPt, corner[0]).normalize();
    final Vector2D u2 = Vector2D.create(nearPt, corner[1]).normalize();
    final double ang = u1.angle(u2);
    final Vector2D innerBisec = u2.rotate(ang / 2);
    Vector2D offset = innerBisec.multiply(dist);
    if (!isInsideCorner(vertexPt, nearPt, corner[0], corner[1])) {
      offset = offset.multiply(-1);
    }
    return offset.translate(vertexPt);
  }

  /**
   * Displaces a vertex from a corner,
   * with angle limiting
   * used to ensure that the displacement is not close to the arms of the corner.
   * 
   * @param nearPt
   * @param p1
   * @param p2
   * @param dist
   * @return
   */
  private Coordinates displaceFromCornerAwayFromArms(final Coordinates nearPt,
    final Coordinates p1, final Coordinates p2, final double dist) {
    final Coordinates[] corner = orientCorner(nearPt, p1, p2);
    final boolean isInsideCorner = isInsideCorner(vertexPt, nearPt, corner[0],
      corner[1]);

    final Vector2D u1 = Vector2D.create(nearPt, corner[0]).normalize();
    final Vector2D u2 = Vector2D.create(nearPt, corner[1]).normalize();
    final double cornerAng = u1.angle(u2);

    double maxAngToBisec = maxAngleToBisector(cornerAng);

    Vector2D bisec = u2.rotate(cornerAng / 2);
    if (!isInsideCorner) {
      bisec = bisec.multiply(-1);
      final double outerAng = 2 * Math.PI - cornerAng;
      maxAngToBisec = maxAngleToBisector(outerAng);
    }

    final Vector2D pointwiseDisplacement = Vector2D.create(nearPt, vertexPt)
      .normalize();
    final double stretchAng = pointwiseDisplacement.angleTo(bisec);
    final double stretchAngClamp = MathUtil.clamp(stretchAng, -maxAngToBisec,
      maxAngToBisec);
    final Vector2D cornerDisplacement = bisec.rotate(-stretchAngClamp)
      .multiply(dist);

    return cornerDisplacement.translate(vertexPt);
  }

  private Coordinates displaceFromCornerOriginal(final Coordinates nearPt,
    final Coordinates p1, final Coordinates p2, final double dist) {
    // if corner is nearly flat, just displace point
    // TODO: displace from vertex on appropriate side of flat line, with
    // suitable angle
    if (isFlat(nearPt, p1, p2)) {
      return displaceFromFlatCorner(nearPt, p1, p2, dist);
    }

    final Coordinates[] corner = orientCorner(nearPt, p1, p2);

    // find quadrant of corner that vertex pt lies in
    final int quadrant = quadrant(vertexPt, nearPt, corner);

    final Vector2D normOffset = normalizedOffset(nearPt, p1, p2);
    final Vector2D baseOffset = normOffset.multiply(dist);
    final Vector2D rotatedOffset = baseOffset.rotateByQuarterCircle(quadrant);

    return rotatedOffset.translate(vertexPt);
    // return null;
  }

  private Coordinates displaceFromFlatCorner(final Coordinates nearPt,
    final Coordinates p1, final Coordinates p2, final double dist) {
    // compute perpendicular bisector of p1-p2
    final Vector2D bisecVec = Vector2D.create(p2, p1).rotateByQuarterCircle(1);
    final Vector2D offset = bisecVec.normalize().multiply(dist);
    return offset.translate(vertexPt);
  }

  private Coordinates displaceFromPoint(final Coordinates nearPt,
    final double dist) {
    final LineSegment seg = new LineSegmentImpl(nearPt, vertexPt);

    // compute an adjustment which displaces in the direction of the
    // nearPt-vertexPt vector
    // TODO: make this robust!
    final double len = seg.getLength();
    final double frac = (dist + len) / len;
    final Coordinates strPt = seg.pointAlong(frac);
    return strPt;
  }

  private Coordinates displaceFromSeg(final LineSegment nearSeg, double dist) {
    final double frac = nearSeg.projectionFactor(vertexPt);

    // displace away from the segment on the same side as the original point
    final int side = nearSeg.orientationIndex(vertexPt);
    if (side == CGAlgorithms.RIGHT) {
      dist = -dist;
    }

    return nearSeg.pointAlongOffset(frac, dist);
  }

  private Coordinates displaceFromVertex(final Coordinates nearPt,
    final double dist) {
    // handle linestring endpoints - do simple displacement
    if (!isNearRing() && nearIndex == 0 || nearIndex >= nearPts.length - 1) {
      return displaceFromPoint(nearPt, dist);
    }

    // analyze corner to see how to displace the vertex
    // find corner points
    final Coordinates p1 = getNearRingPoint(nearIndex - 1);
    final Coordinates p2 = getNearRingPoint(nearIndex + 1);

    // if vertexPt is identical to an arm of the corner, just displace the point
    if (p1.equals2d(vertexPt) || p2.equals2d(vertexPt)) {
      return displaceFromPoint(nearPt, dist);
    }

    return displaceFromCornerAwayFromArms(nearPt, p1, p2, dist);
  }

  private Coordinates getNearRingPoint(final int i) {
    int index = i;
    if (i < 0) {
      index = i + nearPts.length - 1;
    } else if (i >= nearPts.length - 1) {
      index = i - (nearPts.length - 1);
    }
    return nearPts[index];
  }

  /**
   * Gets the point which this near vertex will be stretched to
   * (by a given distance)
   * 
   * @param dist the distance to adjust the point by 
   * @return the stretched coordinate
   */
  public Coordinates getStretchedVertex(final double dist) {
    if (stretchedPt != null) {
      return stretchedPt;
    }

    if (nearPt != null) {
      stretchedPt = displaceFromVertex(nearPt, dist);
      // stretchedPt = displaceFromPoint(nearPt, dist);
      // displace in direction of segment this pt lies on
    } else {
      stretchedPt = displaceFromSeg(nearSeg, dist);
    }
    return stretchedPt;
  }

  public Coordinates getVertexCoordinate() {
    return vertexPt;
  }

  private boolean isInsideCorner(final Coordinates queryPt,
    final Coordinates base, final Coordinates p1, final Coordinates p2) {
    return CGAlgorithms.orientationIndex(base, p1, queryPt) == CGAlgorithms.CLOCKWISE
      && CGAlgorithms.orientationIndex(base, p2, queryPt) == CGAlgorithms.COUNTERCLOCKWISE;
  }

  private boolean isNearRing() {
    return CoordinateArrays.isRing(nearPts);
  }
}
