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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.measure.quantity.Area;
import javax.measure.quantity.Length;
import javax.measure.unit.Unit;

import com.revolsys.datatype.DataType;
import com.revolsys.datatype.DataTypes;
import com.revolsys.geometry.model.impl.PointDouble;
import com.revolsys.geometry.model.segment.Segment;
import com.revolsys.geometry.model.vertex.MultiPointVertex;
import com.revolsys.geometry.model.vertex.Vertex;
import com.revolsys.geometry.operation.simple.DuplicateVertexError;
import com.revolsys.geometry.operation.valid.GeometryValidationError;
import com.revolsys.util.Property;

/**
 * Models a collection of {@link Point}s.
 * <p>
 * Any collection of Point is a valid MultiPoint.
 *
 *@version 1.7
 */
public interface MultiPoint extends GeometryCollection, Punctual {
  @SuppressWarnings("unchecked")
  static <G extends Geometry> G newMultiPoint(final Object value) {
    if (value == null) {
      return null;
    } else if (value instanceof MultiPoint) {
      return (G)value;
    } else if (value instanceof Geometry) {
      final Geometry geometry = (Geometry)value;
      final GeometryFactory geometryFactory = geometry.getGeometryFactory();
      return (G)geometryFactory.multiPoint(geometry);
    } else {
      final String string = DataTypes.toString(value);
      return (G)GeometryFactory.DEFAULT.geometry(string, false);
    }
  }

  @Override
  default boolean addIsSimpleErrors(final List<GeometryValidationError> errors,
    final boolean shortCircuit) {
    final Set<Point> points = new TreeSet<>();
    for (final Vertex vertex : vertices()) {
      final Point point = new PointDouble(vertex, 2);
      if (points.contains(point)) {
        final DuplicateVertexError error = new DuplicateVertexError(vertex);
        if (shortCircuit) {
          return false;
        } else {
          errors.add(error);
        }
      } else {
        points.add(point);
      }
    }
    return errors.isEmpty();
  }

  @Override
  @SuppressWarnings("unchecked")
  default <V extends Geometry> V appendVertex(final Point newPoint, final int... geometryId) {
    if (newPoint == null || newPoint.isEmpty()) {
      return (V)this;
    } else {
      final GeometryFactory geometryFactory = getGeometryFactory();
      if (isEmpty()) {
        return newPoint.copy(geometryFactory);
      } else {
        final List<Point> points = getPoints();
        points.add(newPoint);
        return (V)geometryFactory.multiPoint(points);
      }
    }
  }

  @Override
  MultiPoint clone();

  @Override
  @SuppressWarnings("unchecked")

  default <V extends Geometry> V copy(final GeometryFactory geometryFactory) {
    final List<Point> newPoints = new ArrayList<>();
    final List<Point> points = getPoints();
    for (final Point point : points) {
      final Point newPoint = point.copy(geometryFactory);
      newPoints.add(newPoint);
    }
    return (V)geometryFactory.multiPoint(newPoints);
  }

  @Override
  default double distance(Geometry geometry, final double terminateDistance) {
    if (isEmpty()) {
      return 0.0;
    } else if (Property.isEmpty(geometry)) {
      return 0.0;
    } else {
      final GeometryFactory geometryFactory = getGeometryFactory();
      geometry = geometry.convertGeometry(geometryFactory, 2);
      double minDistance = Double.MAX_VALUE;
      for (final Point point : getPoints()) {
        final double distance = geometry.distance(point, terminateDistance);
        if (distance < minDistance) {
          minDistance = distance;
          if (distance <= terminateDistance) {
            return distance;
          }
        }
      }
      return minDistance;
    }
  }

  @Override
  default boolean equalsExact(final Geometry other, final double tolerance) {
    if (!isEquivalentClass(other)) {
      return false;
    }
    return GeometryCollection.super.equalsExact(other, tolerance);
  }

  @Override
  default double getArea() {
    return 0;
  }

  @Override
  default double getArea(final Unit<Area> unit) {
    return 0;
  }

  /**
   * Gets the boundary of this geometry.
   * Zero-dimensional geometries have no boundary by definition,
   * so an empty GeometryCollection is returned.
   *
   * @return an empty GeometryCollection
   * @see Geometry#getBoundary
   */

  @Override
  default Geometry getBoundary() {
    return getGeometryFactory().geometryCollection();
  }

  @Override
  default int getBoundaryDimension() {
    return Dimension.FALSE;
  }

  /**
   *  Returns the <code>Coordinate</code> at the given position.
   *
   *@param  n  the partIndex of the <code>Coordinate</code> to retrieve, beginning
   *      at 0
   *@return    the <code>n</code>th <code>Coordinate</code>
   */
  default Point getCoordinate(final int n) {
    return getPoint(n);
  }

