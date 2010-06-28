package com.revolsys.gis.graph.comparator;

import java.util.Comparator;

import com.revolsys.gis.graph.Edge;
import com.revolsys.gis.graph.Node;

/**
 * Compare the distance of the from node of two edges from the point (0, 0).
 * 
 * @author paustin
 */

public class FromNodeDistanceEdgeComparator implements Comparator<Edge<?>> {
  public static final FromNodeDistanceEdgeComparator INSTANCE = new FromNodeDistanceEdgeComparator();

  public int compare(
    Edge<?> edge1,
    Edge<?> edge2) {
    final Node<?> fromNode1 = edge1.getFromNode();
    final Node<?> fromNode2 = edge2.getFromNode();
    return DistanceNodeComparator.INSTANCE.compare(fromNode1, fromNode2);
  }
}
