package com.revolsys.gis.model.geometry.operation.relate;

import java.util.Iterator;

import com.revolsys.gis.model.geometry.operation.geomgraph.EdgeEnd;
import com.revolsys.gis.model.geometry.operation.geomgraph.EdgeEndStar;
import com.revolsys.jts.geom.IntersectionMatrix;

/**
 * An ordered list of {@link EdgeEndBundle}s around a {@link RelateNode}.
 * They are maintained in CCW order (starting with the positive x-axis) around the node
 * for efficient lookup and topology building.
 *
 * @version 1.7
 */
public class EdgeEndBundleStar extends EdgeEndStar {
  /**
   * Creates a new empty EdgeEndBundleStar
   */
  public EdgeEndBundleStar() {
  }

  /**
   * Insert a EdgeEnd in order in the list.
   * If there is an existing EdgeStubBundle which is parallel, the EdgeEnd is
   * added to the bundle.  Otherwise, a new EdgeEndBundle is created
   * to contain the EdgeEnd.
   * <br>
   */
  @Override
  public void insert(final EdgeEnd e) {
    EdgeEndBundle eb = (EdgeEndBundle)edgeMap.get(e);
    if (eb == null) {
      eb = new EdgeEndBundle(e);
      insertEdgeEnd(e, eb);
    } else {
      eb.insert(e);
    }
  }

  /**
   * Update the IM with the contribution for the EdgeStubs around the node.
   */
  void updateIM(final IntersectionMatrix im) {
    for (final Iterator it = iterator(); it.hasNext();) {
      final EdgeEndBundle esb = (EdgeEndBundle)it.next();
      esb.updateIM(im);
    }
  }

}
