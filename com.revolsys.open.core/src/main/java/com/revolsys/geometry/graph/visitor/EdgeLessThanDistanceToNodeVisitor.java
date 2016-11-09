package com.revolsys.geometry.graph.visitor;

import java.util.List;
import java.util.function.Consumer;

import com.revolsys.geometry.graph.Edge;
import com.revolsys.geometry.graph.Graph;
import com.revolsys.geometry.graph.Node;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.Point;
import com.revolsys.visitor.CreateListVisitor;
import com.revolsys.visitor.DelegatingVisitor;

public class EdgeLessThanDistanceToNodeVisitor<T> extends DelegatingVisitor<Edge<T>> {
  public static <T> List<Edge<T>> edgesWithinDistance(final Graph<T> graph, final Node<T> node,
    final double maxDistance) {
    final CreateListVisitor<Edge<T>> results = new CreateListVisitor<>();
    final Point point = node;
    BoundingBox env = point.getBoundingBox();
    env = env.expand(maxDistance);
    graph.getEdgeIndex().forEach(env,
      new EdgeLessThanDistanceToNodeVisitor<>(node, maxDistance, results));
    return results.getList();

  }

  private BoundingBox envelope;

  private final double maxDistance;

  private final Node<T> node;

  public EdgeLessThanDistanceToNodeVisitor(final Node<T> node, final double maxDistance,
    final Consumer<Edge<T>> matchVisitor) {
    super(matchVisitor);
    this.node = node;
    this.maxDistance = maxDistance;
    final Point point = node;
    this.envelope = point.getBoundingBox();
    this.envelope = this.envelope.expand(maxDistance);
  }

  @Override
  public void accept(final Edge<T> edge) {
    final com.revolsys.geometry.model.BoundingBox envelope = edge.getBoundingBox();
    if (this.envelope.distance(envelope) < this.maxDistance) {
      if (!edge.hasNode(this.node)) {
        if (edge.isLessThanDistance(this.node, this.maxDistance)) {
          super.accept(edge);
        }
      }
    }
  }

}
