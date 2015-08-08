package com.revolsys.gis.graph.visitor;

import java.util.Collections;
import java.util.List;

import com.revolsys.collection.Visitor;
import com.revolsys.gis.algorithm.index.IdObjectIndex;
import com.revolsys.gis.graph.Graph;
import com.revolsys.gis.graph.Node;
import com.revolsys.jts.geom.BoundingBox;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.jts.geom.Point;
import com.revolsys.visitor.CreateListVisitor;

public class NodeLessThanDistanceOfGeometryVisitor<T> implements Visitor<Node<T>> {
  public static <T> List<Node<T>> getNodes(final Graph<T> graph, final Geometry geometry,
    final double maxDistance) {
    if (geometry == null) {
      return Collections.emptyList();
    } else {
      final CreateListVisitor<Node<T>> results = new CreateListVisitor<Node<T>>();
      BoundingBox env = geometry.getBoundingBox();
      env = env.expand(maxDistance);
      final IdObjectIndex<Node<T>> index = graph.getNodeIndex();
      final NodeLessThanDistanceOfGeometryVisitor<T> visitor = new NodeLessThanDistanceOfGeometryVisitor<T>(
        geometry, maxDistance, results);
      index.forEach(visitor, env);
      return results.getList();
    }
  }

  private final Geometry geometry;

  private final Visitor<Node<T>> matchVisitor;

  private final double maxDistance;

  private final GeometryFactory geometryFactory;

  public NodeLessThanDistanceOfGeometryVisitor(final Geometry geometry, final double maxDistance,
    final Visitor<Node<T>> matchVisitor) {
    this.geometry = geometry;
    this.maxDistance = maxDistance;
    this.matchVisitor = matchVisitor;
    this.geometryFactory = geometry.getGeometryFactory();
  }

  @Override
  public void accept(final Node<T> node) {
    final Point coordinate = node;
    final Point point = this.geometryFactory.point(coordinate);
    final double distance = this.geometry.distance(point);
    if (distance < this.maxDistance) {
      this.matchVisitor.accept(node);
    }
  }

}
