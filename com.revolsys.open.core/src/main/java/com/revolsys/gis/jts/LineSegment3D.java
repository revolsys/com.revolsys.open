package com.revolsys.gis.jts;

import com.revolsys.gis.model.coordinates.AbstractCoordinates;
import com.revolsys.jts.geom.Coordinate;
import com.revolsys.jts.geom.Coordinates;
import com.revolsys.jts.geom.Envelope;
import com.revolsys.jts.geom.LineSegment;
import com.revolsys.jts.geom.PrecisionModel;
import com.revolsys.util.MathUtil;

public class LineSegment3D extends LineSegment {

  /**
   * 
   */
  private static final long serialVersionUID = -4858771831440204506L;

  public static Coordinates midpoint(final Coordinates coordinate1,
    final Coordinates coordinate2) {
    final double x1 = coordinate1.getX();
    final double y1 = coordinate1.getY();
    final double z1 = coordinate1.getZ();
    final double x2 = coordinate2.getX();
    final double y2 = coordinate2.getY();
    final double z2 = coordinate2.getZ();
    final double x = MathUtil.midpoint(x1, x2);
    final double y = MathUtil.midpoint(y1, y2);
    double z;
    if (Double.isNaN(z1)) {
      z = z2;
    } else if (Double.isNaN(z2)) {
      z = z1;
    } else {
      z = MathUtil.midpoint(z1, z2);
    }
    final Coordinates coordinate = new Coordinate(x, y, z);
    return coordinate;
  }

  public LineSegment3D() {
  }

  public LineSegment3D(final Coordinate p0, final Coordinate p1) {
    super(p0, p1);
  }

  public LineSegment3D(final LineSegment ls) {
    super(ls);
  }

  /**
   * Add a evelation (z) value for a coordinate that is on this line segment.
   * 
   * @param coordinate The Coordinate.
   * @param line The line segment the coordinate is on.
   */
  public void addElevation(final AbstractCoordinates coordinate) {
    final double z0 = p0.getZ();
    final double z1 = p1.getZ();
    if (!Double.isNaN(z0) && !Double.isNaN(z0)) {
      final double fraction = coordinate.distance(p0) / getLength();
      coordinate.setZ(z0 + (z1 - z0) * (fraction));
    }
  }

  /**
   * Add a evelation (z) value for a coordinate that is on this line segment.
   * 
   * @param coordinate The Coordinate.
   * @param line The line segment the coordinate is on.
   */
  public void addElevation(final AbstractCoordinates coordinate,
    final PrecisionModel model) {
    final double z0 = p0.getZ();
    final double z1 = p1.getZ();
    if (!Double.isNaN(z0) && !Double.isNaN(z0)) {
      final double fraction = coordinate.distance(p0) / getLength();
      coordinate.setZ(model.makePrecise(z0 + (z1 - z0) * (fraction)));
    }
  }

  public Envelope getEnvelope() {
    return new Envelope(p0, p1);
  }

  public Coordinate intersection3D(final LineSegment line) {
    final Coordinate intersection = super.intersection(line);
    if (intersection != null) {
      addElevation(intersection);
    }
    return intersection;
  }

  public Coordinates pointAlong3D(final double segmentLengthFraction) {
    final AbstractCoordinates coord = new Coordinate();
    coord.setX(p0.getX() + segmentLengthFraction * (p1.getX() - p0.getX()));
    coord.setY(p0.getY() + segmentLengthFraction * (p1.getY() - p0.getY()));
    addElevation(coord);
    return coord;
  }
}
