package com.revolsys.gis.model.geometry.operation.geomgraph.index;

import java.util.ArrayList;
import java.util.List;

import com.revolsys.gis.model.coordinates.Coordinates;
import com.revolsys.gis.model.coordinates.list.CoordinatesList;
import com.revolsys.gis.model.geometry.operation.geomgraph.Quadrant;

/**
 * MonotoneChains are a way of partitioning the segments of an edge to allow for
 * fast searching of intersections. Specifically, a sequence of contiguous line
 * segments is a monotone chain iff all the vectors defined by the oriented
 * segments lies in the same quadrant.
 * <p>
 * Monotone Chains have the following useful properties:
 * <ol>
 * <li>the segments within a monotone chain will never intersect each other
 * <li>the envelope of any contiguous subset of the segments in a monotone chain
 * is simply the envelope of the endpoints of the subset.
 * </ol>
 * Property 1 means that there is no need to test pairs of segments from within
 * the same monotone chain for intersection. Property 2 allows binary search to
 * be used to find the intersection points of two monotone chains. For many
 * types of real-world data, these properties eliminate a large number of
 * segment comparisons, producing substantial speed gains.
 * 
 * @version 1.7
 */
public class MonotoneChainIndexer {

  public static int[] toIntArray(List list) {
    int[] array = new int[list.size()];
    for (int i = 0; i < array.length; i++) {
      array[i] = ((Integer)list.get(i)).intValue();
    }
    return array;
  }

  public MonotoneChainIndexer() {
  }

  public int[] getChainStartIndices(CoordinatesList pts) {
    // find the startpoint (and endpoints) of all monotone chains in this edge
    int start = 0;
    List<Integer> startIndexList = new ArrayList<Integer>();
    startIndexList.add(start);
    do {
      int last = findChainEnd(pts, start);
      startIndexList.add(last);
      start = last;
    } while (start < pts.size() - 1);
    // copy list to an array of ints, for efficiency
    int[] startIndex = toIntArray(startIndexList);
    return startIndex;
  }

  /**
   * @return the index of the last point in the monotone chain
   */
  private int findChainEnd(CoordinatesList pts, int start) {
    // determine quadrant for chain
    int chainQuad = Quadrant.quadrant(pts.get(start), pts.get(start + 1));
    int last = start + 1;
    while (last < pts.size()) {
      // compute quadrant for next possible segment in chain
      int quad = Quadrant.quadrant(pts.get(last - 1), pts.get(last));
      if (quad != chainQuad)
        break;
      last++;
    }
    return last - 1;
  }

}
