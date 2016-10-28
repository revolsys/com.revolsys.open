/*
 * The JCS Conflation Suite (JCS) is a library of Java classes that
 * can be used to build automated or semi-automated conflation solutions.
 *
 * Copyright (C) 2003 Vivid Solutions
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 * For more information, contact:
 *
 * Vivid Solutions
 * Suite #1A
 * 2328 Government Street
 * Victoria BC  V8T 5G5
 * Canada
 *
 * (250)385-6040
 * www.vividsolutions.com
 */

package com.revolsys.geometry.algorithm;

import com.revolsys.geometry.algorithm.distance.PointPairDistance;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.segment.LineSegment;
import com.revolsys.geometry.model.vertex.Vertex;

/**
 * Implements algorithm for computing a distance metric which can be thought of
 * as the "Vertex Hausdorff Distance". This is the Hausdorff distance restricted
 * to vertices for one of the geometries. Also computes two points of the
 * Geometries which are separated by the computed distance.
 * <p>
 * <b>NOTE: This algorithm does NOT compute the full Hausdorff distance
 * correctly, but an approximation that is correct for a large subset of useful
 * cases. One important part of this subset is Linestrings that are roughly
 * parallel to each other, and roughly equal in length - just what is needed for
 * line matching. </b>
 */
public class VertexHausdorffDistance {

  public static double distance(final Geometry g0, final Geometry g1) {
    final VertexHausdorffDistance vhd = new VertexHausdorffDistance(g0, g1);
    return vhd.distance();
  }

  private final PointPairDistance ptDist = new PointPairDistance();

  public VertexHausdorffDistance(final Geometry g0, final Geometry g1) {
    compute(g0, g1);
  }

  public VertexHausdorffDistance(final LineSegment seg0, final LineSegment seg1) {
    compute(seg0, seg1);
  }

  private void compute(final Geometry g0, final Geometry g1) {
    computeMaxPointDistance(g0, g1, this.ptDist);
    computeMaxPointDistance(g1, g0, this.ptDist);
  }

  private void compute(final LineSegment seg0, final LineSegment seg1) {
    computeMaxPointDistance(seg0, seg1, this.ptDist);
    computeMaxPointDistance(seg1, seg0, this.ptDist);
  }

  private void computeMaxPointDistance(final Geometry pointGeom, final Geometry geom,
    final PointPairDistance ptDist) {
    ptDist.setMaximum(ptDist);
    final EuclideanDistanceToPoint euclideanDist = new EuclideanDistanceToPoint();
    final PointPairDistance maxPtDist = new PointPairDistance();
    final PointPairDistance minPtDist = new PointPairDistance();
    for (final Vertex vertex : pointGeom.vertices()) {
      minPtDist.initialize();
      euclideanDist.computeDistance(geom, vertex, minPtDist);
      maxPtDist.setMaximum(minPtDist);
    }
    ptDist.setMaximum(maxPtDist);
  }

  /**
   * Computes the maximum oriented distance between two line segments, as well
   * as the point pair separated by that distance.
   *
   * @param seg0 the line segment containing the furthest point
   * @param seg1 the line segment containing the closest point
   * @param ptDist the point pair and distance to be updated
   */
  private void computeMaxPointDistance(final LineSegment seg0, final LineSegment seg1,
    final PointPairDistance ptDist) {
    final Point closestPt0 = seg0.closestPoint(seg1.getP0());
    ptDist.setMaximum(closestPt0, seg1.getP0());
    final Point closestPt1 = seg0.closestPoint(seg1.getP1());
    ptDist.setMaximum(closestPt1, seg1.getP1());
  }

  public double distance() {
    return this.ptDist.getDistance();
  }

  public Point[] getCoordinates() {
    return this.ptDist.getPoints();
  }

}
