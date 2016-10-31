package com.revolsys.geometry.algorithm;

import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.LineString;

/**
 * Computes various distance functions for determining how far apart are two
 * matched segments. In general these functions use the Hausdorff distance to
 * compute how far apart geometries are, since the normal Euclidean distance is
 * not a useful measure of "far apartness".
 */
public final class MatchDistance {

  private static double farLength(final Geometry a, final Geometry b, final double tolerance) {
    final Geometry farA = a.difference(b.buffer(tolerance));
    final double farALen = farA.getLength();
    return farALen;
  }

  /**
   * Computes the maximum distance apart between two linestrings. (Note this is
   * NOT the distance between the two furthest points on the linestrings, which
   * is not a useful measure of "farness").
   *
   * @param a
   * @param b
   * @return
   */
  public static double maxDistance(final LineString a, final LineString b) {
    return VertexHausdorffDistance.distance(a, b);
  }

  /**
   * Computes the fraction of length of LineStrings which is within a given
   * tolerance value, after trimming. The previously computed maxDistance
   * between the lines can be supplied to allow optimizing the calculation (if
   * maxDistance < tolerance, the nearness fraction = 1.0).
   *
   * @param a a LineString
   * @param b a LineString
   * @param fullDistance the full distance between the lines (previously
   *          computed)
   * @param tolerance the distance beyond which to total the length
   * @return the fraction of length beyond the tolerance
   */
  public static double nearnessFraction(final LineString a, final LineString b,
    final double tolerance) {
    final double lenA = a.getLength();
    final double lenB = b.getLength();
    final double lenAB = lenA + lenB;
    // this can happen if the segments are badly aligned and get trimmed to
    // points
    if (lenAB <= 0.0) {
      final boolean inTolerance = a.distance(b) <= tolerance;
      if (inTolerance) {
        return 0.0;
      } else {
        return 1.0;
      }
    }

    final double farLenA = farLength(a, b, tolerance);
    final double farLenB = farLength(b, a, tolerance);
    final double nearLenA = lenA - farLenA;
    final double nearLenB = lenB - farLenB;

    // avoid division by 0.0

    double nearPctA;
    if (lenA > 0) {
      nearPctA = nearLenA / lenA;
    } else {
      nearPctA = 0.0;
    }
    double nearPctB;
    if (lenB > 0) {
      nearPctB = nearLenB / lenB;
    } else {
      nearPctB = 0.0;
    }

    // choose the worst case scenario as the final value
    final double nearnessFrac = Math.min(nearPctA, nearPctB);
    return nearnessFrac;
  }

  /**
   * Computes the fraction of length of LineStrings which is within a given
   * tolerance value, after trimming.
   *
   * @param a a LineString
   * @param b a LineString
   * @param tolerance the distance beyond which to total the length
   * @param trimLines <code>true</code> if the computation should take the
   *          trimmed lines into account
   * @return the fraction of matched line length beyond the tolerance
   */
  public static double nearnessFraction(final LineString a, final LineString b,
    final double tolerance, final boolean trimLines) {
    double nearnessFrac = nearnessFraction(a, b, tolerance);

    if (trimLines) {
      final LineString trimmedA = a.getMaximalNearestSubline(b);
      final LineString trimmedB = b.getMaximalNearestSubline(a);
      final double trimmedNF = nearnessFraction(trimmedA, trimmedB, tolerance);
      // choose the largest fraction
      // (it can happen that the original nearness is greater, if the lines are
      // not well-aligned)
      if (trimmedNF > nearnessFrac) {
        nearnessFrac = trimmedNF;
      }
    }
    return nearnessFrac;
  }

  /**
   * Computes the fraction of length of matched LineStrings which is nearer than
   * a given tolerance value (optionally after trimming). The previously
   * computed maxDistance between the lines can be supplied to allow optimizing
   * the calculation (if maxDistance < tolerance, the nearness fraction = 1.0).
   *
   * @param a a LineString
   * @param b a LineString
   * @param maxDistance the maximum distance between the lines (if previously
   *          computed)
   * @param tolerance the distance beyond which to total the length
   * @param trimLines <code>true</code> if the computation should take the
   *          trimmed lines into account
   * @return the fraction of matched line length beyond the tolerance
   */
  public static double nearnessFraction(final LineString a, final LineString b,
    final double maxDistance, final double tolerance, final boolean trimLines) {
    // if the orginal lines are closer than the tolerance there is no need for
    // further computation
    if (maxDistance < tolerance) {
      return 1.0;
    }
    return nearnessFraction(a, b, tolerance, trimLines);
  }

  /**
   * Computes how far apart are two linestrings after trimming any unmatched
   * length at the ends.
   *
   * @param a
   * @param b
   * @return
   * @see MaximalNearestSubline
   */
  public static double trimmedDistance(final LineString a, final LineString b) {
    final LineString trimA = a.getMaximalNearestSubline(b);
    final LineString trimB = b.getMaximalNearestSubline(a);
    return VertexHausdorffDistance.distance(trimA, trimB);
  }

  private MatchDistance() {
  }
}
