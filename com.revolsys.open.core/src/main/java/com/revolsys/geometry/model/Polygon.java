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
import java.util.function.Function;

import javax.measure.quantity.Area;
import javax.measure.quantity.Length;
import javax.measure.unit.Unit;

import com.revolsys.datatype.DataType;
import com.revolsys.datatype.DataTypes;
import com.revolsys.geometry.algorithm.RayCrossingCounter;
import com.revolsys.geometry.model.impl.BoundingBoxDoubleGf;
import com.revolsys.geometry.model.prep.PreparedPolygon;
import com.revolsys.geometry.model.segment.LineSegment;
import com.revolsys.geometry.model.segment.LineSegmentDouble;
import com.revolsys.geometry.model.segment.PolygonSegment;
import com.revolsys.geometry.model.segment.Segment;
import com.revolsys.geometry.model.vertex.PolygonVertex;
import com.revolsys.geometry.model.vertex.Vertex;
import com.revolsys.geometry.operation.polygonize.Polygonizer;
import com.revolsys.geometry.operation.valid.GeometryValidationError;
import com.revolsys.util.Property;

/**
 * Represents a polygon with linear edges, which may include holes.
 * The outer boundary (shell)
 * and inner boundaries (holes) of the polygon are represented by {@link LinearRing}s.
 * The boundary rings of the polygon may have any orientation.
 * Polygons are closed, simple geometries by definition.
 * <p>
 * The polygon model conforms to the assertions specified in the
 * <A HREF="http://www.opengis.org/techno/specs.htm">OpenGIS Simple Features
 * Specification for SQL</A>.
 * <p>
 * A <code>Polygon</code> is topologically valid if and only if:
 * <ul>
 * <li>the coordinates which define it are valid coordinates
 * <li>the linear rings for the shell and holes are valid
 * (i.e. are closed and do not self-intersect)
 * <li>holes touch the shell or another hole at at most one point
 * (which implies that the rings of the shell and holes must not cross)
 * <li>the interior of the polygon is connected,
 * or equivalently no sequence of touching holes
 * makes the interior of the polygon disconnected
 * (i.e. effectively split the polygon into two pieces).
 * </ul>
 *
 *@version 1.7
 */
public interface Polygon extends Polygonal {
  @SuppressWarnings("unchecked")
  static <G extends Geometry> G newPolygon(final Object value) {
    if (value == null) {
      return null;
    } else if (value instanceof Polygon) {
      return (G)value;
    } else if (value instanceof GeometryCollection) {
      final GeometryCollection geometryCollection = (GeometryCollection)value;
      if (geometryCollection.getGeometryCount() == 1) {
        final Geometry geometry = geometryCollection.getGeometry(0);
        if (geometry instanceof Polygon) {
          return (G)geometry;
        }
      }
      throw new IllegalArgumentException(
        geometryCollection.getGeometryType() + " cannot be converted to a Polygon");
    } else if (value instanceof Geometry) {
      throw new IllegalArgumentException(
        ((Geometry)value).getGeometryType() + " cannot be converted to a Polygon");
    } else {
      final String string = DataTypes.toString(value);
      return (G)GeometryFactory.DEFAULT.geometry(string, false);
    }
  }

