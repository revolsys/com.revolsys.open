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

import java.util.Collections;

import com.revolsys.jts.util.Assert;

/**
 * Represents a single point.
 *
 * A <code>Point</code> is topologically valid if and only if:
 * <ul>
 * <li>the coordinate which defines it (if any) is a valid coordinate 
 * (i.e does not have an <code>NaN</code> X or Y ordinate)
 * </ul>
 * 
 *@version 1.7
 */
public class Point extends Geometry implements Puntal {
  private static final long serialVersionUID = 4902022702746614570L;

  /**
   *  The <code>Coordinate</code> wrapped by this <code>Point</code>.
   */
  private CoordinateSequence coordinates;

  /**
   *  Constructs a <code>Point</code> with the given coordinate.
   *
   *@param  coordinate      the coordinate on which to base this <code>Point</code>
   *      , or <code>null</code> to create the empty geometry.
   *@param  precisionModel  the specification of the grid of allowable points
   *      for this <code>Point</code>
   *@param  SRID            the ID of the Spatial Reference System used by this
   *      <code>Point</code>
   * @deprecated Use GeometryFactory instead
   */
  @Deprecated
  public Point(final Coordinate coordinate,
    final PrecisionModel precisionModel, final int SRID) {
    super(new GeometryFactory(precisionModel, SRID));
    init(getGeometryFactory().getCoordinateSequenceFactory().create(
      coordinate != null ? new Coordinate[] {
        coordinate
      } : new Coordinate[] {}));
  }

  /**
   *@param  coordinates      contains the single coordinate on which to base this <code>Point</code>
   *      , or <code>null</code> to create the empty geometry.
   */
  public Point(final CoordinateSequence coordinates,
    final GeometryFactory factory) {
    super(factory);
    init(coordinates);
  }

  @Override
  public void apply(final CoordinateFilter filter) {
    if (isEmpty()) {
      return;
    }
    filter.filter(getCoordinate());
  }

  @Override
  public void apply(final CoordinateSequenceFilter filter) {
    if (isEmpty()) {
      return;
    }
    filter.filter(coordinates, 0);
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
   * Creates and returns a full copy of this {@link Point} object.
   * (including all coordinates contained by it).
   *
   * @return a clone of this instance
   */
  @Override
  public Object clone() {
    final Point p = (Point)super.clone();
    p.coordinates = (CoordinateSequence)coordinates.clone();
    return p;// return the clone
  }

  @Override
  protected int compareToSameClass(final Object other) {
    final Point point = (Point)other;
    return getCoordinate().compareTo(point.getCoordinate());
  }

  @Override
  protected int compareToSameClass(final Object other,
    final CoordinateSequenceComparator comp) {
    final Point point = (Point)other;
    return comp.compare(this.coordinates, point.coordinates);
  }

  @Override
  protected Envelope computeEnvelopeInternal() {
    if (isEmpty()) {
      return new Envelope();
    }
    final Envelope env = new Envelope();
    env.expandToInclude(coordinates.getX(0), coordinates.getY(0));
    return env;
  }

  @Override
  public boolean equalsExact(final Geometry other, final double tolerance) {
    if (!isEquivalentClass(other)) {
      return false;
    }
    if (isEmpty() && other.isEmpty()) {
      return true;
    }
    if (isEmpty() != other.isEmpty()) {
      return false;
    }
    return equal(((Point)other).getCoordinate(), this.getCoordinate(),
      tolerance);
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
  public Geometry getBoundary() {
    return getGeometryFactory().createEmptyGeometryCollection();
  }

  @Override
  public int getBoundaryDimension() {
    return Dimension.FALSE;
  }

  @Override
  public Coordinate getCoordinate() {
    return coordinates.size() != 0 ? coordinates.getCoordinate(0) : null;
  }

  @Override
  public Coordinate[] getCoordinates() {
    return isEmpty() ? new Coordinate[] {} : new Coordinate[] {
      getCoordinate()
    };
  }

  public CoordinateSequence getCoordinateSequence() {
    return coordinates;
  }

  @Override
  public int getDimension() {
    return 0;
  }

  @Override
  public String getGeometryType() {
    return "Point";
  }

  @Override
  public int getNumPoints() {
    return isEmpty() ? 0 : 1;
  }

  public double getX() {
    if (getCoordinate() == null) {
      throw new IllegalStateException("getX called on empty Point");
    }
    return getCoordinate().x;
  }

  public double getY() {
    if (getCoordinate() == null) {
      throw new IllegalStateException("getY called on empty Point");
    }
    return getCoordinate().y;
  }

  private void init(CoordinateSequence coordinates) {
    if (coordinates == null) {
      coordinates = getGeometryFactory().getCoordinateSequenceFactory().create(
        new Coordinate[] {});
    }
    Assert.isTrue(coordinates.size() <= 1);
    this.coordinates = coordinates;
  }

  @Override
  public boolean isEmpty() {
    return getCoordinate() == null;
  }

  @Override
  public boolean isSimple() {
    return true;
  }

  @Override
  public void normalize() {
    // a Point is always in normalized form
  }

  @Override
  public Geometry reverse() {
    return (Geometry)clone();
  }

  @Override
  public Iterable<GeometryVertex> vertices() {
    final GeometryVertex vertex = new GeometryVertex(this, 0);
    return Collections.singletonList(vertex);
  }
}
