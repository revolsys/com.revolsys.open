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

import java.util.List;

import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryCollection;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.MultiPoint;
import com.revolsys.util.WrappedException;

public class PreparedMultiPoint implements MultiPoint {
  private static final long serialVersionUID = 1L;

  private final MultiPoint multiPoint;

  public PreparedMultiPoint(final MultiPoint multiPoint) {
    this.multiPoint = multiPoint;
  }

  /**
   * Creates and returns a full copy of this {@link GeometryCollection} object.
   * (including all coordinates contained by it).
   *
   * @return a clone of this instance
   */
  @Override
  public MultiPoint clone() {
    try {
      return (MultiPoint)super.clone();
    } catch (final CloneNotSupportedException e) {
      throw new WrappedException(e);
    }
  }

  /**
   * Tests whether this geometry is structurally and numerically equal
   * to a given <code>Object</code>.
   * If the argument <code>Object</code> is not a <code>Geometry</code>,
   * the result is <code>false</code>.
   * Otherwise, the result is computed using
   * {@link #equals(2,Geometry)}.
   * <p>
   * This method is provided to fulfill the Java contract
   * for value-based object equality.
   * In conjunction with {@link #hashCode()}
   * it provides semantics which are most useful
   * for using
   * <code>Geometry</code>s as keys and values in Java collections.
   * <p>
   * Note that to produce the expected result the input geometries
   * should be in normal form.  It is the caller's
   * responsibility to perform this where required
   * (using {@link Geometry#norm()
   * or {@link #normalize()} as appropriate).
   *
   * @param other the Object to compare
   * @return true if this geometry is exactly equal to the argument
   *
   * @see #equals(2,Geometry)
   * @see #hashCode()
   * @see #norm()
   * @see #normalize()
   */
  @Override
  public boolean equals(final Object other) {
    if (other instanceof Geometry) {
      final Geometry geometry = (Geometry)other;
      return equals(2, geometry);
    } else {
      return false;
    }
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
   * Gets a hash code for the Geometry.
   *
   * @return an integer value suitable for use as a hashcode
   */

  @Override
  public int hashCode() {
    return getBoundingBox().hashCode();
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
      return Geometry.isAnyTargetComponentInTest(this, geometry);
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

  @Override
  public String toString() {
    return toWkt();
  }
}
