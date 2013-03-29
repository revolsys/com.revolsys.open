package com.revolsys.gis.graph.visitor;

import java.util.List;

import com.revolsys.collection.Visitor;
import com.revolsys.gis.cs.BoundingBox;
import com.revolsys.gis.data.visitor.CreateListVisitor;
import com.revolsys.gis.data.visitor.NestedVisitor;
import com.revolsys.gis.graph.Edge;
import com.revolsys.gis.graph.Graph;
import com.revolsys.gis.graph.Node;
import com.revolsys.gis.model.coordinates.Coordinates;
import com.vividsolutions.jts.geom.Envelope;

public class EdgeLessThanDistanceToNodeVisitor<T> extends
  NestedVisitor<Edge<T>> {
  public static <T> List<Edge<T>> edgesWithinDistance(final Graph<T> graph,
    final Node<T> node, final double maxDistance) {
    final CreateListVisitor<Edge<T>> results = new CreateListVisitor<Edge<T>>();
    final Coordinates point = node;
    BoundingBox env = new BoundingBox(point);
    env = env.expand(maxDistance);
    graph.getEdgeIndex().visit(env,
      new EdgeLessThanDistanceToNodeVisitor<T>(node, maxDistance, results));
    return results.getList();

  }

  private BoundingBox envelope;

  private final double maxDistance;

  private final Node<T> node;

  public EdgeLessThanDistanceToNodeVisitor(final Node<T> node,
    final double maxDistance, final Visitor<Edge<T>> matchVisitor) {
    super(matchVisitor);
    this.node = node;
    this.maxDistance = maxDistance;
    final Coordinates point = node;
    this.envelope = new BoundingBox(point);
    this.envelope = this.envelope.expand(maxDistance);
  }

  @Override
  public boolean visit(final Edge<T> edge) {
    final Envelope envelope = edge.getEnvelope();
    if (this.envelope.distance(envelope) < maxDistance) {
      if (!edge.hasNode(node)) {
        if (edge.isLessThanDistance(node, maxDistance)) {
          super.visit(edge);
        }
      }
    }
    return true;
  }

}
