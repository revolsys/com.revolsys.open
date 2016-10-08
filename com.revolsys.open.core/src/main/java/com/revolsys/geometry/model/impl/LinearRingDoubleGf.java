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
package com.revolsys.geometry.model.impl;

import java.util.Arrays;

import com.revolsys.geometry.model.Dimension;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.LineString;
import com.revolsys.geometry.model.LinearRing;
import com.revolsys.geometry.model.Point;
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
public class LinearRingDoubleGf extends LineStringDoubleGf implements LinearRing {
  private static final long serialVersionUID = -4261142084085851829L;

  public LinearRingDoubleGf(final GeometryFactory factory) {
    super(factory);
  }

  /**
   * Constructs a <code>LinearRing</code> with the vertices
   * specifed by the given {@link LineString}.
   *
   *@param  points  a sequence points forming a closed and simple linestring, or
   *      <code>null</code> to create the empty geometry.
   *
   * @throws IllegalArgumentException if the ring is not closed, or has too few points
   *
   */
  public LinearRingDoubleGf(final GeometryFactory factory, final int axisCount,
    final double... points) {
    super(factory, axisCount, points);
    validate();
  }

  public LinearRingDoubleGf(final GeometryFactory geometryFactory, final int axisCount,
    final int vertexCount, final double... coordinates) {
    super(geometryFactory, axisCount,
      getNewCoordinates(geometryFactory, axisCount, vertexCount, coordinates));
    validate();
  }

  /**
   * Constructs a <code>LinearRing</code> with the vertices
   * specifed by the given {@link LineString}.
   *
   *@param  points  a sequence points forming a closed and simple linestring, or
   *      <code>null</code> to create the empty geometry.
   *
   * @throws IllegalArgumentException if the ring is not closed, or has too few points
   *
   */
  public LinearRingDoubleGf(final GeometryFactory factory, final LineString points) {
    super(factory, points);
    validate();
  }

  @SuppressWarnings("unchecked")
  @Override
  public <V extends Geometry> V appendVertex(Point newPoint, final int... geometryId) {
    if (newPoint == null || newPoint.isEmpty()) {
      return (V)this;
    } else if (geometryId.length == 0) {
      final GeometryFactory geometryFactory = getGeometryFactory();
      if (isEmpty()) {
        return newPoint.convertGeometry(geometryFactory);
      } else {
        newPoint = newPoint.convertGeometry(geometryFactory);
        final int vertexCount = getVertexCount();
        final double[] coordinates = getCoordinates();
        final int axisCount = getAxisCount();
        final double[] newCoordinates = new double[axisCount * (vertexCount + 1)];
        final Vertex fromPoint = getVertex(0);
        final int length = (vertexCount - 1) * axisCount;
        System.arraycopy(coordinates, 0, newCoordinates, 0, length);
        CoordinatesListUtil.setCoordinates(newCoordinates, axisCount, vertexCount - 1, newPoint);
        CoordinatesListUtil.setCoordinates(newCoordinates, axisCount, vertexCount, fromPoint);

        return (V)geometryFactory.linearRing(axisCount, newCoordinates);
      }
    } else {
      throw new IllegalArgumentException(
        "Geometry id's for LinearRings must have length 0. " + Arrays.toString(geometryId));
    }
  }

  @Override
  public LinearRingDoubleGf clone() {
    return (LinearRingDoubleGf)super.clone();
  }

  @SuppressWarnings("unchecked")
  @Override
  public <V extends Geometry> V copy(final GeometryFactory geometryFactory) {
    if (geometryFactory == null) {
      return (V)this.clone();
    } else if (isEmpty()) {
      return (V)geometryFactory.linearRing();
    } else {
      final double[] coordinates = convertCoordinates(geometryFactory);
      final int axisCount = getAxisCount();
      return (V)geometryFactory.linearRing(axisCount, coordinates);
    }
  }

  @Override
  public LinearRing deleteVertex(final int vertexIndex) {
    if (isEmpty()) {
      throw new IllegalArgumentException("Cannot delete vertex for empty LinearRing");
    } else {
      final int vertexCount = getVertexCount();
      if (vertexCount <= 4) {
        throw new IllegalArgumentException("LineString must have a minimum of 4 vertices");
      } else if (vertexIndex >= 0 && vertexIndex < vertexCount) {
        final GeometryFactory geometryFactory = getGeometryFactory();

        final double[] coordinates = getCoordinates();
        final int axisCount = getAxisCount();
        final double[] newCoordinates = new double[axisCount * (vertexCount - 1)];
        if (vertexIndex == 0 || vertexIndex == vertexCount - 1) {
          System.arraycopy(coordinates, axisCount, newCoordinates, 0,
            (vertexCount - 2) * axisCount);
          System.arraycopy(coordinates, axisCount, newCoordinates, (vertexCount - 2) * axisCount,
            axisCount);
        } else {
          final int beforeLength = vertexIndex * axisCount;
          System.arraycopy(coordinates, 0, newCoordinates, 0, beforeLength);

          final int sourceIndex = (vertexIndex + 1) * axisCount;
          final int afterLength = (vertexCount - vertexIndex - 1) * axisCount;
          System.arraycopy(coordinates, sourceIndex, newCoordinates, vertexIndex * axisCount,
            afterLength);
        }
        return geometryFactory.linearRing(axisCount, newCoordinates);
      } else {
        throw new IllegalArgumentException("Vertex index must be between 0 and " + vertexCount);
      }
    }
  }

