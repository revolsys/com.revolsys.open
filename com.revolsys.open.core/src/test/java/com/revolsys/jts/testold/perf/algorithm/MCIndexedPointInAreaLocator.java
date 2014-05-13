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
package com.revolsys.jts.testold.perf.algorithm;

import java.util.Iterator;
import java.util.List;

import com.revolsys.jts.algorithm.RayCrossingCounter;
import com.revolsys.jts.algorithm.locate.PointOnGeometryLocator;
import com.revolsys.jts.geom.BoundingBox;
import com.revolsys.jts.geom.Point;
import com.revolsys.jts.geom.CoordinatesList;
import com.revolsys.jts.geom.Envelope;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.LineSegment;
import com.revolsys.jts.geom.LineString;
import com.revolsys.jts.geom.Location;
import com.revolsys.jts.geom.Polygonal;
import com.revolsys.jts.index.SpatialIndex;
import com.revolsys.jts.index.chain.MonotoneChain;
import com.revolsys.jts.index.chain.MonotoneChainBuilder;
import com.revolsys.jts.index.chain.MonotoneChainSelectAction;
import com.revolsys.jts.index.strtree.STRtree;
import com.revolsys.jts.noding.BasicSegmentString;
import com.revolsys.jts.noding.SegmentString;

class MCIndexedGeometry {
  private final SpatialIndex index = new STRtree();

  public MCIndexedGeometry(final Geometry geom) {
    init(geom);
  }

  private void addLine(final CoordinatesList points) {
    final SegmentString segStr = new BasicSegmentString(points, null);
    final List<MonotoneChain> segChains = MonotoneChainBuilder.getChains(
      segStr.getPoints(), segStr);
    for (final MonotoneChain mc : segChains) {
      this.index.insert(mc.getEnvelope(), mc);
    }
  }

  private void init(final Geometry geom) {
    final List<LineString> lines = geom.getGeometryComponents(LineString.class);
    for (final LineString line : lines) {
      final CoordinatesList points = line.getCoordinatesList();
      addLine(points);
    }
  }

  public List query(final Envelope searchEnv) {
    return this.index.query(searchEnv);
  }
}

/**
 * Determines the location of {@link Coordinates}s relative to
 * a {@link Polygonal} geometry, using indexing for efficiency.
 * This algorithm is suitable for use in cases where
 * many points will be tested against a given area.
 * 
 * @author Martin Davis
 *
 */
public class MCIndexedPointInAreaLocator implements PointOnGeometryLocator {
  static class MCSegmentCounter extends MonotoneChainSelectAction {
    RayCrossingCounter rcc;

    public MCSegmentCounter(final RayCrossingCounter rcc) {
      this.rcc = rcc;
    }

    @Override
    public void select(final LineSegment ls) {
      this.rcc.countSegment(ls.getCoordinate(0), ls.getCoordinate(1));
    }
  }

  private MCIndexedGeometry index;

  private final double maxXExtent;

  public MCIndexedPointInAreaLocator(final Geometry g) {
    if (!(g instanceof Polygonal)) {
      throw new IllegalArgumentException("Argument must be Polygonal");
    }
    buildIndex(g);
    final BoundingBox env = g.getBoundingBox();
    this.maxXExtent = env.getMaxX() + 1.0;
  }

  private void buildIndex(final Geometry g) {
    this.index = new MCIndexedGeometry(g);
  }

  private void countSegs(final RayCrossingCounter rcc,
    final BoundingBox rayEnv, final List monoChains,
    final MCSegmentCounter mcSegCounter) {
    for (final Iterator i = monoChains.iterator(); i.hasNext();) {
      final MonotoneChain mc = (MonotoneChain)i.next();
      mc.select(rayEnv, mcSegCounter);
      // short-circuit if possible
      if (rcc.isOnSegment()) {
        return;
      }
    }
  }

  /**
   * Determines the {@link Location} of a point in an areal {@link Geometry}.
   * 
   * @param p the point to test
   * @return the location of the point in the geometry  
   */
  @Override
  public Location locate(final Point p) {
    final RayCrossingCounter rcc = new RayCrossingCounter(p);
    final MCSegmentCounter mcSegCounter = new MCSegmentCounter(rcc);
    final Envelope rayEnv = new Envelope(2, p.getX(), p.getY(),
      this.maxXExtent, p.getY());
    final List mcs = this.index.query(rayEnv);
    countSegs(rcc, rayEnv, mcs, mcSegCounter);

    return rcc.getLocation();
  }

}
