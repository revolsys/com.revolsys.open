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
import java.util.Collections;
import java.util.List;

import com.revolsys.jts.algorithm.CGAlgorithms;
import com.revolsys.jts.geom.CoordinateArrays;
import com.revolsys.jts.geom.CoordinateFilter;
import com.revolsys.jts.geom.CoordinateSequenceComparator;
import com.revolsys.jts.geom.CoordinateSequenceFilter;
import com.revolsys.jts.geom.Coordinates;
import com.revolsys.jts.geom.CoordinatesList;
import com.revolsys.jts.geom.Envelope;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.GeometryComponentFilter;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.jts.geom.GeometryFilter;
import com.revolsys.jts.geom.LinearRing;
import com.revolsys.jts.geom.Polygon;
import com.revolsys.jts.geom.vertex.PolygonVertexIterable;
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
public class PolygonImpl extends GeometryImpl implements Polygon {

  private static final long serialVersionUID = -3494792200821764533L;

  /**
   *  The exterior boundary,
   * or <code>null</code> if this <code>Polygon</code>
   *  is empty.
   */
  protected LinearRing shell = null;

  /**
   * The interior boundaries, if any.
   * This instance var is never null.
   * If there are no holes, the array is of zero length.
   */
  protected LinearRing[] holes;

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
  public PolygonImpl(LinearRing shell, LinearRing[] holes,
    final GeometryFactory factory) {
    super(factory);
    if (shell == null) {
      shell = getGeometryFactory().createLinearRing((CoordinatesList)null);
    }
    if (holes == null) {
      holes = new LinearRing[] {};
    }
    if (hasNullElements(holes)) {
      throw new IllegalArgumentException("holes must not contain null elements");
    }
    if (shell.isEmpty() && hasNonEmptyElements(holes)) {
      throw new IllegalArgumentException("shell is empty but holes are not");
    }
    this.shell = shell;
    this.holes = holes;
  }

  @Override
  public void apply(final CoordinateFilter filter) {
    shell.apply(filter);
    for (int i = 0; i < holes.length; i++) {
      holes[i].apply(filter);
    }
  }

  @Override
  public void apply(final CoordinateSequenceFilter filter) {
    shell.apply(filter);
    if (!filter.isDone()) {
      for (int i = 0; i < holes.length; i++) {
        holes[i].apply(filter);
        if (filter.isDone()) {
          break;
        }
      }
    }
    if (filter.isGeometryChanged()) {
      geometryChanged();
    }
  }

  @Override
  public void apply(final GeometryComponentFilter filter) {
    filter.filter(this);
    shell.apply(filter);
    for (int i = 0; i < holes.length; i++) {
      holes[i].apply(filter);
    }
  }

  @Override
  public void apply(final GeometryFilter filter) {
    filter.filter(this);
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
    poly.shell = shell.clone();
    poly.holes = new LinearRing[holes.length];
    for (int i = 0; i < holes.length; i++) {
      poly.holes[i] = holes[i].clone();
    }
    return poly;// return the clone
  }

  @Override
  public int compareToSameClass(final Geometry o) {
    final LinearRing thisShell = shell;
    final LinearRing otherShell = ((Polygon)o).getExteriorRing();
    return thisShell.compareToSameClass(otherShell);
  }

