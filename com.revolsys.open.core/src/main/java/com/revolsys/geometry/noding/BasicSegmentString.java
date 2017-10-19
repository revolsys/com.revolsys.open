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

import com.revolsys.geometry.model.LineString;
import com.revolsys.geometry.model.impl.AbstractDelegatingLineString;

/**
 * Represents a list of contiguous line segments,
 * and supports noding the segments.
 * The line segments are represented by an array of {@link Coordinates}s.
 * Intended to optimize the noding of contiguous segments by
 * reducing the number of allocated objects.
 * SegmentStrings can carry a context object, which is useful
 * for preserving topological or parentage information.
 * All noded substrings are initialized with the same context object.
 *
 * @version 1.7
 */
public class BasicSegmentString extends AbstractDelegatingLineString implements SegmentString {
  private static final long serialVersionUID = 1L;

  private Object data;

  /**
   * Creates a new segment string from a list of vertices.
   *
   * @param line the vertices of the segment string
   * @param data the user-defined data of this segment string (may be null)
   */
  public BasicSegmentString(final LineString line, final Object data) {
    super(line);
    this.data = data;
  }

  @Override
  public SegmentString clone() {
    return this;
  }

  /**
   * Gets the user-defined data for this segment string.
   *
   * @return the user-defined data
   */
  @Override
  public Object getData() {
    return this.data;
  }

  /**
   * Gets the octant of the segment starting at vertex <code>index</code>.
   *
   * @param index the index of the vertex starting the segment.  Must not be
   * the last index in the vertex list
   * @return the octant of the segment at the vertex
   */
  public int getSegmentOctant(final int index) {
    if (index == getVertexCount() - 1) {
      return -1;
    }
    final double x1 = getX(index);
    final double y1 = getY(index);
    final double x2 = getX(index + 1);
    final double y2 = getY(index + 1);
    return Octant.octant(x1, y1, x2, y2);
  }

  /**
   * Sets the user-defined data for this segment string.
   *
   * @param data an Object containing user-defined data
   */
  @Override
  public void setData(final Object data) {
    this.data = data;
  }

  @Override
  public int size() {
    return getVertexCount();
  }

  @Override
  public String toString() {
    return toWkt();
  }
}
