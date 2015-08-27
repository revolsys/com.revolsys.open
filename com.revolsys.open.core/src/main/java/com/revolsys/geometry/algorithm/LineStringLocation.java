package com.revolsys.geometry.algorithm;

import com.revolsys.geometry.model.LineString;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.impl.PointDouble;

/**
 * Represents a location along a {@link LineString}.
 */
public class LineStringLocation implements Comparable<LineStringLocation> {

  public static int compareLocationValues(final int segmentIndex0, final double segmentFraction0,
    final int segmentIndex1, final double segmentFraction1) {
    // compare segments
    if (segmentIndex0 < segmentIndex1) {
      return -1;
    }
    if (segmentIndex0 > segmentIndex1) {
      return 1;
    }
    // same segment, so compare segment fraction
    if (segmentFraction0 < segmentFraction1) {
      return -1;
    }
    if (segmentFraction0 > segmentFraction1) {
      return 1;
    }
    // same location
    return 0;
  }

  /**
   * Computes the location of a point a given length along a line segment. If
   * the length exceeds the length of the line segment the last point of the
   * segment is returned. If the length is negative the first point of the
   * segment is returned.
   *
   * @param p0 the first point of the line segment
   * @param p1 the last point of the line segment
   * @param length the length to the desired point
   * @return the {@link Coordinates} of the desired point
   */
  public static Point pointAlongSegmentByFraction(final Point p0, final Point p1,
    final double frac) {
    if (frac <= 0.0) {
      return p0;
    }
    if (frac >= 1.0) {
      return p1;
    }
    final double x = (p1.getX() - p0.getX()) * frac + p0.getX();
    final double y = (p1.getY() - p0.getY()) * frac + p0.getY();
    return new PointDouble(x, y, Point.NULL_ORDINATE);
  }

  private final LineString line;

  private double segmentFraction;

  private int segmentIndex;

  public LineStringLocation(final LineString line, final int segmentIndex,
    final double segmentFraction) {
    this.line = line;
    this.segmentIndex = segmentIndex;
    this.segmentFraction = segmentFraction;
    normalize();
  }

  @Override
  public Object clone() {
    return new LineStringLocation(this.line, this.segmentIndex, this.segmentFraction);
  }

  /**
   * Compares this object with the specified object for order.
   *
   * @param o the <code>LineStringLocation</code> with which this
   *          <code>Coordinate</code> is being compared
   * @return a negative integer, zero, or a positive integer as this
   *         <code>LineStringLocation</code> is less than, equal to, or greater
   *         than the specified <code>LineStringLocation</code>
   */
  @Override
  public int compareTo(final LineStringLocation other) {
    // compare segments
    if (this.segmentIndex < other.segmentIndex) {
      return -1;
    }
    if (this.segmentIndex > other.segmentIndex) {
      return 1;
    }
    // same segment, so compare segment fraction
    if (this.segmentFraction < other.segmentFraction) {
      return -1;
    }
    if (this.segmentFraction > other.segmentFraction) {
      return 1;
    }
    // same location
    return 0;
  }

  public Point getCoordinate() {
    final Point p0 = this.line.getPoint(this.segmentIndex);
    final Point p1 = this.line.getPoint(this.segmentIndex + 1);
    return pointAlongSegmentByFraction(p0, p1, this.segmentFraction);
  }

  public LineString getLine() {
    return this.line;
  }

  public double getSegmentFraction() {
    return this.segmentFraction;
  }

  public int getSegmentIndex() {
    return this.segmentIndex;
  }

  public boolean isFirst() {
    return this.segmentIndex == 0 && this.segmentFraction == 0.0;
  }

  public boolean isLast() {
    return this.segmentIndex == this.line.getVertexCount() - 1 && this.segmentFraction == 1.0;
  }

  public boolean isVertex() {
    return this.segmentFraction <= 0.0 || this.segmentFraction >= 1.0;
  }

  /**
   * Ensures the values in this object are valid
   */
  private void normalize() {
    if (this.segmentFraction < 0.0) {
      this.segmentFraction = 0.0;
    }
    if (this.segmentFraction > 1.0) {
      this.segmentFraction = 1.0;
    }

    if (this.segmentIndex < 0) {
      this.segmentIndex = 0;
      this.segmentFraction = 0.0;
    } else if (this.segmentIndex >= this.line.getVertexCount()) {
      this.segmentIndex = this.line.getVertexCount() - 1;
      this.segmentFraction = 1.0;
    }
  }
}