  @Override
  public int compareToSameClass(final Geometry o,
    final CoordinateSequenceComparator comp) {
    final Polygon poly = (Polygon)o;

    final LinearRing thisShell = shell;
    final LinearRing otherShell = poly.getExteriorRing();
    final int shellComp = thisShell.compareToSameClass(otherShell, comp);
    if (shellComp != 0) {
      return shellComp;
    }

    final int nHole1 = getNumInteriorRing();
    final int nHole2 = poly.getNumInteriorRing();
    int i = 0;
    while (i < nHole1 && i < nHole2) {
      final LinearRing thisHole = getInteriorRingN(i);
      final LinearRing otherHole = poly.getInteriorRingN(i);
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
  protected Envelope computeEnvelopeInternal() {
    return shell.getEnvelopeInternal();
  }

  @Override
  public Geometry convexHull() {
    return getExteriorRing().convexHull();
  }

  @Override
  public boolean equalsExact(final Geometry other, final double tolerance) {
    if (!isEquivalentClass(other)) {
      return false;
    }
    final Polygon otherPolygon = (Polygon)other;
    final Geometry thisShell = shell;
    final Geometry otherPolygonShell = otherPolygon.getExteriorRing();
    if (!thisShell.equalsExact(otherPolygonShell, tolerance)) {
      return false;
    }
    if (holes.length != otherPolygon.getNumInteriorRing()) {
      return false;
    }
    for (int i = 0; i < holes.length; i++) {
      if (!((Geometry)holes[i]).equalsExact(otherPolygon.getInteriorRingN(i),
        tolerance)) {
        return false;
      }
    }
    return true;
  }

  /**
   *  Returns the area of this <code>Polygon</code>
   *
   *@return the area of the polygon
   */
  @Override
  public double getArea() {
    double area = 0.0;
    area += Math.abs(CGAlgorithms.signedArea(shell.getCoordinatesList()));
    for (int i = 0; i < holes.length; i++) {
      area -= Math.abs(CGAlgorithms.signedArea(holes[i].getCoordinatesList()));
    }
    return area;
  }

  /**
   * Computes the boundary of this geometry
   *
   * @return a lineal geometry (which may be empty)
   * @see Geometry#getBoundary
   */
  @Override
  public Geometry getBoundary() {
    if (isEmpty()) {
      return getGeometryFactory().createMultiLineString();
    } else {
      final LinearRing[] rings = new LinearRing[holes.length + 1];
      rings[0] = shell;
      for (int i = 0; i < holes.length; i++) {
        rings[i + 1] = holes[i];
      }
      // create LineString or MultiLineString as appropriate
      if (rings.length <= 1) {
        return getGeometryFactory().createLinearRing(
          rings[0].getCoordinatesList());
      }
      return getGeometryFactory().createMultiLineString(rings);
    }
  }

  @Override
  public int getBoundaryDimension() {
    return 1;
  }

  @Override
  public Coordinates getCoordinate() {
    return shell.getCoordinate();
  }

  @Override
  public Coordinates[] getCoordinateArray() {
    if (isEmpty()) {
      return new Coordinates[] {};
    }
    final Coordinates[] coordinates = new Coordinates[getNumPoints()];
    int k = -1;
    final Coordinates[] shellCoordinates = shell.getCoordinateArray();
    for (int x = 0; x < shellCoordinates.length; x++) {
      k++;
      coordinates[k] = shellCoordinates[x];
    }
    for (int i = 0; i < holes.length; i++) {
      final Coordinates[] childCoordinates = holes[i].getCoordinateArray();
      for (int j = 0; j < childCoordinates.length; j++) {
        k++;
        coordinates[k] = childCoordinates[j];
      }
    }
    return coordinates;
  }

  @Override
  public int getDimension() {
    return 2;
  }

  @Override
  public LinearRing getExteriorRing() {
    return shell;
  }

  @Override
  public String getGeometryType() {
    return "Polygon";
  }

  @Override
  public LinearRing getInteriorRingN(final int n) {
    return holes[n];
  }

  /**
   *  Returns the perimeter of this <code>Polygon</code>
   *
   *@return the perimeter of the polygon
   */
  @Override
  public double getLength() {
    double len = 0.0;
    len += shell.getLength();
    for (int i = 0; i < holes.length; i++) {
      len += holes[i].getLength();
    }
    return len;
  }

  @Override
  public int getNumInteriorRing() {
    return holes.length;
  }

  @Override
  public int getNumPoints() {
    int numPoints = shell.getNumPoints();
    for (int i = 0; i < holes.length; i++) {
      numPoints += holes[i].getNumPoints();
    }
    return numPoints;
  }

  @Override
  public boolean isEmpty() {
    return shell.isEmpty();
  }

  /**
   * Tests if a valid polygon is simple.
   * This method always returns true, since a valid polygon is always simple
   *
   * @return <code>true</code>
   */
  /*
   * public boolean isSimple() { return true; }
   */

  @Override
  public boolean isRectangle() {
    if (getNumInteriorRing() != 0) {
      return false;
    }
    if (shell == null) {
      return false;
    }
    if (shell.getNumPoints() != 5) {
      return false;
    }

    final CoordinatesList seq = shell.getCoordinatesList();

    // check vertices have correct values
    final Envelope env = getEnvelopeInternal();
    for (int i = 0; i < 5; i++) {
      final double x = seq.getX(i);
      if (!(x == env.getMinX() || x == env.getMaxX())) {
        return false;
      }
      final double y = seq.getY(i);
      if (!(y == env.getMinY() || y == env.getMaxY())) {
        return false;
      }
    }

    // check vertices are in right order
    double prevX = seq.getX(0);
    double prevY = seq.getY(0);
    for (int i = 1; i <= 4; i++) {
      final double x = seq.getX(i);
      final double y = seq.getY(i);
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

  @Override
  public Polygon normalize() {
    if (isEmpty()) {
      return this;
    } else {
      final LinearRing exteriorRing = normalize(shell, true);
      final List<LinearRing> rings = new ArrayList<>();
      for (final LinearRing hole : holes) {
        final LinearRing normalizedHole = normalize(hole, false);
        rings.add(normalizedHole);
      }
      Collections.sort(rings);
      rings.add(0, exteriorRing);
      final GeometryFactory geometryFactory = getGeometryFactory();
      return geometryFactory.createPolygon(rings);
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
      return geometryFactory.createLinearRing(ringCoordinates);
    }
  }

  @Override
  public Polygon reverse() {
    final List<LinearRing> rings = new ArrayList<>();
    rings.add(shell.reverse());
    for (final LinearRing ring : holes) {
      rings.add(ring.reverse());
    }
    final GeometryFactory geometryFactory = getGeometryFactory();
    return geometryFactory.createPolygon(rings);
  }

  /**
   * @author Paul Austin <paul.austin@revolsys.com>
   */
  @Override
  public Iterable<Vertex> vertices() {
    return new PolygonVertexIterable(this);
  }
}
