package com.revolsys.gis.graph.comparator;

import java.util.Comparator;

import com.revolsys.gis.graph.Node;
import com.revolsys.gis.model.coordinates.Coordinates;

public class NodeDegreeComparator<T> implements Comparator<Node<T>> {

  private boolean invert = false;

  public NodeDegreeComparator() {
  }

  public NodeDegreeComparator(final boolean invert) {
    this.invert = invert;
  }

  public int compare(final Node<T> node1, final Node<T> node2) {
    int compare;
    final int degree1 = node1.getDegree();
    final int degree2 = node2.getDegree();
    if (degree1 == degree2) {
      final Coordinates point1 = node1;
      final Coordinates point2 = node2;
      compare = point1.compareTo(point2);
    } else if (degree1 < degree2) {
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
