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

import java.util.NoSuchElementException;

import com.revolsys.collection.AbstractIterator;
import com.revolsys.gis.model.coordinates.Coordinates;
import com.revolsys.gis.model.coordinates.list.CoordinatesList;
import com.revolsys.gis.model.coordinates.list.CoordinatesListUtil;
import com.revolsys.jts.algorithm.CGAlgorithms;
import com.revolsys.jts.operation.BoundaryOp;

/**
 *  Models an OGC-style <code>LineString</code>.
 *  A LineString consists of a sequence of two or more vertices,
 *  along with all points along the linearly-interpolated curves
 *  (line segments) between each 
 *  pair of consecutive vertices.
 *  Consecutive vertices may be equal.
 *  The line segments in the line may intersect each other (in other words, 
 *  the linestring may "curl back" in itself and self-intersect.
 *  Linestrings with exactly two identical points are invalid. 
 *  <p> 
 * A linestring must have either 0 or 2 or more points.  
 * If these conditions are not met, the constructors throw 
 * an {@link IllegalArgumentException}
 *
 *@version 1.7
 */
public class LineString extends Geometry implements Lineal {
  private static final long serialVersionUID = 3110669828065365560L;

  /**
   *  The points of this <code>LineString</code>.
   */
  protected CoordinateSequence points;

  /**
   *  Constructs a <code>LineString</code> with the given points.
   *
   *@param  points the points of the linestring, or <code>null</code>
   *      to create the empty geometry. This array must not contain <code>null</code>
   *      elements. Consecutive points may be equal.
   *@param  precisionModel  the specification of the grid of allowable points
   *      for this <code>LineString</code>
   *@param  SRID            the ID of the Spatial Reference System used by this
   *      <code>LineString</code>
   * @throws IllegalArgumentException if too few points are provided
   */
  /** @deprecated Use GeometryFactory instead */
  @Deprecated
  public LineString(final Coordinate points[],
    final PrecisionModel precisionModel, final int SRID) {
    super(new GeometryFactory(precisionModel, SRID));
    init(getGeometryFactory().getCoordinateSequenceFactory().create(points));
  }

  /**
   * Constructs a <code>LineString</code> with the given points.
   *  
   *@param  points the points of the linestring, or <code>null</code>
   *      to create the empty geometry. 
   * @throws IllegalArgumentException if too few points are provided
   */
  public LineString(final CoordinateSequence points,
    final GeometryFactory factory) {
    super(factory);
    init(points);
  }

  @Override
  public void apply(final CoordinateFilter filter) {
    for (int i = 0; i < points.size(); i++) {
      filter.filter(points.getCoordinate(i));
    }
  }

