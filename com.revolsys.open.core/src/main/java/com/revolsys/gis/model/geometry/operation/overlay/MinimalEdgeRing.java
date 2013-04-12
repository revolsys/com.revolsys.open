package com.revolsys.gis.model.geometry.operation.overlay;

import com.revolsys.gis.model.geometry.GeometryFactory;
import com.revolsys.gis.model.geometry.operation.geomgraph.DirectedEdge;
import com.revolsys.gis.model.geometry.operation.geomgraph.Edge;
import com.revolsys.gis.model.geometry.operation.geomgraph.EdgeRing;

/**
 * A ring of {@link Edge}s with the property that no node
 * has degree greater than 2.  These are the form of rings required
 * to represent polygons under the OGC SFS spatial data model.
 *
 * @version 1.7
 * @see com.vividsolutions.jts.operation.overlay.MaximalEdgeRing
 */
public class MinimalEdgeRing extends EdgeRing {

  public MinimalEdgeRing(final DirectedEdge start,
    final GeometryFactory geometryFactory) {
    super(start, geometryFactory);
  }

  @Override
  public DirectedEdge getNext(final DirectedEdge de) {
    return de.getNextMin();
  }

  @Override
  public void setEdgeRing(final DirectedEdge de, final EdgeRing er) {
    de.setMinEdgeRing(er);
  }

}
