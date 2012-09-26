package com.revolsys.gis.model.geometry.operation.geomgraph;

/**
 * @version 1.7
 */
import com.revolsys.gis.model.coordinates.Coordinates;

/**
 * Utility functions for working with quadrants, which are numbered as follows:
 * 
 * <pre>
 * 1 | 0
 * --+--
 * 2 | 3
 * 
 * <pre>
 * 
 * @version 1.7
 */
public class Quadrant {
  public static final int NE = 0;

  public static final int NW = 1;

  public static final int SW = 2;

  public static final int SE = 3;

  /**
   * Returns the right-hand quadrant of the halfplane defined by the two
   * quadrants, or -1 if the quadrants are opposite, or the quadrant if they are
   * identical.
   */
  public static int commonHalfPlane(final int quad1, final int quad2) {
    // if quadrants are the same they do not determine a unique common
    // halfplane.
    // Simply return one of the two possibilities
    if (quad1 == quad2) {
      return quad1;
    }
    final int diff = (quad1 - quad2 + 4) % 4;
    // if quadrants are not adjacent, they do not share a common halfplane
    if (diff == 2) {
      return -1;
    }
    //
    final int min = (quad1 < quad2) ? quad1 : quad2;
    final int max = (quad1 > quad2) ? quad1 : quad2;
    // for this one case, the righthand plane is NOT the minimum index;
    if (min == 0 && max == 3) {
      return 3;
    }
    // in general, the halfplane index is the minimum of the two adjacent
    // quadrants
    return min;
  }

  /**
   * Returns whether the given quadrant lies within the given halfplane
   * (specified by its right-hand quadrant).
   */
  public static boolean isInHalfPlane(final int quad, final int halfPlane) {
    if (halfPlane == SE) {
      return quad == SE || quad == SW;
    }
    return quad == halfPlane || quad == halfPlane + 1;
  }

  /**
   * Returns true if the given quadrant is 0 or 1.
   */
  public static boolean isNorthern(final int quad) {
    return quad == NE || quad == NW;
  }

  /**
   * Returns true if the quadrants are 1 and 3, or 2 and 4
   */
  public static boolean isOpposite(final int quad1, final int quad2) {
    if (quad1 == quad2) {
      return false;
    }
    final int diff = (quad1 - quad2 + 4) % 4;
    // if quadrants are not adjacent, they are opposite
    if (diff == 2) {
      return true;
    }
    return false;
  }

  /**
   * Returns the quadrant of a directed line segment from p0 to p1.
   * 
   * @throws IllegalArgumentException if the points are equal
   */
  public static int quadrant(final Coordinates p0, final Coordinates p1) {
    if (p1.getX() == p0.getX() && p1.getY() == p0.getY()) {
      throw new IllegalArgumentException(
        "Cannot compute the quadrant for two identical points " + p0);
    }

    if (p1.getX() >= p0.getX()) {
      if (p1.getY() >= p0.getY()) {
        return NE;
      } else {
        return SE;
      }
    } else {
      if (p1.getY() >= p0.getY()) {
        return NW;
      } else {
        return SW;
      }
    }
  }

  /**
   * Returns the quadrant of a directed line segment (specified as x and y
   * displacements, which cannot both be 0).
   * 
   * @throws IllegalArgumentException if the displacements are both 0
   */
  public static int quadrant(final double dx, final double dy) {
    if (dx == 0.0 && dy == 0.0) {
      throw new IllegalArgumentException(
        "Cannot compute the quadrant for point ( " + dx + ", " + dy + " )");
    }
    if (dx >= 0.0) {
      if (dy >= 0.0) {
        return NE;
      } else {
        return SE;
      }
    } else {
      if (dy >= 0.0) {
        return NW;
      } else {
        return SW;
      }
    }
  }
}
