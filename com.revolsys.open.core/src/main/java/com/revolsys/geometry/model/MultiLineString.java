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
import java.util.LinkedList;
import java.util.List;

import javax.measure.quantity.Area;
import javax.measure.unit.Unit;

import com.revolsys.datatype.DataType;
import com.revolsys.datatype.DataTypes;
import com.revolsys.geometry.model.segment.MultiLineStringSegment;
import com.revolsys.geometry.model.segment.Segment;
import com.revolsys.geometry.model.vertex.MultiLineStringVertex;
import com.revolsys.geometry.model.vertex.Vertex;
import com.revolsys.geometry.operation.BoundaryOp;
import com.revolsys.geometry.operation.valid.GeometryValidationError;
import com.revolsys.util.Property;

/**
 * Models a collection of (@link LineString}s.
 * <p>
 * Any collection of LineStrings is a valid MultiLineString.
 *
 *@version 1.7
 */
public interface MultiLineString extends GeometryCollection, Lineal {
  @SuppressWarnings("unchecked")
  static <G extends Geometry> G newMultiLineString(final Object value) {
    if (value == null) {
      return null;
    } else if (value instanceof MultiLineString) {
      return (G)value;
    } else if (value instanceof Geometry) {
      final Geometry geometry = (Geometry)value;
      final GeometryFactory geometryFactory = geometry.getGeometryFactory();
      return (G)geometryFactory.multiLineString(geometry);
    } else {
      final String string = DataTypes.toString(value);
      return (G)GeometryFactory.DEFAULT.geometry(string, false);
    }
  }

  @Override
  default boolean addIsSimpleErrors(final List<GeometryValidationError> errors,
    final boolean shortCircuit) {
    return Lineal.addIsSimpleErrors(this, errors, shortCircuit);
  }

  @Override
  MultiLineString clone();

