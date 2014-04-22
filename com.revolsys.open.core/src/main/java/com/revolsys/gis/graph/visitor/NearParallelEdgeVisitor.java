package com.revolsys.gis.graph.visitor;

import com.revolsys.gis.graph.Edge;
import com.revolsys.gis.graph.EdgeVisitor;
import com.revolsys.jts.algorithm.Angle;
import com.revolsys.jts.algorithm.CGAlgorithms;
import com.revolsys.jts.geom.BoundingBox;
import com.revolsys.jts.geom.Coordinates;
import com.revolsys.jts.geom.CoordinatesList;
import com.revolsys.jts.geom.LineString;

public class NearParallelEdgeVisitor<T> extends EdgeVisitor<T> {

  private final LineString line;

  private final double maxDistance;

  public NearParallelEdgeVisitor(final LineString line, final double maxDistance) {
    this.line = line;
    this.maxDistance = maxDistance;
  }

  @Override
  public BoundingBox getEnvelope() {
    BoundingBox envelope = line.getBoundingBox();
    envelope = envelope.expand(maxDistance);
    return envelope;
  }

  private boolean isAlmostParallel(final LineString matchLine) {
    if (line.getBoundingBox().distance(matchLine.getBoundingBox()) > maxDistance) {
      return false;
    }
    final CoordinatesList coords = line.getCoordinatesList();
    final CoordinatesList matchCoords = line.getCoordinatesList();
    Coordinates previousCoordinate = coords.getCoordinate(0);
    for (int i = 1; i < coords.size(); i++) {
      final Coordinates coordinate = coords.getCoordinate(i);
      Coordinates previousMatchCoordinate = matchCoords.getCoordinate(0);
      for (int j = 1; j < coords.size(); j++) {
        final Coordinates matchCoordinate = matchCoords.getCoordinate(i);
        final double distance = CGAlgorithms.distanceLineLine(
          previousCoordinate, coordinate, previousMatchCoordinate,
          matchCoordinate);
        if (distance <= maxDistance) {
          final double angle1 = Angle.normalizePositive(Angle.angle(
            previousCoordinate, coordinate));
          final double angle2 = Angle.normalizePositive(Angle.angle(
            previousMatchCoordinate, matchCoordinate));
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
