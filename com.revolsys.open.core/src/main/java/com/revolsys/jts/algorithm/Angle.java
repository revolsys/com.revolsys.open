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
package com.revolsys.jts.algorithm;

import com.revolsys.jts.geom.Point;

/**
 * Utility functions for working with angles.
 * Unless otherwise noted, methods in this class express angles in radians.
 */
public class Angle {
  public static final double PI_TIMES_2 = 2.0 * Math.PI;

  public static final double PI_OVER_2 = Math.PI / 2.0;

  public static final double PI_OVER_4 = Math.PI / 4.0;

  /** Constant representing counterclockwise orientation */
  public static final int COUNTERCLOCKWISE = CGAlgorithms.COUNTERCLOCKWISE;

  /** Constant representing clockwise orientation */
  public static final int CLOCKWISE = CGAlgorithms.CLOCKWISE;

  /** Constant representing no orientation */
  public static final int NONE = CGAlgorithms.COLLINEAR;

  /**
   * Returns the angle that the vector from (0,0) to p,
   * relative to the positive X-axis.
   * The angle is normalized to be in the range ( -Pi, Pi ].
   *
   * @return the normalized angle (in radians) that p makes with the positive x-axis.
   */
  public static double angle(final Point p) {
    return Math.atan2(p.getY(), p.getX());
  }

  /**
   * Returns the angle of the vector from p0 to p1,
   * relative to the positive X-axis.
   * The angle is normalized to be in the range [ -Pi, Pi ].
   *
   * @return the normalized angle (in radians) that p0-p1 makes with the positive x-axis.
   */
  public static double angle(final Point p0, final Point p1) {
    final double dx = p1.getX() - p0.getX();
    final double dy = p1.getY() - p0.getY();
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
  public static double angleBetween(final Point tip1, final Point tail,
    final Point tip2) {
    final double a1 = angle(tail, tip1);
    final double a2 = angle(tail, tip2);

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
  public static double angleBetweenOriented(final Point tip1, final Point tail,
    final Point tip2) {
    final double a1 = angle(tail, tip1);
    final double a2 = angle(tail, tip2);
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
      delAngle = (2 * Math.PI) - delAngle;
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
  public static double interiorAngle(final Point p0, final Point p1,
    final Point p2) {
    final double anglePrev = Angle.angle(p1, p0);
    final double angleNext = Angle.angle(p1, p2);
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
    return (radians * 180) / (Math.PI);
  }

  /**
   * Converts from degrees to radians.
   *
   * @param angleDegrees an angle in degrees
   * @return the angle in radians
   */
  public static double toRadians(final double angleDegrees) {
    return (angleDegrees * Math.PI) / 180.0;
  }
}
