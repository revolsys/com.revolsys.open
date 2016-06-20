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
import java.util.function.Function;

import javax.measure.quantity.Area;
import javax.measure.quantity.Length;
import javax.measure.unit.Unit;

import com.revolsys.datatype.DataType;
import com.revolsys.datatype.DataTypes;
import com.revolsys.geometry.algorithm.PointLocator;
import com.revolsys.geometry.model.impl.BoundingBoxDoubleGf;
import com.revolsys.geometry.model.segment.GeometryCollectionSegment;
import com.revolsys.geometry.model.segment.Segment;
import com.revolsys.geometry.model.vertex.GeometryCollectionVertex;
import com.revolsys.geometry.model.vertex.Vertex;
import com.revolsys.geometry.operation.polygonize.Polygonizer;
import com.revolsys.geometry.operation.valid.GeometryValidationError;

/**
 * Models a collection of {@link Geometry}s of
 * arbitrary type and dimension.
 *
 *
 *@version 1.7
 */
public interface GeometryCollection extends Geometry {
  static Geometry newGeometryCollection(final Object value) {
    if (value == null) {
      return null;
    } else if (value instanceof Geometry) {
      return (Geometry)value;
    } else {
      final String string = DataTypes.toString(value);
      return GeometryFactory.DEFAULT.geometry(string, false);
    }
  }

  static Geometry toSingleGeometry(Geometry value) {
    if (value instanceof GeometryCollection) {
      final GeometryCollection geometryCollection = (GeometryCollection)value;
      if (geometryCollection.getGeometryCount() == 1) {
        value = geometryCollection.getGeometry(0);
      }
    }
    return value;
  }

  @Override
  default boolean addIsSimpleErrors(final List<GeometryValidationError> errors,
    final boolean shortCircuit) {
    for (final Geometry geometry : geometries()) {
      if (!geometry.addIsSimpleErrors(errors, shortCircuit) && shortCircuit) {
        return false;
      }
    }
    return errors.isEmpty();
  }

  @Override
  @SuppressWarnings("unchecked")
  default <V extends Geometry> V appendVertex(final Point newPoint, final int... geometryId) {
    if (newPoint == null || newPoint.isEmpty()) {
      return (V)this;
    } else if (geometryId.length > 0) {
      final GeometryFactory geometryFactory = getGeometryFactory();
      if (isEmpty()) {
        return newPoint.copy(geometryFactory);
      } else {
        final int partIndex = geometryId[0];
        final int partCount = getGeometryCount();
        if (partIndex >= 0 && partIndex < partCount) {
          final int[] subId = new int[geometryId.length - 1];
          System.arraycopy(geometryId, 1, subId, 0, subId.length);
          final Geometry geometry = getGeometry(partIndex);
          final Geometry newGeometry = geometry.appendVertex(newPoint, subId);

          final List<Geometry> geometries = new ArrayList<>(getGeometries());
          geometries.set(partIndex, newGeometry);
          return (V)geometryFactory.geometryCollection(geometries);
        } else {
          throw new IllegalArgumentException(
            "Part index must be between 0 and " + partCount + " not " + partIndex);
        }
      }
    } else {
      throw new IllegalArgumentException(
        "Vertex id's for GeometryCollection must have length > 1. " + Arrays.toString(geometryId));
    }
  }

  @Override
  default int compareToSameClass(final Geometry geometry) {
    final Set<Geometry> theseElements = new TreeSet<>(getGeometries());
    final Set<Geometry> otherElements = new TreeSet<>(geometry.getGeometries());
    return Geometry.compare(theseElements, otherElements);
  }

  @Override
  @SuppressWarnings("unchecked")
  default <V extends Geometry> V copy(final GeometryFactory geometryFactory) {
    final List<Geometry> geometries = new ArrayList<Geometry>();
    for (final Geometry geometry : geometries()) {
      geometries.add(geometry.copy(geometryFactory));
    }
    return (V)geometryFactory.geometryCollection(geometries);
  }

  @Override
  @SuppressWarnings("unchecked")
  default <V extends Geometry> V deleteVertex(final int... vertexId) {
    if (vertexId.length > 1) {
      if (isEmpty()) {
        throw new IllegalArgumentException("Cannot delete vertex for empty MultiPoint");
      } else {
        final int partIndex = vertexId[0];
        final int partCount = getGeometryCount();
        if (partIndex >= 0 && partIndex < partCount) {
          final GeometryFactory geometryFactory = getGeometryFactory();

          final int[] subId = new int[vertexId.length - 1];
          System.arraycopy(vertexId, 1, subId, 0, subId.length);
          final Geometry geometry = getGeometry(partIndex);
          final Geometry newGeometry = geometry.deleteVertex(subId);

          final List<Geometry> geometries = new ArrayList<>(getGeometries());
          geometries.set(partIndex, newGeometry);
          return (V)geometryFactory.geometryCollection(geometries);
        } else {
          throw new IllegalArgumentException(
            "Part index must be between 0 and " + partCount + " not " + partIndex);
        }
      }
    } else {
      throw new IllegalArgumentException(
        "Vertex id's for GeometryCollection must have length > 1. " + Arrays.toString(vertexId));
    }
  }

