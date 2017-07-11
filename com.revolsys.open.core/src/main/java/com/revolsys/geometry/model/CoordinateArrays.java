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
package com.revolsys.geometry.model;

import java.util.Collection;
import java.util.Comparator;

import com.revolsys.util.MathUtil;

/**
 * Useful utility functions for handling Point arrays
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
      final Point[] pts1 = (Point[])o1;
      final Point[] pts2 = (Point[])o2;

      return CoordinateArrays.compare(pts1, pts2);
    }
  }

  private final static Point[] coordArrayType = new Point[0];

  /**
   * Returns either the given coordinate array if its length is greater than the
   * given amount, or an empty coordinate array.
   */
  public static Point[] atLeastNCoordinatesOrNothing(final int n, final Point[] c) {
    return c.length >= n ? c : new Point[] {};
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
  public static int compare(final Point[] pts1, final Point[] pts2) {
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
   * @param points an array of Coordinates
   * @return a deep copy of the input
   */
  public static Point[] copyDeep(final Point[] points) {
    final Point[] copy = new Point[points.length];
    for (int i = 0; i < points.length; i++) {
      copy[i] = points[i].clone();
    }
    return copy;
  }

  /**
   * Creates a deep copy of a given section of a source {@link Coordinates} array
   * into a destination Point array.
   * The destination array must be an appropriate size to receive
   * the copied coordinates.
   *
   * @param src an array of Coordinates
   * @param srcStart the index to start copying from
   * @param dest the
   * @param destStart the destination index to start copying to
   * @param length the number of items to copy
   */
  public static void copyDeep(final Point[] src, final int srcStart, final Point[] dest,
    final int destStart, final int length) {
    for (int i = 0; i < length; i++) {
      dest[destStart + i] = src[srcStart + i].clone();
    }
  }

  /**
   * Returns true if the two arrays are identical, both null, or pointwise
   * equal (as compared using Coordinate#equals)
   * @see Point#equals(Object)
   */
  public static boolean equals(final Point[] coord1, final Point[] coord2) {
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
  public static boolean equals(final Point[] coord1, final Point[] coord2,
    final Comparator coordinateComparator) {
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
  public static Point[] extract(final Point[] pts, int start, int end) {
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

    final Point[] extractPts = new Point[npts];
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
  public static boolean hasRepeatedPoints(final Point[] coord) {
    for (int i = 1; i < coord.length; i++) {
      if (coord[i - 1].equals(coord[i])) {
        return true;
      }
    }
    return false;
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
  public static int indexOf(final Point coordinate, final Point[] coordinates) {
    for (int i = 0; i < coordinates.length; i++) {
      if (coordinate.equals(coordinates[i])) {
        return i;
      }
    }
    return -1;
  }

  /**
   * Tests whether an array of {@link Coordinates}s forms a ring,
   * by checking length and closure.
   * Self-intersection is not checked.
   *
   * @param pts an array of Coordinates
   * @return true if the coordinate form a ring.
   */
  public static boolean isRing(final Point[] pts) {
    if (pts.length < 4) {
      return false;
    }
    if (!pts[0].equals(2, pts[pts.length - 1])) {
      return false;
    }
    return true;
  }

  /**
   * Finds a point in a list of points which is not contained in another list of points
   * @param testPts the {@link Coordinates}s to test
   * @param pts an array of {@link Coordinates}s to test the input points against
   * @return a {@link Coordinates} from <code>testPts</code> which is not in <code>pts</code>, '
   * or <code>null</code>
   */
  public static Point ptNotInList(final Point[] testPts, final Point[] pts) {
    for (final Point testPt : testPts) {
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
  public static Point[] removeNull(final Point[] coord) {
    int nonNull = 0;
    for (final Point element : coord) {
      if (element != null) {
        nonNull++;
      }
    }
    final Point[] newCoord = new Point[nonNull];
    // empty case
    if (nonNull == 0) {
      return newCoord;
    }

    int j = 0;
    for (final Point element : coord) {
      if (element != null) {
        newCoord[j++] = element;
      }
    }
    return newCoord;
  }

  /**
   * If the coordinate array argument has repeated points,
   * constructs a new array containing no repeated points.
   * Otherwise, returns the argument.
   * @see #hasRepeatedPoints(Point[])
   */
  public static Point[] removeRepeatedPoints(final Point[] coord) {
    if (!hasRepeatedPoints(coord)) {
      return coord;
    }
    final CoordinateList coordList = new CoordinateList(coord, false);
    return coordList.toCoordinateArray();
  }

  /**
   * Reverses the coordinates in an array in-place.
   */
  public static void reverse(final Point[] coord) {
    final int last = coord.length - 1;
    final int mid = last / 2;
    for (int i = 0; i <= mid; i++) {
      final Point tmp = coord[i];
      coord[i] = coord[last - i];
      coord[last - i] = tmp;
    }
  }

  /**
   * Converts the given Collection of Point into a Point array.
   */
  public static Point[] toCoordinateArray(final Collection coordList) {
    return (Point[])coordList.toArray(coordArrayType);
  }

}
