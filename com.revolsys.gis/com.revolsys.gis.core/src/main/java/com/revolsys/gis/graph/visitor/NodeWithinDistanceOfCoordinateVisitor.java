package com.revolsys.gis.graph.visitor;

import com.revolsys.gis.data.visitor.Visitor;
import com.revolsys.gis.graph.Node;
import com.revolsys.gis.model.coordinates.CoordinateCoordinates;
import com.revolsys.gis.model.coordinates.Coordinates;
import com.vividsolutions.jts.geom.Coordinate;

public class NodeWithinDistanceOfCoordinateVisitor<T> implements
  Visitor<Node<T>> {
  private final Coordinates coordinates;

  private final Visitor<Node<T>> matchVisitor;

  private final double maxDistance;

  public NodeWithinDistanceOfCoordinateVisitor(
    final Coordinate coordinate,
    final double maxDistance,
    final Visitor<Node<T>> matchVisitor) {
    this.coordinates = new CoordinateCoordinates(coordinate);
    this.maxDistance = maxDistance;
    this.matchVisitor = matchVisitor;
  }
  public NodeWithinDistanceOfCoordinateVisitor(
    final Coordinates coordinates,
    final double maxDistance,
    final Visitor<Node<T>> matchVisitor) {
    this.coordinates = coordinates;
    this.maxDistance = maxDistance;
    this.matchVisitor = matchVisitor;
  }

  public boolean visit(
    final Node<T> node) {
    final Coordinates coordinate = node;
    final double distance = this.coordinates.distance(coordinate);
    if (distance <= maxDistance) {
      matchVisitor.visit(node);
    }
    return true;
  }

}
