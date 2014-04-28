package com.revolsys.gis.algorithm;

import com.revolsys.gis.model.coordinates.LineSegmentUtil;
import com.revolsys.jts.geom.Coordinates;
import com.revolsys.jts.geom.LineString;
import com.revolsys.jts.geom.segment.Segment;
import com.revolsys.jts.util.Assert;

/**
 * Computes the {@link LineStringLocation} of the point on a {@link LineString}
 * nearest a given point. The nearest point is not necessarily unique, but this
 * class always computes the nearest point closest to the start of the
 * linestring.
 */
public class LocationOfPoint {

  public static LineStringLocation locate(final LineString line,
    final Coordinates inputPt) {
    final LocationOfPoint locater = new LocationOfPoint(line);
    return locater.locate(inputPt);
  }

  public static double segmentFraction(final Coordinates p0,
    final Coordinates p1, final Coordinates inputPt) {
    double segFrac = LineSegmentUtil.projectionFactor(p0, p1, inputPt);
    if (segFrac < 0.0) {
      segFrac = 0.0;
    } else if (segFrac > 1.0) {
      segFrac = 1.0;
    }
    return segFrac;
  }

  public static double segmentFraction(final Segment segment,
    final Coordinates inputPt) {
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
  private boolean isGreater(final int i, final double segFrac,
    final LineStringLocation loc) {
    return LineStringLocation.compareLocationValues(i, segFrac,
      loc.getSegmentIndex(), loc.getSegmentFraction()) > 0;
  }

  /**
   * Find the nearest location along a {@link LineString} to a given point.
   * 
   * @param inputPt the coordinate to locate
   * @return the location of the nearest point
   */
  public LineStringLocation locate(final Coordinates inputPt) {
    double minDistance = Double.MAX_VALUE;
    int minIndex = 0;
    double minFrac = -1.0;

    int i = 0;
    for (final Segment segment : line.segments()) {
      final double segDistance = segment.distance(inputPt);
      final double segFrac = segmentFraction(segment, inputPt);

      if (segDistance < minDistance) {
        minIndex = i;
        minFrac = segFrac;
        minDistance = segDistance;
      }
      i++;
    }
    return new LineStringLocation(line, minIndex, minFrac);
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
  public LineStringLocation locateAfter(final Coordinates inputPt,
    final LineStringLocation minLocation) {
    if (minLocation == null) {
      return locate(inputPt);
    }

    // sanity check for minLocation at or past end of line
    if (minLocation.getSegmentIndex() > line.getSegmentCount()) {
      return new LineStringLocation(line, line.getSegmentCount(), 1.0);
    } else {
      final int startIndex = minLocation.getSegmentIndex();
      final Segment segment = line.getSegment(startIndex - 1);
      double minDistance = Double.MAX_VALUE;
      LineStringLocation nextClosestLocation = minLocation;
      int i = startIndex;
      while (segment.hasNext()) {
        segment.next();
        final double segDistance = segment.distance(inputPt);
        final double segFrac = segment.segmentFraction(inputPt);

        if (segDistance < minDistance && isGreater(i, segFrac, minLocation)) {
          nextClosestLocation = new LineStringLocation(line, i, segFrac);
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
