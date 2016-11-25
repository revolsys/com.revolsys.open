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
import com.revolsys.geometry.model.LinearRing;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.Polygon;
import com.revolsys.geometry.model.Polygonal;
import com.revolsys.geometry.model.impl.AbstractPolygon;
import com.revolsys.geometry.model.vertex.Vertex;
import com.revolsys.geometry.noding.FastSegmentSetIntersectionFinder;
import com.revolsys.geometry.noding.NodedSegmentString;
import com.revolsys.geometry.noding.SegmentStringUtil;
import com.revolsys.geometry.operation.predicate.RectangleIntersects;

/**
 * A prepared version for {@link Polygonal} geometries.
 * This class supports both {@link Polygon}.
 * <p>
 * This class does <b>not</b> support MultiPolygons which are non-valid
 * (e.g. with overlapping elements).
 * <p>
 * Instances of this class are thread-safe and immutable.
 *
 * @author mbdavis
 *
 */
public class PreparedPolygon extends AbstractPolygon {
  private static final long serialVersionUID = 1L;

  private final boolean isRectangle;

  private PointOnGeometryLocator pia = null;

  private final Polygon polygon;

  // create these lazily, since they are expensive
  private FastSegmentSetIntersectionFinder segIntFinder = null;

  public PreparedPolygon(final Polygon polygon) {
    this.polygon = polygon;
    this.isRectangle = polygon.isRectangle();
  }

  @Override
  public boolean contains(final Geometry g) {
    if (envelopeCovers(g)) {
      if (this.isRectangle) {
        final BoundingBox boundingBox = this.polygon.getBoundingBox();
        return boundingBox.containsSFS(g);
      } else {
        final PreparedPolygonContains contains = new PreparedPolygonContains(this, this.polygon);
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
      return new PreparedPolygonCovers(this, this.polygon).covers(geometry);
    }
  }

  @Override
  public BoundingBox getBoundingBox() {
    return this.polygon.getBoundingBox();
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
        SegmentStringUtil.extractSegmentStrings(getPolygon()));
    }
    return this.segIntFinder;
  }

  public synchronized PointOnGeometryLocator getPointLocator() {
    if (this.pia == null) {
      this.pia = new IndexedPointInAreaLocator(getPolygon());
    }

    return this.pia;
  }

  public Polygon getPolygon() {
    return this.polygon;
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

  @Override
  public LinearRing getRing(final int ringIndex) {
    return this.polygon.getRing(ringIndex);
  }

  @Override
  public int getRingCount() {
    return this.polygon.getRingCount();
  }

  @Override
  public List<LinearRing> getRings() {
    return this.polygon.getRings();
  }

  @Override
  public boolean intersects(final Geometry geometry) {
    if (envelopesIntersect(geometry)) {
      if (this.isRectangle) {
        return RectangleIntersects.intersects(getPolygon(), geometry);
      } else {
        final PointOnGeometryLocator pointLocator = getPointLocator();
        /**
         * Do point-in-poly tests first, since they are cheaper and may result in a
         * quick positive result.
         *
         * If a point of any test components lie in target, result is true
         */
        final boolean isInPrepGeomArea = AbstractPreparedPolygonContains
          .isAnyTestComponentInTarget(pointLocator, geometry);
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
          final List<NodedSegmentString> lineSegStr = SegmentStringUtil
            .extractSegmentStrings(geometry);
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

  @SuppressWarnings("deprecation")
  @Override
  public Polygon newPolygon(final LinearRing... rings) {
    final Polygon polygon = super.newPolygon(rings);
    return polygon.prepare();
  }

  @Override
  public Polygon prepare() {
    return this;
  }
}
