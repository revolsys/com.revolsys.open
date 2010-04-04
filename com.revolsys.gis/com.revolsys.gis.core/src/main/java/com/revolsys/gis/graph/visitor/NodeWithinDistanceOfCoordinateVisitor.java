package com.revolsys.gis.graph.visitor;

import java.util.List;

import com.revolsys.gis.data.visitor.CreateListVisitor;
import com.revolsys.gis.data.visitor.Visitor;
import com.revolsys.gis.graph.Graph;
import com.revolsys.gis.graph.Node;
import com.revolsys.gis.graph.NodeQuadTree;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;

public class NodeWithinDistanceOfCoordinateVisitor<T> implements
  Visitor<Node<T>> {

  public static <T> List<Node<T>> getNodes(
    final Graph<T> graph,
    final Coordinate coordinate,
    final double maxDistance) {
    final CreateListVisitor<Node<T>> results = new CreateListVisitor<Node<T>>();
    final Envelope env = new Envelope(coordinate);
    env.expandBy(maxDistance);
    final NodeQuadTree<T> index = graph.getNodeIndex();
    final NodeWithinDistanceOfCoordinateVisitor<T> visitor = new NodeWithinDistanceOfCoordinateVisitor<T>(
      coordinate, maxDistance, results);
    index.query(env, visitor);
    return results.getList();
  }

  public static <T> List<Node<T>> getNodes(
    final Graph<T> graph,
    final Node<T> node,
    final double maxDistance) {
    final Coordinate coordinate = node.getCoordinate();
    return getNodes(graph, coordinate, maxDistance);
  }

  private final Coordinate coordinate;

  private final Visitor<Node<T>> matchVisitor;

  private final double maxDistance;

  public NodeWithinDistanceOfCoordinateVisitor(
    final Coordinate coordinate,
    final double maxDistance,
    final Visitor<Node<T>> matchVisitor) {
    this.coordinate = coordinate;
    this.maxDistance = maxDistance;
    this.matchVisitor = matchVisitor;
  }

  public boolean visit(
    final Node<T> node) {
    final Coordinate coordinate = node.getCoordinate();
    final double distance = this.coordinate.distance(coordinate);
    if (distance <= maxDistance) {
      matchVisitor.visit(node);
    }
    return true;
  }

}
