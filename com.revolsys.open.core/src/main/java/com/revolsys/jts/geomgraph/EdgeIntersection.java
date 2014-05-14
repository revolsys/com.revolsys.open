
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
package com.revolsys.jts.geomgraph;

import java.io.PrintStream;

import com.revolsys.jts.geom.Point;
import com.revolsys.jts.geom.impl.PointDouble;

/**
 * Represents a point on an
 * edge which intersects with another edge.
 * <p>
 * The intersection may either be a single point, or a line segment
 * (in which case this point is the start of the line segment)
 * The intersection point must be precise.
 *
 * @version 1.7
 */
public class EdgeIntersection implements Comparable {

  public Point coord; // the point of intersection

  public int segmentIndex; // the index of the containing line segment in the
                           // parent edge

  public double dist; // the edge distance of this point along the containing
                      // line segment

  public EdgeIntersection(final Point coord, final int segmentIndex,
    final double dist) {
    this.coord = new PointDouble(coord);
    this.segmentIndex = segmentIndex;
    this.dist = dist;
  }

  /**
   * @return -1 this EdgeIntersection is located before the argument location
   * @return 0 this EdgeIntersection is at the argument location
   * @return 1 this EdgeIntersection is located after the argument location
   */
  public int compare(final int segmentIndex, final double dist) {
    if (this.segmentIndex < segmentIndex) {
      return -1;
    }
    if (this.segmentIndex > segmentIndex) {
      return 1;
    }
    if (this.dist < dist) {
      return -1;
    }
    if (this.dist > dist) {
      return 1;
    }
    return 0;
  }

  @Override
  public int compareTo(final Object obj) {
    final EdgeIntersection other = (EdgeIntersection)obj;
    return compare(other.segmentIndex, other.dist);
  }

  public Point getCoordinate() {
    return coord;
  }

  public double getDistance() {
    return dist;
  }

  public int getSegmentIndex() {
    return segmentIndex;
  }

  public boolean isEndPoint(final int maxSegmentIndex) {
    if (segmentIndex == 0 && dist == 0.0) {
      return true;
    }
    if (segmentIndex == maxSegmentIndex) {
      return true;
    }
    return false;
  }

  public void print(final PrintStream out) {
    out.print(coord);
    out.print(" seg # = " + segmentIndex);
    out.println(" dist = " + dist);
  }

  @Override
  public String toString() {
    return coord + " seg # = " + segmentIndex + " dist = " + dist;
  }
}
