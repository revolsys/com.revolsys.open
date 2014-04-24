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

import com.revolsys.gis.data.model.types.DataType;
import com.revolsys.gis.data.model.types.DataTypes;
import com.revolsys.jts.algorithm.CGAlgorithms;
import com.revolsys.jts.geom.BoundingBox;
import com.revolsys.jts.geom.CoordinateArrays;
import com.revolsys.jts.geom.CoordinateFilter;
import com.revolsys.jts.geom.CoordinateSequenceComparator;
import com.revolsys.jts.geom.CoordinateSequenceFilter;
import com.revolsys.jts.geom.Coordinates;
import com.revolsys.jts.geom.Envelope;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.GeometryComponentFilter;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.jts.geom.LinearRing;
import com.revolsys.jts.geom.Polygon;
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
public class PolygonImpl extends AbstractGeometry implements Polygon {

  private static final long serialVersionUID = -3494792200821764533L;

  protected LinearRing[] rings;

  public PolygonImpl(final GeometryFactory factory) {
    super(factory);
  }

  /**
   *  Constructs a <code>Polygon</code> with the given exterior boundary and
   *  interior boundaries.
   *
   *@param  shell           the outer boundary of the new <code>Polygon</code>,
   *      or <code>null</code> or an empty <code>LinearRing</code> if the empty
   *      geometry is to be created.
   *@param  holes           the inner boundaries of the new <code>Polygon</code>
   *      , or <code>null</code> or empty <code>LinearRing</code>s if the empty
   *      geometry is to be created.
   */
  public PolygonImpl(final GeometryFactory factory, final LinearRing... rings) {
    super(factory);
    if (rings == null || rings.length == 0) {

    } else if (hasNullElements(rings)) {
      throw new IllegalArgumentException("rings must not contain null elements");
    } else {
      if (rings[0].isEmpty()) {
        for (int i = 1; i < rings.length; i++) {
          final LinearRing ring = rings[i];
          if (!ring.isEmpty()) {
            throw new IllegalArgumentException("shell is empty but hole "
              + (i - 1) + " is not");
          }
        }
      } else {
        this.rings = rings;
      }
    }
  }

  @Override
  public void apply(final CoordinateFilter filter) {
    for (final LinearRing ring : rings()) {
      ring.apply(filter);
    }
  }

  @Override
  public void apply(final CoordinateSequenceFilter filter) {
    for (final LinearRing ring : rings()) {
      ring.apply(filter);
      if (filter.isDone()) {
        break;
      }
    }
    if (filter.isGeometryChanged()) {
      geometryChanged();
    }
  }

  @Override
  public void apply(final GeometryComponentFilter filter) {
    filter.filter(this);
    for (final LinearRing ring : rings()) {
      ring.apply(filter);
    }
  }

  /**
   * Creates and returns a full copy of this {@link Polygon} object.
   * (including all coordinates contained by it).
   *
   * @return a clone of this instance
   */
  @Override
  public PolygonImpl clone() {
    final PolygonImpl poly = (PolygonImpl)super.clone();
    for (int i = 0; i < rings.length; i++) {
      poly.rings[i] = rings[i].clone();
    }
    return poly;
  }

  @Override
  public int compareToSameClass(final Geometry geometry) {
    final LinearRing thisShell = getExteriorRing();
    final LinearRing otherShell = ((Polygon)geometry).getExteriorRing();
    return thisShell.compareToSameClass(otherShell);
  }

  @Override
  public int compareToSameClass(final Geometry o,
    final CoordinateSequenceComparator comp) {
    final Polygon poly = (Polygon)o;

    final LinearRing thisShell = getExteriorRing();
    final LinearRing otherShell = poly.getExteriorRing();
    final int shellComp = thisShell.compareToSameClass(otherShell, comp);
    if (shellComp != 0) {
      return shellComp;
    }

    final int nHole1 = getNumInteriorRing();
    final int nHole2 = poly.getNumInteriorRing();
    int i = 0;
    while (i < nHole1 && i < nHole2) {
      final LinearRing thisHole = getInteriorRing(i);
      final LinearRing otherHole = poly.getInteriorRing(i);
      final int holeComp = thisHole.compareToSameClass(otherHole, comp);
      if (holeComp != 0) {
        return holeComp;
      }
      i++;
    }
    if (i < nHole1) {
      return 1;
    }
    if (i < nHole2) {
      return -1;
    }
    return 0;
  }

