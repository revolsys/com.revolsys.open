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
package com.revolsys.geometry.index.chain;

import java.util.ArrayList;
import java.util.List;

import com.revolsys.geometry.geomgraph.Quadrant;
import com.revolsys.geometry.model.LineString;

/**
 * Constructs {@link MonotoneChain}s
 * for sequences of {@link Coordinates}s.
 *
 * @version 1.7
 */
public class MonotoneChainBuilder {

  /**
   * Finds the index of the last point in a monotone chain
   * starting at a given point.
   * Any repeated points (0-length segments) will be included
   * in the monotone chain returned.
   *
   * @return the index of the last point in the monotone chain
   * starting at <code>start</code>.
   */
  private static int findChainEnd(final LineString points, final int start) {
    int safeStart = start;
    // skip any zero-length segments at the start of the sequence
    // (since they cannot be used to establish a quadrant)
    final int size = points.getVertexCount();
    double startX1 = points.getX(safeStart);
    double startY1 = points.getY(safeStart);
    double startX2 = points.getX(safeStart + 1);
    double startY2 = points.getY(safeStart + 1);
    while (safeStart < size - 1 && startX1 == startX2 && startY1 == startY2) {
      startX1 = startX2;
      startY1 = startY2;
      safeStart++;
      startX2 = points.getX(safeStart + 1);
      startY2 = points.getY(safeStart + 1);
    }
    // check if there are NO non-zero-length segments
    if (safeStart >= size - 1) {
      return size - 1;
    }
    // determine overall quadrant for chain (which is the starting quadrant)
    final int chainQuad = Quadrant.quadrant(startX1, startY1, startX2, startY2);
    int last = start + 1;
    if (last < size) {
      double lastX1 = points.getX(last - 1);
      double lastY1 = points.getY(last - 1);
      while (last < size) {
        final double lastX2 = points.getX(last);
        final double lastY2 = points.getY(last);
        // skip zero-length segments, but include them in the chain
        if (!(lastX1 == lastX2 && lastY1 == lastY2)) {
          // compute quadrant for next possible segment in chain
          final int quad = Quadrant.quadrant(lastX1, lastY1, lastX2, lastY2);
          if (quad != chainQuad) {
            break;
          }
        }
        last++;
        lastX1 = lastX2;
        lastY1 = lastY2;
      }
    }
    return last - 1;
  }

  public static List<MonotoneChain> getChains(final LineString pts) {
    return getChains(pts, null);
  }

  /**
   * Return a list of the {@link MonotoneChain}s
   * for the given list of coordinates.
   */
  public static List<MonotoneChain> getChains(final LineString points, final Object context) {
    final List<MonotoneChain> mcList = new ArrayList<>();
    final List<Integer> indices = getChainStartIndices(points);
    int startIndex = indices.get(0);
    for (int i = 1; i < indices.size(); i++) {
      final int endIndex = indices.get(i);
      final MonotoneChain chain = new MonotoneChain(points, startIndex, endIndex, context);
      mcList.add(chain);
      startIndex = endIndex;
    }
    return mcList;
  }

  /**
   * Return an array containing lists of start/end indexes of the monotone chains
   * for the given list of coordinates.
   * The last entry in the array points to the end point of the point array,
   * for use as a sentinel.
   */
  public static List<Integer> getChainStartIndices(final LineString points) {
    // find the startpoint (and endpoints) of all monotone chains in this edge
    int start = 0;
    final List<Integer> startIndexList = new ArrayList<>();
    startIndexList.add(start);
    do {
      final int last = findChainEnd(points, start);
      startIndexList.add(last);
      start = last;
    } while (start < points.getVertexCount() - 1);
    // copy list to an array of ints, for efficiency
    return startIndexList;
  }

  public MonotoneChainBuilder() {
  }
}
