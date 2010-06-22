package com.revolsys.gis.graph.visitor;

import java.util.List;

import com.revolsys.gis.cs.BoundingBox;
import com.revolsys.gis.data.visitor.CreateListVisitor;
import com.revolsys.gis.data.visitor.NestedVisitor;
import com.revolsys.gis.data.visitor.Visitor;
import com.revolsys.gis.graph.Edge;
import com.revolsys.gis.graph.EdgeQuadTree;
import com.revolsys.gis.graph.Graph;
import com.revolsys.gis.graph.Node;
import com.revolsys.gis.jts.LineStringUtil;
import com.revolsys.gis.model.coordinates.Coordinates;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.LineString;

public class NodeOnEdgeVisitor<T> extends NestedVisitor<Edge<T>> {
  public static <T> List<Edge<T>> getEdges(
    final Graph<T> graph,
    final Node<T> node,
    final double maxDistance) {
    final CreateListVisitor<Edge<T>> results = new CreateListVisitor<Edge<T>>();
    final Coordinates point = node.getCoordinates();
    final BoundingBox boundingBox = new BoundingBox(point);
    boundingBox.expandBy(maxDistance);
    final EdgeQuadTree<T> index = graph.getEdgeIndex();
    final NodeOnEdgeVisitor<T> visitor = new NodeOnEdgeVisitor<T>(node,
      boundingBox, results);
    index.query(boundingBox, visitor);
    return results.getList();

  }

  private final Node<T> node;

  private Coordinates point;

  private BoundingBox boundingBox;

  public NodeOnEdgeVisitor(
    final Node<T> node,
    final BoundingBox boundingBox,
    final Visitor<Edge<T>> matchVisitor) {
    super(matchVisitor);
    this.node = node;
    this.boundingBox = boundingBox;
    this.point = node.getCoordinates();
  }

  @Override
  public boolean visit(
    final Edge<T> edge) {
    if (!edge.hasNode(node)) {
      final LineString line = edge.getLine();
      if (line.getEnvelopeInternal().intersects(boundingBox)) {
        if (LineStringUtil.isPointOnLine(line, point)) {
          super.visit(edge);
        }
      }
    }
    return true;
  }

}
