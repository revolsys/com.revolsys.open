package com.revolsys.geometry.graph.linestring;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

import com.revolsys.geometry.algorithm.index.IdObjectIndex;
import com.revolsys.geometry.graph.Edge;
import com.revolsys.geometry.graph.Graph;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.segment.LineSegment;
import com.revolsys.geometry.model.segment.LineSegmentDoubleGF;
import com.revolsys.visitor.CreateListVisitor;
import com.revolsys.visitor.DelegatingVisitor;

public class EdgeLessThanDistance extends DelegatingVisitor<Edge<LineSegment>>
  implements Predicate<Edge<LineSegment>> {
  public static List<Edge<LineSegment>> getEdges(final Graph<LineSegment> graph,
    final LineSegment lineSegment, final double maxDistance) {
    final CreateListVisitor<Edge<LineSegment>> results = new CreateListVisitor<Edge<LineSegment>>();
    BoundingBox envelope = lineSegment.getBoundingBox();
    envelope = envelope.expand(maxDistance);
    final IdObjectIndex<Edge<LineSegment>> edgeIndex = graph.getEdgeIndex();
    edgeIndex.forEach(new EdgeLessThanDistance(lineSegment, maxDistance, results), envelope);
    return results.getList();
  }

  public static List<Edge<LineSegment>> getEdges(final LineStringGraph graph, final Point fromPoint,
    final Point toPoint, final double maxDistance) {
    final LineSegment lineSegment = new LineSegmentDoubleGF(fromPoint, toPoint);
    return getEdges(graph, lineSegment, maxDistance);

  }

  private final LineSegment lineSegment;

  private final double maxDistance;

  public EdgeLessThanDistance(final LineSegment lineSegment, final double maxDistance) {
    this.lineSegment = lineSegment;
    this.maxDistance = maxDistance;
  }

  public EdgeLessThanDistance(final LineSegment lineSegment, final double maxDistance,
    final Consumer<Edge<LineSegment>> matchVisitor) {
    super(matchVisitor);
    this.lineSegment = lineSegment;
    this.maxDistance = maxDistance;
  }

  @Override
  public void accept(final Edge<LineSegment> edge) {
    if (test(edge)) {
      super.accept(edge);
    }
  }

  @Override
  public boolean test(final Edge<LineSegment> edge) {
    final LineSegment lineSegment = edge.getObject();
    final double distance = lineSegment.distance(this.lineSegment);
    if (distance <= this.maxDistance) {
      return true;
    } else {
      return false;
    }
  }
}
