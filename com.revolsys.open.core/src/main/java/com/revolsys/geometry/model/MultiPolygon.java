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

import com.revolsys.datatype.DataType;
import com.revolsys.datatype.DataTypes;
import com.revolsys.geometry.model.segment.MultiPolygonSegment;
import com.revolsys.geometry.model.segment.Segment;
import com.revolsys.geometry.model.vertex.MultiPolygonVertex;
import com.revolsys.geometry.model.vertex.Vertex;
import com.revolsys.io.IteratorReader;
import com.revolsys.io.Reader;

/**
 * Models a collection of {@link Polygon}s.
 * <p>
 * As per the OGC SFS specification,
 * the Polygons in a MultiPolygon may not overlap,
 * and may only touch at single points.
 * This allows the topological point-set semantics
 * to be well-defined.
 *
 *
 *@version 1.7
 */
public interface MultiPolygon extends GeometryCollection, Polygonal {

  @Override
  MultiPolygon clone();

  @Override
  @SuppressWarnings("unchecked")

  default <V extends Geometry> V copy(final GeometryFactory geometryFactory) {
    final List<Polygon> polygons = new ArrayList<>();
    for (final Polygon polygon : getPolygons()) {
      final Polygon newPolygon = polygon.copy(geometryFactory);
      polygons.add(newPolygon);
    }
    return (V)geometryFactory.multiPolygon(polygons);
  }

  @Override
  default boolean equalsExact(final Geometry other, final double tolerance) {
    if (!isEquivalentClass(other)) {
      return false;
    }
    return GeometryCollection.super.equalsExact(other, tolerance);
  }

  /**
   * Computes the boundary of this geometry
   *
   * @return a lineal geometry (which may be empty)
   * @see Geometry#getBoundary
   */

  @Override
  default Geometry getBoundary() {
    final GeometryFactory geometryFactory = getGeometryFactory();
    if (isEmpty()) {
      return geometryFactory.multiLineString();
    }
    final List<LineString> allRings = new ArrayList<>();
    for (final Polygon polygon : getPolygons()) {
      final Geometry rings = polygon.getBoundary();
      for (int j = 0; j < rings.getGeometryCount(); j++) {
        final LineString ring = (LineString)rings.getGeometry(j);
        allRings.add(ring);
      }
    }
    return geometryFactory.multiLineString(allRings);
  }

  @Override
  default int getBoundaryDimension() {
    return 1;
  }

  @Override
  default DataType getDataType() {
    return DataTypes.MULTI_POLYGON;
  }

  @Override
  default int getDimension() {
    return 2;
  }

  default Polygon getPolygon(final int partIndex) {
    return (Polygon)getGeometry(partIndex);
  }

  @SuppressWarnings({
    "unchecked", "rawtypes"
  })
  default <V extends Polygon> List<V> getPolygons() {
    return (List)getGeometries();
  }

  @Override
  default Segment getSegment(final int... segmentId) {
    if (segmentId == null || segmentId.length != 3) {
      return null;
    } else {
      final int partIndex = segmentId[0];
      if (partIndex >= 0 && partIndex < getGeometryCount()) {
        final Polygon polygon = getPolygon(partIndex);
        final int ringIndex = segmentId[1];
        if (ringIndex >= 0 && ringIndex < polygon.getRingCount()) {
          final LinearRing ring = polygon.getRing(ringIndex);
          final int segmentIndex = segmentId[2];
          if (segmentIndex >= 0 && segmentIndex < ring.getSegmentCount()) {
            return new MultiPolygonSegment(this, segmentId);
          }
        }
      }
      return null;
    }
  }

  @Override
  default Vertex getToVertex(int... vertexId) {
    if (vertexId == null || vertexId.length != 3) {
      return null;
    } else {
      final int partIndex = vertexId[0];
      if (partIndex >= 0 && partIndex < getGeometryCount()) {
        final Polygon polygon = getPolygon(partIndex);
        final int ringIndex = vertexId[1];
        if (ringIndex >= 0 && ringIndex < polygon.getRingCount()) {
          final LinearRing ring = polygon.getRing(ringIndex);
          int vertexIndex = vertexId[2];
          final int vertexCount = ring.getVertexCount();
          vertexIndex = vertexCount - 2 - vertexIndex;
          if (vertexIndex <= vertexCount) {
            while (vertexIndex < 0) {
              vertexIndex += vertexCount - 1;
            }
            vertexId = Geometry.setVertexIndex(vertexId, vertexIndex);
            return new MultiPolygonVertex(this, vertexId);
          }
        }
      }
      return null;
    }
  }

