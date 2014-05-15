package com.revolsys.gis.graph.linestring;

import java.util.List;

import com.revolsys.collection.Visitor;
import com.revolsys.filter.Filter;
import com.revolsys.gis.algorithm.index.IdObjectIndex;
import com.revolsys.gis.graph.Edge;
import com.revolsys.gis.graph.Graph;
import com.revolsys.gis.jts.LineSegmentDoubleGF;
import com.revolsys.jts.geom.BoundingBox;
import com.revolsys.jts.geom.LineSegment;
import com.revolsys.jts.geom.Point;
import com.revolsys.visitor.CreateListVisitor;
import com.revolsys.visitor.DelegatingVisitor;

public class EdgeLessThanDistance extends DelegatingVisitor<Edge<LineSegment>>
  implements Filter<Edge<LineSegment>> {
  public static List<Edge<LineSegment>> getEdges(
    final Graph<LineSegment> graph, final LineSegment lineSegment,
    final double maxDistance) {
    final CreateListVisitor<Edge<LineSegment>> results = new CreateListVisitor<Edge<LineSegment>>();
    BoundingBox envelope = lineSegment.getBoundingBox();
    envelope = envelope.expand(maxDistance);
    final IdObjectIndex<Edge<LineSegment>> edgeIndex = graph.getEdgeIndex();
    edgeIndex.visit(envelope, new EdgeLessThanDistance(lineSegment,
      maxDistance, results));
    return results.getList();
  }

  public static List<Edge<LineSegment>> getEdges(final LineStringGraph graph,
    final Point fromPoint, final Point toPoint, final double maxDistance) {
    final LineSegment lineSegment = new LineSegmentDoubleGF(fromPoint, toPoint);
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
