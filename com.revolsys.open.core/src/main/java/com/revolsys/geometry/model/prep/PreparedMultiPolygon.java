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
import java.util.List;

import com.revolsys.geometry.algorithm.locate.IndexedPointInAreaLocator;
import com.revolsys.geometry.algorithm.locate.PointOnGeometryLocator;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.MultiPolygon;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.Polygonal;
import com.revolsys.geometry.model.vertex.Vertex;
import com.revolsys.geometry.noding.FastSegmentSetIntersectionFinder;
import com.revolsys.geometry.noding.NodedSegmentString;
import com.revolsys.geometry.noding.SegmentStringUtil;
import com.revolsys.geometry.operation.predicate.RectangleIntersects;
import com.revolsys.util.Exceptions;

/**
 * A prepared version for {@link MultiPolygonal} geometries.
 * This class supports both {@link Polygonal}s.
 * <p>
 * This class does <b>not</b> support MultiMultiPolygons which are non-valid
 * (e.g. with overlapping elements).
 * <p>
 * Instances of this class are thread-safe and immutable.
 *
 * @author mbdavis
 *
 */
public class PreparedMultiPolygon implements MultiPolygon {
  private static final long serialVersionUID = 1L;

  private final boolean isRectangle;

  private final Polygonal polygonal;

  private PointOnGeometryLocator pia = null;

  // create these lazily, since they are expensive
  private FastSegmentSetIntersectionFinder segIntFinder = null;

  public PreparedMultiPolygon(final Polygonal polygon) {
    this.polygonal = polygon;
    this.isRectangle = polygon.isRectangle();
  }

  /**
   * Creates and returns a full copy of this  object.
   * (including all coordinates contained by it).
   *
   * @return a clone of this instance
   */
  @Override
  public Polygonal clone() {
    try {
      return (Polygonal)super.clone();
    } catch (final CloneNotSupportedException e) {
      throw Exceptions.wrap(e);
    }
  }

  @Override
  public boolean contains(final Geometry g) {
    if (envelopeCovers(g)) {
      if (this.isRectangle) {
        final BoundingBox boundingBox = this.polygonal.getBoundingBox();
        return boundingBox.containsSFS(g);
      } else {
        final PreparedPolygonContains contains = new PreparedPolygonContains(this, getPolygonal());
        return contains.contains(g);
      }
    } else {
      return false;
    }
  }

  @Override
  public boolean containsProperly(final Geometry geometry) {
    // short-circuit test
    if (envelopeCovers(geometry)) {
      /**
       * Do point-in-poly tests first, since they are cheaper and may result
       * in a quick negative result.
       *
       * If a point of any test components does not lie in the target interior, result is false
       */
      final boolean isAllInPrepGeomAreaInterior = AbstractPreparedPolygonContains
        .isAllTestComponentsInTargetInterior(getPointLocator(), geometry);
      if (!isAllInPrepGeomAreaInterior) {
        return false;
      }

      /**
       * If any segments intersect, result is false.
       */
      final List<NodedSegmentString> lineSegStr = SegmentStringUtil.extractSegmentStrings(geometry);
      final boolean segsIntersect = getIntersectionFinder().intersects(lineSegStr);
      if (segsIntersect) {
        return false;
      }

      /**
       * Given that no segments intersect, if any vertex of the target
       * is contained in some test component.
       * the test is NOT properly contained.
       */
      if (geometry instanceof Polygonal) {
        // TODO: generalize this to handle GeometryCollections
        final boolean isTargetGeomInTestArea = AbstractPreparedPolygonContains
          .isAnyTargetComponentInAreaTest(geometry, this);
        if (isTargetGeomInTestArea) {
          return false;
        }
      }

      return true;
    } else {
      return false;
    }
  }

  @Override
  public boolean covers(final Geometry geometry) {
    if (!envelopeCovers(geometry)) {
      return false;
    } else if (this.isRectangle) {
      return true;
    } else {
      return new PreparedPolygonCovers(this, this.polygonal).covers(geometry);
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
    return this.polygonal.getBoundingBox();
  }

  @Override
  public <V extends Geometry> List<V> getGeometries() {
    return this.polygonal.getGeometries();
  }

  @Override
  public <V extends Geometry> V getGeometry(final int partIndex) {
    return this.polygonal.getGeometry(partIndex);
  }

  @Override
  public int getGeometryCount() {
    return this.polygonal.getGeometryCount();
  }

  @Override
  public GeometryFactory getGeometryFactory() {
    return this.polygonal.getGeometryFactory();
  }

  /**
   * Gets the indexed intersection finder for this geometry.
   *
   * @return the intersection finder
   */
  public synchronized FastSegmentSetIntersectionFinder getIntersectionFinder() {
    /**
     * MD - Another option would be to use a simple scan for
     * segment testing for small geometries.
     * However, testing indicates that there is no particular advantage
     * to this approach.
     */
    if (this.segIntFinder == null) {
      this.segIntFinder = new FastSegmentSetIntersectionFinder(
        SegmentStringUtil.extractSegmentStrings(getPolygonal()));
    }
    return this.segIntFinder;
  }

  public synchronized PointOnGeometryLocator getPointLocator() {
    if (this.pia == null) {
      this.pia = new IndexedPointInAreaLocator(getPolygonal());
    }

    return this.pia;
  }

  public Polygonal getPolygonal() {
    return this.polygonal;
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
    final List<Point> points = new ArrayList<>();
    for (final Vertex vertex : vertices()) {
      points.add(vertex.newPoint2D());
    }
    return points;
  }

  /**
   * Gets a hash code for the Geometry.
   *
   * @return an integer value suitable for use as a hashcode
   */
  @Override
  public int hashCode() {
    return this.polygonal.hashCode();
  }

  @Override
  public boolean intersects(final Geometry geometry) {
    if (envelopesIntersect(geometry)) {
      if (this.isRectangle) {
        return RectangleIntersects.intersects(getPolygonal().getPolygon(0), geometry);
      } else {
        /**
         * Do point-in-poly tests first, since they are cheaper and may result in a
         * quick positive result.
         *
         * If a point of any test components lie in target, result is true
         */
        final boolean isInPrepGeomArea = AbstractPreparedPolygonContains
          .isAnyTestComponentInTarget(getPointLocator(), geometry);
        if (isInPrepGeomArea) {
          return true;
        }
        /**
         * If input contains only points, then at
         * this point it is known that none of them are contained in the target
         */
        if (geometry.getDimension() == 0) {
          return false;
        } else {
          /**
           * If any segments intersect, result is true
           */
          final List lineSegStr = SegmentStringUtil.extractSegmentStrings(geometry);
          // only request intersection finder if there are segments
          // (i.e. NOT for point inputs)
          if (lineSegStr.size() > 0) {
            final boolean segsIntersect = getIntersectionFinder().intersects(lineSegStr);
            if (segsIntersect) {
              return true;
            }
          }

          /**
           * If the test has dimension = 2 as well, it is necessary to test for proper
           * inclusion of the target. Since no segments intersect, it is sufficient to
           * test representative points.
           */
          if (geometry.getDimension() == 2) {
            // TODO: generalize this to handle GeometryCollections
            final boolean isPrepGeomInArea = AbstractPreparedPolygonContains
              .isAnyTargetComponentInAreaTest(geometry, this);
            if (isPrepGeomInArea) {
              return true;
            }
          }

          return false;

        }
      }
    } else {
      return false;
    }
  }

  @Override
  public Polygonal prepare() {
    return this;
  }

  @Override
  public String toString() {
    return toEwkt();
  }
}
