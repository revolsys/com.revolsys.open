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
package com.revolsys.geometry.geomgraph.index;

import com.revolsys.geometry.geomgraph.Edge;

/**
 * @version 1.7
 */
public class SweepLineSegment {

  private final Edge edge;

  private final int ptIndex;

  public SweepLineSegment(final Edge edge, final int ptIndex) {
    this.edge = edge;
    this.ptIndex = ptIndex;
  }

  public void computeIntersections(final SweepLineSegment ss, final SegmentIntersector si) {
    si.addIntersections(this.edge, this.ptIndex, ss.edge, ss.ptIndex);
  }

  public double getMaxX() {
    final double x1 = this.edge.getPoint(this.ptIndex).getX();
    final double x2 = this.edge.getPoint(this.ptIndex + 1).getX();
    return x1 > x2 ? x1 : x2;
  }

  public double getMinX() {
    final double x1 = this.edge.getPoint(this.ptIndex).getX();
    final double x2 = this.edge.getPoint(this.ptIndex + 1).getX();
    return x1 < x2 ? x1 : x2;
  }

}
