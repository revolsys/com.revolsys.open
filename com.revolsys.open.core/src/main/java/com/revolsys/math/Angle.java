/*
 * The JTS Topology Suite is a collection of Java classes that
 * implement the fundamental operations required to validate a given
 * geo-spatial data set to a known topological specification.
 *
 * Copyright (C) 2001 Vivid Solutions
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * For more information, contact:
 *
 *     Vivid Solutions
 *     Suite #1A
 *     2328 Government Street
 *     Victoria BC  V8T 5G5
 *     Canada
 *
 *     (250)385-6040
 *     www.vividsolutions.com
 */
package com.revolsys.math;

import com.revolsys.geometry.algorithm.CGAlgorithms;
import com.revolsys.geometry.model.Point;
import com.revolsys.util.MathUtil;

/**
 * Utility functions for working with angles.
 * Unless otherwise noted, methods in this class express angles in radians.
 */
public class Angle {
  /** Constant representing clockwise orientation */
  public static final int CLOCKWISE = CGAlgorithms.CLOCKWISE;

  /** Constant representing counterclockwise orientation */
  public static final int COUNTERCLOCKWISE = CGAlgorithms.COUNTERCLOCKWISE;

  /** Constant representing no orientation */
  public static final int NONE = CGAlgorithms.COLLINEAR;

  public static final double PI_OVER_2 = Math.PI / 2.0;

  public static final double PI_OVER_4 = Math.PI / 4.0;

  public static final double PI_TIMES_2 = 2.0 * Math.PI;

  /**
   * Calculate the angle of a coordinates
   *
   * @param x The x coordinate.
   * @param y The y coordinate.
   * @return The distance.
   */
  public static double angle(final double x, final double y) {
    final double angle = Math.atan2(y, x);
    return angle;
  }

  /**
   * Calculate the angle between three coordinates.
   *
   * @param x1 The first x coordinate.
   * @param y1 The first y coordinate.
   * @param x2 The second x coordinate.
   * @param y2 The second y coordinate.
   * @param x3 The third x coordinate.
   * @param y3 The third y coordinate.
   * @return The distance.
   */
  public static double angle(final double x1, final double y1, final double x2, final double y2,
    final double x3, final double y3) {
    final double angle1 = angle2d(x2, y2, x1, y1);
    final double angle2 = angle2d(x2, y2, x3, y3);
    return angleDiff(angle1, angle2);
  }

  /**
   * Returns the angle that the vector from (0,0) to p,
   * relative to the positive X-axis.
   * The angle is normalized to be in the range ( -Pi, Pi ].
   *
   * @return the normalized angle (in radians) that p makes with the positive x-axis.
   */
  public static double angle(final Point p) {
    final double x = p.getX();
    final double y = p.getY();
    return angle(x, y);
  }

  public static double angle(final Point p1, final Point p2, final Point p3) {
    final double x1 = p1.getX();
    final double y1 = p1.getY();
    final double x2 = p2.getX();
    final double y2 = p2.getY();
    final double x3 = p3.getX();
    final double y3 = p3.getY();
    return angle(x1, y1, x2, y2, x3, y3);
  }

  public static double angle2d(final double x1, final double y1, final double x2, final double y2) {
    final double dx = x2 - x1;
    final double dy = y2 - y1;
    return Math.atan2(dy, dx);
  }

  /**
   * Returns the unoriented smallest angle between two vectors.
   * The computed angle will be in the range [0, Pi).
   *
   * @param tip1 the tip of one vector
   * @param tail the tail of each vector
   * @param tip2 the tip of the other vector
   * @return the angle between tail-tip1 and tail-tip2
   */
  public static double angleBetween(final Point tip1, final Point tail, final Point tip2) {
    final double a1 = tail.angle2d(tip1);
    final double a2 = tail.angle2d(tip2);

    return diff(a1, a2);
  }

