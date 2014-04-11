/*
 * The JTS Topology Suite is a collection of Java classes that
 * implement the fundamental operations required to validate a given
 * geo-spatial data set to a known topological specification.
 *
 * Copyright (C) 2001 Vivid Solutions
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * For more information, contact:
 *
 *     Vivid Solutions
 *     Suite #1A
 *     2328 Government Street
 *     Victoria BC  V8T 5G5
 *     Canada
 *
 *     (250)385-6040
 *     www.vividsolutions.com
 */

package com.revolsys.jts.linearref;

import com.revolsys.jts.geom.Coordinate;
import com.revolsys.jts.geom.Coordinates;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.LineSegment;
import com.revolsys.jts.util.Assert;

/**
 * Computes the length index of the point
 * on a linear {@link Geometry} nearest a given {@link Coordinates}.
 * The nearest point is not necessarily unique; this class
 * always computes the nearest point closest to
 * the start of the geometry.
 */
class LengthIndexOfPoint {
  public static double indexOf(final Geometry linearGeom,
    final Coordinates inputPt) {
    final LengthIndexOfPoint locater = new LengthIndexOfPoint(linearGeom);
    return locater.indexOf(inputPt);
  }

  public static double indexOfAfter(final Geometry linearGeom,
    final Coordinates inputPt, final double minIndex) {
    final LengthIndexOfPoint locater = new LengthIndexOfPoint(linearGeom);
    return locater.indexOfAfter(inputPt, minIndex);
  }

  private final Geometry linearGeom;

  public LengthIndexOfPoint(final Geometry linearGeom) {
    this.linearGeom = linearGeom;
  }

  /**
   * Find the nearest location along a linear {@link Geometry} to a given point.
   *
   * @param inputPt the coordinate to locate
   * @return the location of the nearest point
   */
  public double indexOf(final Coordinates inputPt) {
    return indexOfFromStart(inputPt, -1.0);
  }

  /**
   * Finds the nearest index along the linear {@link Geometry}
   * to a given {@link Coordinates}
   * after the specified minimum index.
   * If possible the location returned will be strictly greater than the
   * <code>minLocation</code>.
   * If this is not possible, the
   * value returned will equal <code>minLocation</code>.
   * (An example where this is not possible is when
   * minLocation = [end of line] ).
   *
   * @param inputPt the coordinate to locate
   * @param minIndex the minimum location for the point location
   * @return the location of the nearest point
   */
  public double indexOfAfter(final Coordinates inputPt, final double minIndex) {
    if (minIndex < 0.0) {
      return indexOf(inputPt);
    }

    // sanity check for minIndex at or past end of line
    final double endIndex = linearGeom.getLength();
    if (endIndex < minIndex) {
      return endIndex;
    }

    final double closestAfter = indexOfFromStart(inputPt, minIndex);
    /**
     * Return the minDistanceLocation found.
     */
    Assert.isTrue(closestAfter >= minIndex,
      "computed index is before specified minimum index");
    return closestAfter;
  }

  private double indexOfFromStart(final Coordinates inputPt,
    final double minIndex) {
    double minDistance = Double.MAX_VALUE;

    double ptMeasure = minIndex;
    double segmentStartMeasure = 0.0;
    final LineSegment seg = new LineSegment();
    final LinearIterator it = new LinearIterator(linearGeom);
    while (it.hasNext()) {
      if (!it.isEndOfLine()) {
        seg.p0 = it.getSegmentStart();
        seg.p1 = it.getSegmentEnd();
        final double segDistance = seg.distance(inputPt);
        final double segMeasureToPt = segmentNearestMeasure(seg, inputPt,
          segmentStartMeasure);
        if (segDistance < minDistance && segMeasureToPt > minIndex) {
          ptMeasure = segMeasureToPt;
          minDistance = segDistance;
        }
        segmentStartMeasure += seg.getLength();
      }
      it.next();
    }
    return ptMeasure;
  }

  private double segmentNearestMeasure(final LineSegment seg,
    final Coordinates inputPt, final double segmentStartMeasure) {
    // found new minimum, so compute location distance of point
    final double projFactor = seg.projectionFactor(inputPt);
    if (projFactor <= 0.0) {
      return segmentStartMeasure;
    }
    if (projFactor <= 1.0) {
      return segmentStartMeasure + projFactor * seg.getLength();
    }
    // projFactor > 1.0
    return segmentStartMeasure + seg.getLength();
  }
}
