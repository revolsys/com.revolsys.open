package com.revolsys.gis.model.geometry.operation.chain;

import java.util.ArrayList;
import java.util.List;

import com.revolsys.gis.model.geometry.operation.geomgraph.Quadrant;
import com.revolsys.jts.geom.Coordinates;
import com.revolsys.jts.geom.CoordinatesList;

/**
 * Constructs {@link MonotoneChain}s for sequences of {@link Coordinates}s.
 * 
 * @version 1.7
 */
public class MonotoneChainBuilder {

  /**
   * Finds the index of the last point in a monotone chain starting at a given
   * point. Any repeated points (0-length segments) will be included in the
   * monotone chain returned.
   * 
   * @return the index of the last point in the monotone chain starting at
   *         <code>start</code>.
   */
  private static int findChainEnd(final CoordinatesList pts, final int start) {
    int safeStart = start;
    // skip any zero-length segments at the start of the sequence
    // (since they cannot be used to establish a quadrant)
    while (safeStart < pts.size() - 1
      && pts.equal(safeStart, pts, safeStart + 1, 2)) {
      safeStart++;
    }
    // check if there are NO non-zero-length segments
    if (safeStart >= pts.size() - 1) {
      return pts.size() - 1;
    }
    // determine overall quadrant for chain (which is the starting quadrant)
    final int chainQuad = Quadrant.quadrant(pts.get(safeStart),
      pts.get(safeStart + 1));
    int last = start + 1;
    while (last < pts.size()) {
      // skip zero-length segments, but include them in the chain
      if (!pts.equal(last - 1, pts, last, 2)) {
        // compute quadrant for next possible segment in chain
        final int quad = Quadrant.quadrant(pts.get(last - 1), pts.get(last));
        if (quad != chainQuad) {
          break;
        }
      }
      last++;
    }
    return last - 1;
  }

  public static List<MonotoneChain> getChains(final CoordinatesList pts) {
    return getChains(pts, null);
  }

  /**
   * Return a list of the {@link MonotoneChain}s for the given list of
   * coordinates.
   */
  public static List<MonotoneChain> getChains(final CoordinatesList pts,
    final Object context) {
    final List<MonotoneChain> mcList = new ArrayList<MonotoneChain>();
    final int[] startIndex = getChainStartIndices(pts);
    for (int i = 0; i < startIndex.length - 1; i++) {
      final MonotoneChain mc = new MonotoneChain(pts, startIndex[i],
        startIndex[i + 1], context);
      mcList.add(mc);
    }
    return mcList;
  }

  /**
   * Return an array containing lists of start/end indexes of the monotone
   * chains for the given list of coordinates. The last entry in the array
   * points to the end point of the point array, for use as a sentinel.
   */
  public static int[] getChainStartIndices(final CoordinatesList pts) {
    // find the startpoint (and endpoints) of all monotone chains in this edge
    int start = 0;
    final List<Integer> startIndexList = new ArrayList<Integer>();
    startIndexList.add(start);
    do {
      final int last = findChainEnd(pts, start);
      startIndexList.add(last);
      start = last;
    } while (start < pts.size() - 1);
    // copy list to an array of ints, for efficiency
    final int[] startIndex = toIntArray(startIndexList);
    return startIndex;
  }

  public static int[] toIntArray(final List<Integer> list) {
    final int[] array = new int[list.size()];
    for (int i = 0; i < array.length; i++) {
      array[i] = list.get(i);
    }
    return array;
  }

  public MonotoneChainBuilder() {
  }
}
