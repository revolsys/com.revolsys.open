package com.revolsys.gis.graph.visitor;

import java.util.List;

import com.revolsys.collection.Visitor;
import com.revolsys.gis.graph.Edge;
import com.revolsys.gis.graph.Graph;
import com.revolsys.gis.graph.Node;
import com.revolsys.jts.geom.BoundingBox;
import com.revolsys.jts.geom.Point;
import com.revolsys.jts.geom.impl.BoundingBoxDoubleGf;
import com.revolsys.visitor.CreateListVisitor;
import com.revolsys.visitor.DelegatingVisitor;

public class EdgeLessThanDistanceToNodeVisitor<T> extends DelegatingVisitor<Edge<T>> {
  public static <T> List<Edge<T>> edgesWithinDistance(final Graph<T> graph, final Node<T> node,
    final double maxDistance) {
    final CreateListVisitor<Edge<T>> results = new CreateListVisitor<Edge<T>>();
    final Point point = node;
    BoundingBox env = new BoundingBoxDoubleGf(point);
    env = env.expand(maxDistance);
    graph.getEdgeIndex().visit(env,
      new EdgeLessThanDistanceToNodeVisitor<T>(node, maxDistance, results));
    return results.getList();

  }

  private BoundingBox envelope;

  private final double maxDistance;

  private final Node<T> node;

  public EdgeLessThanDistanceToNodeVisitor(final Node<T> node, final double maxDistance,
    final Visitor<Edge<T>> matchVisitor) {
    super(matchVisitor);
    this.node = node;
    this.maxDistance = maxDistance;
    final Point point = node;
    this.envelope = new BoundingBoxDoubleGf(point);
    this.envelope = this.envelope.expand(maxDistance);
  }

  @Override
  public boolean visit(final Edge<T> edge) {
    final com.revolsys.jts.geom.BoundingBox envelope = edge.getEnvelope();
    if (this.envelope.distance(envelope) < this.maxDistance) {
      if (!edge.hasNode(this.node)) {
        if (edge.isLessThanDistance(this.node, this.maxDistance)) {
          super.visit(edge);
        }
      }
    }
    return true;
  }

}
