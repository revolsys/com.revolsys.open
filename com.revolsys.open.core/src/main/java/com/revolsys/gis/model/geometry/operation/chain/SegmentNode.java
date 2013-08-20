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
package com.revolsys.gis.model.geometry.operation.chain;

import java.io.PrintStream;

import com.revolsys.gis.model.coordinates.Coordinates;
import com.revolsys.gis.model.coordinates.DoubleCoordinates;

/**
 * Represents an intersection point between two {@link SegmentString}s.
 * 
 * @version 1.7
 */
public class SegmentNode implements Comparable {
  private final NodedSegmentString segString;

  public final Coordinates coord; // the point of intersection

  public final int segmentIndex; // the index of the containing line segment in
                                 // the parent edge

  private final int segmentOctant;

  private final boolean isInterior;

  public SegmentNode(final NodedSegmentString segString,
    final Coordinates coord, final int segmentIndex, final int segmentOctant) {
    this.segString = segString;
    this.coord = new DoubleCoordinates(coord);
    this.segmentIndex = segmentIndex;
    this.segmentOctant = segmentOctant;
    isInterior = !coord.equals2d(segString.getCoordinate(segmentIndex));
  }

  /**
   * @return -1 this SegmentNode is located before the argument location
   * @return 0 this SegmentNode is at the argument location
   * @return 1 this SegmentNode is located after the argument location
   */
  @Override
  public int compareTo(final Object obj) {
    final SegmentNode other = (SegmentNode)obj;

    if (segmentIndex < other.segmentIndex) {
      return -1;
    }
    if (segmentIndex > other.segmentIndex) {
      return 1;
    }

    if (coord.equals2d(other.coord)) {
      return 0;
    }

    return SegmentPointComparator.compare(segmentOctant, coord, other.coord);
    // return segment.compareNodePosition(this, other);
  }

  public boolean isEndPoint(final int maxSegmentIndex) {
    if (segmentIndex == 0 && !isInterior) {
      return true;
    }
    if (segmentIndex == maxSegmentIndex) {
      return true;
    }
    return false;
  }

  public boolean isInterior() {
    return isInterior;
  }

  public void print(final PrintStream out) {
    out.print(coord);
    out.print(" seg # = " + segmentIndex);
  }
}
