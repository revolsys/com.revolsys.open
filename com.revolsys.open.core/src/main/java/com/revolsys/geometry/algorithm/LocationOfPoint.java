package com.revolsys.geometry.algorithm;

import com.revolsys.geometry.model.LineString;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.coordinates.LineSegmentUtil;
import com.revolsys.geometry.model.segment.Segment;
import com.revolsys.geometry.util.Assert;

/**
 * Computes the {@link LineStringLocation} of the point on a {@link LineString}
 * nearest a given point. The nearest point is not necessarily unique, but this
 * class always computes the nearest point closest to the start of the
 * linestring.
 */
public class LocationOfPoint {

  public static LineStringLocation locate(final LineString line, final Point inputPt) {
    final LocationOfPoint locater = new LocationOfPoint(line);
    return locater.locate(inputPt);
  }

  public static double segmentFraction(final Point p0, final Point p1, final Point inputPt) {
    double segFrac = LineSegmentUtil.projectionFactor(p0, p1, inputPt);
    if (segFrac < 0.0) {
      segFrac = 0.0;
    } else if (segFrac > 1.0) {
      segFrac = 1.0;
    }
    return segFrac;
  }

  public static double segmentFraction(final Segment segment, final Point inputPt) {
    double segFrac = segment.projectionFactor(inputPt);
    if (segFrac < 0.0) {
      segFrac = 0.0;
    } else if (segFrac > 1.0) {
      segFrac = 1.0;
    }
    return segFrac;
  }

  private final LineString line;

  public LocationOfPoint(final LineString line) {
    this.line = line;
  }

  /**
   * Tests whether a location given by a <index, segmentFraction> pair is
   * located after a {@link LineStringLocation}.
   *
   * @param i the segment index
   * @param segFrac the fraction along the segment
   * @param loc a location
   * @return <code>true</code> if the first location is greater than the second
   */
  private boolean isGreater(final int i, final double segFrac, final LineStringLocation loc) {
    return LineStringLocation.compareLocationValues(i, segFrac, loc.getSegmentIndex(),
      loc.getSegmentFraction()) > 0;
  }

  /**
   * Find the nearest location along a {@link LineString} to a given point.
   *
   * @param inputPt the coordinate to locate
   * @return the location of the nearest point
   */
  public LineStringLocation locate(final Point inputPt) {
    double minDistance = Double.MAX_VALUE;
    int minIndex = 0;
    double minFrac = -1.0;

    int i = 0;
    for (final Segment segment : this.line.segments()) {
      final double segDistance = segment.distance(inputPt);
      final double segFrac = segmentFraction(segment, inputPt);

      if (segDistance < minDistance) {
        minIndex = i;
        minFrac = segFrac;
        minDistance = segDistance;
      }
      i++;
    }
    return new LineStringLocation(this.line, minIndex, minFrac);
  }

  /**
   * Find the nearest location along a {@link LineString} to a given point after
   * the specified minimum {@link LineStringLocation}. If possible the location
   * returned will be strictly greater than the <code>minLocation</code>. If
   * this is not possible, the value returned will equal
   * <code>minLocation</code>. (An example where this is not possible is when
   * minLocation = [end of line] ).
   *
   * @param inputPt the coordinate to locate
   * @param minLocation the minimum location for the point location
   * @return the location of the nearest point
   */
  public LineStringLocation locateAfter(final Point inputPt, final LineStringLocation minLocation) {
    if (minLocation == null) {
      return locate(inputPt);
    }

    // sanity check for minLocation at or past end of line
    if (minLocation.getSegmentIndex() > this.line.getSegmentCount()) {
      return new LineStringLocation(this.line, this.line.getSegmentCount(), 1.0);
    } else {
      final int startIndex = minLocation.getSegmentIndex();
      final Segment segment = this.line.getSegment(startIndex - 1);
      double minDistance = Double.MAX_VALUE;
      LineStringLocation nextClosestLocation = minLocation;
      int i = startIndex;
      while (segment.hasNext()) {
        segment.next();
        final double segDistance = segment.distance(inputPt);
        final double segFrac = segment.segmentFraction(inputPt);

        if (segDistance < minDistance && isGreater(i, segFrac, minLocation)) {
          nextClosestLocation = new LineStringLocation(this.line, i, segFrac);
          minDistance = segDistance;
        }
        i++;
      }
      /**
       * Return the minDistanceLocation found. This will not be null, since it was
       * initialized to minLocation
       */
      Assert.isTrue(nextClosestLocation.compareTo(minLocation) >= 0,
        "computed location is before specified minimum location");
      return nextClosestLocation;
    }
  }
}
