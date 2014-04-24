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

import com.revolsys.gis.data.model.types.DataType;
import com.revolsys.gis.data.model.types.DataTypes;
import com.revolsys.jts.geom.Coordinates;
import com.revolsys.jts.geom.Dimension;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.jts.geom.MultiPoint;
import com.revolsys.jts.geom.Point;
import com.revolsys.jts.geom.vertex.MultiPointVertexIterable;
import com.revolsys.jts.geom.vertex.Vertex;

/**
 * Models a collection of {@link Point}s.
 * <p>
 * Any collection of Points is a valid MultiPoint.
 *
 *@version 1.7
 */
public class MultiPointImpl extends GeometryCollectionImpl implements
  MultiPoint {

  private static final long serialVersionUID = -8048474874175355449L;

  public MultiPointImpl(final GeometryFactory geometryFactory) {
    super(geometryFactory, null);
  }

  /**
   *@param  points          the <code>Point</code>s for this <code>MultiPoint</code>
   *      , or <code>null</code> or an empty array to create the empty geometry.
   *      Elements may be empty <code>Point</code>s, but not <code>null</code>s.
   */
  public MultiPointImpl(final GeometryFactory geometryFactory,
    final Point[] points) {
    super(geometryFactory, points);
  }

  @Override
  public boolean equalsExact(final Geometry other, final double tolerance) {
    if (!isEquivalentClass(other)) {
      return false;
    }
    return super.equalsExact(other, tolerance);
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
    return getGeometryFactory().geometryCollection();
  }

  @Override
  public int getBoundaryDimension() {
    return Dimension.FALSE;
  }

  /**
   *  Returns the <code>Coordinate</code> at the given position.
   *
   *@param  n  the partIndex of the <code>Coordinate</code> to retrieve, beginning
   *      at 0
   *@return    the <code>n</code>th <code>Coordinate</code>
   */
  protected Coordinates getCoordinate(final int n) {
    return getPoint(n);
  }

  @Override
  public double getCoordinate(final int partIndex, final int vertexIndex) {
    final Point point = getPoint(partIndex);
    return point.getCoordinate(vertexIndex);
  }

  @Override
  public DataType getDataType() {
    return DataTypes.MULTI_POINT;
  }

  @Override
  public int getDimension() {
    return 0;
  }

  @Override
  public Point getPoint(final int partIndex) {
    return (Point)getGeometry(partIndex);
  }

  /**
   * @author Paul Austin <paul.austin@revolsys.com>
   */
  @Override
  @SuppressWarnings({
    "unchecked", "rawtypes"
  })
  public <V extends Point> List<V> getPoints() {
    return (List)super.getGeometries();
  }

  @Override
  public boolean isValid() {
    return true;
  }

  @Override
  public MultiPoint normalize() {
    if (isEmpty()) {
      return this;
    } else {
      final List<Point> geometries = new ArrayList<>();
      for (final Geometry part : geometries()) {
        final Point normalizedPart = (Point)part.normalize();
        geometries.add(normalizedPart);
      }
      Collections.sort(geometries);
      final GeometryFactory geometryFactory = getGeometryFactory();
      final MultiPoint normalizedGeometry = geometryFactory.multiPoint(geometries);
      return normalizedGeometry;
    }
  }

  /**
   * @author Paul Austin <paul.austin@revolsys.com>
   */
  @Override
  public Iterable<Vertex> vertices() {
    return new MultiPointVertexIterable(this);
  }

}
