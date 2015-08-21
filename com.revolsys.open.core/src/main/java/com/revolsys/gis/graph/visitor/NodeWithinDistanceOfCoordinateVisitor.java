package com.revolsys.gis.graph.visitor;

import java.util.function.Consumer;

import com.revolsys.geometry.model.Point;
import com.revolsys.gis.graph.Node;

public class NodeWithinDistanceOfCoordinateVisitor<T> implements Consumer<Node<T>> {
  private final Point coordinates;

  private final Consumer<Node<T>> matchVisitor;

  private final double maxDistance;

  public NodeWithinDistanceOfCoordinateVisitor(final Point coordinates, final double maxDistance,
    final Consumer<Node<T>> matchVisitor) {
    this.coordinates = coordinates;
    this.maxDistance = maxDistance;
    this.matchVisitor = matchVisitor;
  }

  @Override
  public void accept(final Node<T> node) {
    final Point coordinate = node;
    final double distance = this.coordinates.distance(coordinate);
    if (distance <= this.maxDistance) {
      this.matchVisitor.accept(node);
    }
  }

}