  @Override
  protected BoundingBox computeBoundingBox() {
    final GeometryFactory geometryFactory = getGeometryFactory();
    BoundingBox boundingBox = new Envelope(geometryFactory);
    for (final LinearRing ring : rings) {
      boundingBox = boundingBox.expandToInclude(ring);
    }
    return boundingBox;
  }

  @Override
  public Geometry convexHull() {
    return getExteriorRing().convexHull();
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

  @Override
  public boolean equalsExact3d(final Geometry geometry) {
    if (geometry == this) {
      return true;
    } else if (geometry instanceof Polygon) {
      final Polygon polygon = (Polygon)geometry;
      final int ringCount = getRingCount();
      if (ringCount == polygon.getRingCount()) {
        for (int i = 0; i < ringCount; i++) {
          final LinearRing ring1 = getRing(i);
          final LinearRing ring2 = polygon.getRing(i);
          if (!ring1.equalsExact3d(ring2)) {
            return false;
          }
        }
        return true;

      }
    }
    return false;
  }

  /**
   *  Returns the area of this <code>Polygon</code>
   *
   *@return the area of the polygon
   */
  @Override
  public double getArea() {
    double totalArea = 0.0;
    for (int i = 0; i < getRingCount(); i++) {
      final LinearRing ring = getRing(i);
      final double area = Math.abs(CGAlgorithms.signedArea(ring.getCoordinatesList()));
      if (i == 0) {
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
      if (rings.length == 1) {
        return geometryFactory.linearRing(this.rings[0]);
      } else {
        return geometryFactory.multiLineString(this.rings);
      }
    }
  }

  @Override
  public int getBoundaryDimension() {
    return 1;
  }

  @Override
  public Coordinates getCoordinate() {
    return getExteriorRing().getCoordinate();
  }

  @Override
  public Coordinates[] getCoordinateArray() {
    if (isEmpty()) {
      return new Coordinates[0];
    } else {
      final Coordinates[] coordinates = new Coordinates[getVertexCount()];
      int i = 0;
      for (final Vertex vertex : vertices()) {
        coordinates[i] = vertex.cloneCoordinates();
        i++;
      }
      return coordinates;
    }
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
      return rings.length - 1;
    }
  }

  @Override
  public LinearRing getRing(final int ringIndex) {
    if (isEmpty() || ringIndex < 0 || ringIndex >= rings.length) {
      return null;
    } else {
      return this.rings[ringIndex];
    }
  }

  @Override
  public int getRingCount() {
    if (isEmpty()) {
      return 0;
    } else {
      return rings.length;
    }
  }

  @Override
  public List<LinearRing> getRings() {
    return new ArrayList<>(Arrays.asList(rings));
  }

  @Override
  public Vertex getVertex(final int... vertexId) {
    if (vertexId == null || vertexId.length != 2) {
      return null;
    } else {
      final int ringIndex = vertexId[0];
      if (ringIndex >= 0) {
        final LinearRing ring = getRing(ringIndex);
        if (ring != null) {
          final int vertexIndex = vertexId[1];
          if (vertexIndex >= 0 && vertexIndex < ring.getVertexCount()) {
            return new PolygonVertex(this, vertexId);
          }
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

  @Override
  public boolean isEmpty() {
    return rings == null;
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

  private LinearRing normalize(final LinearRing ring, final boolean clockwise) {
    if (ring.isEmpty()) {
      return ring;
    } else {
      final Coordinates[] ringCoordinates = ring.getCoordinateArray();
      final Coordinates[] uniqueCoordinates = new Coordinates[ringCoordinates.length - 1];
      System.arraycopy(ringCoordinates, 0, uniqueCoordinates, 0,
        uniqueCoordinates.length);
      final Coordinates minCoordinate = CoordinateArrays.minCoordinate(ringCoordinates);
      CoordinateArrays.scroll(uniqueCoordinates, minCoordinate);
      System.arraycopy(uniqueCoordinates, 0, ringCoordinates, 0,
        uniqueCoordinates.length);
      ringCoordinates[uniqueCoordinates.length] = uniqueCoordinates[0];
      if (CGAlgorithms.isCCW(ringCoordinates) == clockwise) {
        CoordinateArrays.reverse(ringCoordinates);
      }
      final GeometryFactory geometryFactory = getGeometryFactory();
      return geometryFactory.linearRing(ringCoordinates);
    }
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
      return Arrays.asList(this.rings);
    }
  }

  /**
   * @author Paul Austin <paul.austin@revolsys.com>
   */
  @Override
  public Iterable<Vertex> vertices() {
    return new PolygonVertex(this, 0, -1);
  }
}
