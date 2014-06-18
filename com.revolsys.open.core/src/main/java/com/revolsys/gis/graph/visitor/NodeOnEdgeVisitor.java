package com.revolsys.gis.graph.visitor;

import java.util.Collections;
import java.util.List;

import com.revolsys.collection.Visitor;
import com.revolsys.gis.algorithm.index.IdObjectIndex;
import com.revolsys.gis.graph.Edge;
import com.revolsys.gis.graph.Graph;
import com.revolsys.gis.graph.Node;
import com.revolsys.gis.jts.LineStringUtil;
import com.revolsys.jts.geom.BoundingBox;
import com.revolsys.jts.geom.LineString;
import com.revolsys.jts.geom.Point;
import com.revolsys.jts.geom.impl.BoundingBoxDoubleGf;
import com.revolsys.visitor.CreateListVisitor;
import com.revolsys.visitor.DelegatingVisitor;

public class NodeOnEdgeVisitor<T> extends DelegatingVisitor<Edge<T>> {
  public static <T> List<Edge<T>> getEdges(final Graph<T> graph,
    final Node<T> node, final double maxDistance) {
    final CreateListVisitor<Edge<T>> results = new CreateListVisitor<Edge<T>>();
    final Point point = node;
    BoundingBox boundingBox = new BoundingBoxDoubleGf(point);
    boundingBox = boundingBox.expand(maxDistance);
    final IdObjectIndex<Edge<T>> index = graph.getEdgeIndex();
    final NodeOnEdgeVisitor<T> visitor = new NodeOnEdgeVisitor<T>(node,
      boundingBox, maxDistance, results);
    index.visit(boundingBox, visitor);
    final List<Edge<T>> edges = results.getList();
    Collections.sort(edges);
    return edges;

  }

  private final Node<T> node;

  private final Point point;

  private final BoundingBox boundingBox;

  private final double maxDistance;

  public NodeOnEdgeVisitor(final Node<T> node, final BoundingBox boundingBox,
    final double maxDistance, final Visitor<Edge<T>> matchVisitor) {
    super(matchVisitor);
    this.node = node;
    this.boundingBox = boundingBox;
    this.maxDistance = maxDistance;
    this.point = node;
  }

  @Override
  public boolean visit(final Edge<T> edge) {
    if (!edge.hasNode(node)) {
      final LineString line = edge.getLine();
      if (line.getBoundingBox().intersects(boundingBox)) {
        if (LineStringUtil.isPointOnLine(line, point, maxDistance)) {
          super.visit(edge);
        }
      }
    }
    return true;
  }

}
