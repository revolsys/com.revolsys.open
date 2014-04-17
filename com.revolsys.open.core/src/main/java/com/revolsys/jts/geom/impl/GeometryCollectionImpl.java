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
import java.util.Set;
import java.util.TreeSet;

import com.revolsys.gis.data.model.types.DataType;
import com.revolsys.gis.data.model.types.DataTypes;
import com.revolsys.jts.geom.BoundingBox;
import com.revolsys.jts.geom.CoordinateFilter;
import com.revolsys.jts.geom.CoordinateSequenceComparator;
import com.revolsys.jts.geom.CoordinateSequenceFilter;
import com.revolsys.jts.geom.Coordinates;
import com.revolsys.jts.geom.Dimension;
import com.revolsys.jts.geom.Envelope;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.GeometryCollection;
import com.revolsys.jts.geom.GeometryComponentFilter;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.jts.geom.vertex.Vertex;
import com.revolsys.jts.util.Assert;

/**
 * Models a collection of {@link Geometry}s of
 * arbitrary type and dimension.
 * 
 *
 *@version 1.7
 */
public class GeometryCollectionImpl extends GeometryImpl implements
  GeometryCollection {
  // With contributions from Markus Schaber [schabios@logi-track.com] 2004-03-26
  private static final long serialVersionUID = -5694727726395021467L;

  /**
   *  Internal representation of this <code>GeometryCollection</code>.
   */
  private Geometry[] geometries;

  public GeometryCollectionImpl(final GeometryFactory geometryFactory) {
    super(geometryFactory);
  }

  /**
   * @param geometries
   *            the <code>Geometry</code>s for this <code>GeometryCollection</code>,
   *            or <code>null</code> or an empty array to create the empty
   *            geometry. Elements may be empty <code>Geometry</code>s,
   *            but not <code>null</code>s.
   */
  public GeometryCollectionImpl(final GeometryFactory geometryFactory,
    final Geometry[] geometries) {
    super(geometryFactory);
    if (geometries == null || geometries.length == 0) {
      this.geometries = null;
    } else if (hasNullElements(geometries)) {
      throw new IllegalArgumentException(
        "geometries must not contain null elements");
    } else {
      this.geometries = geometries;
    }
  }

  @Override
  public void apply(final CoordinateFilter filter) {
    for (final Geometry geometry : geometries()) {
      geometry.apply(filter);
    }
  }

  @Override
  public void apply(final CoordinateSequenceFilter filter) {
    for (final Geometry geometry : geometries()) {
      geometry.apply(filter);
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
    for (final Geometry geometry : geometries()) {
      geometry.apply(filter);
    }
  }

  /**
   * Creates and returns a full copy of this {@link GeometryCollection} object.
   * (including all coordinates contained by it).
   *
   * @return a clone of this instance
   */
  @Override
  public GeometryCollectionImpl clone() {
    final GeometryCollectionImpl gc = (GeometryCollectionImpl)super.clone();
    gc.geometries = new Geometry[geometries.length];
    for (int i = 0; i < geometries.length; i++) {
      gc.geometries[i] = geometries[i].clone();
    }
    return gc;// return the clone
  }

  @Override
  public int compareToSameClass(final Geometry geometry) {
    final Set<Geometry> theseElements = new TreeSet<>(getGeometries());
    final Set<Geometry> otherElements = new TreeSet<>(geometry.getGeometries());
    return compare(theseElements, otherElements);
  }

  @Override
  public int compareToSameClass(final Geometry o,
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
  protected BoundingBox computeEnvelopeInternal() {
    BoundingBox envelope = new Envelope(getGeometryFactory());
    for (final Geometry geometry : geometries()) {
      envelope = envelope.expandToInclude(geometry);
    }
    return envelope;
  }

  @Override
  public boolean equalsExact(final Geometry other, final double tolerance) {
    if (!isEquivalentClass(other)) {
      return false;
    }
    final GeometryCollection otherCollection = (GeometryCollection)other;
    if (getNumGeometries() != otherCollection.getNumGeometries()) {
      return false;
    }
    int i = 0;
    for (final Geometry geometry : geometries()) {
      if (!geometry.equalsExact(otherCollection.getGeometry(i), tolerance)) {
        return false;
      }
      i++;
    }
    return true;
  }

  @Override
  public Iterable<Geometry> geometries() {
    return getGeometries();
  }

  /**
   *  Returns the area of this <code>GeometryCollection</code>
   *
   * @return the area of the polygon
   */
  @Override
  public double getArea() {
    double area = 0.0;
    for (final Geometry geometry : geometries()) {
      area += geometry.getArea();
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
    for (final Geometry geometry : geometries()) {
      dimension = Math.max(dimension, geometry.getBoundaryDimension());
    }
    return dimension;
  }

  @Override
  public Coordinates getCoordinate() {
    if (isEmpty()) {
      return null;
    } else {
      return getGeometry(0).getCoordinate();
    }
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
  public Coordinates[] getCoordinateArray() {
    final Coordinates[] coordinates = new Coordinates[getVertexCount()];
    int k = -1;
    for (final Geometry geometry : geometries()) {
      final Coordinates[] childCoordinates = geometry.getCoordinateArray();
      for (int j = 0; j < childCoordinates.length; j++) {
        k++;
        coordinates[k] = childCoordinates[j];
      }
    }
    return coordinates;
  }

  @Override
  public DataType getDataType() {
    return DataTypes.GEOMETRY_COLLECTION;
  }

  @Override
  public int getDimension() {
    int dimension = Dimension.FALSE;
    for (final Geometry geometry : geometries()) {
      dimension = Math.max(dimension, geometry.getDimension());
    }
    return dimension;
  }

  @SuppressWarnings("unchecked")
  @Override
  public <V extends Geometry> List<V> getGeometries() {
    if (geometries == null) {
      return Collections.emptyList();
    } else {
      return (List<V>)Arrays.asList(geometries);
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public <V extends Geometry> V getGeometry(final int n) {
    if (geometries == null) {
      return null;
    } else {
      return (V)geometries[n];
    }
  }

  @Override
  public double getLength() {
    double sum = 0.0;
    for (final Geometry geometry : geometries()) {
      sum += geometry.getLength();
    }
    return sum;
  }

  @Override
  public int getNumGeometries() {
    if (geometries == null) {
      return 0;
    } else {
      return geometries.length;
    }
  }

  @Override
  public int getVertexCount() {
    int numPoints = 0;
    for (final Geometry geometry : geometries()) {
      numPoints += geometry.getVertexCount();
    }
    return numPoints;
  }

  @Override
  public boolean isEmpty() {
    if (getNumGeometries() == 0) {
      return true;
    } else {
      for (final Geometry geometry : geometries()) {
        if (!geometry.isEmpty()) {
          return false;
        }
      }
      return true;
    }
  }

  @Override
  public GeometryCollection normalize() {
    final List<Geometry> geometries = new ArrayList<>();
    for (final Geometry part : geometries()) {
      final Geometry normalizedPart = part.normalize();
      geometries.add(normalizedPart);
    }
    Collections.sort(geometries);
    final GeometryFactory geometryFactory = getGeometryFactory();
    final GeometryCollection normalizedGeometry = geometryFactory.createGeometryCollection(geometries);
    return normalizedGeometry;
  }

  /**
   * Creates a {@link GeometryCollection} with
   * every component reversed.
   * The order of the components in the collection are not reversed.
   *
   * @return a {@link GeometryCollection} in the reverse order
   */
  @Override
  public GeometryCollection reverse() {
    final List<Geometry> revGeoms = new ArrayList<>();
    for (final Geometry geometry : geometries()) {
      if (!geometry.isEmpty()) {
        final Geometry reverse = geometry.reverse();
        revGeoms.add(reverse);
      }
    }
    final GeometryFactory geometryFactory = getGeometryFactory();
    return geometryFactory.createGeometryCollection(revGeoms);
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
