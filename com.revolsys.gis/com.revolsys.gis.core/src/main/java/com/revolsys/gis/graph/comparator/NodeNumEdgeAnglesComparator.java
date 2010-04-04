package com.revolsys.gis.graph.comparator;

import com.revolsys.gis.graph.Node;
import com.revolsys.gis.graph.attribute.NodeAttributes;

public class NodeNumEdgeAnglesComparator<T> extends NodeDegreeComparator<T> {

  public NodeNumEdgeAnglesComparator() {
  }

  public NodeNumEdgeAnglesComparator(
    final boolean invert) {
    super(invert);
  }

  @Override
  public int compare(
    final Node<T> node1,
    final Node<T> node2) {
    int compare;
    final int numAngles1 = NodeAttributes.getEdgeAngles(node1).size();
    final int numAngles2 = NodeAttributes.getEdgeAngles(node2).size();
    if (numAngles1 == numAngles2) {
      return super.compare(node1, node2);
    } else if (numAngles1 < numAngles2) {
      compare = -1;
    } else {
      compare = 1;
    }

    if (isInvert()) {
      return -compare;
    } else {
      return compare;
    }
  }
}
