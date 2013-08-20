package com.revolsys.gis.model.geometry.operation.relate;

/**
 * A RelateNode is a Node that maintains a list of EdgeStubs
 * for the edges that are incident on it.
 *
 * @version 1.7
 */

import com.revolsys.gis.model.coordinates.Coordinates;
import com.revolsys.gis.model.geometry.operation.geomgraph.EdgeEndStar;
import com.revolsys.gis.model.geometry.operation.geomgraph.Node;
import com.vividsolutions.jts.geom.IntersectionMatrix;

/**
 * Represents a node in the topological graph used to compute spatial
 * relationships.
 * 
 * @version 1.7
 */
public class RelateNode extends Node {

  public RelateNode(final Coordinates coord, final EdgeEndStar edges) {
    super(coord, edges);
  }

  /**
   * Update the IM with the contribution for this component. A component only
   * contributes if it has a labelling for both parent geometries
   */
  @Override
  protected void computeIM(final IntersectionMatrix im) {
    im.setAtLeastIfValid(label.getLocation(0), label.getLocation(1), 0);
  }

  /**
   * Update the IM with the contribution for the EdgeEnds incident on this node.
   */
  void updateIMFromEdges(final IntersectionMatrix im) {
    ((EdgeEndBundleStar)edges).updateIM(im);
  }

}
