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
package com.revolsys.jts.operation.buffer.validate;

import java.util.ArrayList;
import java.util.List;

import com.revolsys.io.wkt.WktWriter;
import com.revolsys.jts.algorithm.distance.DiscreteHausdorffDistance;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.GeometryCollection;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.jts.geom.LineString;
import com.revolsys.jts.geom.MultiPolygon;
import com.revolsys.jts.geom.Point;
import com.revolsys.jts.geom.Polygon;
import com.revolsys.jts.operation.distance.DistanceOp;

/**
 * Validates that a given buffer curve lies an appropriate distance
 * from the input generating it. 
 * Useful only for round buffers (cap and join).
 * Can be used for either positive or negative distances.
 * <p>
 * This is a heuristic test, and may return false positive results
 * (I.e. it may fail to detect an invalid result.)
 * It should never return a false negative result, however
 * (I.e. it should never report a valid result as invalid.)
 * 
 * @author mbdavis
 *
 */
public class BufferDistanceValidator {
  private static boolean VERBOSE = false;

  /**
   * Maximum allowable fraction of buffer distance the 
   * actual distance can differ by.
   * 1% sometimes causes an error - 1.2% should be safe.
   */
  private static final double MAX_DISTANCE_DIFF_FRAC = .012;

  private final Geometry input;

  private final double bufDistance;

  private final Geometry result;

  private double minValidDistance;

  private double maxValidDistance;

  private double minDistanceFound;

  private double maxDistanceFound;

  private boolean isValid = true;

  private String errMsg = null;

  private Point errorLocation = null;

  private Geometry errorIndicator = null;

  public BufferDistanceValidator(final Geometry input,
    final double bufDistance, final Geometry result) {
    this.input = input;
    this.bufDistance = bufDistance;
    this.result = result;
  }

  /**
   * Checks that the furthest distance from the buffer curve to the input
   * is less than the given maximum distance.
   * This uses the Oriented Hausdorff distance metric.
   * It corresponds to finding
   * the point on the buffer curve which is furthest from <i>some</i> point on the input.
   * 
   * @param input a geometry
   * @param bufCurve a geometry
   * @param maxDist the maximum distance that a buffer result can be from the input
   */
  private void checkMaximumDistance(final Geometry input,
    final Geometry bufCurve, final double maxDist) {
    // BufferCurveMaximumDistanceFinder maxDistFinder = new
    // BufferCurveMaximumDistanceFinder(input);
    // maxDistanceFound = maxDistFinder.findDistance(bufCurve);

    final DiscreteHausdorffDistance haus = new DiscreteHausdorffDistance(
      bufCurve, input);
    haus.setDensifyFraction(0.25);
    maxDistanceFound = haus.orientedDistance();

    if (maxDistanceFound > maxDist) {
      isValid = false;
      final Point[] pts = haus.getCoordinates();
      errorLocation = pts[1];
      errorIndicator = input.getGeometryFactory().lineString(pts);
      errMsg = "Distance between buffer curve and input is too large " + "("
        + maxDistanceFound + " at " + WktWriter.lineString(pts[0], pts[1])
        + ")";
    }
  }

  /**
   * Checks that two geometries are at least a minumum distance apart.
   * 
   * @param g1 a geometry
   * @param g2 a geometry
   * @param minDist the minimum distance the geometries should be separated by
   */
  private void checkMinimumDistance(final Geometry g1, final Geometry g2,
    final double minDist) {
    final DistanceOp distOp = new DistanceOp(g1, g2, minDist);
    minDistanceFound = distOp.distance();

    if (minDistanceFound < minDist) {
      isValid = false;
      final Point[] pts = distOp.nearestPoints();
      errorLocation = distOp.nearestPoints()[1];
      errorIndicator = g1.getGeometryFactory().lineString(pts);
      errMsg = "Distance between buffer curve and input is too small " + "("
        + minDistanceFound + " at " + WktWriter.lineString(pts[0], pts[1])
        + " )";
    }
  }

  private void checkNegativeValid() {
    // Assert: only polygonal inputs can be checked for negative buffers

    // MD - could generalize this to handle GCs too
    if (!(input instanceof Polygon || input instanceof MultiPolygon || input instanceof GeometryCollection)) {
      return;
    }
    final Geometry inputCurve = getPolygonLines(input);
    checkMinimumDistance(inputCurve, result, minValidDistance);
    if (!isValid) {
      return;
    }

    checkMaximumDistance(inputCurve, result, maxValidDistance);
  }

  private void checkPositiveValid() {
    final Geometry bufCurve = result.getBoundary();
    checkMinimumDistance(input, bufCurve, minValidDistance);
    if (!isValid) {
      return;
    }

    checkMaximumDistance(input, bufCurve, maxValidDistance);
  }

  /**
   * Gets a geometry which indicates the location and nature of a validation failure.
   * <p>
   * The indicator is a line segment showing the location and size
   * of the distance discrepancy.
   * 
   * @return a geometric error indicator
   * or null if no error was found
   */
  public Geometry getErrorIndicator() {
    return errorIndicator;
  }

  public Point getErrorLocation() {
    return errorLocation;
  }

  public String getErrorMessage() {
    return errMsg;
  }

  private Geometry getPolygonLines(final Geometry geometry) {
    final List<LineString> lines = new ArrayList<>();
    for (final Polygon polygon : geometry.getGeometries(Polygon.class)) {
      lines.addAll(polygon.getRings());
    }
    final GeometryFactory geometryFactory = geometry.getGeometryFactory();
    return geometryFactory.geometry(lines);
  }

  public boolean isValid() {
    final double posDistance = Math.abs(bufDistance);
    final double distDelta = MAX_DISTANCE_DIFF_FRAC * posDistance;
    minValidDistance = posDistance - distDelta;
    maxValidDistance = posDistance + distDelta;

    // can't use this test if either is empty
    if (input.isEmpty() || result.isEmpty()) {
      return true;
    }

    if (bufDistance > 0.0) {
      checkPositiveValid();
    } else {
      checkNegativeValid();
    }
    if (VERBOSE) {
      System.out.println("Min Dist= " + minDistanceFound + "  err= "
        + (1.0 - minDistanceFound / bufDistance) + "  Max Dist= "
        + maxDistanceFound + "  err= " + (maxDistanceFound / bufDistance - 1.0));
    }
    return isValid;
  }

  /*
   * private void OLDcheckMaximumDistance(Geometry input, Geometry bufCurve,
   * double maxDist) { BufferCurveMaximumDistanceFinder maxDistFinder = new
   * BufferCurveMaximumDistanceFinder(input); maxDistanceFound =
   * maxDistFinder.findDistance(bufCurve); if (maxDistanceFound > maxDist) {
   * isValid = false; PointPairDistance ptPairDist =
   * maxDistFinder.getDistancePoints(); errorLocation =
   * ptPairDist.getCoordinate(1); errMsg =
   * "Distance between buffer curve and input is too large " + "(" +
   * ptPairDist.getDistance() + " at " + ptPairDist.toString() +")"; } }
   */

}
