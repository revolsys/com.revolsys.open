package com.revolsys.gis.model.geometry.operation.overlay;

import java.util.ArrayList;
import java.util.List;

import com.revolsys.gis.model.geometry.GeometryFactory;
import com.revolsys.gis.model.geometry.operation.geomgraph.DirectedEdge;
import com.revolsys.gis.model.geometry.operation.geomgraph.DirectedEdgeStar;
import com.revolsys.gis.model.geometry.operation.geomgraph.EdgeRing;
import com.revolsys.gis.model.geometry.operation.geomgraph.Node;

/**
 * A ring of {@link DirectedEdge}s which may contain nodes of degree > 2.
 * A <tt>MaximalEdgeRing</tt> may represent two different spatial entities:
 * <ul>
 * <li>a single polygon possibly containing inversions (if the ring is oriented CW)
 * <li>a single hole possibly containing exversions (if the ring is oriented CCW)
 * </ul>
 * If the MaximalEdgeRing represents a polygon,
 * the interior of the polygon is strongly connected.
 * <p>
 * These are the form of rings used to define polygons under some spatial data models.
 * However, under the OGC SFS model, {@link MinimalEdgeRing}s are required.
 * A MaximalEdgeRing can be converted to a list of MinimalEdgeRings using the
 * {@link #buildMinimalRings() } method.
 *
 * @version 1.7
 * @see com.revolsys.jts.operation.overlay.MinimalEdgeRing
 */
public class MaximalEdgeRing extends EdgeRing {

  public MaximalEdgeRing(final DirectedEdge start,
    final GeometryFactory geometryFactory) {
    super(start, geometryFactory);
  }

  public List buildMinimalRings() {
    final List minEdgeRings = new ArrayList();
    DirectedEdge de = startDe;
    do {
      if (de.getMinEdgeRing() == null) {
        final EdgeRing minEr = new MinimalEdgeRing(de, geometryFactory);
        minEdgeRings.add(minEr);
      }
      de = de.getNext();
    } while (de != startDe);
    return minEdgeRings;
  }

  @Override
  public DirectedEdge getNext(final DirectedEdge de) {
    return de.getNext();
  }

  /**
   * For all nodes in this EdgeRing,
   * link the DirectedEdges at the node to form minimalEdgeRings
   */
  public void linkDirectedEdgesForMinimalEdgeRings() {
    DirectedEdge de = startDe;
    do {
      final Node node = de.getNode();
      ((DirectedEdgeStar)node.getEdges()).linkMinimalDirectedEdges(this);
      de = de.getNext();
    } while (de != startDe);
  }

  @Override
  public void setEdgeRing(final DirectedEdge de, final EdgeRing er) {
    de.setEdgeRing(er);
  }

}