  default double getCoordinate(final int partIndex, final int axisIndex) {
    final Point point = getPoint(partIndex);
    return point.getCoordinate(axisIndex);
  }

  @Override
  default DataType getDataType() {
    return DataTypes.MULTI_POINT;
  }

  @Override
  default int getDimension() {
    return 0;
  }

  @Override
  default double getLength() {
    return 0;
  }

  @Override
  default double getLength(final Unit<Length> unit) {
    return 0;
  }

  default Point getPoint(final int partIndex) {
    return (Point)getGeometry(partIndex);
  }

  @SuppressWarnings({
    "unchecked", "rawtypes"
  })
  default <V extends Point> List<V> getPoints() {
    return (List)getGeometries();
  }

  @Override
  default Segment getSegment(final int... segmentId) {
    return null;
  }

  @Override
  default Vertex getToVertex(final int... vertexId) {
    if (vertexId.length <= 2) {
      if (vertexId.length == 1 || vertexId[1] == 0) {
        final int vertexIndex = vertexId[0];
        final int geometryCount = getGeometryCount();
        if (vertexIndex >= 0 || vertexIndex < geometryCount) {
          return new MultiPointVertex(this, geometryCount - vertexIndex - 1);
        }
      }
    }
    return null;
  }

  @Override
  default Vertex getVertex(final int... vertexId) {
    if (vertexId.length <= 2) {
      if (vertexId.length == 1 || vertexId[1] == 0) {
        final int vertexIndex = vertexId[0];
        if (vertexIndex >= 0 || vertexIndex < getGeometryCount()) {
          return new MultiPointVertex(this, vertexId);
        }
      }
    }
    return null;
  }

  @Override
  default boolean intersects(final Geometry geometry) {
    for (final Point point : points()) {
      if (point.intersects(geometry)) {
        return true;
      }
    }
    return false;
  }

  @Override
  default boolean isEquivalentClass(final Geometry other) {
    return other instanceof MultiPoint;
  }

  @Override
  default boolean isValid() {
    return true;
  }

  @Override
  @SuppressWarnings("unchecked")

  default <V extends Geometry> V moveVertex(Point newPoint, final int... vertexId) {
    if (newPoint == null || newPoint.isEmpty()) {
      return (V)this;
    } else if (vertexId.length <= 2) {
      if (isEmpty()) {
        throw new IllegalArgumentException("Cannot move vertex for empty MultiPoint");
      } else {
        final int partIndex = vertexId[0];
        final int partCount = getGeometryCount();
        if (partIndex >= 0 && partIndex < partCount) {
          final GeometryFactory geometryFactory = getGeometryFactory();

          newPoint = newPoint.copy(geometryFactory);
          final List<Point> points = new ArrayList<>(getPoints());
          points.set(partIndex, newPoint);
          return (V)geometryFactory.multiPoint(points);
        } else {
          throw new IllegalArgumentException(
            "Part index must be between 0 and " + partCount + " not " + partIndex);
        }
      }
    } else {
      throw new IllegalArgumentException(
        "Vertex id's for MultiPoint must have length 1. " + Arrays.toString(vertexId));
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  default <G> G newUsingGeometryFactory(final GeometryFactory factory) {
    if (factory == getGeometryFactory()) {
      return (G)this;
    } else if (isEmpty()) {
      return (G)factory.multiPoint();
    } else {
      final Point[] points = new Point[getGeometryCount()];
      for (int i = 0; i < getGeometryCount(); i++) {
        Point point = getPoint(i);
        point = point.newUsingGeometryFactory(factory);
        points[i] = point;
      }
      return (G)factory.multiPoint(points);
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  default <G extends Geometry> G newValidGeometry() {
    if (isEmpty()) {
      return (G)this;
    } else if (isValid()) {
      return (G)normalize();
    } else {
      return (G)union();
    }
  }

  @Override
  default MultiPoint normalize() {
    if (isEmpty()) {
      return this;
    } else {
      final List<Point> geometries = new ArrayList<>();
      for (final Geometry part : geometries()) {
        final Point normalizedPart = (Point)part.normalize();
        geometries.add(normalizedPart);
      }
      Collections.sort(geometries);
      final GeometryFactory geometryFactory = getGeometryFactory();
      final MultiPoint normalizedGeometry = geometryFactory.multiPoint(geometries);
      return normalizedGeometry;
    }
  }

  default Iterable<Point> points() {
    return getGeometries();
  }

  @Override
  @SuppressWarnings("unchecked")
  default <G extends Geometry> G toClockwise() {
    return (G)this;
  }

  @Override
  @SuppressWarnings("unchecked")
  default <G extends Geometry> G toCounterClockwise() {
    return (G)this;
  }

  @Override
  default MultiPointVertex vertices() {
    return new MultiPointVertex(this, -1);
  }
}
