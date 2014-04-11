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

import java.util.List;

import com.revolsys.gis.model.coordinates.AbstractCoordinates;
import com.revolsys.util.MathUtil;

/**
 * A lightweight class used to store coordinates
 * on the 2-dimensional Cartesian plane.
 * It is distinct from {@link Point}, which is a subclass of {@link Geometry}. 
 * Unlike objects of type {@link Point} (which contain additional
 * information such as an envelope, a precision model, and spatial reference
 * system information), a <code>Coordinate</code> only contains ordinate values
 * and accessor methods. <P>
 *
 * <code>Coordinate</code>s are two-dimensional points, with an additional Z-ordinate. 
 * If an Z-ordinate value is not specified or not defined, 
 * constructed coordinates have a Z-ordinate of <code>NaN</code>
 * (which is also the value of <code>Coordinate.NULL_ORDINATE</code>).  
 * The standard comparison functions ignore the Z-ordinate.
 * Apart from the basic accessor functions, JTS supports
 * only specific operations involving the Z-ordinate. 
 *
 *@version 1.7
 */
public class Coordinate extends AbstractCoordinates {
  private static final long serialVersionUID = 6683108902428366910L;

  private final double[] coordinates;

  /**
   *  Constructs a <code>Coordinate</code> at (0,0,NaN).
   */
  public Coordinate() {
    this(0.0, 0.0, Coordinates.NULL_ORDINATE);
  }

  /**
   *  Constructs a <code>Coordinate</code> having the same (x,y,z) values as
   *  <code>other</code>.
   *
   *@param  c  the <code>Coordinate</code> to copy.
   */
  public Coordinate(final Coordinates point) {
    final byte numAxis = point.getNumAxis();
    this.coordinates = new double[numAxis];
    for (int i = 0; i < numAxis; i++) {
      final double value = point.getValue(i);
      this.coordinates[i] = value;
    }
  }

  public Coordinate(final Coordinates point, final int numAxis) {
    this(numAxis);
    final int count = Math.min(numAxis, point.getNumAxis());
    for (int i = 0; i < count; i++) {
      final double value = point.getValue(i);
      setValue(i, value);
    }
  }

  public Coordinate(final double... coordinates) {
    this(coordinates.length, coordinates);
  }

  public Coordinate(final int numAxis) {
    this.coordinates = new double[numAxis];
  }

  public Coordinate(final int numAxis, final double... coordinates) {
    this.coordinates = new double[numAxis];
    System.arraycopy(coordinates, 0, this.coordinates, 0,
      Math.min(numAxis, coordinates.length));
  }

  public Coordinate(final List<Number> coordinates) {
    this(MathUtil.toDoubleArray(coordinates));
  }

  @Override
  public Coordinate cloneCoordinates() {
    return (Coordinate)super.cloneCoordinates();
  }

  /**
   *  Compares this {@link Coordinate} with the specified {@link Coordinate} for order.
   *  This method ignores the z value when making the comparison.
   *  Returns:
   *  <UL>
   *    <LI> -1 : this.x < other.x || ((this.x == other.x) && (this.y <
   *    other.y))
   *    <LI> 0 : this.x == other.x && this.y = other.y
   *    <LI> 1 : this.x > other.x || ((this.x == other.x) && (this.y > other.y))
   *
   *  </UL>
   *  Note: This method assumes that ordinate values
   * are valid numbers.  NaN values are not handled correctly.
   *
   *@param  o  the <code>Coordinate</code> with which this <code>Coordinate</code>
   *      is being compared
   *@return    -1, zero, or 1 as this <code>Coordinate</code>
   *      is less than, equal to, or greater than the specified <code>Coordinate</code>
   */
  @Override
  public int compareTo(final Object o) {
    final Coordinates point = (Coordinates)o;

    final double x = getX();
    final double otherX = point.getX();
    if (x < otherX) {
      return -1;
    } else if (x > otherX) {
      return 1;
    } else {
      final double y = getY();
      final double otherY = point.getY();
      if (y < otherY) {
        return -1;
      } else if (y > otherY) {
        return 1;
      }
      return 0;
    }
  }

  @Override
  public byte getNumAxis() {
    return (byte)coordinates.length;
  }

  /**
   * Gets the ordinate value for the given index.
   * The supported values for the index are 
   * {@link #X}, {@link #Y}, and {@link #Z}.
   * 
   * @param axisIndex the ordinate index
   * @return the value of the ordinate
   * @throws IllegalArgumentException if the index is not valid
   */
  @Override
  public double getValue(final int axisIndex) {
    if (axisIndex >= 0 && axisIndex < getNumAxis()) {
      return coordinates[axisIndex];
    } else {
      return Double.NaN;
    }
  }

  /**
   * Sets the ordinate for the given index
   * to a given value.
   * The supported values for the index are 
   * {@link #X}, {@link #Y}, and {@link #Z}.
   * 
   * @param axisIndex the ordinate index
   * @param value the value to set
   * @throws IllegalArgumentException if the index is not valid
   */
  @Override
  public void setValue(final int axisIndex, final double value) {
    if (axisIndex >= 0 && axisIndex < getNumAxis()) {
      coordinates[axisIndex] = value;
    }
  }

  /**
   *  Returns a <code>String</code> of the form <I>POINT(x y z)</I> .
   *
   *@return    a <code>String</code> of the form <I>POINT(x y z)</I>
   */
  @Override
  public String toString() {
    final StringBuffer s = new StringBuffer("POINT(");
    s.append(coordinates[0]);
    final byte numAxis = getNumAxis();
    for (int i = 1; i < numAxis; i++) {
      final Double ordinate = coordinates[i];
      s.append(' ');
      s.append(ordinate);
    }
    s.append(")");
    return s.toString();
  }

}
