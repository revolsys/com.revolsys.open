package com.revolsys.jts.testold.perf.operation.buffer;

import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.jts.geom.LineString;
import com.revolsys.jts.geom.Point;
import com.revolsys.jts.geom.impl.PointDouble;
import com.revolsys.jts.geom.segment.LineSegment;
import com.revolsys.jts.geom.segment.LineSegmentDouble;

/**
 * Generates random {@link LineString}s, which are somewhat coherent
 * in terms of how far they deviate from a given line segment,
 * and how much they twist around.
 * <p>
 * The method is to recursively perturb line segment midpoints by a random offset.
 *
 * @author mbdavis
 *
 */
public class RandomOffsetLineStringGenerator {
  public static Geometry generate(final double maxSegLen, final int numPts,
    final GeometryFactory fact) {
    final RandomOffsetLineStringGenerator rlg = new RandomOffsetLineStringGenerator(
      maxSegLen, numPts);
    return rlg.generate(fact);
  }

  private static int pow2(final int exponent) {
    int pow2 = 1;
    for (int i = 0; i < exponent; i++) {
      pow2 *= 2;
    }
    return pow2;
  }

  private final double maxSegLen;

  private final int numPts;

  private int exponent2 = 5;

  private Point[] pts;

  private Point endPoint;

  public RandomOffsetLineStringGenerator(final double maxSegLen,
    final int numPts) {
    this.maxSegLen = maxSegLen;

    this.exponent2 = (int)(Math.log(numPts) / Math.log(2));
    final int pow2 = pow2(this.exponent2);
    if (pow2 < numPts) {
      this.exponent2 += 1;
    }

    this.numPts = pow2(this.exponent2) + 1;
  }

  private Point computeRandomOffset(final Point p0,
    final Point p1, final double segFrac) {
    final double len = p0.distance(p1);
    final double len2 = len / 2;
    final double offsetLen = len * Math.random() - len2;
    final LineSegment seg = new LineSegmentDouble(p0, p1);
    return seg.pointAlongOffset(segFrac, offsetLen);
  }

  private void computeRandomOffsets(final int inc) {
    final int inc2 = inc / 2;
    for (int i = 0; i + inc2 < this.numPts; i += inc) {
      final int midIndex = i + inc2;
      final int endIndex = i + inc;

      Point segEndPoint;

      double segFrac = 0.5 + randomFractionPerturbation();

      if (endIndex >= this.numPts) {
        segEndPoint = this.endPoint;
        segFrac = midIndex / this.numPts;
      } else {
        segEndPoint = this.pts[i + inc];
      }
      this.pts[midIndex] = computeRandomOffset(this.pts[i], segEndPoint,
        segFrac);
    }
  }

  private void createRandomOffsets(final int interval) {
    // for (int i = 0; i )
    int inc = pow2(this.exponent2);

    while (inc > 1) {
      computeRandomOffsets(inc);
      inc /= 2;
    }
  }

  public Geometry generate(final GeometryFactory fact) {
    this.pts = new Point[this.numPts];

    this.pts[0] = new PointDouble();

    final double ang = Math.PI * Math.random();
    this.endPoint = new PointDouble(this.maxSegLen * Math.cos(ang),
      this.maxSegLen * Math.sin(ang), Point.NULL_ORDINATE);
    this.pts[this.numPts - 1] = this.endPoint;

    int interval = this.numPts / 2;
    while (interval >= 1) {
      createRandomOffsets(interval);
      interval /= 2;
    }
    return fact.lineString(this.pts);
  }

  private double randomFractionPerturbation() {
    final double rnd = Math.random();
    final double mag = rnd * rnd * rnd;
    final int sign = Math.random() > 0.5 ? 1 : -1;
    return sign * mag;
  }

}
