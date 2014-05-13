package com.revolsys.jts.testold.geom;

import com.revolsys.jts.geom.Coordinate;
import com.revolsys.jts.geom.CoordinateList;
import com.revolsys.jts.geom.Point;
import com.revolsys.jts.geom.CoordinatesList;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.LineString;

/**
 * Densifies a LineString
 *
 * @version 1.7
 */
public class SegmentDensifier {
  private final LineString inputLine;

  private CoordinateList newCoords;

  public SegmentDensifier(final LineString line) {
    this.inputLine = line;
  }

  private void densify(final Point p0, final Point p1,
    final double segLength) {
    final double origLen = p1.distance(p0);
    final int nPtsToAdd = (int)Math.floor(origLen / segLength);

    final double delx = p1.getX() - p0.getX();
    final double dely = p1.getY() - p0.getY();

    final double segLenFrac = segLength / origLen;
    for (int i = 0; i <= nPtsToAdd; i++) {
      final double addedPtFrac = i * segLenFrac;
      final Point pt = new Coordinate(p0.getX() + addedPtFrac * delx,
        p0.getY() + addedPtFrac * dely, Point.NULL_ORDINATE);
      this.newCoords.add(pt, false);
    }
    this.newCoords.add(new Coordinate(p1), false);
  }

  public Geometry densify(final double segLength) {
    this.newCoords = new CoordinateList();

    final CoordinatesList seq = this.inputLine.getCoordinatesList();

    this.newCoords.add(seq.getCoordinate(0).cloneCoordinates());

    for (int i = 0; i < seq.size() - 1; i++) {
      final Point p0 = seq.getCoordinate(i);
      final Point p1 = seq.getCoordinate(i + 1);
      densify(p0, p1, segLength);
    }
    final Point[] newPts = this.newCoords.toCoordinateArray();
    return this.inputLine.getGeometryFactory().lineString(newPts);
  }
}
