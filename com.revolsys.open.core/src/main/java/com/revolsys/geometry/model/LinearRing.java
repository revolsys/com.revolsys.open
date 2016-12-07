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

import javax.measure.Measure;
import javax.measure.quantity.Area;
import javax.measure.quantity.Length;
import javax.measure.unit.Unit;

import com.revolsys.datatype.DataTypes;
import com.revolsys.geometry.cs.CoordinateSystem;
import com.revolsys.geometry.cs.GeographicCoordinateSystem;
import com.revolsys.geometry.cs.ProjectedCoordinateSystem;
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

  @SuppressWarnings("unchecked")
  static <G extends LinearRing> G newLinearRing(final Object value) {
    if (value == null) {
      return null;
    } else if (value instanceof LinearRing) {
      return (G)value;
    } else if (value instanceof Geometry) {
      throw new IllegalArgumentException(
        ((Geometry)value).getGeometryType() + " cannot be converted to a LinearRing");
    } else {
      final String string = DataTypes.toString(value);
      return (G)GeometryFactory.DEFAULT.geometry(string, false);
    }
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

  /**
   * Get the area of the polygon using the http://en.wikipedia.org/wiki/Shoelace_formula
   *
   * @return The area of the polygon.
   */
  default double getPolygonArea() {
    final int vertexCount = getVertexCount();
    double area;
    if (vertexCount < 3) {
      area = 0;
    } else {
      /**
       * Based on the Shoelace formula.
       * http://en.wikipedia.org/wiki/Shoelace_formula
       */
      double p1x = getX(0);
      double p1y = getY(0);

      final double x0 = p1x;
      double p2x = getX(1) - x0;
      double p2y = getY(1);
      double sum = 0;
      for (int i = 1; i < vertexCount - 1; i++) {
        final double p0y = p1y;
        p1x = p2x;
        p1y = p2y;
        p2x = getX(i + 1) - x0;
        p2y = getY(i + 1);
        sum += p1x * (p0y - p2y);
      }
      area = Math.abs(sum / 2.0);
    }
    return area;
  }

  default double getPolygonArea(final Unit<Area> unit) {
    double area = 0;
    final CoordinateSystem coordinateSystem = getCoordinateSystem();
    if (coordinateSystem instanceof GeographicCoordinateSystem) {
      // TODO better algorithm than converting to world mercator
      final GeometryFactory geometryFactory = GeometryFactory.worldMercator();
      final LinearRing ring = convertGeometry(geometryFactory, 2);
      return ring.getPolygonArea(unit);
    } else if (coordinateSystem instanceof ProjectedCoordinateSystem) {
      final ProjectedCoordinateSystem projectedCoordinateSystem = (ProjectedCoordinateSystem)coordinateSystem;
      final Unit<Length> lengthUnit = projectedCoordinateSystem.getLengthUnit();
      @SuppressWarnings("unchecked")
      final Unit<Area> areaUnit = (Unit<Area>)lengthUnit.times(lengthUnit);
      area = getPolygonArea();
      final Measure<Area> areaMeasure = Measure.valueOf(area, areaUnit);
      area = areaMeasure.doubleValue(unit);
    } else {
      area = getPolygonArea();
    }
    return area;
  }

  @Override
  default boolean isValid() {
    return LineString.super.isValid();
  }

  @Override
  default LinearRing move(final double... deltas) {
    return (LinearRing)LineString.super.move(deltas);
  }

  @Override
  default LinearRing moveVertex(final Point newPoint, final int vertexIndex) {
    return (LinearRing)LineString.super.moveVertex(newPoint, vertexIndex);
  }

  @Override
  default LinearRing newGeometry(final GeometryFactory geometryFactory) {
    if (geometryFactory == null) {
      return this.clone();
    } else if (isEmpty()) {
      return geometryFactory.linearRing();
    } else {
      final double[] coordinates = convertCoordinates(geometryFactory);
      final int axisCount = getAxisCount();
      return geometryFactory.linearRing(axisCount, coordinates);
    }
  }

  @Override
  default LinearRing newLineStringEmpty() {
    final GeometryFactory geometryFactory = getGeometryFactory();
    return geometryFactory.linearRing(this);
  }

  @Override
  default LinearRing newLineString(final double... coordinates) {
    final GeometryFactory geometryFactory = getGeometryFactory();
    final int axisCount = getAxisCount();
    return geometryFactory.linearRing(axisCount, coordinates);
  }

  @Override
  default LineString newLineString(final int vertexCount, final double... coordinates) {
    final GeometryFactory geometryFactory = getGeometryFactory();
    final int axisCount = getAxisCount();
    return geometryFactory.linearRing(axisCount, vertexCount, coordinates);
  }

  default Polygon newPolygon() {
    final GeometryFactory geometryFactory = getGeometryFactory();
    return geometryFactory.polygon(this);
  }

  @SuppressWarnings("unchecked")
  @Override
  default <G> G newUsingGeometryFactory(final GeometryFactory factory) {
    if (factory == getGeometryFactory()) {
      return (G)this;
    } else if (isEmpty()) {
      return (G)factory.linearRing();
    } else {
      final double[] coordinates = getCoordinates();
      final int axisCount = getAxisCount();
      return (G)factory.linearRing(axisCount, coordinates);
    }
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
  default LinearRing removeDuplicatePoints() {
    if (isEmpty()) {
      return this;
    } else {
      final int vertexCount = getVertexCount();
      if (vertexCount < 3) {
        return this;
      } else {
        final int axisCount = getAxisCount();
        final double[] coordinates = new double[vertexCount * axisCount];
        double previousX = getX(0);
        double previousY = getY(0);
        CoordinatesListUtil.setCoordinates(coordinates, axisCount, 0, this, 0);
        int j = 1 + 0;
        for (int i = 1; i < vertexCount; i++) {
          final double x = getX(i);
          final double y = getY(i);
          if (x != previousX || y != previousY) {
            CoordinatesListUtil.setCoordinates(coordinates, axisCount, j++, this, i);
          }

          previousX = x;
          previousY = y;
        }
        final GeometryFactory geometryFactory = getGeometryFactory();
        if (j < 3) {
          return geometryFactory.linearRing();
        } else {
          return geometryFactory.linearRing(axisCount, j, coordinates);
        }
      }
    }
  }

  @Override
  default LinearRing reverse() {
    return (LinearRing)LineString.super.reverse();
  }
}
