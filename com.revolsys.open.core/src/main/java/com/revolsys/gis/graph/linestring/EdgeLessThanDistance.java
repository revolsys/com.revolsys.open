package com.revolsys.gis.graph.linestring;

import java.util.List;

import com.revolsys.collection.Visitor;
import com.revolsys.filter.Filter;
import com.revolsys.gis.algorithm.index.IdObjectIndex;
import com.revolsys.gis.data.visitor.CreateListVisitor;
import com.revolsys.gis.data.visitor.NestedVisitor;
import com.revolsys.gis.graph.Edge;
import com.revolsys.gis.graph.Graph;
import com.revolsys.gis.model.coordinates.Coordinates;
import com.revolsys.gis.model.geometry.LineSegment;
import com.vividsolutions.jts.geom.Envelope;

public class EdgeLessThanDistance extends NestedVisitor<Edge<LineSegment>>
  implements Filter<Edge<LineSegment>> {
  public static List<Edge<LineSegment>> getEdges(
    final Graph<LineSegment> graph, final LineSegment lineSegment,
    final double maxDistance) {
    final CreateListVisitor<Edge<LineSegment>> results = new CreateListVisitor<Edge<LineSegment>>();
    final Envelope envelope = lineSegment.getBoundingBox();
    envelope.expandBy(maxDistance);
    final IdObjectIndex<Edge<LineSegment>> edgeIndex = graph.getEdgeIndex();
    edgeIndex.visit(envelope, new EdgeLessThanDistance(lineSegment,
      maxDistance, results));
    return results.getList();
  }

  public static List<Edge<LineSegment>> getEdges(final LineStringGraph graph,
    final Coordinates fromPoint, final Coordinates toPoint,
    final double maxDistance) {
    final LineSegment lineSegment = new LineSegment(fromPoint, toPoint);
    return getEdges(graph, lineSegment, maxDistance);

  }

  private final LineSegment lineSegment;

  private final double maxDistance;

  public EdgeLessThanDistance(final LineSegment lineSegment,
    final double maxDistance) {
    this.lineSegment = lineSegment;
    this.maxDistance = maxDistance;
  }

  public EdgeLessThanDistance(final LineSegment lineSegment,
    final double maxDistance, final Visitor<Edge<LineSegment>> matchVisitor) {
    super(matchVisitor);
    this.lineSegment = lineSegment;
    this.maxDistance = maxDistance;
  }

  @Override
  public boolean accept(final Edge<LineSegment> edge) {
    final LineSegment lineSegment = edge.getObject();
    final double distance = lineSegment.distance(this.lineSegment);
    if (distance <= maxDistance) {
      return true;
    } else {
      return false;
    }
  }

  @Override
  public boolean visit(final Edge<LineSegment> edge) {
    if (accept(edge)) {
      super.visit(edge);
    }
    return true;
  }
}
