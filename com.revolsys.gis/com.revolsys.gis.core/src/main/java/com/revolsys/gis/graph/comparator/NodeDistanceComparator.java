package com.revolsys.gis.graph.comparator;

import java.util.Comparator;

import com.revolsys.gis.graph.Node;

public class NodeDistanceComparator implements Comparator<Node<?>> {

  private final boolean invert;

  private final Node<?> node;

  public NodeDistanceComparator(
    final Node<?> node) {
    this.node = node;
    this.invert = false;
  }

  public NodeDistanceComparator(
    final Node<?> node,
    final boolean invert) {
    this.node = node;
    this.invert = invert;
  }

  public int compare(
    final Node<?> node1,
    final Node<?> node2) {
    int compare;
    final double distance1 = node1.getDistance(node);
    final double distance2 = node2.getDistance(node);
    if (distance1 == distance2) {
      compare = node1.getCoordinate().compareTo(node2.getCoordinate());
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
