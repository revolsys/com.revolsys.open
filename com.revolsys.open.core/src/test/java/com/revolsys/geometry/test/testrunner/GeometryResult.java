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
package com.revolsys.geometry.test.testrunner;

import com.revolsys.geometry.model.Geometry;

/**
 * @version 1.7
 */
public class GeometryResult implements Result {
  private final Geometry geometry;

  public GeometryResult(final Geometry geometry) {
    this.geometry = geometry;
  }

  @Override
  public boolean equals(final Result other, final double tolerance) {
    if (!(other instanceof GeometryResult)) {
      return false;
    }
    final GeometryResult otherGeometryResult = (GeometryResult)other;
    final Geometry otherGeometry = otherGeometryResult.geometry;

    final Geometry thisGeometryClone = this.geometry.normalize();
    final Geometry otherGeometryClone = otherGeometry.normalize();
    return thisGeometryClone.equalsExact(otherGeometryClone, tolerance);
  }

  public Geometry getGeometry() {
    return this.geometry;
  }

  @Override
  public Geometry getResult() {
    return this.geometry;
  }

  @Override
  public String toFormattedString() {
    return this.geometry.toEwkt();
  }

  @Override
  public String toLongString() {
    return this.geometry.toEwkt();
  }

  @Override
  public String toShortString() {
    return this.geometry.getClass().getName();
  }

  @Override
  public String toString() {
    return toLongString();
  }
}
