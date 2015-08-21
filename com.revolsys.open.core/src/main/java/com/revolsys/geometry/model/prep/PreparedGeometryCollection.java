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
package com.revolsys.geometry.model.prep;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryCollection;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.impl.AbstractGeometryCollection;
import com.revolsys.geometry.model.vertex.Vertex;

/**
 * A base class for {@link PreparedGeometry} subclasses.
 * Contains default implementations for methods, which simply delegate
 * to the equivalent {@link Geometry} methods.
 * This class may be used as a "no-op" class for Geometry types
 * which do not have a corresponding {@link PreparedGeometry} implementation.
 *
 * @author Martin Davis
 *
 */
public class PreparedGeometryCollection extends AbstractGeometryCollection {
  /**
   *
   */
  private static final long serialVersionUID = 1L;

  private final GeometryCollection geometryCollection;

  public PreparedGeometryCollection(final GeometryCollection geometryCollection) {
    this.geometryCollection = geometryCollection;
  }

  @Override
  public BoundingBox getBoundingBox() {
    return this.geometryCollection.getBoundingBox();
  }

  @Override
  public <V extends Geometry> List<V> getGeometries() {
    return this.geometryCollection.getGeometries();
  }

  @Override
  public <V extends Geometry> V getGeometry(final int partIndex) {
    return this.geometryCollection.getGeometry(partIndex);
  }

  @Override
  public int getGeometryCount() {
    return this.geometryCollection.getGeometryCount();
  }

  @Override
  public GeometryFactory getGeometryFactory() {
    return this.geometryCollection.getGeometryFactory();
  }

  /**
   * Gets the list of representative points for this geometry.
   * One vertex is included for every component of the geometry
   * (i.e. including one for every ring of polygonal geometries).
   *
   * Do not modify the returned list!
   *
   * @return a List of Coordinate
   */
  public List<Point> getRepresentativePoints() {
    final List<Point> points = new ArrayList<Point>();
    for (final Vertex vertex : vertices()) {
      points.add(vertex.clonePoint());
    }
    return points;
  }

  @SuppressWarnings("unchecked")
  @Override
  public <V extends Geometry> V moveVertex(final Point newPoint, final int... vertexId) {
    if (newPoint == null || newPoint.isEmpty()) {
      return (V)this;
    } else if (vertexId.length > 1) {
      if (isEmpty()) {
        throw new IllegalArgumentException("Cannot move vertex for empty MultiPoint");
      } else {
        final int partIndex = vertexId[0];
        final int partCount = getGeometryCount();
        if (partIndex >= 0 && partIndex < partCount) {
          final GeometryFactory geometryFactory = getGeometryFactory();

          final int[] subId = new int[vertexId.length - 1];
          System.arraycopy(vertexId, 1, subId, 0, subId.length);
          final Geometry geometry = getGeometry(partIndex);
          final Geometry newGeometry = geometry.moveVertex(newPoint, subId);

          final List<Geometry> geometries = new ArrayList<>(getGeometries());
          geometries.set(partIndex, newGeometry);
          return (V)geometryFactory.geometryCollection(geometries);
        } else {
          throw new IllegalArgumentException(
            "Part index must be between 0 and " + partCount + " not " + partIndex);
        }
      }
    } else {
      throw new IllegalArgumentException(
        "Vertex id's for GeometryCollection must have length > 1. " + Arrays.toString(vertexId));
    }
  }

  @Override
  public Geometry prepare() {
    return this;
  }
}