  /**
   * Returns the oriented smallest angle between two vectors.
   * The computed angle will be in the range (-Pi, Pi].
   * A positive result corresponds to a counterclockwise
   * (CCW) rotation
   * from v1 to v2;
   * a negative result corresponds to a clockwise (CW) rotation;
   * a zero result corresponds to no rotation.
   *
   * @param tip1 the tip of v1
   * @param tail the tail of each vector
   * @param tip2 the tip of v2
   * @return the angle between v1 and v2, relative to v1
   */
  public static double angleBetweenOriented(final Point tip1, final Point tail, final Point tip2) {
    final double a1 = tail.angle2d(tip1);
    final double a2 = tail.angle2d(tip2);
    final double angDel = a2 - a1;

    // normalize, maintaining orientation
    if (angDel <= -Math.PI) {
      return angDel + PI_TIMES_2;
    }
    if (angDel > Math.PI) {
      return angDel - PI_TIMES_2;
    }
    return angDel;
  }

  public static double angleDegrees(final double x1, final double y1, final double x2,
    final double y2) {
    final double width = x2 - x1;
    final double height = y2 - y1;
    if (width == 0) {
      if (height < 0) {
        return 270;
      } else {
        return 90;
      }
    } else if (height == 0) {
      if (width < 0) {
        return 180;
      } else {
        return 0;
      }
    }
    final double arctan = Math.atan(height / width);
    double degrees = Math.toDegrees(arctan);
    if (width < 0) {
      degrees = 180 + degrees;
    } else {
      degrees = (360 + degrees) % 360;
    }
    return degrees;
  }

  public static double angleDiff(final double ang1, final double ang2) {
    double delAngle;

    if (ang1 < ang2) {
      delAngle = ang2 - ang1;
    } else {
      delAngle = ang1 - ang2;
    }

    if (delAngle > Math.PI) {
      delAngle = 2 * Math.PI - delAngle;
    }

    return delAngle;
  }

  public static double angleDiff(final double angle1, final double angle2,
    final boolean clockwise) {
    if (clockwise) {
      if (angle2 < angle1) {
        final double angle = angle2 + Math.PI * 2 - angle1;
        return angle;
      } else {
        final double angle = angle2 - angle1;
        return angle;
      }
    } else {
      if (angle1 < angle2) {
        final double angle = angle1 + Math.PI * 2 - angle2;
        return angle;
      } else {
        final double angle = angle1 - angle2;
        return angle;
      }
    }
  }

  public static double angleDiffDegrees(final double a, final double b) {
    final double largest = Math.max(a, b);
    final double smallest = Math.min(a, b);
    double diff = largest - smallest;
    if (diff > 180) {
      diff = 360 - diff;
    }
    return diff;
  }

  public static double angleNorthDegrees(final double x1, final double y1, final double x2,
    final double y2) {
    final double angle = angleDegrees(x1, y1, x2, y2);
    return MathUtil.getNorthClockwiseAngle(angle);
  }

  /**
   * Computes the unoriented smallest difference between two angles.
   * The angles are assumed to be normalized to the range [-Pi, Pi].
   * The result will be in the range [0, Pi].
   *
   * @param ang1 the angle of one vector (in [-Pi, Pi] )
   * @param ang2 the angle of the other vector (in range [-Pi, Pi] )
   * @return the angle (in radians) between the two vectors (in range [0, Pi] )
   */
  public static double diff(final double ang1, final double ang2) {
    double delAngle;

    if (ang1 < ang2) {
      delAngle = ang2 - ang1;
    } else {
      delAngle = ang1 - ang2;
    }

    if (delAngle > Math.PI) {
      delAngle = 2 * Math.PI - delAngle;
    }

    return delAngle;
  }

  /**
   * Returns whether an angle must turn clockwise or counterclockwise
   * to overlap another angle.
   *
   * @param ang1 an angle (in radians)
   * @param ang2 an angle (in radians)
   * @return whether a1 must turn CLOCKWISE, COUNTERCLOCKWISE or NONE to
   * overlap a2.
   */
  public static int getTurn(final double ang1, final double ang2) {
    final double crossproduct = Math.sin(ang2 - ang1);

    if (crossproduct > 0) {
      return COUNTERCLOCKWISE;
    }
    if (crossproduct < 0) {
      return CLOCKWISE;
    }
    return NONE;
  }

