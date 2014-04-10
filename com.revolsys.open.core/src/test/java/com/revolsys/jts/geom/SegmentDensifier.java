package com.revolsys.jts.geom;

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

  private void densify(final Coordinate p0, final Coordinate p1,
    final double segLength) {
    final double origLen = p1.distance(p0);
    final int nPtsToAdd = (int)Math.floor(origLen / segLength);

    final double delx = p1.x - p0.x;
    final double dely = p1.y - p0.y;

    final double segLenFrac = segLength / origLen;
    for (int i = 0; i <= nPtsToAdd; i++) {
      final double addedPtFrac = i * segLenFrac;
      final Coordinate pt = new Coordinate(p0.x + addedPtFrac * delx, p0.y
        + addedPtFrac * dely);
      this.newCoords.add(pt, false);
    }
    this.newCoords.add(new Coordinate(p1), false);
  }

  public Geometry densify(final double segLength) {
    this.newCoords = new CoordinateList();

    final CoordinatesList seq = this.inputLine.getCoordinatesList();

    final Coordinate p0 = new Coordinate();
    final Coordinate p1 = new Coordinate();
    seq.getCoordinate(0, p0);
    this.newCoords.add(new Coordinate(p0));

    for (int i = 0; i < seq.size() - 1; i++) {
      seq.getCoordinate(i, p0);
      seq.getCoordinate(i + 1, p1);
      densify(p0, p1, segLength);
    }
    final Coordinate[] newPts = this.newCoords.toCoordinateArray();
    return this.inputLine.getGeometryFactory().createLineString(newPts);
  }
}
