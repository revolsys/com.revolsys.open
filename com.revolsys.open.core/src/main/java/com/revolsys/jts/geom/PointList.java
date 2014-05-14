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

import java.io.Serializable;
import java.util.List;

/**
 * The internal representation of a list of coordinates inside a Geometry.
 * <p>
 * This allows Geometries to store their
 * points using something other than the JTS {@link Coordinates} class. 
 * For example, a storage-efficient implementation
 * might store coordinate sequences as an array of x's
 * and an array of y's. 
 * Or a custom coordinate class might support extra attributes like M-values.
 * <p>
 * Implementing a custom coordinate storage structure
 * requires implementing the {@link PointList} and
 *interfaces. 
 * To use the custom PointList, create a
 * new {@link GeometryFactory} parameterized by the CoordinateSequenceFactory
 * The {@link GeometryFactory} can then be used to create new {@link Geometry}s.
 * The new Geometries
 * will use the custom PointList implementation.
 * <p>
 * For an example, see the code for
 * {@link ExtendedCoordinateExample}.
 *
 *
 * @version 1.7
 */
public interface PointList extends Cloneable, Iterable<Point>,
  Serializable {
  /**
   * Standard ordinate index values
   */
  int X = 0;

  int Y = 1;

  int Z = 2;

  int M = 3;

  /**
   * Returns a deep copy of this collection.
   * Called by Geometry#clone.
   *
   * @return a copy of the coordinate sequence containing copies of all points
   */
  PointList clone();

  boolean contains(Point point);

  double distance(int index, PointList other, int otherIndex);

  double distance(int index, Point point);

  boolean equal(int index, PointList other, int otherIndex);

  boolean equal(int index, PointList other, int otherIndex, int axisCount);

  boolean equal(int i, Point point);

  boolean equal(int i, Point point, int axisCount);

  boolean equal2d(int index, Point point);

  boolean equals(PointList coordinatesList);

  boolean equals(PointList coordinatesList, int axisCount);

  Point get(int i);

  int getAxisCount();

  /**
   * Returns (possibly a copy of) the i'th coordinate in this sequence.
   * Whether or not the Point returned is the actual underlying
   * Point or merely a copy depends on the implementation.
   * <p>
   * Note that in the future the semantics of this method may change
   * to guarantee that the Point returned is always a copy.
   * Callers should not to assume that they can modify a PointList by
   * modifying the object returned by this method.
   *
   * @param i the index of the coordinate to retrieve
   * @return the i'th coordinate in the sequence
   */
  Point getCoordinate(int i);

  double[] getCoordinates();

  List<Point> getList();

  double getM(int index);

  long getTime(int index);

  /**
   * Returns the ordinate of a coordinate in this sequence.
   * Ordinate indices 0 and 1 are assumed to be X and Y.
   * Ordinates indices greater than 1 have user-defined semantics
   * (for instance, they may contain other dimensions or measure values).
   *
   * @param index  the coordinate index in the sequence
   * @param ordinateIndex the ordinate index in the coordinate (in range [0, dimension-1])
   */
  double getValue(int index, int axisIndex);

  /**
   * Returns ordinate X (0) of the specified coordinate.
   *
   * @param index
   * @return the value of the X ordinate in the index'th coordinate
   */
  double getX(int index);

  /**
   * Returns ordinate Y (1) of the specified coordinate.
   *
   * @param index
   * @return the value of the Y ordinate in the index'th coordinate
   */
  double getY(int index);

  double getZ(int index);

  boolean isCounterClockwise();

  PointList reverse();

  /**
     * Returns the number of coordinates in this sequence.
     * @return the size of the sequence
     */
  int size();

  boolean startsWith(PointList coordinatesList, int axisCount);

  PointList subList(int index);

  PointList subList(int index, int count);

  PointList subList(int length, int index, int count);

  PointList subList(int length, int sourceIndex, int targetIndex,
    int count);

  /**
   * Returns (possibly copies of) the Point in this collection.
   * Whether or not the Point returned are the actual underlying
   * Point or merely copies depends on the implementation. Note that
   * if this implementation does not store its data as an array of Coordinates,
   * this method will incur a performance penalty because the array needs to
   * be built from scratch.
   *
   * @return a array of coordinates containing the point values in this sequence
   */
  Point[] toCoordinateArray();

  List<Point> toList();
}
