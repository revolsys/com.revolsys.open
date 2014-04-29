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

/**
 * Utility functions for manipulating {@link CoordinatesList}s
 *
 * @version 1.7
 */
public class CoordinateSequences {

  /**
   * Copies a section of a {@link CoordinatesList} to another {@link CoordinatesList}.
   * The sequences may have different dimensions;
   * in this case only the common dimensions are copied.
   *
   * @param src the sequence to copy from
   * @param srcPos the position in the source sequence to start copying at
   * @param dest the sequence to copy to
   * @param destPos the position in the destination sequence to copy to
   * @param length the number of coordinates to copy
   */
  public static void copy(final CoordinatesList src, final int srcPos,
    final CoordinatesList dest, final int destPos, final int length) {
    for (int i = 0; i < length; i++) {
      copyCoord(src, srcPos + i, dest, destPos + i);
    }
  }

  /**
   * Copies a coordinate of a {@link CoordinatesList} to another {@link CoordinatesList}.
   * The sequences may have different dimensions;
   * in this case only the common dimensions are copied.
   * 
   * @param src the sequence to copy from
   * @param srcPos the source coordinate to copy
   * @param dest the sequence to copy to
   * @param destPos the destination coordinate to copy to
   */
  public static void copyCoord(final CoordinatesList src, final int srcPos,
    final CoordinatesList dest, final int destPos) {
    final int minDim = Math.min(src.getAxisCount(), dest.getAxisCount());
    for (int dim = 0; dim < minDim; dim++) {
      dest.setValue(destPos, dim, src.getValue(srcPos, dim));
    }
  }

  private static CoordinatesList createClosedRing(
    final CoordinateSequenceFactory fact, final CoordinatesList seq,
    final int size) {
    final CoordinatesList newseq = fact.create(size, seq.getAxisCount());
    final int n = seq.size();
    copy(seq, 0, newseq, 0, n);
    // fill remaining coordinates with start point
    for (int i = n; i < size; i++) {
      copy(seq, 0, newseq, i, 1);
    }
    return newseq;
  }

  /**
   * Ensures that a CoordinatesList forms a valid ring, 
   * returning a new closed sequence of the correct length if required.
   * If the input sequence is already a valid ring, it is returned 
   * without modification.
   * If the input sequence is too short or is not closed, 
   * it is extended with one or more copies of the start point.
   * 
   * @param fact the CoordinateSequenceFactory to use to create the new sequence
   * @param seq the sequence to test
   * @return the original sequence, if it was a valid ring, or a new sequence which is valid.
   */
  public static CoordinatesList ensureValidRing(
    final CoordinateSequenceFactory fact, final CoordinatesList seq) {
    final int n = seq.size();
    // empty sequence is valid
    if (n == 0) {
      return seq;
    }
    // too short - make a new one
    if (n <= 3) {
      return createClosedRing(fact, seq, 4);
    }

    final boolean isClosed = seq.getValue(0, CoordinatesList.X) == seq.getValue(
      n - 1, CoordinatesList.X)
      && seq.getValue(0, CoordinatesList.Y) == seq.getValue(n - 1,
        CoordinatesList.Y);
    if (isClosed) {
      return seq;
    }
    // make a new closed ring
    return createClosedRing(fact, seq, n + 1);
  }

  public static CoordinatesList extend(final CoordinateSequenceFactory fact,
    final CoordinatesList seq, final int size) {
    final CoordinatesList newseq = fact.create(size, seq.getAxisCount());
    final int n = seq.size();
    copy(seq, 0, newseq, 0, n);
    // fill remaining coordinates with end point, if it exists
    if (n > 0) {
      for (int i = n; i < size; i++) {
        copy(seq, n - 1, newseq, i, 1);
      }
    }
    return newseq;
  }

  /**
   * Tests whether two {@link CoordinatesList}s are equal.
   * To be equal, the sequences must be the same length.
   * They do not need to be of the same dimension, 
   * but the ordinate values for the smallest dimension of the two
   * must be equal.
   * Two <code>NaN</code> ordinates values are considered to be equal. 
   * 
   * @param cs1 a CoordinatesList
   * @param cs2 a CoordinatesList
   * @return true if the sequences are equal in the common dimensions
   */
  public static boolean isEqual(final CoordinatesList cs1,
    final CoordinatesList cs2) {
    final int cs1Size = cs1.size();
    final int cs2Size = cs2.size();
    if (cs1Size != cs2Size) {
      return false;
    }
    final int dim = Math.min(cs1.getAxisCount(), cs2.getAxisCount());
    for (int i = 0; i < cs1Size; i++) {
      for (int d = 0; d < dim; d++) {
        final double v1 = cs1.getValue(i, d);
        final double v2 = cs2.getValue(i, d);
        if (cs1.getValue(i, d) == cs2.getValue(i, d)) {
          continue;
        }
        // special check for NaNs
        if (Double.isNaN(v1) && Double.isNaN(v2)) {
          continue;
        }
        return false;
      }
    }
    return true;
  }

  /**
   * Tests whether a {@link CoordinatesList} forms a valid {@link LinearRing},
   * by checking the sequence length and closure
   * (whether the first and last points are identical in 2D). 
   * Self-intersection is not checked.
   * 
   * @param seq the sequence to test
   * @return true if the sequence is a ring
   * @see LinearRing
   */
  public static boolean isRing(final CoordinatesList seq) {
    final int n = seq.size();
    if (n == 0) {
      return true;
    }
    // too few points
    if (n <= 3) {
      return false;
    }
    // test if closed
    return seq.getValue(0, CoordinatesList.X) == seq.getValue(n - 1,
      CoordinatesList.X)
      && seq.getValue(0, CoordinatesList.Y) == seq.getValue(n - 1,
        CoordinatesList.Y);
  }

  /**
   * Reverses the coordinates in a sequence in-place.
   */
  public static void reverse(final CoordinatesList seq) {
    final int last = seq.size() - 1;
    final int mid = last / 2;
    for (int i = 0; i <= mid; i++) {
      swap(seq, i, last - i);
    }
  }

  /**
   * Swaps two coordinates in a sequence.
   *
   * @param seq the sequence to modify
   * @param i the index of a coordinate to swap
   * @param j the index of a coordinate to swap
   */
  public static void swap(final CoordinatesList seq, final int i, final int j) {
    if (i == j) {
      return;
    }
    for (int dim = 0; dim < seq.getAxisCount(); dim++) {
      final double tmp = seq.getValue(i, dim);
      seq.setValue(i, dim, seq.getValue(j, dim));
      seq.setValue(j, dim, tmp);
    }
  }
}