  /**
   * Returns <code>Dimension.FALSE</code>, since by definition LinearRings do
   * not have a boundary.
   *
   * @return Dimension.FALSE
   */
  @Override
  public int getBoundaryDimension() {
    return Dimension.FALSE;
  }

  @SuppressWarnings("unchecked")
  @Override
  public <V extends Geometry> V insertVertex(Point newPoint, final int... vertexId) {
    if (vertexId.length == 1) {
      final GeometryFactory geometryFactory = getGeometryFactory();
      if (newPoint == null || newPoint.isEmpty()) {
        return (V)this;
      } else if (isEmpty()) {
        return newPoint.convertGeometry(geometryFactory);
      } else {
        final int vertexIndex = vertexId[0];
        final int vertexCount = getVertexCount();
        if (vertexIndex == 0 || vertexIndex == vertexCount - 1) {
          return appendVertex(newPoint);
        } else {
          newPoint = newPoint.convertGeometry(geometryFactory);
          final double[] coordinates = getCoordinates();
          final int axisCount = getAxisCount();
          final double[] newCoordinates = new double[axisCount * (vertexCount + 1)];

          final int beforeLength = vertexIndex * axisCount;
          System.arraycopy(coordinates, 0, newCoordinates, 0, beforeLength);

          CoordinatesListUtil.setCoordinates(newCoordinates, axisCount, vertexIndex, newPoint);

          final int afterSourceIndex = vertexIndex * axisCount;
          final int afterNewIndex = (vertexIndex + 1) * axisCount;
          final int afterLength = (vertexCount - vertexIndex) * axisCount;
          System.arraycopy(coordinates, afterSourceIndex, newCoordinates, afterNewIndex,
            afterLength);

          return (V)geometryFactory.linearRing(axisCount, newCoordinates);
        }
      }
    } else {
      throw new IllegalArgumentException("Geometry id's for " + getGeometryType()
        + " must have length 1. " + Arrays.toString(vertexId));
    }
  }

  /**
   * Tests whether this ring is closed.
   * Empty rings are closed by definition.
   *
   * @return true if this ring is closed
   */
  @Override
  public boolean isClosed() {
    if (isEmpty()) {
      // empty LinearRings are closed by definition
      return true;
    } else {
      return super.isClosed();
    }
  }

  @Override
  public LinearRing move(final double... deltas) {
    final GeometryFactory geometryFactory = getGeometryFactory();
    if (deltas == null || isEmpty()) {
      return this;
    } else {
      final double[] coordinates = moveCoordinates(deltas);
      final int axisCount = getAxisCount();
      return geometryFactory.linearRing(axisCount, coordinates);
    }
  }

  @Override
  public LinearRing moveVertex(Point newPoint, final int vertexIndex) {
    if (newPoint == null || newPoint.isEmpty()) {
      return this;
    } else if (isEmpty()) {
      throw new IllegalArgumentException("Cannot move vertex for empty LinearRing");
    } else {
      final int vertexCount = getVertexCount();

      if (vertexIndex >= 0 && vertexIndex < vertexCount) {
        final GeometryFactory geometryFactory = getGeometryFactory();
        newPoint = newPoint.convertGeometry(geometryFactory);
        final double[] coordinates = getCoordinates();
        final int axisCount = getAxisCount();
        if (vertexIndex == 0 || vertexIndex == vertexCount - 1) {
          CoordinatesListUtil.setCoordinates(coordinates, axisCount, 0, newPoint);
          CoordinatesListUtil.setCoordinates(coordinates, axisCount, vertexCount - 1, newPoint);
        } else {
          CoordinatesListUtil.setCoordinates(coordinates, axisCount, vertexIndex, newPoint);
        }
        return geometryFactory.linearRing(axisCount, coordinates);
      } else {
        throw new IllegalArgumentException("Vertex index must be between 0 and " + vertexCount);
      }
    }
  }

  @Override
  public LinearRing reverse() {
    final int vertexCount = getVertexCount();
    final int axisCount = getAxisCount();
    final double[] coordinates = new double[vertexCount * axisCount];
    for (int vertexIndex = 0; vertexIndex < vertexCount; vertexIndex++) {
      for (int axisIndex = 0; axisIndex < axisCount; axisIndex++) {
        final int coordinateIndex = (vertexCount - 1 - vertexIndex) * axisCount + axisIndex;
        coordinates[coordinateIndex] = getCoordinate(vertexIndex, axisIndex);
      }
    }
    final GeometryFactory geometryFactory = getGeometryFactory();
    final LinearRing reverseLine = geometryFactory.linearRing(axisCount, coordinates);
    return reverseLine;

  }

  private void validate() {
    if (isClosed()) {
      final int vertexCount = getVertexCount();
      if (vertexCount >= 1 && vertexCount <= 2) {
        throw new IllegalArgumentException(
          "Invalid number of points in LinearRing (found " + vertexCount + " - must be 0 or >= 3)");
      }
    } else {
      throw new IllegalArgumentException("Points of LinearRing do not form a closed linestring");
    }
  }
}
