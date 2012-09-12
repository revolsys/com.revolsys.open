package com.revolsys.gis.algorithm.linematch;

import com.revolsys.filter.Filter;
import com.revolsys.gis.cs.BoundingBox;
import com.revolsys.gis.graph.Edge;
import com.revolsys.gis.graph.Node;
import com.vividsolutions.jts.geom.Envelope;

public class LineSegmentMatchWithinDistanceFilter implements
  Filter<Edge<LineSegmentMatch>> {
  private final Envelope envelope;

  private final double maxDistance;

  private final Node<LineSegmentMatch> node;

  public LineSegmentMatchWithinDistanceFilter(
    final Node<LineSegmentMatch> node, final double maxDistance) {
    this.node = node;
    this.maxDistance = maxDistance;
    this.envelope = new BoundingBox(node);
    envelope.expandBy(maxDistance);
  }

  @Override
  public boolean accept(final Edge<LineSegmentMatch> edge) {
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
