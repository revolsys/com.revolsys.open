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

package com.revolsys.jts.operation.overlay.validate;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.revolsys.jts.geom.Coordinate;
import com.revolsys.jts.geom.Point;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.LineString;
import com.revolsys.jts.geom.segment.Segment;

/**
 * Generates points offset by a given distance 
 * from both sides of the midpoint of
 * all segments in a {@link Geometry}.
 * Can be used to generate probe points for
 * determining whether a polygonal overlay result
 * is incorrect.
 * The input geometry may have any orientation for its rings,
 * but {@link #setSidesToGenerate(boolean, boolean)} is
 * only meaningful if the orientation is known.
 *
 * @author Martin Davis
 * @version 1.7
 */
public class OffsetPointGenerator {
  private final Geometry g;

  private boolean doLeft = true;

  private boolean doRight = true;

  public OffsetPointGenerator(final Geometry g) {
    this.g = g;
  }

  /**
   * Generates the two points which are offset from the 
   * midpoint of the segment <tt>(p0, p1)</tt> by the
   * <tt>offsetDistance</tt>.
   * 
   * @param p0 the first point of the segment to offset from
   * @param p1 the second point of the segment to offset from
   */
  private void computeOffsetPoints(final double x1, final double y1,
    final double x2, final double y2, final double offsetDistance,
    final List<Point> offsetPts) {
    final double dx = x2 - x1;
    final double dy = y2 - y1;
    final double len = Math.sqrt(dx * dx + dy * dy);
    // u is the vector that is the length of the offset, in the direction of the
    // segment
    final double ux = offsetDistance * dx / len;
    final double uy = offsetDistance * dy / len;

    final double midX = (x2 + x1) / 2;
    final double midY = (y2 + y1) / 2;

    if (doLeft) {
      final Point offsetLeft = new Coordinate(midX - uy, midY + ux);
      offsetPts.add(offsetLeft);
    }

    if (doRight) {
      final Point offsetRight = new Coordinate(midX + uy, midY - ux);
      offsetPts.add(offsetRight);
    }
  }

  private void extractPoints(final LineString line,
    final double offsetDistance, final List<Point> offsetPts) {
    for (final Segment segment : line.segments()) {
      final double x1 = segment.getX(0);
      final double y1 = segment.getY(0);
      final double x2 = segment.getX(1);
      final double y2 = segment.getY(1);
      computeOffsetPoints(x1, y1, x2, y2, offsetDistance, offsetPts);
    }
  }

  /**
   * Gets the computed offset points.
   *
   * @return List<Point>
   */
  public List getPoints(final double offsetDistance) {
    final List offsetPts = new ArrayList();
    final List lines = g.getGeometryComponents(LineString.class);
    for (final Iterator i = lines.iterator(); i.hasNext();) {
      final LineString line = (LineString)i.next();
      extractPoints(line, offsetDistance, offsetPts);
    }
    // System.out.println(toMultiPoint(offsetPts));
    return offsetPts;
  }

  /**
   * Set the sides on which to generate offset points.
   * 
   * @param doLeft
   * @param doRight
   */
  public void setSidesToGenerate(final boolean doLeft, final boolean doRight) {
    this.doLeft = doLeft;
    this.doRight = doRight;
  }

}
