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
package com.revolsys.jts.geom.impl;

import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.jts.geom.Point;

/**
 * Represents a single point.
 *
 * A <code>Point</code> is topologically valid if and only if:
 * <ul>
 * <li>the coordinate which defines it (if any) is a valid coordinate 
 * (i.e does not have an <code>NaN</code> X or Y ordinate)
 * </ul>
 * 
 *@version 1.7
 */
public class PointImpl extends AbstractPoint {
  private static final long serialVersionUID = 4902022702746614570L;

  /**
  * The {@link GeometryFactory} used to create this Geometry
  */
  private final GeometryFactory geometryFactory;

  private double[] coordinates;

  public PointImpl(final GeometryFactory geometryFactory) {
    this.geometryFactory = geometryFactory;
  }

  /**
   *@param  coordinates      contains the single coordinate on which to base this <code>Point</code>
   *      , or <code>null</code> to create the empty geometry.
   */
  public PointImpl(final GeometryFactory geometryFactory,
    final double... coordinates) {
    this.geometryFactory = geometryFactory;
    final int axisCount = geometryFactory.getAxisCount();
    this.coordinates = new double[axisCount];
    for (int i = 0; i < axisCount; i++) {
      double coordinate;
      if (i < coordinates.length) {
        coordinate = coordinates[i];
        if (i < 2) {
          coordinate = geometryFactory.makeXyPrecise(coordinate);
        } else if (i == 2) {
          coordinate = geometryFactory.makeZPrecise(coordinate);
        }
      } else {
        coordinate = Double.NaN;
      }
      this.coordinates[i] = coordinate;
    }
  }

  /**
   * Creates and returns a full copy of this {@link Point} object.
   * (including all coordinates contained by it).
   *
   * @return a clone of this instance
   */
  @Override
  public PointImpl clone() {
    final PointImpl point = (PointImpl)super.clone();
    if (coordinates != null) {
      point.coordinates = coordinates.clone();
    }
    return point;
  }

  @Override
  public double getCoordinate(final int axisIndex) {
    if (isEmpty()) {
      return Double.NaN;
    } else {
      final int axisCount = getAxisCount();
      if (axisIndex >= 0 && axisIndex < axisCount) {
        return coordinates[axisIndex];
      } else {
        return Double.NaN;
      }
    }
  }

  @Override
  public double[] getCoordinates() {
    if (coordinates == null) {
      return coordinates;
    } else {
      return this.coordinates.clone();
    }
  }

  @Override
  public GeometryFactory getGeometryFactory() {
    return geometryFactory;
  }

  @Override
  public Point move(final double... deltas) {
    final GeometryFactory geometryFactory = getGeometryFactory();
    if (deltas == null || isEmpty()) {
      return this;
    } else {
      final double[] coordinates = this.coordinates.clone();
      final int axisCount = Math.min(deltas.length, getAxisCount());
      for (int axisIndex = 0; axisIndex < axisCount; axisIndex++) {
        coordinates[axisIndex] += deltas[axisIndex];
      }
      return geometryFactory.point(coordinates);
    }
  }

}
