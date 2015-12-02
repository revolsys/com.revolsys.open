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

import com.revolsys.geometry.model.coordinates.list.CoordinatesListUtil;
import com.revolsys.geometry.model.vertex.Vertex;

/**
 * Models an OGC SFS <code>LinearRing</code>.
 * A <code>LinearRing</code> is a {@link LineString} which is both closed and simple.
 * In other words,
 * the first and last coordinate in the ring must be equal,
 * and the interior of the ring must not self-intersect.
 * Either orientation of the ring is allowed.
 * <p>
 * A ring must have either 0 or 4 or more points.
 * The first and last points must be equal (in 2D).
 * If these conditions are not met, the constructors throw
 * an {@link IllegalArgumentException}
 *
 * @version 1.7
 */
public interface LinearRing extends LineString {
  /**
   * The minimum number of vertices allowed in a valid non-empty ring (= 4).
   * Empty rings with 0 vertices are also valid.
   */
  int MINIMUM_VALID_SIZE = 4;

  /**
   *  Returns the minimum coordinate, using the usual lexicographic comparison.
   *
   *@param  coordinates  the array to search
   *@return              the minimum coordinate in the array, found using <code>compareTo</code>
   *@see Point#compareTo(Object)
   */
  static int minCoordinateIndex(final LinearRing ring) {
    Point minCoord = null;
    int minIndex = 0;
    for (final Vertex vertex : ring.vertices()) {
      if (minCoord == null || minCoord.compareTo(vertex) > 0) {
        minCoord = vertex.newPointDouble();
        minIndex = vertex.getVertexIndex();
      }
    }
    return minIndex;
  }

  /**
   *  Shifts the positions of the coordinates until <code>firstCoordinate</code>
   *  is first.
   *
   *@param  coordinates      the array to rearrange
   *@param  firstCoordinate  the coordinate to make first
   */
  static LinearRing scroll(final LinearRing ring, final int index) {
    final LineString points = ring;
    final int vertexCount = ring.getVertexCount();
    final int axisCount = ring.getAxisCount();
    final double[] coordinates = new double[vertexCount * axisCount];
    int newVertexIndex = 0;
    for (int vertexIndex = index; vertexIndex < vertexCount - 1; vertexIndex++) {
      CoordinatesListUtil.setCoordinates(coordinates, axisCount, newVertexIndex++, points,
        vertexIndex);
    }
    for (int vertexIndex = 0; vertexIndex < index; vertexIndex++) {
      CoordinatesListUtil.setCoordinates(coordinates, axisCount, newVertexIndex++, points,
        vertexIndex);
    }
    CoordinatesListUtil.setCoordinates(coordinates, axisCount, vertexCount - 1, points, index);
    final GeometryFactory geometryFactory = ring.getGeometryFactory();
    return geometryFactory.linearRing(axisCount, coordinates);
  }

  @Override
  LinearRing clone();

  @Override
  default LinearRing deleteVertex(final int vertexIndex) {
    return (LinearRing)LineString.super.deleteVertex(vertexIndex);
  }

  @Override
  default LinearRing move(final double... deltas) {
    return (LinearRing)LineString.super.move(deltas);
  }

  @Override
  default LinearRing moveVertex(final Point newPoint, final int vertexIndex) {
    return (LinearRing)LineString.super.moveVertex(newPoint, vertexIndex);
  }

  default LinearRing normalize(final boolean clockwise) {
    if (isEmpty()) {
      return this;
    } else {
      LinearRing ring = this;
      final int index = minCoordinateIndex(ring);
      if (index > 0) {
        ring = scroll(ring, index);
      }
      if (ring.isCounterClockwise() == clockwise) {
        return ring.reverse();
      } else {
        return ring;
      }
    }
  }

  @Override
  default LinearRing reverse() {
    return (LinearRing)LineString.super.reverse();
  }
}
