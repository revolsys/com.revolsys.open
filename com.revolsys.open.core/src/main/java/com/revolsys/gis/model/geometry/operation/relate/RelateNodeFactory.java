package com.revolsys.gis.model.geometry.operation.relate;

import com.revolsys.gis.model.geometry.operation.geomgraph.Node;
import com.revolsys.gis.model.geometry.operation.geomgraph.NodeFactory;
import com.revolsys.gis.model.geometry.operation.geomgraph.NodeMap;
import com.revolsys.jts.geom.Coordinates;

/**
 * Used by the {@link NodeMap} in a {@link RelateNodeGraph} to create {@link RelateNode}s.
 *
 * @version 1.7
 */
public class RelateNodeFactory extends NodeFactory {
  @Override
  public Node createNode(final Coordinates coord) {
    return new RelateNode(coord, new EdgeEndBundleStar());
  }
}