  @Override
  default boolean addIsSimpleErrors(final List<GeometryValidationError> errors,
    final boolean shortCircuit) {
    for (final LinearRing ring : rings()) {
      if (!ring.addIsSimpleErrors(errors, shortCircuit) && shortCircuit) {
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
    } else if (geometryId.length == 1) {
      if (isEmpty()) {
        throw new IllegalArgumentException("Cannot move vertex for empty Polygon");
      } else {
        final int ringIndex = geometryId[0];
        final int ringCount = getRingCount();
        if (ringIndex >= 0 && ringIndex < ringCount) {
          final GeometryFactory geometryFactory = getGeometryFactory();

          final LinearRing ring = getRing(ringIndex);
          final LinearRing newRing = ring.appendVertex(newPoint);
          final List<LinearRing> rings = new ArrayList<>(getRings());
          rings.set(ringIndex, newRing);
          return (V)geometryFactory.polygon(rings);
        } else {
          throw new IllegalArgumentException(
            "Ring index must be between 0 and " + ringCount + " not " + ringIndex);
        }
      }
    } else {
      throw new IllegalArgumentException(
        "Geometry id's for Polygons must have length 1. " + Arrays.toString(geometryId));
    }
  }

  @Override
  default Polygon applyPolygonal(final Function<Polygon, Polygon> function) {
    if (!isEmpty()) {
      final Polygon newPolygon = function.apply(this);
      return newPolygon;
    }
    return this;
  }

  @Override
  Polygon clone();

  @Override
  default int compareToSameClass(final Geometry geometry) {
    final LinearRing thisShell = getShell();
    final Polygon ploygon2 = (Polygon)geometry;
    final LinearRing otherShell = ploygon2.getShell();
    return thisShell.compareToSameClass(otherShell);
  }

  @Override
  default Geometry convexHull() {
    return getShell().convexHull();
  }

  @Override
  @SuppressWarnings("unchecked")
  default <V extends Geometry> V copy(final GeometryFactory geometryFactory) {
    final List<LinearRing> rings = new ArrayList<>();
    for (final LinearRing ring : rings()) {
      final LinearRing newRing = ring.copy(geometryFactory);
      rings.add(newRing);
    }
    return (V)geometryFactory.polygon(rings);
  }

  @Override
  @SuppressWarnings("unchecked")
  default <V extends Geometry> V deleteVertex(final int... vertexId) {
    if (vertexId.length == 2) {
      if (isEmpty()) {
        throw new IllegalArgumentException("Cannot move vertex for empty Polygon");
      } else {
        final int ringIndex = vertexId[0];
        final int vertexIndex = vertexId[1];
        final int ringCount = getRingCount();
        if (ringIndex >= 0 && ringIndex < ringCount) {
          final GeometryFactory geometryFactory = getGeometryFactory();

          final LinearRing ring = getRing(ringIndex);
          final LinearRing newRing = ring.deleteVertex(vertexIndex);
          final List<LinearRing> rings = new ArrayList<>(getRings());
          rings.set(ringIndex, newRing);
          return (V)geometryFactory.polygon(rings);
        } else {
          throw new IllegalArgumentException(
            "Ring index must be between 0 and " + ringCount + " not " + ringIndex);
        }
      }
    } else {
      throw new IllegalArgumentException(
        "Vertex id's for Polygons must have length 2. " + Arrays.toString(vertexId));
    }
  }

  @Override
  default double distance(final Geometry geometry, final double terminateDistance) {
    if (geometry instanceof Point) {
      final Point point = (Point)geometry;
      return distance(point, terminateDistance);
    } else if (geometry instanceof LineString) {
      return Polygonal.super.distance(geometry, terminateDistance);
    } else if (geometry instanceof Polygon) {
      return Polygonal.super.distance(geometry, terminateDistance);
    } else if (Property.isEmpty(geometry)) {
      return 0.0;
    } else {
      return geometry.distance(this, terminateDistance);
    }
  }

  default double distance(Point point, final double terminateDistance) {
    if (isEmpty()) {
      return 0.0;
    } else if (Property.isEmpty(point)) {
      return 0.0;
    } else {
      final GeometryFactory geometryFactory = getGeometryFactory();
      point = point.convertGeometry(geometryFactory, 2);
      if (intersects(point)) {
        return 0.0;
      } else {
        double minDistance = Double.MAX_VALUE;
        for (final LinearRing ring : rings()) {
          final double distance = ring.distance(point, terminateDistance);
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
        final Polygon polygon = (Polygon)geometry;
        final int ringCount = getRingCount();
        final int ringCount2 = polygon.getRingCount();
        if (ringCount == ringCount2) {
          for (int i = 0; i < ringCount; i++) {
            final LinearRing ring1 = getRing(i);
            final LinearRing ring2 = polygon.getRing(i);
            if (!ring1.equals(axisCount, ring2)) {
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
    } else {
      final Polygon otherPolygon = (Polygon)other;
      final int ringCount = getRingCount();
      if (ringCount != otherPolygon.getRingCount()) {
        return false;
      } else {
        for (int i = 0; i < ringCount; i++) {
          final LinearRing ring = getRing(i);
          final LinearRing otherRing = otherPolygon.getRing(i);
          if (!ring.equalsExact(otherRing, tolerance)) {
            return false;
          }
        }
        return true;
      }
    }
  }

  /**
   *  Returns the area of this <code>Polygon</code>
   *
   *@return the area of the polygon
   */
  @Override
  default double getArea() {
    double totalArea = 0;
    final LinearRing shell = getShell();
    if (shell != null) {
      totalArea += shell.getPolygonArea();
    }
    for (final LinearRing hole : holes()) {
      final double area = hole.getPolygonArea();
      totalArea -= area;
    }
    return totalArea;
  }

  /**
   *  Returns the area of this <code>Polygon</code>
   *
   *@return the area of the polygon
   */
  @Override
  default double getArea(final Unit<Area> unit) {
    final LinearRing shell = getShell();
    double totalArea = shell.getPolygonArea(unit);
    for (final LinearRing hole : holes()) {
      final double area = hole.getPolygonArea(unit);
      totalArea -= area;
    }
    return totalArea;
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
    } else {
      if (getRingCount() == 1) {
        return geometryFactory.linearRing(getShell());
      } else {
        return geometryFactory.multiLineString(getRings());
      }
    }
  }

  @Override
  default int getBoundaryDimension() {
    return 1;
  }

  @Override
  default DataType getDataType() {
    return DataTypes.POLYGON;
  }

  @Override
  default int getDimension() {
    return 2;
  }

  @Override
  @SuppressWarnings("unchecked")
  default <V extends Geometry> List<V> getGeometryComponents(final Class<V> geometryClass) {
    final List<V> geometries = new ArrayList<V>();
    if (geometryClass.isAssignableFrom(getClass())) {
      geometries.add((V)this);
    }
    for (final LinearRing ring : rings()) {
      if (ring != null) {
        final List<V> ringGeometries = ring.getGeometries(geometryClass);
        geometries.addAll(ringGeometries);
      }
    }
    return geometries;
  }

  default LinearRing getHole(final int ringIndex) {
    return getRing(ringIndex + 1);
  }

  default int getHoleCount() {
    if (isEmpty()) {
      return 0;
    } else {
      return getRingCount() - 1;
    }
  }

  /**
   *  Returns the perimeter of this <code>Polygon</code>
   *
   *@return the perimeter of the polygon
   */
  @Override
  default double getLength() {
    double totalLength = 0.0;
    for (final LinearRing ring : rings()) {
      final double length = ring.getLength();
      totalLength += length;
    }
    return totalLength;
  }

  @Override
  default double getLength(final Unit<Length> unit) {
    double totalLength = 0;
    for (final LinearRing ring : rings()) {
      final double length = ring.getLength(unit);
      totalLength += length;
    }
    return totalLength;
  }

  @Override
  default Point getPoint() {
    if (isEmpty()) {
      return null;
    } else {
      return getShell().getPoint();
    }
  }

  @Override
  default Point getPointWithin() {
    if (isEmpty()) {
      final GeometryFactory geometryFactory = getGeometryFactory();
      return geometryFactory.point();
    } else {
      if (isValid()) {
        final Point centroid = getCentroid();
        boolean within = false;
        try {
          within = centroid.within(this);
        } catch (final TopologyException e) {
        }
        if (within) {
          return centroid;
        } else {
          final BoundingBox boundingBox = getBoundingBox();
          final double x1 = centroid.getX();
          final double y1 = centroid.getY();
          for (final double x2 : new double[] {
            boundingBox.getMinX(), boundingBox.getMaxX()
          }) {
            for (final double y2 : new double[] {
              boundingBox.getMinY(), boundingBox.getMaxY()
            }) {
              final LineSegment line = new LineSegmentDouble(2, x1, y1, x2, y2);
              try {
                final Geometry intersection = intersection(line);
                if (!intersection.isEmpty()) {
                  return intersection.getPointWithin();
                }
              } catch (final TopologyException e) {

              }
            }
          }
          return getPoint();
        }
      } else {
        final Geometry validGeometry = newValidGeometry();
        if (validGeometry == this) {
          return getPoint();
        } else {
          return validGeometry.getPointWithin();
        }
      }
    }
  }

  @Override
  default Polygon getPolygon(final int partIndex) {
    if (partIndex == 1) {
      return this;
    } else {
      return null;
    }
  }

  LinearRing getRing(int ringIndex);

  int getRingCount();

  List<LinearRing> getRings();

  @Override
  default Segment getSegment(final int... segmentId) {
    if (segmentId == null || segmentId.length != 2) {
      return null;
    } else {
      final int ringIndex = segmentId[0];
      if (ringIndex >= 0 && ringIndex < getRingCount()) {
        final LinearRing ring = getRing(ringIndex);
        final int vertexIndex = segmentId[1];
        if (vertexIndex >= 0 && vertexIndex < ring.getSegmentCount()) {
          return new PolygonSegment(this, segmentId);
        }
      }
      return null;
    }
  }

  @Override
  default int getSegmentCount() {
    int segmentCount = 0;
    for (final LinearRing ring : rings()) {
      segmentCount += ring.getSegmentCount();
    }
    return segmentCount;
  }

  default LinearRing getShell() {
    if (isEmpty()) {
      return null;
    } else {
      return getRing(0);
    }
  }

  @Override
  default Vertex getToVertex(int... vertexId) {
    if (vertexId == null || vertexId.length != 2) {
      return null;
    } else {
      final int ringIndex = vertexId[0];
      if (ringIndex >= 0 && ringIndex < getRingCount()) {
        final LinearRing ring = getRing(ringIndex);
        int vertexIndex = vertexId[1];
        final int vertexCount = ring.getVertexCount();
        vertexIndex = vertexCount - 2 - vertexIndex;
        vertexId = Geometry.setVertexIndex(vertexId, vertexIndex);
        if (vertexIndex >= 0 && vertexIndex < vertexCount) {
          return new PolygonVertex(this, vertexId);
        }
      }
      return null;
    }
  }

  @Override
  default Vertex getVertex(final int... vertexId) {
    if (vertexId == null || vertexId.length > 2) {
      return null;
    } else if (vertexId.length == 1) {
      int vertexIndex = vertexId[0];
      while (vertexIndex < 0) {
        vertexIndex += getVertexCount() - 1;
      }
      int totalVertexCount = 0;
      final int ringIndex = 0;
      for (final LinearRing ring : rings()) {
        final int ringVertexCount = ring.getVertexCount();
        final int ringVertexIndex = vertexIndex - totalVertexCount;
        if (ringVertexIndex <= ringVertexCount) {
          return new PolygonVertex(this, ringIndex, ringVertexIndex);
        } else {
          totalVertexCount += ringVertexCount;
        }
      }
      return null;
    } else {
      final int ringIndex = vertexId[0];
      if (ringIndex >= 0 && ringIndex < getRingCount()) {
        final LinearRing ring = getRing(ringIndex);
        int vertexIndex = vertexId[1];
        final int vertexCount = ring.getVertexCount();
        if (vertexIndex <= vertexCount) {
          while (vertexIndex < 0) {
            vertexIndex += vertexCount - 1;
          }
          return new PolygonVertex(this, vertexId);
        }
      }
      return null;
    }
  }

  @Override
  default int getVertexCount() {
    int vertexCount = 0;
    for (final LinearRing ring : rings()) {
      vertexCount += ring.getVertexCount();
    }
    return vertexCount;
  }

  default Iterable<LinearRing> holes() {
    if (getHoleCount() == 0) {
      return Collections.emptyList();
    } else {
      final List<LinearRing> holes = new ArrayList<>();
      for (int i = 0; i < getHoleCount(); i++) {
        final LinearRing ring = getHole(i);
        holes.add(ring);
      }
      return holes;
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  default <V extends Geometry> V insertVertex(final Point newPoint, final int... vertexId) {
    if (newPoint == null || newPoint.isEmpty()) {
      return (V)this;
    } else if (vertexId.length == 2) {
      if (isEmpty()) {
        throw new IllegalArgumentException("Cannot move vertex for empty Polygon");
      } else {
        final int ringIndex = vertexId[0];
        final int vertexIndex = vertexId[1];
        final int ringCount = getRingCount();
        if (ringIndex >= 0 && ringIndex < ringCount) {
          final GeometryFactory geometryFactory = getGeometryFactory();

          final LinearRing ring = getRing(ringIndex);
          final LinearRing newRing = ring.insertVertex(newPoint, vertexIndex);
          final List<LinearRing> rings = new ArrayList<>(getRings());
          rings.set(ringIndex, newRing);
          return (V)geometryFactory.polygon(rings);
        } else {
          throw new IllegalArgumentException(
            "Ring index must be between 0 and " + ringCount + " not " + ringIndex);
        }
      }
    } else {
      throw new IllegalArgumentException(
        "Vertex id's for Polygons must have length 2. " + Arrays.toString(vertexId));
    }
  }

  @Override
  default boolean intersects(final BoundingBox boundingBox) {
    if (isEmpty() || boundingBox.isEmpty()) {
      return false;
    } else {
      final BoundingBox thisBoundingBox = getBoundingBox();
      if (thisBoundingBox.intersects(boundingBox)) {
        return intersects(boundingBox.toPolygon(getGeometryFactory(), 10));
      }
      // for (final LinearRing ring : rings()) {
      // if (ring.intersects(boundingBox)) {
      // return true;
      // }
      // }
      return false;
    }
  }

  @Override
  default boolean isEmpty() {
    return getRingCount() == 0;
  }

  @Override
  default boolean isEquivalentClass(final Geometry other) {
    return other instanceof Polygon;
  }

  @Override
  default boolean isRectangle() {
    if (isEmpty()) {
      return false;
    } else if (getHoleCount() != 0) {
      return false;
    } else {
      final LinearRing shell = getShell();
      if (shell.getVertexCount() != 5) {
        return false;
      } else {
        // check vertices have correct values
        final BoundingBox boundingBox = getBoundingBox();
        for (int i = 0; i < 5; i++) {
          final double x = shell.getX(i);
          if (!(x == boundingBox.getMinX() || x == boundingBox.getMaxX())) {
            return false;
          }
          final double y = shell.getY(i);
          if (!(y == boundingBox.getMinY() || y == boundingBox.getMaxY())) {
            return false;
          }
        }

        // check vertices are in right order
        double prevX = shell.getX(0);
        double prevY = shell.getY(0);
        for (int i = 1; i <= 4; i++) {
          final double x = shell.getX(i);
          final double y = shell.getY(i);
          final boolean xChanged = x != prevX;
          final boolean yChanged = y != prevY;
          if (xChanged == yChanged) {
            return false;
          }
          prevX = x;
          prevY = y;
        }
        return true;
      }
    }
  }

  @Override
  default Location locate(Point point) {
    if (isEmpty() || point.isEmpty()) {
      return Location.EXTERIOR;
    } else {
      final GeometryFactory geometryFactory = getGeometryFactory();
      point = point.convertGeometry(geometryFactory);
      final LinearRing shell = getShell();
      final Point point1 = point;
      final Location shellLocation = RayCrossingCounter.locatePointInRing(point1, shell);
      if (shellLocation == Location.EXTERIOR) {
        return Location.EXTERIOR;
      } else if (shellLocation == Location.BOUNDARY) {
        return Location.BOUNDARY;
      } else {
        for (final LinearRing hole : holes()) {
          final Point point2 = point;
          final Location holeLocation = RayCrossingCounter.locatePointInRing(point2, hole);
          if (holeLocation == Location.INTERIOR) {
            return Location.EXTERIOR;
          } else if (holeLocation == Location.BOUNDARY) {
            return Location.BOUNDARY;
          }
        }
      }
      return Location.INTERIOR;
    }
  }

  @Override
  default Polygon move(final double... deltas) {
    if (deltas == null || isEmpty()) {
      return this;
    } else {
      final List<LinearRing> rings = new ArrayList<>();
      for (final LinearRing part : rings()) {
        final LinearRing movedPart = part.move(deltas);
        rings.add(movedPart);
      }
      final GeometryFactory geometryFactory = getGeometryFactory();
      return geometryFactory.polygon(rings);
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  default <V extends Geometry> V moveVertex(final Point newPoint, final int... vertexId) {
    if (newPoint == null || newPoint.isEmpty()) {
      return (V)this;
    } else if (vertexId.length == 2) {
      if (isEmpty()) {
        throw new IllegalArgumentException("Cannot move vertex for empty Polygon");
      } else {
        final int ringIndex = vertexId[0];
        final int vertexIndex = vertexId[1];
        final int ringCount = getRingCount();
        if (ringIndex >= 0 && ringIndex < ringCount) {
          final GeometryFactory geometryFactory = getGeometryFactory();

          final LinearRing ring = getRing(ringIndex);
          final LinearRing newRing = ring.moveVertex(newPoint, vertexIndex);
          final List<LinearRing> rings = new ArrayList<>(getRings());
          rings.set(ringIndex, newRing);
          return (V)geometryFactory.polygon(rings);
        } else {
          throw new IllegalArgumentException(
            "Ring index must be between 0 and " + ringCount + " not " + ringIndex);
        }
      }
    } else {
      throw new IllegalArgumentException(
        "Vertex id's for Polygons must have length 2. " + Arrays.toString(vertexId));
    }
  }

  @Override
  default BoundingBox newBoundingBox() {
    final GeometryFactory geometryFactory = getGeometryFactory();
    BoundingBox boundingBox = new BoundingBoxDoubleGf(geometryFactory);
    for (final LinearRing ring : rings()) {
      boundingBox = boundingBox.expandToInclude(ring);
    }
    return boundingBox;
  }

  default Polygon newPolygonWithoutHoles() {
    if (isEmpty()) {
      return this;
    } else if (getRingCount() == 1) {
      return this;
    } else {
      final GeometryFactory geometryFactory = getGeometryFactory();
      return geometryFactory.polygon(getShell());
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  default <G> G newUsingGeometryFactory(final GeometryFactory factory) {
    if (factory == getGeometryFactory()) {
      return (G)this;
    } else if (isEmpty()) {
      return (G)factory.polygon();
    } else {
      final int ringCount = getRingCount();
      final LinearRing[] rings = new LinearRing[ringCount];
      for (int i = 0; i < ringCount; i++) {
        LinearRing ring = getRing(i);
        ring = ring.newUsingGeometryFactory(factory);
        rings[i] = ring;
      }
      return (G)factory.polygon(rings);
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
      final Polygonizer polygonizer = new Polygonizer();
      polygonizer.addPolygon(this);
      final Polygonal polygonal = polygonizer.getPolygonal();
      if (polygonal.isEmpty()) {
        return (G)this;
      } else {
        return (G)polygonal;
      }
    }
  }

  @Override
  default Polygon normalize() {
    if (isEmpty()) {
      return this;
    } else {
      final LinearRing exteriorRing = getShell().normalize(true);
      final List<LinearRing> rings = new ArrayList<>();
      for (final LinearRing hole : holes()) {
        final LinearRing normalizedHole = hole.normalize(false);
        rings.add(normalizedHole);
      }
      Collections.sort(rings);
      rings.add(0, exteriorRing);
      final GeometryFactory geometryFactory = getGeometryFactory();
      return geometryFactory.polygon(rings);
    }
  }

  @Override
  default Iterable<Polygon> polygons() {
    return Collections.singleton(this);
  }

  @Override
  @Deprecated
  default Polygon prepare() {
    return new PreparedPolygon(this);
  }

  @Override
  default Polygon removeDuplicatePoints() {
    if (isEmpty()) {
      return this;
    } else {
      final List<LinearRing> rings = new ArrayList<>();
      for (final LinearRing ring : rings()) {
        final LinearRing newRing = ring.removeDuplicatePoints();
        rings.add(newRing);
      }
      final GeometryFactory geometryFactory = getGeometryFactory();
      return geometryFactory.polygon(rings);
    }
  }

  @Override
  default Polygon reverse() {
    final List<LinearRing> rings = new ArrayList<>();
    for (final LinearRing ring : rings()) {
      rings.add(ring.reverse());
    }
    final GeometryFactory geometryFactory = getGeometryFactory();
    return geometryFactory.polygon(rings);
  }

  default Iterable<LinearRing> rings() {
    if (isEmpty()) {
      return Collections.emptyList();
    } else {
      return getRings();
    }
  }

  @Override
  default Iterable<Segment> segments() {
    return new PolygonSegment(this, 0, -1);
  }

  @Override
  @SuppressWarnings("unchecked")
  default <G extends Geometry> G toClockwise() {
    if (!isEmpty()) {
      boolean changed = false;

      final List<Geometry> rings = new ArrayList<>();
      boolean exterior = true;
      for (LinearRing ring : rings()) {
        if (ring.isClockwise() != exterior) {
          ring = ring.reverse();
          changed = true;
        }
        exterior = false;
        rings.add(ring);
      }
      if (changed) {
        final GeometryFactory geometryFactory = getGeometryFactory();
        return (G)geometryFactory.polygon(rings);
      }
    }
    return (G)this;
  }

  @Override
  @SuppressWarnings("unchecked")
  default <G extends Geometry> G toCounterClockwise() {
    if (!isEmpty()) {
      boolean changed = false;
      final List<Geometry> rings = new ArrayList<>();
      boolean exterior = true;
      for (LinearRing ring : rings()) {
        if (ring.isClockwise() == exterior) {
          ring = ring.reverse();
          changed = true;
        }
        exterior = false;
        rings.add(ring);
      }
      if (changed) {
        final GeometryFactory geometryFactory = getGeometryFactory();
        return (G)geometryFactory.polygon(rings);
      }
    }
    return (G)this;
  }

  @Override
  default PolygonVertex vertices() {
    return new PolygonVertex(this, 0, -1);
  }
}
