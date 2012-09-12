package com.revolsys.gis.graph.filter;

import com.revolsys.filter.Filter;
import com.revolsys.gis.cs.BoundingBox;
import com.revolsys.gis.graph.Edge;
import com.revolsys.gis.graph.Node;
import com.revolsys.gis.jts.LineStringUtil;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.LineString;

public class IsPointOnLineEdgeFilter<T> implements Filter<Node<T>> {

  private final Edge<T> edge;

  private final Envelope envelope;

  private final double maxDistance;

  public IsPointOnLineEdgeFilter(final Edge<T> edge, final double maxDistance) {
    this.edge = edge;
    this.maxDistance = maxDistance;
    this.envelope = new Envelope(edge.getEnvelope());
    envelope.expandBy(maxDistance);
  }

  @Override
  public boolean accept(final Node<T> node) {
    final LineString line = edge.getLine();
    if (!edge.hasNode(node)) {
      if (envelope.intersects(new BoundingBox(node))) {
        if (LineStringUtil.isPointOnLine(line, node, maxDistance)) {
          return true;
        }
      }
    }
    return false;
  }

  public Envelope getEnvelope() {
    return envelope;
  }

}
