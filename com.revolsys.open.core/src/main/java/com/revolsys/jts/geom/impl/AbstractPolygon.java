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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.revolsys.data.io.IteratorReader;
import com.revolsys.data.types.DataType;
import com.revolsys.data.types.DataTypes;
import com.revolsys.gis.model.coordinates.list.CoordinatesListUtil;
import com.revolsys.io.Reader;
import com.revolsys.jts.geom.BoundingBox;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.jts.geom.LineString;
import com.revolsys.jts.geom.LinearRing;
import com.revolsys.jts.geom.Point;
import com.revolsys.jts.geom.Polygon;
import com.revolsys.jts.geom.prep.PreparedPolygon;
import com.revolsys.jts.geom.segment.PolygonSegment;
import com.revolsys.jts.geom.segment.Segment;
import com.revolsys.jts.geom.vertex.PolygonVertex;
import com.revolsys.jts.geom.vertex.Vertex;

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
public abstract class AbstractPolygon extends AbstractGeometry implements
Polygon {

  /**
   *  Returns the minimum coordinate, using the usual lexicographic comparison.
   *
   *@param  coordinates  the array to search
   *@return              the minimum coordinate in the array, found using <code>compareTo</code>
   *@see Point#compareTo(Object)
   */
  public static int minCoordinateIndex(final LinearRing ring) {
    Point minCoord = null;
    int minIndex = 0;
    for (final Vertex vertex : ring.vertices()) {
      if (minCoord == null || minCoord.compareTo(vertex) > 0) {
        minCoord = vertex.clonePoint();
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
  public static LinearRing scroll(final LinearRing ring, final int index) {
    final LineString points = ring;
    final int vertexCount = ring.getVertexCount();
    final int axisCount = ring.getAxisCount();
    final double[] coordinates = new double[vertexCount * axisCount];
    int newVertexIndex = 0;
    for (int vertexIndex = index; vertexIndex < vertexCount - 1; vertexIndex++) {
      CoordinatesListUtil.setCoordinates(coordinates, axisCount,
        newVertexIndex++, points, vertexIndex);
    }
    for (int vertexIndex = 0; vertexIndex < index; vertexIndex++) {
      CoordinatesListUtil.setCoordinates(coordinates, axisCount,
        newVertexIndex++, points, vertexIndex);
    }
    CoordinatesListUtil.setCoordinates(coordinates, axisCount, vertexCount - 1,
      points, index);
    final GeometryFactory geometryFactory = ring.getGeometryFactory();
    return geometryFactory.linearRing(axisCount, coordinates);
  }

  private static final long serialVersionUID = -3494792200821764533L;

  public AbstractPolygon() {
  }

  @SuppressWarnings("unchecked")
  @Override
  public <V extends Geometry> V appendVertex(final Point newPoint,
    final int... geometryId) {
    if (newPoint == null || newPoint.isEmpty()) {
      return (V)this;
    } else if (geometryId.length == 1) {
      if (isEmpty()) {
        throw new IllegalArgumentException(
            "Cannot move vertex for empty Polygon");
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
            "Ring index must be between 0 and " + ringCount + " not "
                + ringIndex);
        }
      }
    } else {
      throw new IllegalArgumentException(
        "Geometry id's for Polygons must have length 1. "
            + Arrays.toString(geometryId));
    }
  }

  /**
   * Creates and returns a full copy of this {@link Polygon} object.
   * (including all coordinates contained by it).
   *
   * @return a clone of this instance
   */
  @Override
  public AbstractPolygon clone() {
    final AbstractPolygon poly = (AbstractPolygon)super.clone();
    return poly;
  }

  @Override
  public int compareToSameClass(final Geometry geometry) {
    final LinearRing thisShell = getExteriorRing();
    final Polygon ploygon2 = (Polygon)geometry;
    final LinearRing otherShell = ploygon2.getExteriorRing();
    return thisShell.compareToSameClass(otherShell);
  }

  @Override
  protected BoundingBox computeBoundingBox() {
    final GeometryFactory geometryFactory = getGeometryFactory();
    BoundingBox boundingBox = new BoundingBoxDoubleGf(geometryFactory);
    for (final LinearRing ring : rings()) {
      boundingBox = boundingBox.expandToInclude(ring);
    }
    return boundingBox;
  }

  @Override
  public Geometry convexHull() {
    return getExteriorRing().convexHull();
  }

  @SuppressWarnings("unchecked")
  @Override
  public <V extends Geometry> V copy(final GeometryFactory geometryFactory) {
    final List<LinearRing> rings = new ArrayList<>();
    for (final LinearRing ring : rings()) {
      final LinearRing newRing = ring.copy(geometryFactory);
      rings.add(newRing);
    }
    return (V)geometryFactory.polygon(rings);
  }

  @SuppressWarnings("unchecked")
  @Override
  public <V extends Geometry> V deleteVertex(final int... vertexId) {
    if (vertexId.length == 2) {
      if (isEmpty()) {
        throw new IllegalArgumentException(
            "Cannot move vertex for empty Polygon");
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
            "Ring index must be between 0 and " + ringCount + " not "
                + ringIndex);
        }
      }
    } else {
      throw new IllegalArgumentException(
        "Vertex id's for Polygons must have length 2. "
            + Arrays.toString(vertexId));
    }
  }

  @Override
  protected boolean doEquals(final int axisCount, final Geometry geometry) {
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
    return false;
  }

  @Override
  public boolean equalsExact(final Geometry other, final double tolerance) {
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
  public double getArea() {
    double totalArea = 0.0;
    for (int ringIndex = 0; ringIndex < getRingCount(); ringIndex++) {
      final LinearRing ring = getRing(ringIndex);
      final int vertexCount = ring.getVertexCount();
      double area;
      if (vertexCount < 3) {
        area = 0.0;
      } else {
        /**
         * Based on the Shoelace formula.
         * http://en.wikipedia.org/wiki/Shoelace_formula
         */
        double p1x = ring.getX(0);
        double p1y = ring.getY(0);

        final double x0 = p1x;
        double p2x = ring.getX(1) - x0;
        double p2y = ring.getY(1);
        double sum = 0.0;
        for (int i = 1; i < vertexCount - 1; i++) {
          final double p0y = p1y;
          p1x = p2x;
          p1y = p2y;
          p2x = ring.getX(i + 1) - x0;
          p2y = ring.getY(i + 1);
          sum += p1x * (p0y - p2y);
        }
        area = Math.abs(sum / 2.0);
      }
      if (ringIndex == 0) {
        totalArea += area;
      } else {
        totalArea -= area;
      }
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
  public Geometry getBoundary() {
    final GeometryFactory geometryFactory = getGeometryFactory();
    if (isEmpty()) {
      return geometryFactory.multiLineString();
    } else {
      if (getRingCount() == 1) {
        return geometryFactory.linearRing(getExteriorRing());
      } else {
        return geometryFactory.multiLineString(getRings());
      }
    }
  }

  @Override
  public int getBoundaryDimension() {
    return 1;
  }

  @Override
  public DataType getDataType() {
    return DataTypes.POLYGON;
  }

  @Override
  public int getDimension() {
    return 2;
  }

  @Override
  public LinearRing getExteriorRing() {
    if (isEmpty()) {
      return null;
    } else {
      return getRing(0);
    }
  }

  @Override
  public <V extends Geometry> List<V> getGeometryComponents(
    final Class<V> geometryClass) {
    final List<V> geometries = super.getGeometryComponents(geometryClass);
    for (final LinearRing ring : rings()) {
      if (ring != null) {
        final List<V> ringGeometries = ring.getGeometries(geometryClass);
        geometries.addAll(ringGeometries);
      }
    }
    return geometries;
  }

  @Override
  public GeometryFactory getGeometryFactory() {
    return GeometryFactory.floating3();
  }

  @Override
  public LinearRing getInteriorRing(final int ringIndex) {
    return getRing(ringIndex + 1);
  }

  /**
   *  Returns the perimeter of this <code>Polygon</code>
   *
   *@return the perimeter of the polygon
   */
  @Override
  public double getLength() {
    double len = 0.0;
    for (final LinearRing ring : rings()) {
      len += ring.getLength();
    }
    return len;
  }

  @Override
  public int getNumInteriorRing() {
    if (isEmpty()) {
      return 0;
    } else {
      return getRingCount() - 1;
    }
  }

  @Override
  public Point getPoint() {
    if (isEmpty()) {
      return null;
    } else {
      return getExteriorRing().getPoint();
    }
  }

  @Override
  public Point getPointWithin() {
    if (isEmpty()) {
      return null;
    } else {
      final Point centroid = getCentroid();
      if (centroid.within(this)) {
        return centroid;
      } else {
        final BoundingBox boundingBox = getBoundingBox();
        for (int i = 0; i < 100; i++) {
          final Point point = boundingBox.getRandomPointWithin();
          if (point.within(this)) {
            return point;
          }
        }
        return getPoint();
      }
    }
  }

  @Override
  public Segment getSegment(final int... segmentId) {
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
  public Vertex getToVertex(int... vertexId) {
    if (vertexId == null || vertexId.length != 2) {
      return null;
    } else {
      final int ringIndex = vertexId[0];
      if (ringIndex >= 0 && ringIndex < getRingCount()) {
        final LinearRing ring = getRing(ringIndex);
        int vertexIndex = vertexId[1];
        final int vertexCount = ring.getVertexCount();
        vertexIndex = vertexCount - 2 - vertexIndex;
        vertexId = setVertexIndex(vertexId, vertexIndex);
        if (vertexIndex >= 0 && vertexIndex < vertexCount) {
          return new PolygonVertex(this, vertexId);
        }
      }
      return null;
    }
  }

  @Override
  public Vertex getVertex(final int... vertexId) {
    if (vertexId == null || vertexId.length != 2) {
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
  public int getVertexCount() {
    int numPoints = 0;
    for (final LinearRing ring : rings()) {
      numPoints += ring.getVertexCount();
    }
    return numPoints;
  }

  @Override
  public Iterable<LinearRing> holes() {
    if (getNumInteriorRing() == 0) {
      return Collections.emptyList();
    } else {
      final List<LinearRing> holes = new ArrayList<>();
      for (int i = 0; i < getNumInteriorRing(); i++) {
        final LinearRing ring = getInteriorRing(i);
        holes.add(ring);
      }
      return holes;
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public <V extends Geometry> V insertVertex(final Point newPoint,
    final int... vertexId) {
    if (newPoint == null || newPoint.isEmpty()) {
      return (V)this;
    } else if (vertexId.length == 2) {
      if (isEmpty()) {
        throw new IllegalArgumentException(
            "Cannot move vertex for empty Polygon");
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
            "Ring index must be between 0 and " + ringCount + " not "
                + ringIndex);
        }
      }
    } else {
      throw new IllegalArgumentException(
        "Vertex id's for Polygons must have length 2. "
            + Arrays.toString(vertexId));
    }
  }

  @Override
  public boolean intersects(final BoundingBox boundingBox) {
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
  public boolean isEmpty() {
    return getRingCount() == 0;
  }

  @Override
  protected boolean isEquivalentClass(final Geometry other) {
    return other instanceof Polygon;
  }

  @Override
  public boolean isRectangle() {
    if (isEmpty()) {
      return false;
    } else if (getNumInteriorRing() != 0) {
      return false;
    } else {
      final LinearRing shell = getExteriorRing();
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
  public Polygon move(final double... deltas) {
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

  @SuppressWarnings("unchecked")
  @Override
  public <V extends Geometry> V moveVertex(final Point newPoint,
    final int... vertexId) {
    if (newPoint == null || newPoint.isEmpty()) {
      return (V)this;
    } else if (vertexId.length == 2) {
      if (isEmpty()) {
        throw new IllegalArgumentException(
            "Cannot move vertex for empty Polygon");
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
            "Ring index must be between 0 and " + ringCount + " not "
                + ringIndex);
        }
      }
    } else {
      throw new IllegalArgumentException(
        "Vertex id's for Polygons must have length 2. "
            + Arrays.toString(vertexId));
    }
  }

  @Override
  public Polygon normalize() {
    if (isEmpty()) {
      return this;
    } else {
      final LinearRing exteriorRing = normalize(getExteriorRing(), true);
      final List<LinearRing> rings = new ArrayList<>();
      for (final LinearRing hole : holes()) {
        final LinearRing normalizedHole = normalize(hole, false);
        rings.add(normalizedHole);
      }
      Collections.sort(rings);
      rings.add(0, exteriorRing);
      final GeometryFactory geometryFactory = getGeometryFactory();
      return geometryFactory.polygon(rings);
    }
  }

  private LinearRing normalize(LinearRing ring, final boolean clockwise) {
    if (ring.isEmpty()) {
      return ring;
    } else {
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
  @Deprecated
  public Polygon prepare() {
    return new PreparedPolygon(this);
  }

  @Override
  public Polygon reverse() {
    final List<LinearRing> rings = new ArrayList<>();
    for (final LinearRing ring : rings()) {
      rings.add(ring.reverse());
    }
    final GeometryFactory geometryFactory = getGeometryFactory();
    return geometryFactory.polygon(rings);
  }

  @Override
  public Iterable<LinearRing> rings() {
    if (isEmpty()) {
      return Collections.emptyList();
    } else {
      return getRings();
    }
  }

  @Override
  public Reader<Segment> segments() {
    final PolygonSegment iterator = new PolygonSegment(this, 0, -1);
    return new IteratorReader<>(iterator);
  }

  @Override
  public <G extends Geometry> G toClockwise() {
    final List<Geometry> geometries = new ArrayList<>();
    for (final LinearRing ring : rings()) {
      geometries.add(ring.toClockwise());
    }
    final GeometryFactory geometryFactory = getGeometryFactory();
    return (G)geometryFactory.polygon(geometries);
  }

  @Override
  public <G extends Geometry> G toCounterClockwise() {
    final List<Geometry> geometries = new ArrayList<>();
    for (final LinearRing ring : rings()) {
      geometries.add(ring.toCounterClockwise());
    }
    final GeometryFactory geometryFactory = getGeometryFactory();
    return (G)geometryFactory.polygon(geometries);
  }

  @Override
  public Reader<Vertex> vertices() {
    final PolygonVertex vertex = new PolygonVertex(this, 0, -1);
    return vertex.reader();
  }
}