  @Override
  default boolean equals(final int axisCount, final Geometry geometry) {
    if (geometry == this) {
      return true;
    } else if (geometry == null) {
      return false;
    } else if (axisCount < 2) {
      throw new IllegalArgumentException("Axis Count must be >=2");
    } else if (isEquivalentClass(geometry)) {
      if (isEmpty()) {
        return geometry.isEmpty();
      } else if (geometry.isEmpty()) {
        return false;
      } else {
        final int geometryCount1 = getGeometryCount();
        final int geometryCount2 = geometry.getGeometryCount();
        if (geometryCount1 == geometryCount2) {
          for (int i = 0; i < geometryCount1; i++) {
            final Geometry part1 = getGeometry(i);
            final Geometry part2 = geometry.getGeometry(i);
            if (!part1.equals(axisCount, part2)) {
              return false;
            }
          }
          return true;
        }
      }
    }
    return false;
  }

  @Override
  default boolean equalsExact(final Geometry other, final double tolerance) {
    if (!isEquivalentClass(other)) {
      return false;
    }
    final GeometryCollection otherCollection = (GeometryCollection)other;
    if (getGeometryCount() != otherCollection.getGeometryCount()) {
      return false;
    }
    int i = 0;
    for (final Geometry geometry : geometries()) {
      if (!geometry.equalsExact(otherCollection.getGeometry(i), tolerance)) {
        return false;
      }
      i++;
    }
    return true;
  }

  @Override
  default Iterable<Geometry> geometries() {
    return getGeometries();
  }

  default GeometryCollection geometryCollection(final Function<Geometry, Geometry> function) {
    if (!isEmpty()) {
      boolean changed = false;
      final List<Geometry> geometries = new ArrayList<>();
      for (final Geometry geometry : geometries()) {
        final Geometry newGeometry = function.apply(geometry);
        changed |= geometry != newGeometry;
        geometries.add(newGeometry);
      }
      if (changed) {
        final GeometryFactory geometryFactory = getGeometryFactory();
        return geometryFactory.geometryCollection(geometries);
      }
    }
    return this;
  }

  /**
   *  Returns the area of this <code>GeometryCollection</code>
   *
   * @return the area of the polygon
   */
  @Override
  default double getArea() {
    double totalArea = 0.0;
    for (final Geometry geometry : geometries()) {
      final double area = geometry.getArea();
      totalArea += area;
    }
    return totalArea;
  }

  @Override
  default double getArea(final Unit<Area> unit) {
    double totalArea = 0.0;
    for (final Geometry geometry : geometries()) {
      final double area = geometry.getArea(unit);
      totalArea += area;
    }
    return totalArea;
  }

  @Override
  default Geometry getBoundary() {
    throw new IllegalArgumentException("This method does not support GeometryCollection arguments");
  }

  @Override
  default int getBoundaryDimension() {
    int dimension = Dimension.FALSE;
    for (final Geometry geometry : geometries()) {
      dimension = Math.max(dimension, geometry.getBoundaryDimension());
    }
    return dimension;
  }

  @Override
  default DataType getDataType() {
    return DataTypes.GEOMETRY_COLLECTION;
  }

  @Override
  default int getDimension() {
    int dimension = Dimension.FALSE;
    for (final Geometry geometry : geometries()) {
      dimension = Math.max(dimension, geometry.getDimension());
    }
    return dimension;
  }

  @Override
  default <V extends Geometry> List<V> getGeometries(final Class<V> geometryClass) {
    final List<V> geometries = Geometry.super.getGeometries(geometryClass);
    for (final Geometry geometry : geometries()) {
      if (geometry != null) {
        final List<V> partGeometries = geometry.getGeometries(geometryClass);
        geometries.addAll(partGeometries);
      }
    }
    return geometries;
  }

  @Override
  @SuppressWarnings("unchecked")
  default <V extends Geometry> List<V> getGeometryComponents(final Class<V> geometryClass) {
    final List<V> geometries = new ArrayList<V>();
    if (geometryClass.isAssignableFrom(getClass())) {
      geometries.add((V)this);
    }
    for (final Geometry geometry : geometries()) {
      if (geometry != null) {
        final List<V> partGeometries = geometry.getGeometryComponents(geometryClass);
        geometries.addAll(partGeometries);
      }
    }
    return geometries;
  }

