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
package com.revolsys.jts.geom;

import java.util.Arrays;
import java.util.NoSuchElementException;

import com.revolsys.collection.AbstractIterator;
import com.revolsys.jts.algorithm.CGAlgorithms;

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
public class Polygon extends Geometry implements Polygonal {
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
  public Polygon(LinearRing shell, LinearRing[] holes,
    final GeometryFactory factory) {
    super(factory);
    if (shell == null) {
      shell = getGeometryFactory().createLinearRing((CoordinateSequence)null);
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
   *@param  precisionModel  the specification of the grid of allowable points
   *      for this <code>Polygon</code>
   *@param  SRID            the ID of the Spatial Reference System used by this
   *      <code>Polygon</code>
   * @deprecated Use GeometryFactory instead
   */
  @Deprecated
  public Polygon(final LinearRing shell, final LinearRing[] holes,
    final PrecisionModel precisionModel, final int SRID) {
    this(shell, holes, new GeometryFactory(precisionModel, SRID));
  }

  /**
   *  Constructs a <code>Polygon</code> with the given exterior boundary.
   *
   *@param  shell           the outer boundary of the new <code>Polygon</code>,
   *      or <code>null</code> or an empty <code>LinearRing</code> if the empty
   *      geometry is to be created.
   *@param  precisionModel  the specification of the grid of allowable points
   *      for this <code>Polygon</code>
   *@param  SRID            the ID of the Spatial Reference System used by this
   *      <code>Polygon</code>
   * @deprecated Use GeometryFactory instead
   */
  @Deprecated
  public Polygon(final LinearRing shell, final PrecisionModel precisionModel,
    final int SRID) {
    this(shell, new LinearRing[] {}, new GeometryFactory(precisionModel, SRID));
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
  public Object clone() {
    final Polygon poly = (Polygon)super.clone();
    poly.shell = (LinearRing)shell.clone();
    poly.holes = new LinearRing[holes.length];
    for (int i = 0; i < holes.length; i++) {
      poly.holes[i] = (LinearRing)holes[i].clone();
    }
    return poly;// return the clone
  }

  @Override
  protected int compareToSameClass(final Object o) {
    final LinearRing thisShell = shell;
    final LinearRing otherShell = ((Polygon)o).shell;
    return thisShell.compareToSameClass(otherShell);
  }

  @Override
  protected int compareToSameClass(final Object o,
    final CoordinateSequenceComparator comp) {
    final Polygon poly = (Polygon)o;

    final LinearRing thisShell = shell;
    final LinearRing otherShell = poly.shell;
    final int shellComp = thisShell.compareToSameClass(otherShell, comp);
    if (shellComp != 0) {
      return shellComp;
    }

    final int nHole1 = getNumInteriorRing();
    final int nHole2 = poly.getNumInteriorRing();
    int i = 0;
    while (i < nHole1 && i < nHole2) {
      final LinearRing thisHole = (LinearRing)getInteriorRingN(i);
      final LinearRing otherHole = (LinearRing)poly.getInteriorRingN(i);
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
    final Geometry otherPolygonShell = otherPolygon.shell;
    if (!thisShell.equalsExact(otherPolygonShell, tolerance)) {
      return false;
    }
    if (holes.length != otherPolygon.holes.length) {
      return false;
    }
    for (int i = 0; i < holes.length; i++) {
      if (!((Geometry)holes[i]).equalsExact(otherPolygon.holes[i], tolerance)) {
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
    area += Math.abs(CGAlgorithms.signedArea(shell.getCoordinateSequence()));
    for (int i = 0; i < holes.length; i++) {
      area -= Math.abs(CGAlgorithms.signedArea(holes[i].getCoordinateSequence()));
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
    }
    final LinearRing[] rings = new LinearRing[holes.length + 1];
    rings[0] = shell;
    for (int i = 0; i < holes.length; i++) {
      rings[i + 1] = holes[i];
    }
    // create LineString or MultiLineString as appropriate
    if (rings.length <= 1) {
      return getGeometryFactory().createLinearRing(rings[0].getCoordinateSequence());
    }
    return getGeometryFactory().createMultiLineString(rings);
  }

  @Override
  public int getBoundaryDimension() {
    return 1;
  }

  @Override
  public Coordinate getCoordinate() {
    return shell.getCoordinate();
  }

  @Override
  public Coordinate[] getCoordinates() {
    if (isEmpty()) {
      return new Coordinate[] {};
    }
    final Coordinate[] coordinates = new Coordinate[getNumPoints()];
    int k = -1;
    final Coordinate[] shellCoordinates = shell.getCoordinates();
    for (int x = 0; x < shellCoordinates.length; x++) {
      k++;
      coordinates[k] = shellCoordinates[x];
    }
    for (int i = 0; i < holes.length; i++) {
      final Coordinate[] childCoordinates = holes[i].getCoordinates();
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

  public LineString getExteriorRing() {
    return shell;
  }

  @Override
  public String getGeometryType() {
    return "Polygon";
  }

  public LineString getInteriorRingN(final int n) {
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

    final CoordinateSequence seq = shell.getCoordinateSequence();

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
  public void normalize() {
    normalize(shell, true);
    for (int i = 0; i < holes.length; i++) {
      normalize(holes[i], false);
    }
    Arrays.sort(holes);
  }

  private void normalize(final LinearRing ring, final boolean clockwise) {
    if (ring.isEmpty()) {
      return;
    }
    final Coordinate[] uniqueCoordinates = new Coordinate[ring.getCoordinates().length - 1];
    System.arraycopy(ring.getCoordinates(), 0, uniqueCoordinates, 0,
      uniqueCoordinates.length);
    final Coordinate minCoordinate = CoordinateArrays.minCoordinate(ring.getCoordinates());
    CoordinateArrays.scroll(uniqueCoordinates, minCoordinate);
    System.arraycopy(uniqueCoordinates, 0, ring.getCoordinates(), 0,
      uniqueCoordinates.length);
    ring.getCoordinates()[uniqueCoordinates.length] = uniqueCoordinates[0];
    if (CGAlgorithms.isCCW(ring.getCoordinates()) == clockwise) {
      CoordinateArrays.reverse(ring.getCoordinates());
    }
  }

  @Override
  public Geometry reverse() {
    final Polygon poly = (Polygon)super.clone();
    poly.shell = (LinearRing)((LinearRing)shell.clone()).reverse();
    poly.holes = new LinearRing[holes.length];
    for (int i = 0; i < holes.length; i++) {
      poly.holes[i] = (LinearRing)((LinearRing)holes[i].clone()).reverse();
    }
    return poly;// return the clone
  }

  /**
   * @author Paul Austin <paul.austin@revolsys.com>
   */
  @Override
  public Iterable<Vertex> vertices() {
    return new AbstractIterator<Vertex>() {
      private Vertex vertex = new Vertex(Polygon.this, 0);

      private int vertexIndex = 0;

      private int ringIndex = 0;

      private LinearRing ring = shell;

      @Override
      protected Vertex getNext() throws NoSuchElementException {
        while (vertexIndex >= ring.getNumPoints()) {
          vertexIndex = 0;
          ringIndex++;
          if (ringIndex < 1 + holes.length) {
            ring = holes[ringIndex - 1];
          } else {
            vertex = null;
            throw new NoSuchElementException();
          }
        }

        vertex.setVertexId(ringIndex, vertexIndex);
        vertexIndex++;
        return vertex;
      }
    };
  }
}
