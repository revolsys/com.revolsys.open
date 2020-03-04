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
package com.revolsys.core.test.geometry.test.function;

import java.util.List;

import com.revolsys.geometry.algorithm.distance.DiscreteHausdorffDistance;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.operation.distance.DistanceWithPoints;

public class DistanceFunctions {
  public static Geometry densifiedDiscreteHausdorffDistanceLine(final Geometry a, final Geometry b,
    final double frac) {
    final DiscreteHausdorffDistance hausDist = new DiscreteHausdorffDistance(a, b);
    hausDist.setDensifyFraction(frac);
    hausDist.distance();
    return a.getGeometryFactory().lineString(hausDist.getCoordinates());
  }

  public static double discreteHausdorffDistance(final Geometry a, final Geometry b) {
    final DiscreteHausdorffDistance dist = new DiscreteHausdorffDistance(a, b);
    return dist.distance();
  }

  public static Geometry discreteHausdorffDistanceLine(final Geometry a, final Geometry b) {
    final DiscreteHausdorffDistance dist = new DiscreteHausdorffDistance(a, b);
    dist.distance();
    return a.getGeometryFactory().lineString(dist.getCoordinates());
  }

  public static double discreteOrientedHausdorffDistance(final Geometry a, final Geometry b) {
    final DiscreteHausdorffDistance dist = new DiscreteHausdorffDistance(a, b);
    return dist.orientedDistance();
  }

  public static Geometry discreteOrientedHausdorffDistanceLine(final Geometry a, final Geometry b) {
    final DiscreteHausdorffDistance dist = new DiscreteHausdorffDistance(a, b);
    dist.orientedDistance();
    return a.getGeometryFactory().lineString(dist.getCoordinates());
  }

  public static double distance(final Geometry a, final Geometry b) {
    return a.distanceGeometry(b);
  }

  public static boolean isWithinDistance(final Geometry a, final Geometry b, final double dist) {
    return a.isWithinDistance(b, dist);
  }

  public static Geometry nearestPoints(final Geometry a, final Geometry b) {
    final List<Point> pts = DistanceWithPoints.nearestPoints(a, b);
    return a.getGeometryFactory().lineString(pts);
  }

}