  @Override
  default Vertex getVertex(final int... vertexId) {
    if (vertexId == null || vertexId.length != 3) {
      return null;
    } else {
      final int partIndex = vertexId[0];
      if (partIndex >= 0 && partIndex < getGeometryCount()) {
        final Polygon polygon = getPolygon(partIndex);
        final int ringIndex = vertexId[1];
        if (ringIndex >= 0 && ringIndex < polygon.getRingCount()) {
          final LinearRing ring = polygon.getRing(ringIndex);
          int vertexIndex = vertexId[2];
          final int vertexCount = ring.getVertexCount();
          if (vertexIndex <= vertexCount) {
            while (vertexIndex < 0) {
              vertexIndex += vertexCount - 1;
            }
            return new MultiPolygonVertex(this, vertexId);
          }
        }
      }
      return null;
    }
  }

  @Override
  default boolean isEquivalentClass(final Geometry other) {
    return other instanceof MultiPolygon;
  }

  @Override
  @SuppressWarnings("unchecked")

  default <V extends Geometry> V moveVertex(final Point newPoint, final int... vertexId) {
    if (newPoint == null || newPoint.isEmpty()) {
      return (V)this;
    } else if (vertexId.length == 3) {
      if (isEmpty()) {
        throw new IllegalArgumentException("Cannot move vertex for empty MultiPolygon");
      } else {
        final int partIndex = vertexId[0];
        final int ringIndex = vertexId[1];
        final int vertexIndex = vertexId[2];
        final int partCount = getGeometryCount();
        if (partIndex >= 0 && partIndex < partCount) {
          final GeometryFactory geometryFactory = getGeometryFactory();

          final Polygon polygon = getPolygon(partIndex);
          final Polygon newPolygon = polygon.moveVertex(newPoint, ringIndex, vertexIndex);
          final List<Polygon> polygons = new ArrayList<>(getPolygons());
          polygons.set(partIndex, newPolygon);
          return (V)geometryFactory.multiPolygon(polygons);
        } else {
          throw new IllegalArgumentException(
            "Part index must be between 0 and " + partCount + " not " + partIndex);
        }
      }
    } else {
      throw new IllegalArgumentException(
        "Vertex id's for MultiPolygons must have length 3. " + Arrays.toString(vertexId));
    }
  }

  @Override
  default MultiPolygon normalize() {
    if (isEmpty()) {
      return this;
    } else {
      final List<Polygon> geometries = new ArrayList<>();
      for (final Geometry part : geometries()) {
        final Polygon normalizedPart = (Polygon)part.normalize();
        geometries.add(normalizedPart);
      }
      Collections.sort(geometries);
      final GeometryFactory geometryFactory = getGeometryFactory();
      final MultiPolygon normalizedGeometry = geometryFactory.multiPolygon(geometries);
      return normalizedGeometry;
    }
  }

  @Override
  default Iterable<Polygon> polygons() {
    return getGeometries();
  }

  /**
   * Creates a {@link MultiPolygon} with
   * every component reversed.
   * The order of the components in the collection are not reversed.
   *
   * @return a MultiPolygon in the reverse order
   */

  @Override
  default MultiPolygon reverse() {
    final List<Polygon> polygons = new ArrayList<>();
    for (final Geometry geometry : geometries()) {
      final Polygon polygon = (Polygon)geometry;
      final Polygon reverse = polygon.reverse();
      polygons.add(reverse);
    }
    final GeometryFactory geometryFactory = getGeometryFactory();
    return geometryFactory.multiPolygon(polygons);
  }

  @Override
  default Reader<Segment> segments() {
    final MultiPolygonSegment iterator = new MultiPolygonSegment(this, 0, 0, -1);
    return new IteratorReader<>(iterator);
  }

  @Override
  default Reader<Vertex> vertices() {
    final MultiPolygonVertex vertex = new MultiPolygonVertex(this, 0, 0, -1);
    return vertex.reader();
  }
}