  @Override
  @SuppressWarnings("unchecked")
  default <V extends Geometry> V copy(final GeometryFactory geometryFactory) {
    final List<LineString> lines = new ArrayList<LineString>();
    for (final LineString line : getLineStrings()) {
      final LineString newLine = line.copy(geometryFactory);
      lines.add(newLine);
    }
    return (V)geometryFactory.multiLineString(lines);
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
      for (final LineString line : lineStrings()) {
        final double distance = geometry.distance(line);
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
    } else {
      return GeometryCollection.super.equalsExact(other, tolerance);
    }
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
   * The boundary of a lineal geometry is always a zero-dimensional geometry (which may be empty).
   *
   * @return the boundary geometry
   * @see Geometry#getBoundary
   */
  @Override
  default Geometry getBoundary() {
    return new BoundaryOp(this).getBoundary();
  }

  @Override
  default int getBoundaryDimension() {
    if (isClosed()) {
      return Dimension.FALSE;
    }
    return 0;
  }

  @Override
  default DataType getDataType() {
    return DataTypes.MULTI_LINE_STRING;
  }

  @Override
  default int getDimension() {
    return 1;
  }

  default LineString getLineString(final int partIndex) {
    return (LineString)getGeometry(partIndex);
  }

  @SuppressWarnings({
    "unchecked", "rawtypes"
  })
  default <V extends LineString> List<V> getLineStrings() {
    return (List)getGeometries();
  }

  @Override
  default Segment getSegment(final int... segmentId) {
    if (segmentId == null || segmentId.length != 2) {
      return null;
    } else {
      final int partIndex = segmentId[0];
      if (partIndex >= 0 && partIndex < getGeometryCount()) {
        final LineString line = getLineString(partIndex);
        final int segmentIndex = segmentId[1];
        if (segmentIndex >= 0 && segmentIndex < line.getSegmentCount()) {
          return new MultiLineStringSegment(this, segmentId);
        }
      }
      return null;
    }
  }

  @Override
  default int getSegmentCount() {
    int segmentCount = 0;
    for (final LineString line : lineStrings()) {
      segmentCount += line.getSegmentCount();
    }
    return segmentCount;
  }

  @Override
  default Vertex getToVertex(int... vertexId) {
    if (vertexId == null || vertexId.length != 2) {
      return null;
    } else {
      final int partIndex = vertexId[0];
      if (partIndex >= 0 && partIndex < getGeometryCount()) {
        final LineString line = getLineString(partIndex);
        int vertexIndex = vertexId[1];
        final int vertexCount = line.getVertexCount();
        vertexIndex = vertexCount - 1 - vertexIndex;
        if (vertexIndex <= vertexCount) {
          while (vertexIndex < 0) {
            vertexIndex += vertexCount - 1;
          }
          vertexId = Geometry.setVertexIndex(vertexId, vertexIndex);
          return new MultiLineStringVertex(this, vertexId);
        }
      }
      return null;
    }
  }

  @Override
  default Vertex getVertex(final int... vertexId) {
    if (vertexId == null || vertexId.length != 2) {
      return null;
    } else {
      final int partIndex = vertexId[0];
      if (partIndex >= 0 && partIndex < getGeometryCount()) {
        final LineString line = getLineString(partIndex);
        int vertexIndex = vertexId[1];
        final int vertexCount = line.getVertexCount();
        if (vertexIndex <= vertexCount) {
          while (vertexIndex < 0) {
            vertexIndex += vertexCount - 1;
          }
          return new MultiLineStringVertex(this, vertexId);
        }
      }
      return null;
    }
  }

  default boolean isClosed() {
    if (isEmpty()) {
      return false;
    }
    for (final LineString line : getLineStrings()) {
      if (line.isEmpty()) {
        return false;
      } else if (!line.isClosed()) {
        return false;
      }
    }
    return true;
  }

  @Override
  default boolean isEquivalentClass(final Geometry other) {
    return other instanceof MultiLineString;
  }

  default Iterable<LineString> lineStrings() {
    return getGeometries();
  }

  @Override
  @SuppressWarnings("unchecked")
  default <V extends Geometry> V moveVertex(final Point newPoint, final int... vertexId) {
    if (newPoint == null || newPoint.isEmpty()) {
      return (V)this;
    } else if (vertexId.length == 2) {
      if (isEmpty()) {
        throw new IllegalArgumentException("Cannot move vertex for empty MultiLineString");
      } else {
        final int partIndex = vertexId[0];
        final int vertexIndex = vertexId[1];
        final int partCount = getGeometryCount();
        if (partIndex >= 0 && partIndex < partCount) {
          final GeometryFactory geometryFactory = getGeometryFactory();

          final LineString line = getLineString(partIndex);
          final LineString newLine = line.moveVertex(newPoint, vertexIndex);
          final List<LineString> lines = new ArrayList<>(getLineStrings());
          lines.set(partIndex, newLine);
          return (V)geometryFactory.multiLineString(lines);
        } else {
          throw new IllegalArgumentException(
            "Part index must be between 0 and " + partCount + " not " + partIndex);
        }
      }
    } else {
      throw new IllegalArgumentException(
        "Vertex id's for MultiLineStrings must have length 2. " + Arrays.toString(vertexId));
    }
  }

  @Override
  default MultiLineString normalize() {
    if (isEmpty()) {
      return this;
    } else {
      final List<LineString> geometries = new ArrayList<>();
      for (final Geometry part : geometries()) {
        final LineString normalizedPart = (LineString)part.normalize();
        geometries.add(normalizedPart);
      }
      Collections.sort(geometries);
      final GeometryFactory geometryFactory = getGeometryFactory();
      final MultiLineString normalizedGeometry = geometryFactory.multiLineString(geometries);
      return normalizedGeometry;
    }
  }

  /**
   * Creates a {@link MultiLineString} in the reverse
   * order to this object.
   * Both the order of the component LineStrings
   * and the order of their coordinate sequences
   * are reversed.
   *
   * @return a {@link MultiLineString} in the reverse order
   */
  @Override
  default MultiLineString reverse() {
    final LinkedList<LineString> revLines = new LinkedList<LineString>();
    for (final Geometry geometry : geometries()) {
      final LineString line = (LineString)geometry;
      final LineString reverse = line.reverse();
      revLines.addFirst(reverse);
    }
    final GeometryFactory geometryFactory = getGeometryFactory();
    return geometryFactory.multiLineString(revLines);
  }

  @Override
  default Iterable<Segment> segments() {
    return new MultiLineStringSegment(this, 0, -1);
  }

  @Override
  default Vertex vertices() {
    return new MultiLineStringVertex(this, 0, -1);
  }

}
