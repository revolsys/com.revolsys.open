package com.revolsys.gis.graph.visitor;

import java.util.List;

import com.revolsys.gis.cs.GeometryFactory;
import com.revolsys.gis.data.visitor.CreateListVisitor;
import com.revolsys.gis.data.visitor.Visitor;
import com.revolsys.gis.graph.Graph;
import com.revolsys.gis.graph.Node;
import com.revolsys.gis.graph.NodeQuadTree;
import com.revolsys.gis.model.coordinates.Coordinates;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;

public class NodeWithinDistanceOfGeometryVisitor<T> implements Visitor<Node<T>> {
  public static <T> List<Node<T>> getNodes(
    final Graph<T> graph,
    final Geometry geometry,
    final double maxDistance) {
    final CreateListVisitor<Node<T>> results = new CreateListVisitor<Node<T>>();
    final Envelope env = new Envelope(geometry.getEnvelopeInternal());
    env.expandBy(maxDistance);
    final NodeQuadTree<T> index = graph.getNodeIndex();
    final NodeWithinDistanceOfGeometryVisitor<T> visitor = new NodeWithinDistanceOfGeometryVisitor<T>(
      geometry, maxDistance, results);
    index.query(env, visitor);
    return results.getList();
  }

  private final Geometry geometry;

  private final GeometryFactory geometryFactory;

  private final Visitor<Node<T>> matchVisitor;

  private final double maxDistance;

  public NodeWithinDistanceOfGeometryVisitor(
    final Geometry geometry,
    final double maxDistance,
    final Visitor<Node<T>> matchVisitor) {
    this.geometry = geometry;
    this.maxDistance = maxDistance;
    this.matchVisitor = matchVisitor;
    this.geometryFactory = GeometryFactory.getFactory(geometry);
  }

  public boolean visit(
    final Node<T> node) {
    final Coordinates coordinates = node.getCoordinates();
    final Point point = geometryFactory.createPoint(coordinates);
    final double distance = geometry.distance(point);
    if (distance <= maxDistance) {
      matchVisitor.visit(node);
    }
    return true;
  }

}
