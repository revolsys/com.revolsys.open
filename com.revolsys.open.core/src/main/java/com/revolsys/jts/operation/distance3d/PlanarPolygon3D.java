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

package com.revolsys.jts.operation.distance3d;

import com.revolsys.gis.model.coordinates.DoubleCoordinates;
import com.revolsys.jts.algorithm.RayCrossingCounter;
import com.revolsys.jts.geom.Coordinate;
import com.revolsys.jts.geom.Point;
import com.revolsys.jts.geom.CoordinatesList;
import com.revolsys.jts.geom.LineString;
import com.revolsys.jts.geom.Location;
import com.revolsys.jts.geom.Polygon;
import com.revolsys.jts.math.Plane3D;
import com.revolsys.jts.math.Vector3D;

/**
 * Models a polygon lying in a plane in 3-dimensional Cartesian space.
 * The polyogn representation is supplied
 * by a {@link Polygon},
 * containing coordinates with XYZ ordinates.
 * 3D polygons are assumed to lie in a single plane.
 * The plane best fitting the polygon coordinates is
 * computed and is represented by a {@link Plane3D}.
 * 
 * @author mdavis
 *
 */
public class PlanarPolygon3D {

  private static Point project(final Point p, final int facingPlane) {
    switch (facingPlane) {
      case Plane3D.XY_PLANE:
        return new Coordinate(p.getX(), p.getY(), Point.NULL_ORDINATE);
      case Plane3D.XZ_PLANE:
        return new Coordinate(p.getX(), p.getZ(), Point.NULL_ORDINATE);
        // Plane3D.YZ
      default:
        return new Coordinate(p.getY(), p.getZ(), Point.NULL_ORDINATE);
    }
  }

  private static CoordinatesList project(final CoordinatesList seq,
    final int facingPlane) {
    switch (facingPlane) {
      case Plane3D.XY_PLANE:
        return AxisPlaneCoordinateSequence.projectToXY(seq);
      case Plane3D.XZ_PLANE:
        return AxisPlaneCoordinateSequence.projectToXZ(seq);
      default:
        return AxisPlaneCoordinateSequence.projectToYZ(seq);
    }
  }

  private final Plane3D plane;

  private final Polygon poly;

  private int facingPlane = -1;

  public PlanarPolygon3D(final Polygon poly) {
    this.poly = poly;
    plane = findBestFitPlane(poly);
    facingPlane = plane.closestAxisPlane();
  }

  /**
   * Computes an average normal vector from a list of polygon coordinates.
   * Uses Newell's method, which is based
   * on the fact that the vector with components
   * equal to the areas of the projection of the polygon onto 
   * the Cartesian axis planes is normal.
   * 
   * @param seq the sequence of coordinates for the polygon
   * @return a normal vector
   */
  private Vector3D averageNormal(final CoordinatesList seq) {
    final int n = seq.size();
    final double[] sum = new double[3];
    for (int i = 0; i < n - 1; i++) {
      final double x1 = seq.getX(0);
      final double y1 = seq.getY(0);
      final double z1 = seq.getZ(0);
      final double x2 = seq.getX(1);
      final double y2 = seq.getY(1);
      final double z2 = seq.getZ(1);
      sum[0] += (y1 - y2) * (z1 + z2);
      sum[1] += (z1 - z2) * (x1 + x2);
      sum[2] += (x1 - x2) * (y1 + y2);
    }
    sum[0] /= n;
    sum[1] /= n;
    sum[2] /= n;
    final Vector3D norm = Vector3D.create(new DoubleCoordinates(sum))
      .normalize();
    return norm;
  }

  /**
   * Computes a point which is the average of all coordinates
   * in a sequence.
   * If the sequence lies in a single plane,
   * the computed point also lies in the plane.
   * 
   * @param seq a coordinate sequence
   * @return a Point with averaged ordinates
   */
  private Point averagePoint(final CoordinatesList seq) {
    final double[] a = new double[3];
    final int n = seq.size();
    for (int i = 0; i < n; i++) {
      a[0] += seq.getValue(i, CoordinatesList.X);
      a[1] += seq.getValue(i, CoordinatesList.Y);
      a[2] += seq.getValue(i, CoordinatesList.Z);
    }
    a[0] /= n;
    a[1] /= n;
    a[2] /= n;
    return new DoubleCoordinates(a);
  }

  /**
   * Finds a best-fit plane for the polygon, 
   * by sampling a few points from the exterior ring.
   * <p>
   * The algorithm used is Newell's algorithm:
   * - a base point for the plane is determined from the average of all vertices
   * - the normal vector is determined by
   *   computing the area of the projections on each of the axis planes
   * 
   * @param poly the polygon to determine the plane for
   * @return the best-fit plane
   */
  private Plane3D findBestFitPlane(final Polygon poly) {
    final CoordinatesList seq = poly.getExteriorRing().getCoordinatesList();
    final Point basePt = averagePoint(seq);
    final Vector3D normal = averageNormal(seq);
    return new Plane3D(normal, basePt);
  }

  public Plane3D getPlane() {
    return plane;
  }

  public Polygon getPolygon() {
    return poly;
  }

  public boolean intersects(final Point intPt) {
    if (Location.EXTERIOR == locate(intPt, poly.getExteriorRing())) {
      return false;
    }

    for (int i = 0; i < poly.getNumInteriorRing(); i++) {
      if (Location.INTERIOR == locate(intPt, poly.getInteriorRing(i))) {
        return false;
      }
    }
    return true;
  }

  public boolean intersects(final Point pt, final LineString ring) {
    final CoordinatesList seq = ring.getCoordinatesList();
    final CoordinatesList seqProj = project(seq, facingPlane);
    final Point ptProj = project(pt, facingPlane);
    return Location.EXTERIOR != RayCrossingCounter.locatePointInRing(ptProj,
      seqProj);
  }

  private Location locate(final Point pt, final LineString ring) {
    final CoordinatesList seq = ring.getCoordinatesList();
    final CoordinatesList seqProj = project(seq, facingPlane);
    final Point ptProj = project(pt, facingPlane);
    return RayCrossingCounter.locatePointInRing(ptProj, seqProj);
  }

}
