package com.revolsys.gis.jts;

import com.revolsys.util.MathUtil;
import com.revolsys.jts.geom.Coordinate;
import com.revolsys.jts.geom.Envelope;
import com.revolsys.jts.geom.LineSegment;
import com.revolsys.jts.geom.PrecisionModel;

public class LineSegment3D extends LineSegment {

  /**
   * 
   */
  private static final long serialVersionUID = -4858771831440204506L;

  public static Coordinate midpoint(final Coordinate coordinate1,
    final Coordinate coordinate2) {
    final double x1 = coordinate1.x;
    final double y1 = coordinate1.y;
    final double z1 = coordinate1.z;
    final double x2 = coordinate2.x;
    final double y2 = coordinate2.y;
    final double z2 = coordinate2.z;
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
    final Coordinate coordinate = new Coordinate(x, y, z);
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
  public void addElevation(final Coordinate coordinate) {
    final double z0 = p0.z;
    final double z1 = p1.z;
    if (!Double.isNaN(z0) && !Double.isNaN(z0)) {
      final double fraction = coordinate.distance(p0) / getLength();
      coordinate.z = z0 + (z1 - z0) * (fraction);
    }
  }

  /**
   * Add a evelation (z) value for a coordinate that is on this line segment.
   * 
   * @param coordinate The Coordinate.
   * @param line The line segment the coordinate is on.
   */
  public void addElevation(final Coordinate coordinate,
    final PrecisionModel model) {
    final double z0 = p0.z;
    final double z1 = p1.z;
    if (!Double.isNaN(z0) && !Double.isNaN(z0)) {
      final double fraction = coordinate.distance(p0) / getLength();
      coordinate.z = model.makePrecise(z0 + (z1 - z0) * (fraction));
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

  public Coordinate pointAlong3D(final double segmentLengthFraction) {
    final Coordinate coord = new Coordinate();
    coord.x = p0.x + segmentLengthFraction * (p1.x - p0.x);
    coord.y = p0.y + segmentLengthFraction * (p1.y - p0.y);
    addElevation(coord);
    return coord;
  }
}
