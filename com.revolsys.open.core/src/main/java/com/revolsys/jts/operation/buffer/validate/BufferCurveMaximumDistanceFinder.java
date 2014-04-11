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

import com.revolsys.jts.geom.Coordinate;
import com.revolsys.jts.geom.CoordinateFilter;
import com.revolsys.jts.geom.CoordinateSequenceFilter;
import com.revolsys.jts.geom.Coordinates;
import com.revolsys.jts.geom.CoordinatesList;
import com.revolsys.jts.geom.Geometry;

/**
 * Finds the approximate maximum distance from a buffer curve to
 * the originating geometry.
 * This is similar to the Discrete Oriented Hausdorff distance
 * from the buffer curve to the input.
 * <p>
 * The approximate maximum distance is determined by testing
 * all vertices in the buffer curve, as well
 * as midpoints of the curve segments.
 * Due to the way buffer curves are constructed, this
 * should be a very close approximation.
 * 
 * @author mbdavis
 *
 */
public class BufferCurveMaximumDistanceFinder {
  public static class MaxMidpointDistanceFilter implements
    CoordinateSequenceFilter {
    private final PointPairDistance maxPtDist = new PointPairDistance();

    private final PointPairDistance minPtDist = new PointPairDistance();

    private final Geometry geom;

    public MaxMidpointDistanceFilter(final Geometry geom) {
      this.geom = geom;
    }

    @Override
    public void filter(final CoordinatesList seq, final int index) {
      if (index == 0) {
        return;
      }

      final Coordinates p0 = seq.getCoordinate(index - 1);
      final Coordinates p1 = seq.getCoordinate(index);
      final Coordinates midPt = new Coordinate((p0.getX() + p1.getX()) / 2,
        (p0.getY() + p1.getY()) / 2, Coordinates.NULL_ORDINATE);

      minPtDist.initialize();
      DistanceToPointFinder.computeDistance(geom, midPt, minPtDist);
      maxPtDist.setMaximum(minPtDist);
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

    private final Geometry geom;

    public MaxPointDistanceFilter(final Geometry geom) {
      this.geom = geom;
    }

    @Override
    public void filter(final Coordinates pt) {
      minPtDist.initialize();
      DistanceToPointFinder.computeDistance(geom, pt, minPtDist);
      maxPtDist.setMaximum(minPtDist);
    }

    public PointPairDistance getMaxPointDistance() {
      return maxPtDist;
    }
  }

  private final Geometry inputGeom;

  private final PointPairDistance maxPtDist = new PointPairDistance();

  public BufferCurveMaximumDistanceFinder(final Geometry inputGeom) {
    this.inputGeom = inputGeom;
  }

  private void computeMaxMidpointDistance(final Geometry curve) {
    final MaxMidpointDistanceFilter distFilter = new MaxMidpointDistanceFilter(
      inputGeom);
    curve.apply(distFilter);
    maxPtDist.setMaximum(distFilter.getMaxPointDistance());
  }

  private void computeMaxVertexDistance(final Geometry curve) {
    final MaxPointDistanceFilter distFilter = new MaxPointDistanceFilter(
      inputGeom);
    curve.apply(distFilter);
    maxPtDist.setMaximum(distFilter.getMaxPointDistance());
  }

  public double findDistance(final Geometry bufferCurve) {
    computeMaxVertexDistance(bufferCurve);
    computeMaxMidpointDistance(bufferCurve);
    return maxPtDist.getDistance();
  }

  public PointPairDistance getDistancePoints() {
    return maxPtDist;
  }

}
