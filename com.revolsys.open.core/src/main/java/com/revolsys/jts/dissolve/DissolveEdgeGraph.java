package com.revolsys.jts.dissolve;

import com.revolsys.jts.edgegraph.EdgeGraph;
import com.revolsys.jts.edgegraph.HalfEdge;
import com.revolsys.jts.geom.Point;

/**
 * A graph containing {@link DissolveHalfEdge}s.
 *
 * @author Martin Davis
 *
 */
class DissolveEdgeGraph extends EdgeGraph {
  @Override
  protected HalfEdge createEdge(final Point p0) {
    return new DissolveHalfEdge(p0);
  }

}
