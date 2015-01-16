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
package com.revolsys.jts.geom.prep;

import java.util.List;

import com.revolsys.jts.geom.BoundingBox;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.jts.geom.MultiPoint;
import com.revolsys.jts.geom.impl.AbstractMultiPoint;

public class PreparedMultiPoint extends AbstractMultiPoint {
  /**
   *
   */
  private static final long serialVersionUID = 1L;
  private final MultiPoint multiPoint;

  public PreparedMultiPoint(final MultiPoint multiPoint) {
    this.multiPoint = multiPoint;
  }

  @Override
  public BoundingBox getBoundingBox() {
    return this.multiPoint.getBoundingBox();
  }

  @Override
  public <V extends Geometry> List<V> getGeometries() {
    return this.multiPoint.getGeometries();
  }

  @Override
  public <V extends Geometry> V getGeometry(final int partIndex) {
    return this.multiPoint.getGeometry(partIndex);
  }

  @Override
  public int getGeometryCount() {
    return this.multiPoint.getGeometryCount();
  }

  @Override
  public GeometryFactory getGeometryFactory() {
    return this.multiPoint.getGeometryFactory();
  }

  /**
   * Tests whether this multiPoint intersects a {@link Geometry}.
   * <p>
   * The optimization here is that computing topology for the test geometry
   * is avoided.  This can be significant for large geometries.
   */
  @Override
  public boolean intersects(final Geometry geometry) {
    if (envelopesIntersect(geometry)) {
      /**
       * This avoids computing topology for the test geometry
       */
      return isAnyTargetComponentInTest(geometry);
    } else {
      return false;
    }
  }

  @Override
  public boolean isEmpty() {
    return this.multiPoint.isEmpty();
  }

  @Override
  public Geometry prepare() {
    return this;
  }
}
