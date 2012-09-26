package com.revolsys.gis.model.geometry.operation.geomgraph;

import com.revolsys.gis.model.coordinates.Coordinates;

/**
 * @version 1.7
 */
public class NodeFactory {
  /**
   * The basic node constructor does not allow for incident edges
   */
  public Node createNode(final Coordinates coord) {
    return new Node(coord, null);
  }
}
