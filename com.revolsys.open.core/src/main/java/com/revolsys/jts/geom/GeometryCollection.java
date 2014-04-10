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
import java.util.List;
import java.util.TreeSet;

import com.revolsys.jts.util.Assert;

/**
 * Models a collection of {@link Geometry}s of
 * arbitrary type and dimension.
 * 
 *
 *@version 1.7
 */
public class GeometryCollection extends Geometry {
  // With contributions from Markus Schaber [schabios@logi-track.com] 2004-03-26
  private static final long serialVersionUID = -5694727726395021467L;

  /**
   *  Internal representation of this <code>GeometryCollection</code>.
   */
  protected Geometry[] geometries;

  /**
   * @param geometries
   *            the <code>Geometry</code>s for this <code>GeometryCollection</code>,
   *            or <code>null</code> or an empty array to create the empty
   *            geometry. Elements may be empty <code>Geometry</code>s,
   *            but not <code>null</code>s.
   */
  public GeometryCollection(Geometry[] geometries, final GeometryFactory factory) {
    super(factory);
    if (geometries == null) {
      geometries = new Geometry[] {};
    }
    if (hasNullElements(geometries)) {
      throw new IllegalArgumentException(
        "geometries must not contain null elements");
    }
    this.geometries = geometries;
  }

  /** @deprecated Use GeometryFactory instead */
  @Deprecated
  public GeometryCollection(final Geometry[] geometries,
    final PrecisionModel precisionModel, final int SRID) {
    this(geometries, new GeometryFactory(precisionModel, SRID));
  }

  @Override
  public void apply(final CoordinateFilter filter) {
    for (int i = 0; i < geometries.length; i++) {
      geometries[i].apply(filter);
    }
  }