  @Override
  default double getLength() {
    double totalLength = 0.0;
    for (final Geometry geometry : geometries()) {
      final double length = geometry.getLength();
      totalLength += length;
    }
    return totalLength;
  }

  @Override
  default double getLength(final Unit<Length> unit) {
    double totalLength = 0.0;
    for (final Geometry geometry : geometries()) {
      final double length = geometry.getLength(unit);
      totalLength += length;
    }
    return totalLength;
  }

  @Override
  default Point getPoint() {
    if (isEmpty()) {
      return null;
    } else {
      return getGeometry(0).getPoint();
    }
  }

  @Override
  default Point getPointWithin() {
    if (!isEmpty()) {
      for (final Geometry geometry : geometries()) {
        final Point point = geometry.getPointWithin();
        if (!point.isEmpty()) {
          return point;
        }
      }
      return getPoint();
    }
    final GeometryFactory geometryFactory = getGeometryFactory();
    return geometryFactory.point();
  }

  @Override
  default Segment getSegment(final int... segmentId) {
    return new GeometryCollectionSegment(this, segmentId);
  }

  @Override
  default Vertex getToVertex(final int... vertexId) {
    return new GeometryCollectionVertex(this, vertexId);
  }

  @Override
  default Vertex getVertex(final int... vertexId) {
    return new GeometryCollectionVertex(this, vertexId);
  }

  @Override
  default int getVertexCount() {
    int numPoints = 0;
    for (final Geometry geometry : geometries()) {
      numPoints += geometry.getVertexCount();
    }
    return numPoints;
  }

  @Override
  @SuppressWarnings("unchecked")
  default <V extends Geometry> V insertVertex(final Point newPoint, final int... vertexId) {
    if (newPoint == null || newPoint.isEmpty()) {
      return (V)this;
    } else if (vertexId.length > 1) {
      final GeometryFactory geometryFactory = getGeometryFactory();
      if (isEmpty()) {
        return newPoint.convertGeometry(geometryFactory);
      } else {
        final int partIndex = vertexId[0];
        final int partCount = getGeometryCount();
        if (partIndex >= 0 && partIndex < partCount) {
          final int[] subId = new int[vertexId.length - 1];
          System.arraycopy(vertexId, 1, subId, 0, subId.length);
          final Geometry geometry = getGeometry(partIndex);
          final Geometry newGeometry = geometry.insertVertex(newPoint, subId);

          final List<Geometry> geometries = new ArrayList<>(getGeometries());
          geometries.set(partIndex, newGeometry);
          return (V)geometryFactory.geometryCollection(geometries);
        } else {
          throw new IllegalArgumentException(
            "Part index must be between 0 and " + partCount + " not " + partIndex);
        }
      }
    } else {
      throw new IllegalArgumentException("Vertex id's for " + getGeometryType()
        + " must have length > 1. " + Arrays.toString(vertexId));
    }
  }

  @Override
  default boolean intersects(final BoundingBox boundingBox) {
    if (isEmpty() || boundingBox.isEmpty()) {
      return false;
    } else {
      for (final Geometry geometry : geometries()) {
        if (geometry.intersects(boundingBox)) {
          return true;
        }
      }
      return false;
    }
  }

  @Override
  default boolean isEmpty() {
    if (getGeometryCount() == 0) {
      return true;
    } else {
      for (final Geometry geometry : geometries()) {
        if (!geometry.isEmpty()) {
          return false;
        }
      }
      return true;
    }
  }

  @Override
  default boolean isEquivalentClass(final Geometry other) {
    return other instanceof GeometryCollection;
  }

  @Override
  default Location locate(final Point point) {
    return new PointLocator().locate(point, this);
  }

