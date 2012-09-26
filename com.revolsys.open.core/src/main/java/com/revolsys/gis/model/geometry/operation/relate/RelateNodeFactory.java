

package com.revolsys.gis.model.geometry.operation.relate;

import com.revolsys.gis.model.coordinates.Coordinates;
import com.revolsys.gis.model.geometry.operation.geomgraph.Node;
import com.revolsys.gis.model.geometry.operation.geomgraph.NodeFactory;
import com.revolsys.gis.model.geometry.operation.geomgraph.NodeMap;

/**
 * Used by the {@link NodeMap} in a {@link RelateNodeGraph} to create {@link RelateNode}s.
 *
 * @version 1.7
 */
public class RelateNodeFactory
  extends NodeFactory
{
  public Node createNode(Coordinates coord)
  {
    return new RelateNode(coord, new EdgeEndBundleStar());
  }
}
