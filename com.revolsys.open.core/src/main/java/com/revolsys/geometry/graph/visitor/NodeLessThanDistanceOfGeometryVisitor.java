package com.revolsys.geometry.graph.visitor;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import com.revolsys.geometry.graph.Graph;
import com.revolsys.geometry.graph.Node;
import com.revolsys.geometry.index.IdObjectIndex;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.Point;
import com.revolsys.visitor.CreateListVisitor;

public class NodeLessThanDistanceOfGeometryVisitor<T> implements Consumer<Node<T>> {
  public static <T> List<Node<T>> getNodes(final Graph<T> graph, final Geometry geometry,
    final double maxDistance) {
    if (geometry == null) {
      return Collections.emptyList();
    } else {
      final CreateListVisitor<Node<T>> results = new CreateListVisitor<>();
      BoundingBox env = geometry.getBoundingBox();
      env = env.expand(maxDistance);
      final IdObjectIndex<Node<T>> index = graph.getNodeIndex();
      final NodeLessThanDistanceOfGeometryVisitor<T> visitor = new NodeLessThanDistanceOfGeometryVisitor<>(
        geometry, maxDistance, results);
      index.forEach(visitor, env);
      return results.getList();
    }
  }

  private final Geometry geometry;

  private final GeometryFactory geometryFactory;

  private final Consumer<Node<T>> matchVisitor;

  private final double maxDistance;

  public NodeLessThanDistanceOfGeometryVisitor(final Geometry geometry, final double maxDistance,
    final Consumer<Node<T>> matchVisitor) {
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
