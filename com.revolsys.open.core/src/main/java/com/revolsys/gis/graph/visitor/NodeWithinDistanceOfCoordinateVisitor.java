package com.revolsys.gis.graph.visitor;

import com.revolsys.collection.Visitor;
import com.revolsys.gis.graph.Node;
import com.revolsys.jts.geom.Point;

public class NodeWithinDistanceOfCoordinateVisitor<T> implements Visitor<Node<T>> {
  private final Point coordinates;

  private final Visitor<Node<T>> matchVisitor;

  private final double maxDistance;

  public NodeWithinDistanceOfCoordinateVisitor(final Point coordinates, final double maxDistance,
    final Visitor<Node<T>> matchVisitor) {
    this.coordinates = coordinates;
    this.maxDistance = maxDistance;
    this.matchVisitor = matchVisitor;
  }

  @Override
  public boolean visit(final Node<T> node) {
    final Point coordinate = node;
    final double distance = this.coordinates.distance(coordinate);
    if (distance <= this.maxDistance) {
      this.matchVisitor.visit(node);
    }
    return true;
  }

}
