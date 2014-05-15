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

import com.revolsys.gis.model.coordinates.list.CoordinatesListUtil;

/**
 * Represents a line segment defined by two {@link Coordinates}s.
 * Provides methods to compute various geometric properties
 * and relationships of line segments.
 * <p>
 * This class is designed to be easily mutable (to the extent of
 * having its contained points public).
 * This supports a common pattern of reusing a single LineSegmentDouble
 * object as a way of computing segment properties on the
 * segments defined by arrays or lists of {@link Coordinates}s.
 *
 *@version 1.7
 */
public class LineSegmentDouble extends AbstractLineSegment {

  private double[] coordinates;

  public LineSegmentDouble() {
    this.coordinates = null;
  }

  public LineSegmentDouble(final int axisCount, final double... coordinates) {
    if (coordinates == null || coordinates.length == 0 || axisCount < 1) {
      this.coordinates = null;
    } else if (coordinates.length % axisCount == 0) {
      this.coordinates = new double[axisCount * 2];
      int i = 0;
      final int axisCount2 = coordinates.length / 2;
      for (int vertexIndex = 0; vertexIndex < 2; vertexIndex++) {
        for (int axisIndex = 0; axisIndex < axisCount; axisIndex++) {
          double value;
          if (axisIndex < axisCount2) {
            value = coordinates[vertexIndex * axisCount2 + axisIndex];
          } else {
            value = Double.NaN;
          }
          this.coordinates[i++] = value;
        }
      }
    } else {
      throw new IllegalArgumentException("Expecting a multiple of " + axisCount
        + " not " + coordinates.length);
    }
  }

  public LineSegmentDouble(final LineSegment line) {
    this(line.getPoint(0), line.getPoint(1));
  }

  public LineSegmentDouble(final LineString line) {
    this(line.getVertex(0), line.getVertex(-1));
  }

  public LineSegmentDouble(final Point point1, final Point point2) {
    final int axisCount = Math.max(point1.getAxisCount(), point2.getAxisCount());
    coordinates = new double[axisCount * 2];
    CoordinatesListUtil.setCoordinates(coordinates, axisCount, 0, point1);
    CoordinatesListUtil.setCoordinates(coordinates, axisCount, 1, point2);
  }

  @Override
  public LineSegmentDouble clone() {
    final LineSegmentDouble clone = (LineSegmentDouble)super.clone();
    if (clone.coordinates != null) {
      clone.coordinates = clone.coordinates.clone();
    }
    return clone;
  }

  @Override
  public int getAxisCount() {
    return coordinates.length / 2;
  }

  @Override
  public double getCoordinate(final int index, final int axisIndex) {
    final int axisCount = getAxisCount();
    if (axisIndex >= 0 && axisIndex < axisCount) {
      if (index >= 0 && index < 2) {
        final int valueIndex = index * axisCount + axisIndex;
        final double value = coordinates[valueIndex];
        return value;
      }
    }
    return Double.NaN;
  }

  @Override
  public double[] getCoordinates() {
    if (coordinates == null) {
      return coordinates;
    } else {
      return coordinates.clone();
    }
  }

  @Override
  public boolean isEmpty() {
    return coordinates == null;
  }

}