  @Override
  public void apply(final CoordinateSequenceFilter filter) {
    if (points.size() == 0) {
      return;
    }
    for (int i = 0; i < points.size(); i++) {
      filter.filter(points, i);
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
  }

  @Override
  public void apply(final GeometryFilter filter) {
    filter.filter(this);
  }

  /**
   * Creates and returns a full copy of this {@link LineString} object.
   * (including all coordinates contained by it).
   *
   * @return a clone of this instance
   */
  @Override
  public Object clone() {
    final LineString ls = (LineString)super.clone();
    ls.points = (CoordinateSequence)points.clone();
    return ls;
  }

  @Override
  protected int compareToSameClass(final Object o) {
    final LineString line = (LineString)o;
    // MD - optimized implementation
    int i = 0;
    int j = 0;
    while (i < points.size() && j < line.points.size()) {
      final int comparison = points.getCoordinate(i).compareTo(
        line.points.getCoordinate(j));
      if (comparison != 0) {
        return comparison;
      }
      i++;
      j++;
    }
    if (i < points.size()) {
      return 1;
    }
    if (j < line.points.size()) {
      return -1;
    }
    return 0;
  }

  @Override
  protected int compareToSameClass(final Object o,
    final CoordinateSequenceComparator comp) {
    final LineString line = (LineString)o;
    return comp.compare(this.points, line.points);
  }

  @Override
  protected Envelope computeEnvelopeInternal() {
    if (isEmpty()) {
      return new Envelope();
    }
    return points.expandEnvelope(new Envelope());
  }

  @Override
  public boolean equalsExact(final Geometry other, final double tolerance) {
    if (!isEquivalentClass(other)) {
      return false;
    }
    final LineString otherLineString = (LineString)other;
    if (points.size() != otherLineString.points.size()) {
      return false;
    }
    for (int i = 0; i < points.size(); i++) {
      if (!equal(points.getCoordinate(i),
        otherLineString.points.getCoordinate(i), tolerance)) {
        return false;
      }
    }
    return true;
  }

  /**
   * Gets the boundary of this geometry.
   * The boundary of a lineal geometry is always a zero-dimensional geometry (which may be empty).
   *
   * @return the boundary geometry
   * @see Geometry#getBoundary
   */
  @Override
  public Geometry getBoundary() {
    return (new BoundaryOp(this)).getBoundary();
  }

  @Override
  public int getBoundaryDimension() {
    if (isClosed()) {
      return Dimension.FALSE;
    }
    return 0;
  }

  @Override
  public Coordinate getCoordinate() {
    if (isEmpty()) {
      return null;
    }
    return points.getCoordinate(0);
  }

  public Coordinate getCoordinateN(final int n) {
    return points.getCoordinate(n);
  }

  @Override
  public Coordinate[] getCoordinates() {
    return points.toCoordinateArray();
  }

  public CoordinateSequence getCoordinateSequence() {
    return points;
  }

  @Override
  public int getDimension() {
    return 1;
  }

  public Point getEndPoint() {
    if (isEmpty()) {
      return null;
    }
    return getPointN(getNumPoints() - 1);
  }

  @Override
  public String getGeometryType() {
    return "LineString";
  }

  /**
   *  Returns the length of this <code>LineString</code>
   *
   *@return the length of the linestring
   */
  @Override
  public double getLength() {
    return CGAlgorithms.length(points);
  }

  @Override
  public int getNumPoints() {
    return points.size();
  }

  public CoordinatesList getPointList() {
    return CoordinatesListUtil.get(this);
  }

  public Point getPointN(final int n) {
    return getGeometryFactory().createPoint(points.getCoordinate(n));
  }

  public Point getStartPoint() {
    if (isEmpty()) {
      return null;
    }
    return getPointN(0);
  }

  private void init(CoordinateSequence points) {
    if (points == null) {
      points = getGeometryFactory().getCoordinateSequenceFactory().create(
        new Coordinate[] {});
    }
    if (points.size() == 1) {
      throw new IllegalArgumentException(
        "Invalid number of points in LineString (found " + points.size()
          + " - must be 0 or >= 2)");
    }
    this.points = points;
  }

  public boolean isClosed() {
    if (isEmpty()) {
      return false;
    }
    return getCoordinateN(0).equals2D(getCoordinateN(getNumPoints() - 1));
  }

  /**
   *  Returns true if the given point is a vertex of this <code>LineString</code>.
   *
   *@param  pt  the <code>Coordinate</code> to check
   *@return     <code>true</code> if <code>pt</code> is one of this <code>LineString</code>
   *      's vertices
   */
  public boolean isCoordinate(final Coordinate pt) {
    for (int i = 0; i < points.size(); i++) {
      if (points.getCoordinate(i).equals(pt)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public boolean isEmpty() {
    return points.size() == 0;
  }

  @Override
  protected boolean isEquivalentClass(final Geometry other) {
    return other instanceof LineString;
  }

  public boolean isRing() {
    return isClosed() && isSimple();
  }

  /**
   * Normalizes a LineString.  A normalized linestring
   * has the first point which is not equal to it's reflected point
   * less than the reflected point.
   */
  @Override
  public void normalize() {
    for (int i = 0; i < points.size() / 2; i++) {
      final int j = points.size() - 1 - i;
      // skip equal points on both ends
      if (!points.getCoordinate(i).equals(points.getCoordinate(j))) {
        if (points.getCoordinate(i).compareTo(points.getCoordinate(j)) > 0) {
          CoordinateArrays.reverse(getCoordinates());
        }
        return;
      }
    }
  }

  public Iterable<Coordinates> points() {
    return getPointList();
  }

  /**
   * Creates a {@link LineString} whose coordinates are in the reverse
   * order of this objects
   *
   * @return a {@link LineString} with coordinates in the reverse order
   */
  @Override
  public Geometry reverse() {
    final CoordinateSequence seq = (CoordinateSequence)points.clone();
    CoordinateSequences.reverse(seq);
    final LineString revLine = getGeometryFactory().createLineString(seq);
    return revLine;
  }

  @Override
  public Iterable<GeometryVertex> vertices() {
    return new AbstractIterator<GeometryVertex>() {
      private GeometryVertex vertex = new GeometryVertex(LineString.this, 0);

      private int index = 0;

      @Override
      protected GeometryVertex getNext() throws NoSuchElementException {
        if (index < getNumPoints()) {
          vertex.setVertexId(index);
          index++;
          return vertex;
        } else {
          vertex = null;
          throw new NoSuchElementException();
        }
      }
    };
  }

}
