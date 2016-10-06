package com.revolsys.geometry.graph.visitor;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import com.revolsys.geometry.graph.Graph;
import com.revolsys.geometry.graph.Node;
import com.revolsys.geometry.index.IdObjectIndex;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.impl.BoundingBoxDoubleGf;
import com.revolsys.visitor.CreateListVisitor;

public class NodeLessThanDistanceOfCoordinatesVisitor<T> implements Consumer<Node<T>> {
  public static <T> List<Node<T>> getNodes(final Graph<T> graph, final Point point,
    final double maxDistance) {
    final CreateListVisitor<Node<T>> results = new CreateListVisitor<>();
    final Consumer<Node<T>> visitor = new NodeWithinDistanceOfCoordinateVisitor<>(point,
      maxDistance, results);
    BoundingBox envelope = new BoundingBoxDoubleGf(point);
    envelope = envelope.expand(maxDistance);
    final IdObjectIndex<Node<T>> nodeIndex = graph.getNodeIndex();
    nodeIndex.forEach(visitor, envelope);
    final List<Node<T>> nodes = results.getList();
    Collections.sort(nodes);
    return nodes;
  }

  private final Point coordinates;

  private final Consumer<Node<T>> matchVisitor;

  private final double maxDistance;

  public NodeLessThanDistanceOfCoordinatesVisitor(final Point coordinates, final double maxDistance,
    final Consumer<Node<T>> matchVisitor) {
    this.coordinates = coordinates;
    this.maxDistance = maxDistance;
    this.matchVisitor = matchVisitor;
  }

  @Override
  public void accept(final Node<T> node) {
    final double distance = this.coordinates.distance(node);
    if (distance < this.maxDistance) {
      this.matchVisitor.accept(node);
    }
  }

}
