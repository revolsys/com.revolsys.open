package com.revolsys.gis.graph.visitor;

import java.util.List;

import com.revolsys.collection.Visitor;
import com.revolsys.filter.Filter;
import com.revolsys.gis.cs.BoundingBox;
import com.revolsys.gis.graph.Edge;
import com.revolsys.gis.graph.Graph;
import com.revolsys.gis.graph.Node;
import com.revolsys.jts.geom.Coordinates;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.jts.geom.LineString;
import com.revolsys.visitor.CreateListVisitor;
import com.revolsys.visitor.DelegatingVisitor;

public class EdgeWithinDistance<T> extends DelegatingVisitor<Edge<T>> implements
  Filter<Edge<T>> {
  public static <T> List<Edge<T>> edgesWithinDistance(final Graph<T> graph,
    final Coordinates point, final double maxDistance) {
    final com.revolsys.jts.geom.GeometryFactory geometryFactory = GeometryFactory.getFactory();
    final Geometry geometry = geometryFactory.createPoint(point);
    return edgesWithinDistance(graph, geometry, maxDistance);

  }

  public static <T> List<Edge<T>> edgesWithinDistance(final Graph<T> graph,
    final Geometry geometry, final double maxDistance) {
    final CreateListVisitor<Edge<T>> results = new CreateListVisitor<Edge<T>>();
    BoundingBox env = BoundingBox.getBoundingBox(geometry);
    env = env.expand(maxDistance);
    graph.getEdgeIndex().visit(env,
      new EdgeWithinDistance<T>(geometry, maxDistance, results));
    return results.getList();
  }

  public static <T> List<Edge<T>> edgesWithinDistance(final Graph<T> graph,
    final Node<T> node, final double maxDistance) {
    final com.revolsys.jts.geom.GeometryFactory geometryFactory = GeometryFactory.getFactory();
    final Coordinates coordinate = node;
    final Geometry geometry = geometryFactory.createPoint(coordinate);
    return edgesWithinDistance(graph, geometry, maxDistance);

  }

  private final Geometry geometry;

  private final double maxDistance;

  public EdgeWithinDistance(final Geometry geometry, final double maxDistance) {
    this.geometry = geometry;
    this.maxDistance = maxDistance;
  }

  public EdgeWithinDistance(final Geometry geometry, final double maxDistance,
    final Visitor<Edge<T>> matchVisitor) {
    super(matchVisitor);
    this.geometry = geometry;
    this.maxDistance = maxDistance;
  }

  @Override
  public boolean accept(final Edge<T> edge) {
    final LineString line = edge.getLine();
    final double distance = line.distance(geometry);
    if (distance <= maxDistance) {
      return true;
    } else {
      return false;
    }
  }

  @Override
  public boolean visit(final Edge<T> edge) {
    if (accept(edge)) {
      super.visit(edge);
    }
    return true;
  }
}
