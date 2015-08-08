package com.revolsys.gis.graph.visitor;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import com.revolsys.gis.algorithm.index.IdObjectIndex;
import com.revolsys.gis.graph.Graph;
import com.revolsys.gis.graph.Node;
import com.revolsys.jts.geom.BoundingBox;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.jts.geom.Point;
import com.revolsys.visitor.CreateListVisitor;

public class NodeWithinDistanceOfGeometryVisitor<T> implements Consumer<Node<T>> {
  public static <T> List<Node<T>> getNodes(final Graph<T> graph, final Geometry geometry,
    final double maxDistance) {
    if (geometry == null) {
      return Collections.emptyList();
    } else {
      final CreateListVisitor<Node<T>> results = new CreateListVisitor<Node<T>>();
      BoundingBox env = geometry.getBoundingBox();
      env = env.expand(maxDistance);
      final IdObjectIndex<Node<T>> index = graph.getNodeIndex();
      final NodeWithinDistanceOfGeometryVisitor<T> visitor = new NodeWithinDistanceOfGeometryVisitor<T>(
        geometry, maxDistance, results);
      index.forEach(visitor, env);
      return results.getList();
    }
  }

  private final Geometry geometry;

  private final GeometryFactory geometryFactory;

  private final Consumer<Node<T>> matchVisitor;

  private final double maxDistance;

  public NodeWithinDistanceOfGeometryVisitor(final Geometry geometry, final double maxDistance,
    final Consumer<Node<T>> matchVisitor) {
    this.geometry = geometry;
    this.maxDistance = maxDistance;
    this.matchVisitor = matchVisitor;
    this.geometryFactory = geometry.getGeometryFactory();
  }

  @Override
  public void accept(final Node<T> node) {
    final Point coordinates = node;
    final Point point = this.geometryFactory.point(coordinates);
    final double distance = this.geometry.distance(point);
    if (distance <= this.maxDistance) {
      this.matchVisitor.accept(node);
    }
  }

}
