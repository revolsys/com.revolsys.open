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
package com.revolsys.geometry.noding;

import java.io.PrintStream;

import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.impl.PointDouble;

/**
 * Represents an intersection point between two {@link SegmentString}s.
 *
 * @version 1.7
 */
public class SegmentNode implements Comparable {
  public final Point coord; // the point of intersection

  private final boolean isInterior;

  // the parent edge

  public final int segmentIndex; // the index of the containing line segment in

  private final int segmentOctant;

  public SegmentNode(final NodedSegmentString segString, final Point coord, final int segmentIndex,
    final int segmentOctant) {
    this.coord = new PointDouble(coord);
    this.segmentIndex = segmentIndex;
    this.segmentOctant = segmentOctant;
    this.isInterior = !coord.equals(2, segString.getCoordinate(segmentIndex));
  }

  /**
   * @return -1 this SegmentNode is located before the argument location;
   * 0 this SegmentNode is at the argument location;
   * 1 this SegmentNode is located after the argument location
   */
  @Override
  public int compareTo(final Object obj) {
    final SegmentNode other = (SegmentNode)obj;

    if (this.segmentIndex < other.segmentIndex) {
      return -1;
    }
    if (this.segmentIndex > other.segmentIndex) {
      return 1;
    }

    if (this.coord.equals(2, other.coord)) {
      return 0;
    }

    return SegmentPointComparator.compare(this.segmentOctant, this.coord, other.coord);
    // return segment.compareNodePosition(this, other);
  }

  /**
   * Gets the {@link Coordinates} giving the location of this node.
   *
   * @return the coordinate of the node
   */
  public Point getCoordinate() {
    return this.coord;
  }

  public boolean isEndPoint(final int maxSegmentIndex) {
    if (this.segmentIndex == 0 && !this.isInterior) {
      return true;
    }
    if (this.segmentIndex == maxSegmentIndex) {
      return true;
    }
    return false;
  }

  public boolean isInterior() {
    return this.isInterior;
  }

  public void print(final PrintStream out) {
    out.print(this.coord);
    out.print(" seg # = " + this.segmentIndex);
  }
}
