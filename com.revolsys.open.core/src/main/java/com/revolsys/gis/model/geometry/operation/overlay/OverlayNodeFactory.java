package com.revolsys.gis.model.geometry.operation.overlay;

/**
 * @version 1.7
 */
import com.revolsys.gis.model.coordinates.Coordinates;
import com.revolsys.gis.model.geometry.operation.geomgraph.DirectedEdgeStar;
import com.revolsys.gis.model.geometry.operation.geomgraph.Node;
import com.revolsys.gis.model.geometry.operation.geomgraph.NodeFactory;
import com.revolsys.gis.model.geometry.operation.geomgraph.PlanarGraph;

/**
 * Creates nodes for use in the {@link PlanarGraph}s constructed during overlay
 * operations.
 * 
 * @version 1.7
 */
public class OverlayNodeFactory extends NodeFactory {
  public Node createNode(Coordinates coord) {
    return new Node(coord, new DirectedEdgeStar());
  }
}
