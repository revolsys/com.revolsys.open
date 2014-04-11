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

import com.revolsys.jts.algorithm.RayCrossingCounter;
import com.revolsys.jts.geom.Coordinate;
import com.revolsys.jts.geom.Coordinates;
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

  private static Coordinates project(final Coordinates p, final int facingPlane) {
    switch (facingPlane) {
      case Plane3D.XY_PLANE:
        return new Coordinate(p.getX(), p.getY(), Coordinates.NULL_ORDINATE);
      case Plane3D.XZ_PLANE:
        return new Coordinate(p.getX(), p.getZ(), Coordinates.NULL_ORDINATE);
        // Plane3D.YZ
      default:
        return new Coordinate(p.getY(), p.getZ(), Coordinates.NULL_ORDINATE);
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
    final Coordinates sum = new Coordinate(0.0, 0.0, 0.0);
    final Coordinates p1 = new Coordinate(0.0, 0.0, 0.0);
    final Coordinates p2 = new Coordinate(0.0, 0.0, 0.0);
    for (int i = 0; i < n - 1; i++) {
      seq.getCoordinate(i, p1);
      seq.getCoordinate(i + 1, p2);
      sum.setX(sum.getX() + (p1.getY() - p2.getY()) * (p1.getZ() + p2.getZ()));
      sum.setY(sum.getY() + (p1.getZ() - p2.getZ()) * (p1.getX() + p2.getX()));
      sum.setZ(sum.getZ() + (p1.getX() - p2.getX()) * (p1.getY() + p2.getY()));
    }
    sum.setX(sum.getX() / n);
    sum.setY(sum.getY() / n);
    sum.setZ(sum.getZ() / n);
    final Vector3D norm = Vector3D.create(sum).normalize();
    return norm;
  }

  /**
   * Computes a point which is the average of all coordinates
   * in a sequence.
   * If the sequence lies in a single plane,
   * the computed point also lies in the plane.
   * 
   * @param seq a coordinate sequence
   * @return a Coordinate with averaged ordinates
   */
  private Coordinates averagePoint(final CoordinatesList seq) {
    final Coordinates a = new Coordinate(0.0, 0.0, 0.0);
    final int n = seq.size();
    for (int i = 0; i < n; i++) {
      a.setX(a.getX() + seq.getOrdinate(i, CoordinatesList.X));
      a.setY(a.getY() + seq.getOrdinate(i, CoordinatesList.Y));
      a.setZ(a.getZ() + seq.getOrdinate(i, CoordinatesList.Z));
    }
    a.setX(a.getX() / n);
    a.setY(a.getY() / n);
    a.setZ(a.getZ() / n);
    return a;
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
    final Coordinates basePt = averagePoint(seq);
    final Vector3D normal = averageNormal(seq);
    return new Plane3D(normal, basePt);
  }

  public Plane3D getPlane() {
    return plane;
  }

  public Polygon getPolygon() {
    return poly;
  }

  public boolean intersects(final Coordinates intPt) {
    if (Location.EXTERIOR == locate(intPt, poly.getExteriorRing())) {
      return false;
    }

    for (int i = 0; i < poly.getNumInteriorRing(); i++) {
      if (Location.INTERIOR == locate(intPt, poly.getInteriorRingN(i))) {
        return false;
      }
    }
    return true;
  }

  public boolean intersects(final Coordinates pt, final LineString ring) {
    final CoordinatesList seq = ring.getCoordinatesList();
    final CoordinatesList seqProj = project(seq, facingPlane);
    final Coordinates ptProj = project(pt, facingPlane);
    return Location.EXTERIOR != RayCrossingCounter.locatePointInRing(ptProj,
      seqProj);
  }

  private int locate(final Coordinates pt, final LineString ring) {
    final CoordinatesList seq = ring.getCoordinatesList();
    final CoordinatesList seqProj = project(seq, facingPlane);
    final Coordinates ptProj = project(pt, facingPlane);
    return RayCrossingCounter.locatePointInRing(ptProj, seqProj);
  }

}
