package com.revolsys.gis.graph.filter;

import com.revolsys.filter.Filter;
import com.revolsys.gis.graph.Edge;
import com.revolsys.gis.graph.Node;
import com.vividsolutions.jts.geom.Envelope;

public class NodeLessThanDistanceToEdgeFilter<T> implements Filter<Node<T>> {

  private final Edge<T> edge;

  private final Envelope envelope;

  private final double maxDistance;

  public NodeLessThanDistanceToEdgeFilter(
    final Edge<T> edge,
    final double maxDistance) {
    this.edge = edge;
    this.maxDistance = maxDistance;
    this.envelope = new Envelope(edge.getEnvelope());
    envelope.expandBy(maxDistance);
  }

  public boolean accept(
    final Node<T> node) {
    if (!edge.hasNode(node) && edge.distance(node) < maxDistance) {
      return true;
    } else {
      return false;
    }
  }

  public Envelope getEnvelope() {
    return envelope;
  }

}
