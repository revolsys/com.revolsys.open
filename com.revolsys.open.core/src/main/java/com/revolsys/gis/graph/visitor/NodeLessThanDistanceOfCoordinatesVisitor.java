package com.revolsys.gis.graph.visitor;

import java.util.Collections;
import java.util.List;

import com.revolsys.collection.Visitor;
import com.revolsys.gis.algorithm.index.IdObjectIndex;
import com.revolsys.gis.graph.Graph;
import com.revolsys.gis.graph.Node;
import com.revolsys.jts.geom.BoundingBox;
import com.revolsys.jts.geom.Point;
import com.revolsys.jts.geom.impl.BoundingBoxDoubleGf;
import com.revolsys.visitor.CreateListVisitor;

public class NodeLessThanDistanceOfCoordinatesVisitor<T> implements Visitor<Node<T>> {
  public static <T> List<Node<T>> getNodes(final Graph<T> graph, final Point point,
    final double maxDistance) {
    final CreateListVisitor<Node<T>> results = new CreateListVisitor<Node<T>>();
    final Visitor<Node<T>> visitor = new NodeWithinDistanceOfCoordinateVisitor<T>(point,
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

  private final Visitor<Node<T>> matchVisitor;

  private final double maxDistance;

  public NodeLessThanDistanceOfCoordinatesVisitor(final Point coordinates, final double maxDistance,
    final Visitor<Node<T>> matchVisitor) {
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
