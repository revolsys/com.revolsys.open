package com.revolsys.gis.model.geometry.operation.geomgraph;

import java.util.Collection;
import java.util.Comparator;

import com.revolsys.gis.model.coordinates.Coordinates;
import com.revolsys.gis.model.coordinates.DoubleCoordinates;
import com.revolsys.jts.geom.CoordinatesList;
import com.revolsys.jts.math.MathUtil;

/**
 * Useful utility functions for handling Coordinates arrays
 *
 * @version 1.7
 */
public class CoordinateArrays {

  /**
   * A {@link Comparator} for {@link Coordinates} arrays
   * in the forward direction of their coordinates,
   * using lexicographic ordering.
   */
  public static class ForwardComparator implements Comparator {
    @Override
    public int compare(final Object o1, final Object o2) {
      final Coordinates[] pts1 = (Coordinates[])o1;
      final Coordinates[] pts2 = (Coordinates[])o2;

      return CoordinateArrays.compare(pts1, pts2);
    }
  }

  private final static Coordinates[] coordArrayType = new Coordinates[0];

  /**
   * Returns either the given coordinate array if its length is greater than the
   * given amount, or an empty coordinate array.
   */
  public static Coordinates[] atLeastNCoordinatessOrNothing(final int n,
    final Coordinates[] c) {
    return c.length >= n ? c : new Coordinates[] {};
  }

  /**
   * Compares two {@link Coordinates} arrays
   * in the forward direction of their coordinates,
   * using lexicographic ordering.
   *
   * @param pts1
   * @param pts2
   * @return an integer indicating the order
   */
  public static int compare(final Coordinates[] pts1, final Coordinates[] pts2) {
    int i = 0;
    while (i < pts1.length && i < pts2.length) {
      final int compare = pts1[i].compareTo(pts2[i]);
      if (compare != 0) {
        return compare;
      }
      i++;
    }
    // handle situation when arrays are of different length
    if (i < pts2.length) {
      return -1;
    }
    if (i < pts1.length) {
      return 1;
    }

    return 0;
  }

  /**
   * Creates a deep copy of the argument {@link Coordinates} array.
   *
   * @param coordinates an array of Coordinatess
   * @return a deep copy of the input
   */
  public static Coordinates[] copyDeep(final Coordinates[] coordinates) {
    final Coordinates[] copy = new Coordinates[coordinates.length];
    for (int i = 0; i < coordinates.length; i++) {
      copy[i] = new DoubleCoordinates(coordinates[i]);
    }
    return copy;
  }

  /**
   * Creates a deep copy of a given section of a source {@link Coordinates} array
   * into a destination Coordinates array.
   * The destination array must be an appropriate size to receive
   * the copied coordinates.
   *
   * @param src an array of Coordinatess
   * @param srcStart the index to start copying from
   * @param dest the 
   * @param destStart the destination index to start copying to
   * @param length the number of items to copy
   */
  public static void copyDeep(final Coordinates[] src, final int srcStart,
    final Coordinates[] dest, final int destStart, final int length) {
    for (int i = 0; i < length; i++) {
      dest[destStart + i] = new DoubleCoordinates(src[srcStart + i]);
    }
  }

  /**
   * Returns true if the two arrays are identical, both null, or pointwise
   * equal (as compared using Coordinates#equals)
   * @see Coordinates#equals(Object)
   */
  public static boolean equals(final Coordinates[] coord1,
    final Coordinates[] coord2) {
    if (coord1 == coord2) {
      return true;
    }
    if (coord1 == null || coord2 == null) {
      return false;
    }
    if (coord1.length != coord2.length) {
      return false;
    }
    for (int i = 0; i < coord1.length; i++) {
      if (!coord1[i].equals(coord2[i])) {
        return false;
      }
    }
    return true;
  }

  /**
   * Returns true if the two arrays are identical, both null, or pointwise
   * equal, using a user-defined {@link Comparator} for {@link Coordinates} s
   *
   * @param coord1 an array of Coordinatess
   * @param coord2 an array of Coordinatess
   * @param coordinateComparator a Comparator for Coordinatess
   */
  public static boolean equals(final Coordinates[] coord1,
    final Coordinates[] coord2, final Comparator coordinateComparator) {
    if (coord1 == coord2) {
      return true;
    }
    if (coord1 == null || coord2 == null) {
      return false;
    }
    if (coord1.length != coord2.length) {
      return false;
    }
    for (int i = 0; i < coord1.length; i++) {
      if (coordinateComparator.compare(coord1[i], coord2[i]) != 0) {
        return false;
      }
    }
    return true;
  }

  /**
   * Extracts a subsequence of the input {@link Coordinates} array
   * from indices <code>start</code> to
   * <code>end</code> (inclusive).
   * The input indices are clamped to the array size;
   * If the end index is less than the start index,
   * the extracted array will be empty.
   *
   * @param pts the input array
   * @param start the index of the start of the subsequence to extract
   * @param end the index of the end of the subsequence to extract
   * @return a subsequence of the input array
   */
  public static Coordinates[] extract(final Coordinates[] pts, int start,
    int end) {
    start = MathUtil.clamp(start, 0, pts.length);
    end = MathUtil.clamp(end, -1, pts.length);

    int npts = end - start + 1;
    if (end < 0) {
      npts = 0;
    }
    if (start >= pts.length) {
      npts = 0;
    }
    if (end < start) {
      npts = 0;
    }

    final Coordinates[] extractPts = new Coordinates[npts];
    if (npts == 0) {
      return extractPts;
    }

    int iPts = 0;
    for (int i = start; i <= end; i++) {
      extractPts[iPts++] = pts[i];
    }
    return extractPts;
  }

