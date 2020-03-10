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
package com.revolsys.core.test.geometry.test.old.perf.algorithm;

import java.util.List;

import com.revolsys.geometry.algorithm.RayCrossingCounter;
import com.revolsys.geometry.algorithm.locate.PointOnGeometryLocator;
import com.revolsys.geometry.index.chain.MonotoneChain;
import com.revolsys.geometry.index.chain.MonotoneChainSelectAction;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.LineString;
import com.revolsys.geometry.model.Location;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.Polygonal;
import com.revolsys.geometry.model.impl.BoundingBoxDoubleXY;
import com.revolsys.geometry.noding.BasicSegmentString;
import com.revolsys.geometry.noding.MonotoneChainIndex;
import com.revolsys.geometry.noding.SegmentString;

class MCIndexedGeometry {
  private final MonotoneChainIndex index = new MonotoneChainIndex();

  public MCIndexedGeometry(final Geometry geom) {
    init(geom);
  }

  private void addLine(final LineString points) {
    final SegmentString segStr = new BasicSegmentString(points, null);
    final MonotoneChain[] chains = MonotoneChain.getChainsArray(segStr.getLineString(), segStr);
    for (final MonotoneChain chain : chains) {
      this.index.insertItem(chain);
    }
  }

  private void init(final Geometry geom) {
    final List<LineString> lines = geom.getGeometryComponents(LineString.class);
    for (final LineString line : lines) {
      final LineString points = line;
      addLine(points);
    }
  }

  public List<MonotoneChain> query(final BoundingBox boundingBox) {
    return this.index.getItems(boundingBox);
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

  private final MCIndexedGeometry index;

  private final double maxXExtent;

  public MCIndexedPointInAreaLocator(final Geometry geometry) {
    if (!(geometry instanceof Polygonal)) {
      throw new IllegalArgumentException("Argument must be Polygonal");
    }
    this.index = new MCIndexedGeometry(geometry);
    final BoundingBox boundingBox = geometry.getBoundingBox();
    this.maxXExtent = boundingBox.getMaxX() + 1.0;
  }

  @Override
  public Location locate(final double x, final double y) {
    final BoundingBox boundingBox = new BoundingBoxDoubleXY(x, y, this.maxXExtent, y);
    final List<MonotoneChain> mcs = this.index.query(boundingBox);
    if (mcs.isEmpty()) {
      return Location.EXTERIOR;
    } else {
      final RayCrossingCounter rcc = new RayCrossingCounter(x, y);
      final MonotoneChainSelectAction action = (chain, startIndex) -> {
        final LineString line = chain.getLine();
        final double x1 = line.getX(startIndex);
        final double y1 = line.getY(startIndex);
        final double x2 = line.getX(startIndex + 1);
        final double y2 = line.getY(startIndex + 1);
        rcc.countSegment(x1, y1, x2, y2);
      };
      for (final MonotoneChain chain : mcs) {
        chain.select(boundingBox, action);
        // short-circuit if possible
        if (rcc.isOnSegment()) {
          return Location.BOUNDARY;
        }
      }

      return rcc.getLocation();
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
    final double x = p.getX();
    final double y = p.getY();
    return locate(x, y);
  }

}