  @Override
  default Geometry move(final double... deltas) {
    if (deltas == null || isEmpty()) {
      return this;
    } else {
      final List<Geometry> parts = new ArrayList<>();
      for (final Geometry part : geometries()) {
        final Geometry movedPart = part.move(deltas);
        parts.add(movedPart);
      }
      final GeometryFactory geometryFactory = getGeometryFactory();
      return geometryFactory.geometryCollection(parts);
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  default <V extends Geometry> V moveVertex(final Point newPoint, final int... vertexId) {
    if (newPoint == null || newPoint.isEmpty()) {
      return (V)this;
    } else if (vertexId.length > 1) {
      if (isEmpty()) {
        throw new IllegalArgumentException("Cannot move vertex for empty " + getGeometryType());
      } else {
        final int partIndex = vertexId[0];
        final int partCount = getGeometryCount();
        if (partIndex >= 0 && partIndex < partCount) {
          final GeometryFactory geometryFactory = getGeometryFactory();

          final int[] subId = new int[vertexId.length - 1];
          System.arraycopy(vertexId, 1, subId, 0, subId.length);
          final Geometry geometry = getGeometry(partIndex);
          final Geometry newGeometry = geometry.moveVertex(newPoint, subId);

          final List<Geometry> geometries = new ArrayList<>(getGeometries());
          geometries.set(partIndex, newGeometry);
          return (V)geometryFactory.geometryCollection(geometries);
        } else {
          throw new IllegalArgumentException(
            "Part index must be between 0 and " + partCount + " not " + partIndex);
        }
      }
    } else {
      throw new IllegalArgumentException("Vertex id's for " + getGeometryType()
        + " must have length > 1. " + Arrays.toString(vertexId));
    }
  }

  @Override
  default BoundingBox newBoundingBox() {
    BoundingBox envelope = new BoundingBoxDoubleGf(getGeometryFactory());
    for (final Geometry geometry : geometries()) {
      envelope = envelope.expandToInclude(geometry);
    }
    return envelope;
  }

  @SuppressWarnings("unchecked")
  @Override
  default <G> G newUsingGeometryFactory(final GeometryFactory factory) {
    if (factory == getGeometryFactory()) {
      return (G)this;
    } else if (isEmpty()) {
      return (G)factory.geometryCollection();
    } else {
      final Geometry[] polygons = new Geometry[getGeometryCount()];
      for (int i = 0; i < getGeometryCount(); i++) {
        Geometry geometry = getGeometry(i);
        geometry = geometry.newUsingGeometryFactory(factory);
        polygons[i] = geometry;
      }
      return (G)factory.geometryCollection(polygons);
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
      final List<Geometry> geometries = new ArrayList<>();
      final Polygonizer polygonizer = new Polygonizer();
      for (final Geometry geometry : geometries()) {
        if (geometry instanceof Polygon) {
          final Polygon polygon = (Polygon)geometry;
          polygonizer.addPolygon(polygon);
        } else {
          geometries.add(geometry.newValidGeometry());
        }
      }
      geometries.addAll(polygonizer.getPolygonal().getGeometries());
      return (G)getGeometryFactory().geometry(geometries).union();
    }
  }

  @Override
  default Geometry normalize() {
    final List<Geometry> geometries = new ArrayList<>();
    for (final Geometry part : geometries()) {
      final Geometry normalizedPart = part.normalize();
      geometries.add(normalizedPart);
    }
    Collections.sort(geometries);
    final GeometryFactory geometryFactory = getGeometryFactory();
    final GeometryCollection normalizedGeometry = geometryFactory.geometryCollection(geometries);
    return normalizedGeometry;
  }

  @Override
  default Geometry removeDuplicatePoints() {
    if (isEmpty()) {
      return this;
    } else {
      final List<Geometry> geometries = new ArrayList<Geometry>();
      for (final Geometry geometry : geometries()) {
        if (geometry != null && !geometry.isEmpty()) {
          geometries.add(geometry.removeDuplicatePoints());
        }
      }
      final GeometryFactory geometryFactory = getGeometryFactory();
      return geometryFactory.geometryCollection(geometries);
    }
  }

  /**
   * Creates a {@link GeometryCollection} with
   * every component reversed.
   * The order of the components in the collection are not reversed.
   *
   * @return a {@link GeometryCollection} in the reverse order
   */
  @Override
  default Geometry reverse() {
    final List<Geometry> revGeoms = new ArrayList<>();
    for (final Geometry geometry : geometries()) {
      if (!geometry.isEmpty()) {
        final Geometry reverse = geometry.reverse();
        revGeoms.add(reverse);
      }
    }
    final GeometryFactory geometryFactory = getGeometryFactory();
    return geometryFactory.geometryCollection(revGeoms);
  }

  @Override
  default Iterable<Segment> segments() {
    return new GeometryCollectionSegment(this, -1);
  }

  @SuppressWarnings("unchecked")
  @Override
  default <G extends Geometry> G toClockwise() {
    return (G)geometryCollection(Geometry::toClockwise);
  }

  @SuppressWarnings("unchecked")
  @Override
  default <G extends Geometry> G toCounterClockwise() {
    return (G)geometryCollection(Geometry::toCounterClockwise);
  }

  @Override
  default Vertex vertices() {
    final GeometryCollectionVertex vertex = new GeometryCollectionVertex(this, -1);
    return vertex;
  }
}
