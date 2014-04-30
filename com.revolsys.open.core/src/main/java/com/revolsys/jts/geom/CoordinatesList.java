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

import com.revolsys.gis.model.coordinates.CoordinatesPrecisionModel;

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
 * requires implementing the {@link CoordinatesList} and
 *interfaces. 
 * To use the custom CoordinatesList, create a
 * new {@link GeometryFactory} parameterized by the CoordinateSequenceFactory
 * The {@link GeometryFactory} can then be used to create new {@link Geometry}s.
 * The new Geometries
 * will use the custom CoordinatesList implementation.
 * <p>
 * For an example, see the code for
 * {@link ExtendedCoordinateExample}.
 *
 *
 * @version 1.7
 */
public interface CoordinatesList extends Cloneable, Iterable<Coordinates>,
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
  CoordinatesList clone();

  boolean contains(Coordinates point);

  void copy(int sourceIndex, CoordinatesList target, int targetIndex,
    int axisCount, int count);

  double distance(int index, Coordinates point);

  double distance(int index, CoordinatesList other, int otherIndex);

  boolean equal(int i, Coordinates point);

  boolean equal(int i, Coordinates point, int axisCount);

  boolean equal(int index, CoordinatesList other, int otherIndex);

  boolean equal(int index, CoordinatesList other, int otherIndex, int axisCount);

  boolean equal2d(int index, Coordinates point);

  boolean equals(CoordinatesList coordinatesList);

  boolean equals(CoordinatesList coordinatesList, int axisCount);

  Coordinates get(int i);

  int getAxisCount();

  /**
   * Returns (possibly a copy of) the i'th coordinate in this sequence.
   * Whether or not the Coordinates returned is the actual underlying
   * Coordinates or merely a copy depends on the implementation.
   * <p>
   * Note that in the future the semantics of this method may change
   * to guarantee that the Coordinates returned is always a copy.
   * Callers should not to assume that they can modify a CoordinatesList by
   * modifying the object returned by this method.
   *
   * @param i the index of the coordinate to retrieve
   * @return the i'th coordinate in the sequence
   */
  Coordinates getCoordinate(int i);

  /**
   * Returns a copy of the i'th coordinate in this sequence.
   * This method optimizes the situation where the caller is
   * going to make a copy anyway - if the implementation
   * has already created a new Coordinates object, no further copy is needed.
   *
   * @param i the index of the coordinate to retrieve
   * @return a copy of the i'th coordinate in the sequence
   */
  Coordinates getCoordinateCopy(int i);

  double[] getCoordinates();

  List<Coordinates> getList();

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

  void makePrecise(CoordinatesPrecisionModel precisionModel);

  CoordinatesList reverse();

  void setCoordinate(int i, Coordinates coordinate);

  void setM(int index, double m);

  void setPoint(int i, Coordinates point);

  void setTime(int index, long time);

  /**
   * Sets the value for a given ordinate of a coordinate in this sequence.
   *
   * @param index  the coordinate index in the sequence
   * @param ordinateIndex the ordinate index in the coordinate (in range [0, dimension-1])
   * @param value  the new ordinate value
   */
  void setValue(int index, int axisIndex, double value);

  void setX(int index, double x);

  void setY(int index, double y);

  void setZ(int index, double z);

  /**
   * Returns the number of coordinates in this sequence.
   * @return the size of the sequence
   */
  int size();

  boolean startsWith(CoordinatesList coordinatesList, int axisCount);

  CoordinatesList subList(int index);

  CoordinatesList subList(int index, int count);

  CoordinatesList subList(int length, int index, int count);

  CoordinatesList subList(int length, int sourceIndex, int targetIndex,
    int count);

  /**
   * Returns (possibly copies of) the Coordinates in this collection.
   * Whether or not the Coordinates returned are the actual underlying
   * Coordinates or merely copies depends on the implementation. Note that
   * if this implementation does not store its data as an array of Coordinates,
   * this method will incur a performance penalty because the array needs to
   * be built from scratch.
   *
   * @return a array of coordinates containing the point values in this sequence
   */
  Coordinates[] toCoordinateArray();
}
