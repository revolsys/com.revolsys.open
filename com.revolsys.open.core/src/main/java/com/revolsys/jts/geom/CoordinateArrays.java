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
package com.revolsys.jts.geom;

import java.util.Collection;
import java.util.Comparator;

import com.revolsys.jts.math.MathUtil;

/**
 * Useful utility functions for handling Coordinates arrays
 *
 * @version 1.7
 */
public class CoordinateArrays {

  /**
   * A {@link Comparator} for {@link Coordinates} arrays
   * modulo their directionality.
   * E.g. if two coordinate arrays are identical but reversed
   * they will compare as equal under this ordering.
   * If the arrays are not equal, the ordering returned
   * is the ordering in the forward direction.
   *
   */
  public static class BidirectionalComparator implements Comparator {
    @Override
    public int compare(final Object o1, final Object o2) {
      final Coordinates[] pts1 = (Coordinates[])o1;
      final Coordinates[] pts2 = (Coordinates[])o2;

      if (pts1.length < pts2.length) {
        return -1;
      }
      if (pts1.length > pts2.length) {
        return 1;
      }

      if (pts1.length == 0) {
        return 0;
      }

      final int forwardComp = CoordinateArrays.compare(pts1, pts2);
      final boolean isEqualRev = isEqualReversed(pts1, pts2);
      if (isEqualRev) {
        return 0;
      }
      return forwardComp;
    }

    public int OLDcompare(final Object o1, final Object o2) {
      final Coordinates[] pts1 = (Coordinates[])o1;
      final Coordinates[] pts2 = (Coordinates[])o2;

      if (pts1.length < pts2.length) {
        return -1;
      }
      if (pts1.length > pts2.length) {
        return 1;
      }

      if (pts1.length == 0) {
        return 0;
      }

      final int dir1 = increasingDirection(pts1);
      final int dir2 = increasingDirection(pts2);

      int i1 = dir1 > 0 ? 0 : pts1.length - 1;
      int i2 = dir2 > 0 ? 0 : pts1.length - 1;

      for (int i = 0; i < pts1.length; i++) {
        final int comparePt = pts1[i1].compareTo(pts2[i2]);
        if (comparePt != 0) {
          return comparePt;
        }
        i1 += dir1;
        i2 += dir2;
      }
      return 0;
    }

  }

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
  public static Coordinates[] atLeastNCoordinatesOrNothing(final int n,
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
   * @param coordinates an array of Coordinates
   * @return a deep copy of the input
   */
  public static Coordinates[] copyDeep(final Coordinates[] coordinates) {
    final Coordinates[] copy = new Coordinates[coordinates.length];
    for (int i = 0; i < coordinates.length; i++) {
      copy[i] = new Coordinate(coordinates[i]);
    }
    return copy;
  }

  /**
   * Creates a deep copy of a given section of a source {@link Coordinates} array
   * into a destination Coordinates array.
   * The destination array must be an appropriate size to receive
   * the copied coordinates.
   *
   * @param src an array of Coordinates
   * @param srcStart the index to start copying from
   * @param dest the 
   * @param destStart the destination index to start copying to
   * @param length the number of items to copy
   */
  public static void copyDeep(final Coordinates[] src, final int srcStart,
    final Coordinates[] dest, final int destStart, final int length) {
    for (int i = 0; i < length; i++) {
      dest[destStart + i] = new Coordinate(src[srcStart + i]);
    }
  }

  /**
   * Returns true if the two arrays are identical, both null, or pointwise
   * equal (as compared using Coordinate#equals)
   * @see Coordinate#equals(Object)
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
   * @param coord1 an array of Coordinates
   * @param coord2 an array of Coordinates
   * @param coordinateComparator a Comparator for Coordinates
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
   * Returns whether #equals returns true for any two consecutive Coordinates
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
   * @param pts the array of Coordinates to test
   * @return <code>1</code> if the array is smaller at the start
   * or is a palindrome,
   * <code>-1</code> if smaller at the end
   */
  public static int increasingDirection(final Coordinates[] pts) {
    for (int i = 0; i < pts.length / 2; i++) {
      final int j = pts.length - 1 - i;
      // skip equal points on both ends
      final int comp = pts[i].compareTo(pts[j]);
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
   *@param  coordinate   the <code>Coordinate</code> to search for
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
   * @param pts an array of Coordinates
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
   *@see Coordinate#compareTo(Object)
   */
  public static Coordinates minCoordinate(final Coordinates[] coordinates) {
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
   * Collapses a coordinate array to remove all null elements.
   * 
   * @param coord the coordinate array to collapse
   * @return an array containing only non-null elements
   */
  public static Coordinates[] removeNull(final Coordinates[] coord) {
    int nonNull = 0;
    for (int i = 0; i < coord.length; i++) {
      if (coord[i] != null) {
        nonNull++;
      }
    }
    final Coordinates[] newCoord = new Coordinates[nonNull];
    // empty case
    if (nonNull == 0) {
      return newCoord;
    }

    int j = 0;
    for (int i = 0; i < coord.length; i++) {
      if (coord[i] != null) {
        newCoord[j++] = coord[i];
      }
    }
    return newCoord;
  }

  /**
   * If the coordinate array argument has repeated points,
   * constructs a new array containing no repeated points.
   * Otherwise, returns the argument.
   * @see #hasRepeatedPoints(Coordinates[])
   */
  public static Coordinates[] removeRepeatedPoints(final Coordinates[] coord) {
    if (!hasRepeatedPoints(coord)) {
      return coord;
    }
    final CoordinateList coordList = new CoordinateList(coord, false);
    return coordList.toCoordinateArray();
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
   *  Shifts the positions of the coordinates until <code>firstCoordinate</code>
   *  is first.
   *
   *@param  coordinates      the array to rearrange
   *@param  firstCoordinate  the coordinate to make first
   */
  public static void scroll(final Coordinates[] coordinates,
    final Coordinates firstCoordinate) {
    final int i = indexOf(firstCoordinate, coordinates);
    if (i < 0) {
      return;
    }
    final Coordinates[] newCoordinates = new Coordinates[coordinates.length];
    System.arraycopy(coordinates, i, newCoordinates, 0, coordinates.length - i);
    System.arraycopy(coordinates, 0, newCoordinates, coordinates.length - i, i);
    System.arraycopy(newCoordinates, 0, coordinates, 0, coordinates.length);
  }

  /**
   * Converts the given Collection of Coordinates into a Coordinates array.
   */
  public static Coordinates[] toCoordinateArray(final Collection coordList) {
    return (Coordinates[])coordList.toArray(coordArrayType);
  }

}