  /**
   * Returns whether #equals returns true for any two consecutive Coordinatess
   * in the given array.
   */
  public static boolean hasRepeatedPoints(final Coordinates[] coord) {
    for (int i = 1; i < coord.length; i++) {
      if (coord[i - 1].equals(coord[i])) {
        return true;
      }
    }
    return false;
  }

  /**
   * Determines which orientation of the {@link Coordinates} array
   * is (overall) increasing.
   * In other words, determines which end of the array is "smaller"
   * (using the standard ordering on {@link Coordinates}).
   * Returns an integer indicating the increasing direction.
   * If the sequence is a palindrome, it is defined to be
   * oriented in a positive direction.
   *
   * @param pts the array of Coordinatess to test
   * @return <code>1</code> if the array is smaller at the start
   * or is a palindrome,
   * <code>-1</code> if smaller at the end
   */
  public static int increasingDirection(final CoordinatesList pts) {
    for (int i = 0; i < pts.size() / 2; i++) {
      final int j = pts.size() - 1 - i;
      // skip equal points on both ends
      final int comp = pts.get(i).compareTo(pts.get(j));
      if (comp != 0) {
        return comp;
      }
    }
    // array must be a palindrome - defined to be in positive direction
    return 1;
  }

  /**
   *  Returns the index of <code>coordinate</code> in <code>coordinates</code>.
   *  The first position is 0; the second, 1; etc.
   *
   *@param  coordinate   the <code>Coordinates</code> to search for
   *@param  coordinates  the array to search
   *@return              the position of <code>coordinate</code>, or -1 if it is
   *      not found
   */
  public static int indexOf(final Coordinates coordinate,
    final Coordinates[] coordinates) {
    for (int i = 0; i < coordinates.length; i++) {
      if (coordinate.equals(coordinates[i])) {
        return i;
      }
    }
    return -1;
  }

  /**
   * Determines whether two {@link Coordinates} arrays of equal length
   * are equal in opposite directions.
   *
   * @param pts1
   * @param pts2
   * @return <code>true</code> if the two arrays are equal in opposite directions.
   */
  private static boolean isEqualReversed(final Coordinates[] pts1,
    final Coordinates[] pts2) {
    for (int i = 0; i < pts1.length; i++) {
      final Coordinates p1 = pts1[i];
      final Coordinates p2 = pts2[pts1.length - i - 1];
      if (p1.compareTo(p2) != 0) {
        return false;
      }
    }
    return true;
  }

  /**
   * Tests whether an array of {@link Coordinates}s forms a ring,
   * by checking length and closure. 
   * Self-intersection is not checked.
   * 
   * @param pts an array of Coordinatess
   * @return true if the coordinate form a ring.
   */
  public static boolean isRing(final Coordinates[] pts) {
    if (pts.length < 4) {
      return false;
    }
    if (!pts[0].equals2d(pts[pts.length - 1])) {
      return false;
    }
    return true;
  }

  /**
   *  Returns the minimum coordinate, using the usual lexicographic comparison.
   *
   *@param  coordinates  the array to search
   *@return              the minimum coordinate in the array, found using <code>compareTo</code>
   *@see Coordinates#compareTo(Object)
   */
  public static Coordinates minCoordinates(final Coordinates[] coordinates) {
    Coordinates minCoord = null;
    for (int i = 0; i < coordinates.length; i++) {
      if (minCoord == null || minCoord.compareTo(coordinates[i]) > 0) {
        minCoord = coordinates[i];
      }
    }
    return minCoord;
  }

  /**
   * Finds a point in a list of points which is not contained in another list of points
   * @param testPts the {@link Coordinates}s to test
   * @param pts an array of {@link Coordinates}s to test the input points against
   * @return a {@link Coordinates} from <code>testPts</code> which is not in <code>pts</code>, '
   * or <code>null</code>
   */
  public static Coordinates ptNotInList(final Coordinates[] testPts,
    final Coordinates[] pts) {
    for (int i = 0; i < testPts.length; i++) {
      final Coordinates testPt = testPts[i];
      if (CoordinateArrays.indexOf(testPt, pts) < 0) {
        return testPt;
      }
    }
    return null;
  }

  /**
   * Reverses the coordinates in an array in-place.
   */
  public static void reverse(final Coordinates[] coord) {
    final int last = coord.length - 1;
    final int mid = last / 2;
    for (int i = 0; i <= mid; i++) {
      final Coordinates tmp = coord[i];
      coord[i] = coord[last - i];
      coord[last - i] = tmp;
    }
  }

  /**
   *  Shifts the positions of the coordinates until <code>firstCoordinates</code>
   *  is first.
   *
   *@param  coordinates      the array to rearrange
   *@param  firstCoordinates  the coordinate to make first
   */
  public static void scroll(final Coordinates[] coordinates,
    final Coordinates firstCoordinates) {
    final int i = indexOf(firstCoordinates, coordinates);
    if (i < 0) {
      return;
    }
    final Coordinates[] newCoordinatess = new Coordinates[coordinates.length];
    System.arraycopy(coordinates, i, newCoordinatess, 0, coordinates.length - i);
    System.arraycopy(coordinates, 0, newCoordinatess, coordinates.length - i, i);
    System.arraycopy(newCoordinatess, 0, coordinates, 0, coordinates.length);
  }

  /**
   * Converts the given Collection of Coordinatess into a Coordinates array.
   */
  public static Coordinates[] toCoordinatesArray(final Collection coordList) {
    return (Coordinates[])coordList.toArray(coordArrayType);
  }

}