  /**
   * Computes the interior angle between two segments of a ring. The ring is
   * assumed to be oriented in a clockwise direction. The computed angle will be
   * in the range [0, 2Pi]
   *
   * @param p0
   *          a point of the ring
   * @param p1
   *          the next point of the ring
   * @param p2
   *          the next point of the ring
   * @return the interior angle based at <code>p1</code>
   */
  public static double interiorAngle(final Point p0, final Point p1, final Point p2) {
    final double anglePrev = p1.angle2d(p0);
    final double angleNext = p1.angle2d(p2);
    return Math.abs(angleNext - anglePrev);
  }

  /**
   * Tests whether the angle between p0-p1-p2 is acute.
   * An angle is acute if it is less than 90 degrees.
   * <p>
   * Note: this implementation is not precise (determistic) for angles very close to 90 degrees.
   *
   * @param p0 an endpoint of the angle
   * @param p1 the base of the angle
   * @param p2 the other endpoint of the angle
   */
  public static boolean isAcute(final Point p0, final Point p1, final Point p2) {
    // relies on fact that A dot B is positive iff A ang B is acute
    final double dx0 = p0.getX() - p1.getX();
    final double dy0 = p0.getY() - p1.getY();
    final double dx1 = p2.getX() - p1.getX();
    final double dy1 = p2.getY() - p1.getY();
    final double dotprod = dx0 * dx1 + dy0 * dy1;
    return dotprod > 0;
  }

  /**
   * Tests whether the angle between p0-p1-p2 is obtuse.
   * An angle is obtuse if it is greater than 90 degrees.
   * <p>
   * Note: this implementation is not precise (determistic) for angles very close to 90 degrees.
   *
   * @param p0 an endpoint of the angle
   * @param p1 the base of the angle
   * @param p2 the other endpoint of the angle
   */
  public static boolean isObtuse(final Point p0, final Point p1, final Point p2) {
    // relies on fact that A dot B is negative iff A ang B is obtuse
    final double dx0 = p0.getX() - p1.getX();
    final double dy0 = p0.getY() - p1.getY();
    final double dx1 = p2.getX() - p1.getX();
    final double dy1 = p2.getY() - p1.getY();
    final double dotprod = dx0 * dx1 + dy0 * dy1;
    return dotprod < 0;
  }

  /**
   * Computes the normalized value of an angle, which is the
   * equivalent angle in the range ( -Pi, Pi ].
   *
   * @param angle the angle to normalize
   * @return an equivalent angle in the range (-Pi, Pi]
   */
  public static double normalize(double angle) {
    while (angle > Math.PI) {
      angle -= PI_TIMES_2;
    }
    while (angle <= -Math.PI) {
      angle += PI_TIMES_2;
    }
    return angle;
  }

  /**
   * Computes the normalized positive value of an angle, which is the
   * equivalent angle in the range [ 0, 2*Pi ).
   * E.g.:
   * <ul>
   * <li>normalizePositive(0.0) = 0.0
   * <li>normalizePositive(-PI) = PI
   * <li>normalizePositive(-2PI) = 0.0
   * <li>normalizePositive(-3PI) = PI
   * <li>normalizePositive(-4PI) = 0
   * <li>normalizePositive(PI) = PI
   * <li>normalizePositive(2PI) = 0.0
   * <li>normalizePositive(3PI) = PI
   * <li>normalizePositive(4PI) = 0.0
   * </ul>
   *
   * @param angle the angle to normalize, in radians
   * @return an equivalent positive angle
   */
  public static double normalizePositive(double angle) {
    if (angle < 0.0) {
      while (angle < 0.0) {
        angle += PI_TIMES_2;
      }
      // in case round-off error bumps the value over
      if (angle >= PI_TIMES_2) {
        angle = 0.0;
      }
    } else {
      while (angle >= PI_TIMES_2) {
        angle -= PI_TIMES_2;
      }
      // in case round-off error bumps the value under
      if (angle < 0.0) {
        angle = 0.0;
      }
    }
    return angle;
  }

  /**
   * Converts from radians to degrees.
   * @param radians an angle in radians
   * @return the angle in degrees
   */
  public static double toDegrees(final double radians) {
    return radians * 180 / Math.PI;
  }

  /**
   * Converts from degrees to radians.
   *
   * @param angleDegrees an angle in degrees
   * @return the angle in radians
   */
  public static double toRadians(final double angleDegrees) {
    return angleDegrees * Math.PI / 180.0;
  }
}
