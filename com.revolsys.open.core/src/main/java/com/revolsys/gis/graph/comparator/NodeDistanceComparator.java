package com.revolsys.gis.graph.comparator;

import java.util.Comparator;

import com.revolsys.gis.graph.Node;
import com.revolsys.gis.model.coordinates.Coordinates;

/**
 * Compare the distance of nodes from a given node.
 * 
 * @author paustin
 */
public class NodeDistanceComparator<T> implements Comparator<Node<T>> {

  private final boolean invert;

  private final Node<T> node;

  public NodeDistanceComparator(final Node<T> node) {
    this.node = node;
    this.invert = false;
  }

  public NodeDistanceComparator(final Node<T> node, final boolean invert) {
    this.node = node;
    this.invert = invert;
  }

  @Override
  public int compare(final Node<T> node1, final Node<T> node2) {
    int compare;
    final double distance1 = node1.distance(node);
    final double distance2 = node2.distance(node);
    if (distance1 == distance2) {
      final Coordinates point1 = node1;
      final Coordinates point2 = node2;
      compare = point1.compareTo(point2);
    } else if (distance1 < distance2) {
      compare = -1;
    } else {
      compare = 1;
    }

    if (invert) {
      return -compare;
    } else {
      return compare;
    }
  }

  public boolean isInvert() {
    return invert;
  }

}
