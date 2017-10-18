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
import java.util.function.Consumer;

import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.Lineal;
import com.revolsys.geometry.model.MultiLineString;
import com.revolsys.geometry.model.vertex.Vertex;
import com.revolsys.geometry.noding.FastSegmentSetIntersectionFinder;
import com.revolsys.geometry.noding.NodedSegmentString;
import com.revolsys.geometry.noding.SegmentStringUtil;
import com.revolsys.util.Exceptions;

/**
 * A prepared version for {@link Lineal} geometries.
 * <p>
 * Instances of this class are thread-safe.
 *
 * @author mbdavis
 *
 */
public class PreparedMultiLineString implements MultiLineString {
  private static final long serialVersionUID = 1L;

  private final Lineal lineal;

  private FastSegmentSetIntersectionFinder segIntFinder = null;

  public PreparedMultiLineString(final Lineal lineal) {
    this.lineal = lineal;
  }

  /**
   * Creates and returns a full copy of this object.
   * (including all coordinates contained by it).
   *
   * @return a clone of this instance
   */
  @Override
  public Lineal clone() {
    try {
      return (Lineal)super.clone();
    } catch (final CloneNotSupportedException e) {
      throw Exceptions.wrap(e);
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
  public void forEachGeometry(final Consumer<Geometry> action) {
    this.lineal.forEachGeometry(action);
  }

  @Override
  public int getAxisCount() {
    return this.lineal.getAxisCount();
  }

  @Override
  public BoundingBox getBoundingBox() {
    return this.lineal.getBoundingBox();
  }

  @Override
  public <V extends Geometry> List<V> getGeometries() {
    return this.lineal.getGeometries();
  }

  @Override
  public <V extends Geometry> V getGeometry(final int partIndex) {
    return this.lineal.getGeometry(partIndex);
  }

  @Override
  public int getGeometryCount() {
    return this.lineal.getGeometryCount();
  }

  @Override
  public GeometryFactory getGeometryFactory() {
    return this.lineal.getGeometryFactory();
  }

  public synchronized FastSegmentSetIntersectionFinder getIntersectionFinder() {
    /**
     * MD - Another option would be to use a simple scan for
     * segment testing for small geometries.
     * However, testing indicates that there is no particular advantage
     * to this approach.
     */
    if (this.segIntFinder == null) {
      this.segIntFinder = new FastSegmentSetIntersectionFinder(
        SegmentStringUtil.extractSegmentStrings(this.lineal));
    }
    return this.segIntFinder;
  }

  /**
   * Gets a hash code for the Geometry.
   *
   * @return an integer value suitable for use as a hashcode
   */

  @Override
  public int hashCode() {
    return this.lineal.hashCode();
  };

  @Override
  public boolean intersects(final Geometry geometry) {
    if (envelopesIntersect(geometry)) {
      /**
       * If any segments intersect, obviously intersects = true
       */
      final List<NodedSegmentString> lineSegStr = SegmentStringUtil.extractSegmentStrings(geometry);
      // only request intersection finder if there are segments (ie NOT for
      // point
      // inputs)
      if (lineSegStr.size() > 0) {
        final boolean segsIntersect = getIntersectionFinder().intersects(lineSegStr);
        // MD - performance testing
        // boolean segsIntersect = false;
        if (segsIntersect) {
          return true;
        }
      }
      /**
       * For L/L case we are done
       */
      final int dimension = geometry.getDimension();
      if (dimension == 1) {
        return false;
      } else if (dimension == 2 && Geometry.isAnyTargetComponentInTest(this, geometry)) {
        /**
         * For L/A case, need to check for proper inclusion of the target in the test
         */
        return true;
      } else if (dimension == 0) {
        /**
         * For L/P case, need to check if any points lie on line(s)
         */
        return isAnyTestPointInTarget(geometry);
      } else {
        return false;
      }
    } else {
      return false;
    }
  }

  /**
   * Tests whether any representative point of the test Geometry intersects
   * the target geometry.
   * Only handles test geometries which are Punctual (dimension 0)
   *
   * @param geom a Punctual geometry to test
   * @return true if any point of the argument intersects the prepared geometry
   */
  public boolean isAnyTestPointInTarget(final Geometry geometry) {
    /**
     * This could be optimized by using the segment index on the lineal target.
     * However, it seems like the L/P case would be pretty rare in practice.
     */
    for (final Vertex vertex : geometry.vertices()) {
      if (this.lineal.intersects(vertex)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public boolean isEmpty() {
    return this.lineal.isEmpty();
  }

  @Override
  public Lineal prepare() {
    return this;
  }

  @Override
  public String toString() {
    return toEwkt();
  }
}
