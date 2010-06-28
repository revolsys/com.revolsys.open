package com.revolsys.gis.graph.comparator;

import java.util.Comparator;

import com.revolsys.gis.graph.Node;
import com.revolsys.gis.model.coordinates.Coordinates;

/**
 * Compare the distance of two nodes from the point (0, 0).
 * 
 * @author paustin
 */
public class DistanceNodeComparator implements Comparator<Node<?>> {
  public static final DistanceNodeComparator INSTANCE = new DistanceNodeComparator();

  public int compare(
    final Node<?> node1,
    final Node<?> node2) {
    final Coordinates point1 = node1.getCoordinates();
    final Coordinates point2 = node2.getCoordinates();
    return point1.compareTo(point2);
  }

}
