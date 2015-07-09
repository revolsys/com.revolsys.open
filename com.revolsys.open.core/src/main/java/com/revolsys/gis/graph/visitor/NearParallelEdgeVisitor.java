package com.revolsys.gis.graph.visitor;

import com.revolsys.gis.graph.Edge;
import com.revolsys.gis.graph.EdgeVisitor;
import com.revolsys.gis.model.coordinates.LineSegmentUtil;
import com.revolsys.jts.geom.BoundingBox;
import com.revolsys.jts.geom.LineString;
import com.revolsys.jts.geom.Point;
import com.revolsys.math.Angle;

public class NearParallelEdgeVisitor<T> extends EdgeVisitor<T> {

  private final LineString line;

  private final double maxDistance;

  public NearParallelEdgeVisitor(final LineString line, final double maxDistance) {
    this.line = line;
    this.maxDistance = maxDistance;
  }

  @Override
  public BoundingBox getEnvelope() {
    BoundingBox envelope = this.line.getBoundingBox();
    envelope = envelope.expand(this.maxDistance);
    return envelope;
  }

  private boolean isAlmostParallel(final LineString matchLine) {
    if (this.line.getBoundingBox().distance(matchLine.getBoundingBox()) > this.maxDistance) {
      return false;
    }
    final LineString coords = this.line;
    final LineString matchCoords = this.line;
    Point previousCoordinate = coords.getPoint(0);
    for (int i = 1; i < coords.getVertexCount(); i++) {
      final Point coordinate = coords.getPoint(i);
      Point previousMatchCoordinate = matchCoords.getPoint(0);
      for (int j = 1; j < coords.getVertexCount(); j++) {
        final Point matchCoordinate = matchCoords.getPoint(i);
        final double distance = LineSegmentUtil.distanceLineLine(previousCoordinate, coordinate,
          previousMatchCoordinate, matchCoordinate);
        if (distance <= this.maxDistance) {
          final double angle1 = Angle.normalizePositive(previousCoordinate.angle2d(coordinate));
          final double angle2 = Angle
            .normalizePositive(previousMatchCoordinate.angle2d(matchCoordinate));
          final double angleDiff = Math.abs(angle1 - angle2);
          if (angleDiff <= Math.PI / 6) {
            return true;
          }
        }
        previousMatchCoordinate = matchCoordinate;
      }
      previousCoordinate = coordinate;
    }
    return false;
  }

  @Override
  public boolean visit(final Edge<T> edge) {
    final LineString matchLine = edge.getLine();
    if (isAlmostParallel(matchLine)) {
      return super.visit(edge);
    } else {
      return true;
    }
  }
}
