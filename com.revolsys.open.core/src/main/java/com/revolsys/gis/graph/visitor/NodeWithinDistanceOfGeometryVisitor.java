package com.revolsys.gis.graph.visitor;

import java.util.List;

import com.revolsys.collection.Visitor;
import com.revolsys.gis.algorithm.index.IdObjectIndex;
import com.revolsys.gis.cs.BoundingBox;
import com.revolsys.gis.cs.GeometryFactory;
import com.revolsys.gis.data.visitor.CreateListVisitor;
import com.revolsys.gis.graph.Graph;
import com.revolsys.gis.graph.Node;
import com.revolsys.gis.model.coordinates.Coordinates;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;

public class NodeWithinDistanceOfGeometryVisitor<T> implements Visitor<Node<T>> {
  public static <T> List<Node<T>> getNodes(final Graph<T> graph,
    final Geometry geometry, final double maxDistance) {
    final CreateListVisitor<Node<T>> results = new CreateListVisitor<Node<T>>();
    BoundingBox env = BoundingBox.getBoundingBox(geometry);
    env = env.expand(maxDistance);
    final IdObjectIndex<Node<T>> index = graph.getNodeIndex();
    final NodeWithinDistanceOfGeometryVisitor<T> visitor = new NodeWithinDistanceOfGeometryVisitor<T>(
      geometry, maxDistance, results);
    index.visit(env, visitor);
    return results.getList();
  }

  private final Geometry geometry;

  private final GeometryFactory geometryFactory;

  private final Visitor<Node<T>> matchVisitor;

  private final double maxDistance;

  public NodeWithinDistanceOfGeometryVisitor(final Geometry geometry,
    final double maxDistance, final Visitor<Node<T>> matchVisitor) {
    this.geometry = geometry;
    this.maxDistance = maxDistance;
    this.matchVisitor = matchVisitor;
    this.geometryFactory = GeometryFactory.getFactory(geometry);
  }

  @Override
  public boolean visit(final Node<T> node) {
    final Coordinates coordinates = node;
    final Point point = geometryFactory.createPoint(coordinates);
    final double distance = geometry.distance(point);
    if (distance <= maxDistance) {
      matchVisitor.visit(node);
    }
    return true;
  }

}
