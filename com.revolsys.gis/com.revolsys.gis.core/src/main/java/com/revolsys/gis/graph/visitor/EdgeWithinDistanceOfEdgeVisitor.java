package com.revolsys.gis.graph.visitor;

import java.util.List;

import com.revolsys.gis.data.visitor.CreateListVisitor;
import com.revolsys.gis.data.visitor.NestedVisitor;
import com.revolsys.gis.data.visitor.Visitor;
import com.revolsys.gis.graph.Edge;
import com.revolsys.gis.graph.EdgeQuadTree;
import com.revolsys.gis.graph.Graph;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.LineString;

public class EdgeWithinDistanceOfEdgeVisitor<T> extends NestedVisitor<Edge<T>> {
  public static <T> List<Edge<T>> getEdges(
    final Graph<T> graph,
    final Edge<T> edge,
    final double maxDistance) {
    final CreateListVisitor<Edge<T>> results = new CreateListVisitor<Edge<T>>();

    final LineString line = edge.getLine();
    final Envelope envelope = line.getEnvelopeInternal();
    final EdgeWithinDistanceOfEdgeVisitor<T> visitor = new EdgeWithinDistanceOfEdgeVisitor<T>(
      edge, maxDistance, results);
    final EdgeQuadTree<T> index = graph.getEdgeIndex();
    index.query(envelope, visitor);
    return results.getList();

  }

  private final Edge<T> edge;

  private final double maxDistance;

  public EdgeWithinDistanceOfEdgeVisitor(
    final Edge<T> edge,
    final double maxDistance,
    final Visitor<Edge<T>> matchVisitor) {
    super(matchVisitor);
    this.edge = edge;
    this.maxDistance = maxDistance;
  }

  @Override
  public boolean visit(
    final Edge<T> edge2) {
    if (edge2 != edge) {
      final LineString line1 = edge.getLine();
      final Envelope envelope1 = line1.getEnvelopeInternal();
      final LineString line2 = edge2.getLine();
      final Envelope envelope2 = line2.getEnvelopeInternal();
      if (envelope1.distance(envelope2) < maxDistance) {
        final double distance = line1.distance(line2);
        if (distance < maxDistance) {
          super.visit(edge2);
        }
      }
    }
    return true;
  }
}