  @Override
  public void apply(final CoordinateSequenceFilter filter) {
    if (geometries.length == 0) {
      return;
    }
    for (int i = 0; i < geometries.length; i++) {
      geometries[i].apply(filter);
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
    for (int i = 0; i < geometries.length; i++) {
      geometries[i].apply(filter);
    }
  }

  @Override
  public void apply(final GeometryFilter filter) {
    filter.filter(this);
    for (int i = 0; i < geometries.length; i++) {
      geometries[i].apply(filter);
    }
  }

  /**
   * Creates and returns a full copy of this {@link GeometryCollection} object.
   * (including all coordinates contained by it).
   *
   * @return a clone of this instance
   */
  @Override
  public Object clone() {
    final GeometryCollection gc = (GeometryCollection)super.clone();
    gc.geometries = new Geometry[geometries.length];
    for (int i = 0; i < geometries.length; i++) {
      gc.geometries[i] = (Geometry)geometries[i].clone();
    }
    return gc;// return the clone
  }

  @Override
  protected int compareToSameClass(final Object o) {
    final TreeSet theseElements = new TreeSet(Arrays.asList(geometries));
    final TreeSet otherElements = new TreeSet(
      Arrays.asList(((GeometryCollection)o).geometries));
    return compare(theseElements, otherElements);
  }

  @Override
  protected int compareToSameClass(final Object o,
    final CoordinateSequenceComparator comp) {
    final GeometryCollection gc = (GeometryCollection)o;

    final int n1 = getNumGeometries();
    final int n2 = gc.getNumGeometries();
    int i = 0;
    while (i < n1 && i < n2) {
      final Geometry thisGeom = getGeometry(i);
      final Geometry otherGeom = gc.getGeometry(i);
      final int holeComp = thisGeom.compareToSameClass(otherGeom, comp);
      if (holeComp != 0) {
        return holeComp;
      }
      i++;
    }
    if (i < n1) {
      return 1;
    }
    if (i < n2) {
      return -1;
    }
    return 0;

  }

  @Override
  protected Envelope computeEnvelopeInternal() {
    final Envelope envelope = new Envelope();
    for (int i = 0; i < geometries.length; i++) {
      envelope.expandToInclude(geometries[i].getEnvelopeInternal());
    }
    return envelope;
  }

  @Override
  public boolean equalsExact(final Geometry other, final double tolerance) {
    if (!isEquivalentClass(other)) {
      return false;
    }
    final GeometryCollection otherCollection = (GeometryCollection)other;
    if (geometries.length != otherCollection.geometries.length) {
      return false;
    }
    for (int i = 0; i < geometries.length; i++) {
      if (!geometries[i].equalsExact(otherCollection.geometries[i], tolerance)) {
        return false;
      }
    }
    return true;
  }

  /**
   *  Returns the area of this <code>GeometryCollection</code>
   *
   * @return the area of the polygon
   */
  @Override
  public double getArea() {
    double area = 0.0;
    for (int i = 0; i < geometries.length; i++) {
      area += geometries[i].getArea();
    }
    return area;
  }

  @Override
  public Geometry getBoundary() {
    checkNotGeometryCollection(this);
    Assert.shouldNeverReachHere();
    return null;
  }

  @Override
  public int getBoundaryDimension() {
    int dimension = Dimension.FALSE;
    for (int i = 0; i < geometries.length; i++) {
      dimension = Math.max(dimension, geometries[i].getBoundaryDimension());
    }
    return dimension;
  }

  @Override
  public Coordinate getCoordinate() {
    if (isEmpty()) {
      return null;
    }
    return geometries[0].getCoordinate();
  }

  /**
   * Collects all coordinates of all subgeometries into an Array.
   *
   * Note that while changes to the coordinate objects themselves
   * may modify the Geometries in place, the returned Array as such
   * is only a temporary container which is not synchronized back.
   *
   * @return the collected coordinates
   *    */
  @Override
  public Coordinate[] getCoordinateArray() {
    final Coordinate[] coordinates = new Coordinate[getNumPoints()];
    int k = -1;
    for (int i = 0; i < geometries.length; i++) {
      final Coordinate[] childCoordinates = geometries[i].getCoordinateArray();
      for (int j = 0; j < childCoordinates.length; j++) {
        k++;
        coordinates[k] = childCoordinates[j];
      }
    }
    return coordinates;
  }

  @Override
  public int getDimension() {
    int dimension = Dimension.FALSE;
    for (int i = 0; i < geometries.length; i++) {
      dimension = Math.max(dimension, geometries[i].getDimension());
    }
    return dimension;
  }

  @SuppressWarnings("unchecked")
  @Override
  public <V extends Geometry> List<V> getGeometries() {
    return (List<V>)Arrays.asList(geometries);
  }

  @Override
  public Geometry getGeometry(final int n) {
    return geometries[n];
  }

  @Override
  public String getGeometryType() {
    return "GeometryCollection";
  }

  @Override
  public double getLength() {
    double sum = 0.0;
    for (int i = 0; i < geometries.length; i++) {
      sum += (geometries[i]).getLength();
    }
    return sum;
  }

  @Override
  public int getNumGeometries() {
    return geometries.length;
  }

  @Override
  public int getNumPoints() {
    int numPoints = 0;
    for (int i = 0; i < geometries.length; i++) {
      numPoints += geometries[i].getNumPoints();
    }
    return numPoints;
  }

  @Override
  public boolean isEmpty() {
    for (int i = 0; i < geometries.length; i++) {
      if (!geometries[i].isEmpty()) {
        return false;
      }
    }
    return true;
  }

  @Override
  public void normalize() {
    for (int i = 0; i < geometries.length; i++) {
      geometries[i].normalize();
    }
    Arrays.sort(geometries);
  }

  /**
   * Creates a {@link GeometryCollection} with
   * every component reversed.
   * The order of the components in the collection are not reversed.
   *
   * @return a {@link GeometryCollection} in the reverse order
   */
  @Override
  public Geometry reverse() {
    final int n = geometries.length;
    final Geometry[] revGeoms = new Geometry[n];
    for (int i = 0; i < geometries.length; i++) {
      revGeoms[i] = geometries[i].reverse();
    }
    return getGeometryFactory().createGeometryCollection(revGeoms);
  }

  /**
   * @author Paul Austin <paul.austin@revolsys.com>
   */
  @Override
  public Iterable<Vertex> vertices() {
    // TODO;
    throw new UnsupportedOperationException();
  }
}
