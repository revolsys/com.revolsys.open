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

package com.revolsys.jts.algorithm.distance;

import com.revolsys.jts.geom.Coordinate;
import com.revolsys.jts.geom.CoordinateFilter;
import com.revolsys.jts.geom.CoordinateSequenceFilter;
import com.revolsys.jts.geom.Coordinates;
import com.revolsys.jts.geom.CoordinatesList;
import com.revolsys.jts.geom.Geometry;

/**
 * An algorithm for computing a distance metric
 * which is an approximation to the Hausdorff Distance
 * based on a discretization of the input {@link Geometry}.
 * The algorithm computes the Hausdorff distance restricted to discrete points
 * for one of the geometries.
 * The points can be either the vertices of the geometries (the default), 
 * or the geometries with line segments densified by a given fraction.
 * Also determines two points of the Geometries which are separated by the computed distance.
* <p>
 * This algorithm is an approximation to the standard Hausdorff distance.
 * Specifically, 
 * <pre>
 *    for all geometries a, b:    DHD(a, b) <= HD(a, b)
 * </pre>
 * The approximation can be made as close as needed by densifying the input geometries.  
 * In the limit, this value will approach the true Hausdorff distance:
 * <pre>
 *    DHD(A, B, densifyFactor) -> HD(A, B) as densifyFactor -> 0.0
 * </pre>
 * The default approximation is exact or close enough for a large subset of useful cases.
 * Examples of these are:
 * <ul>
 * <li>computing distance between Linestrings that are roughly parallel to each other,
 * and roughly equal in length.  This occurs in matching linear networks.
 * <li>Testing similarity of geometries.
 * </ul>
 * An example where the default approximation is not close is:
 * <pre>
 *   A = LINESTRING (0 0, 100 0, 10 100, 10 100)
 *   B = LINESTRING (0 100, 0 10, 80 10)
 *   
 *   DHD(A, B) = 22.360679774997898
 *   HD(A, B) ~= 47.8
 * </pre>
 */
public class DiscreteHausdorffDistance {
  public static class MaxDensifiedByFractionDistanceFilter implements
    CoordinateSequenceFilter {
    private final PointPairDistance maxPtDist = new PointPairDistance();

    private final PointPairDistance minPtDist = new PointPairDistance();

    private final Geometry geom;

    private int numSubSegs = 0;

    public MaxDensifiedByFractionDistanceFilter(final Geometry geom,
      final double fraction) {
      this.geom = geom;
      numSubSegs = (int)Math.rint(1.0 / fraction);
    }

    @Override
    public void filter(final CoordinatesList seq, final int index) {
      /**
       * This logic also handles skipping Point geometries
       */
      if (index == 0) {
        return;
      }

      final Coordinates p0 = seq.getCoordinate(index - 1);
      final Coordinates p1 = seq.getCoordinate(index);

      final double delx = (p1.getX() - p0.getX()) / numSubSegs;
      final double dely = (p1.getY() - p0.getY()) / numSubSegs;

      for (int i = 0; i < numSubSegs; i++) {
        final double x = p0.getX() + i * delx;
        final double y = p0.getY() + i * dely;
        final Coordinates pt = new Coordinate(x, y, Coordinates.NULL_ORDINATE);
        minPtDist.initialize();
        DistanceToPoint.computeDistance(geom, pt, minPtDist);
        maxPtDist.setMaximum(minPtDist);
      }

    }

    public PointPairDistance getMaxPointDistance() {
      return maxPtDist;
    }

    @Override
    public boolean isDone() {
      return false;
    }

    @Override
    public boolean isGeometryChanged() {
      return false;
    }
  }

  public static class MaxPointDistanceFilter implements CoordinateFilter {
    private final PointPairDistance maxPtDist = new PointPairDistance();

    private final PointPairDistance minPtDist = new PointPairDistance();

    private final DistanceToPoint euclideanDist = new DistanceToPoint();

    private final Geometry geom;

    public MaxPointDistanceFilter(final Geometry geom) {
      this.geom = geom;
    }

    @Override
    public void filter(final Coordinates pt) {
      minPtDist.initialize();
      DistanceToPoint.computeDistance(geom, pt, minPtDist);
      maxPtDist.setMaximum(minPtDist);
    }

    public PointPairDistance getMaxPointDistance() {
      return maxPtDist;
    }
  }

  public static double distance(final Geometry g0, final Geometry g1) {
    final DiscreteHausdorffDistance dist = new DiscreteHausdorffDistance(g0, g1);
    return dist.distance();
  }

  public static double distance(final Geometry g0, final Geometry g1,
    final double densifyFrac) {
    final DiscreteHausdorffDistance dist = new DiscreteHausdorffDistance(g0, g1);
    dist.setDensifyFraction(densifyFrac);
    return dist.distance();
  }

  private final Geometry g0;

  private final Geometry g1;

  private final PointPairDistance ptDist = new PointPairDistance();

  /**
   * Value of 0.0 indicates that no densification should take place
   */
  private double densifyFrac = 0.0;

  public DiscreteHausdorffDistance(final Geometry g0, final Geometry g1) {
    this.g0 = g0;
    this.g1 = g1;
  }

  private void compute(final Geometry g0, final Geometry g1) {
    computeOrientedDistance(g0, g1, ptDist);
    computeOrientedDistance(g1, g0, ptDist);
  }

  private void computeOrientedDistance(final Geometry discreteGeom,
    final Geometry geom, final PointPairDistance ptDist) {
    final MaxPointDistanceFilter distFilter = new MaxPointDistanceFilter(geom);
    discreteGeom.apply(distFilter);
    ptDist.setMaximum(distFilter.getMaxPointDistance());

    if (densifyFrac > 0) {
      final MaxDensifiedByFractionDistanceFilter fracFilter = new MaxDensifiedByFractionDistanceFilter(
        geom, densifyFrac);
      discreteGeom.apply(fracFilter);
      ptDist.setMaximum(fracFilter.getMaxPointDistance());

    }
  }

  public double distance() {
    compute(g0, g1);
    return ptDist.getDistance();
  }

  public Coordinates[] getCoordinates() {
    return ptDist.getCoordinates();
  }

  public double orientedDistance() {
    computeOrientedDistance(g0, g1, ptDist);
    return ptDist.getDistance();
  }

  /**
   * Sets the fraction by which to densify each segment.
   * Each segment will be (virtually) split into a number of equal-length
   * subsegments, whose fraction of the total length is closest
   * to the given fraction.
   * 
   * @param densifyPercent
   */
  public void setDensifyFraction(final double densifyFrac) {
    if (densifyFrac > 1.0 || densifyFrac <= 0.0) {
      throw new IllegalArgumentException("Fraction is not in range (0.0 - 1.0]");
    }

    this.densifyFrac = densifyFrac;
  }

}
