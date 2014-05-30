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
package com.revolsys.jts.algorithm.locate;

import java.util.List;

import com.revolsys.collection.Visitor;
import com.revolsys.gis.jts.GeometryProperties;
import com.revolsys.jts.algorithm.RayCrossingCounter;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.jts.geom.LineString;
import com.revolsys.jts.geom.Location;
import com.revolsys.jts.geom.Point;
import com.revolsys.jts.geom.Polygonal;
import com.revolsys.jts.geom.segment.LineSegment;
import com.revolsys.jts.geom.segment.Segment;
import com.revolsys.jts.index.intervalrtree.SortedPackedIntervalRTree;

/**
 * Determines the {@link Location} of {@link Coordinates}s relative to
 * a {@link Polygonal} geometry, using indexing for efficiency.
 * This algorithm is suitable for use in cases where
 * many points will be tested against a given area.
 *
 * Thread-safe and immutable.
 *
 * @author Martin Davis
 *
 */
public class IndexedPointInAreaLocator implements PointOnGeometryLocator {
  public static class IntervalIndexedGeometry {
    private final SortedPackedIntervalRTree<LineSegment> index = new SortedPackedIntervalRTree<>();

    public IntervalIndexedGeometry(final Geometry geom) {
      init(geom);
    }

    private void addLine(final LineString line) {
      for (final Segment segment : line.segments()) {
        final double y1 = segment.getY(0);
        final double y2 = segment.getY(1);
        final double min = Math.min(y1, y2);
        final double max = Math.max(y1, y2);
        this.index.insert(min, max, segment.clone());
      }
    }

    private void init(final Geometry geometry) {
      final List<LineString> lines = geometry.getGeometryComponents(LineString.class);
      for (final LineString line : lines) {
        addLine(line);
      }
    }

    public void query(final double min, final double max,
      final Visitor<LineSegment> visitor) {
      this.index.query(min, max, visitor);
    }
  }

  private static final String KEY = IndexedPointInAreaLocator.class.getName();

  public static IndexedPointInAreaLocator get(final Geometry geometry) {
    IndexedPointInAreaLocator locator = GeometryProperties.getGeometryProperty(
      geometry, KEY);
    if (locator == null) {
      locator = new IndexedPointInAreaLocator(geometry);
      GeometryProperties.setGeometryProperty(geometry, KEY, locator);
    }
    return locator;
  }

  private final IntervalIndexedGeometry index;

  private final Geometry geometry;

  /**
   * Creates a new locator for a given {@link Geometry}
   *
   * @param geometry the Geometry to locate in
   */
  public IndexedPointInAreaLocator(final Geometry geometry) {
    if (!(geometry instanceof Polygonal)) {
      throw new IllegalArgumentException("Argument must be Polygonal");
    }
    this.geometry = geometry;
    this.index = new IntervalIndexedGeometry(geometry);
  }

  public Geometry getGeometry() {
    return this.geometry;
  }

  public GeometryFactory getGeometryFactory() {
    return this.geometry.getGeometryFactory();
  }

  public IntervalIndexedGeometry getIndex() {
    return this.index;
  }

  public Location locate(final double x, final double y) {
    final RayCrossingCounter visitor = new RayCrossingCounter(x, y);
    this.index.query(y, y, visitor);

    return visitor.getLocation();
  }

  /**
   * Determines the {@link Location} of a point in an areal {@link Geometry}.
   *
   * @param p the point to test
   * @return the location of the point in the geometry
   */
  @Override
  public Location locate(final Point point) {
    final double x = point.getX();
    final double y = point.getY();
    return locate(x, y);
  }

}
