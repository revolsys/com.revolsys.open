

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
 * @see com.vividsolutions.jts.operation.overlay.MinimalEdgeRing
 */
public class MaximalEdgeRing
  extends EdgeRing
{

  public MaximalEdgeRing(DirectedEdge start, GeometryFactory geometryFactory) {
    super(start, geometryFactory);
  }

  public DirectedEdge getNext(DirectedEdge de)
  {
    return de.getNext();
  }
  public void setEdgeRing(DirectedEdge de, EdgeRing er)
  {
    de.setEdgeRing(er);
  }

  /**
   * For all nodes in this EdgeRing,
   * link the DirectedEdges at the node to form minimalEdgeRings
   */
  public void linkDirectedEdgesForMinimalEdgeRings()
  {
    DirectedEdge de = startDe;
    do {
      Node node = de.getNode();
      ((DirectedEdgeStar) node.getEdges()).linkMinimalDirectedEdges(this);
      de = de.getNext();
    } while (de != startDe);
  }

  public List buildMinimalRings()
  {
    List minEdgeRings = new ArrayList();
    DirectedEdge de = startDe;
    do {
      if (de.getMinEdgeRing() == null) {
        EdgeRing minEr = new MinimalEdgeRing(de, geometryFactory);
        minEdgeRings.add(minEr);
      }
      de = de.getNext();
    } while (de != startDe);
    return minEdgeRings;
  }

}
