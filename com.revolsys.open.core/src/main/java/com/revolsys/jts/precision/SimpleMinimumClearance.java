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
package com.revolsys.jts.precision;

import com.revolsys.jts.algorithm.CGAlgorithms;
import com.revolsys.jts.geom.Coordinate;
import com.revolsys.jts.geom.CoordinateFilter;
import com.revolsys.jts.geom.CoordinateSequenceFilter;
import com.revolsys.jts.geom.Coordinates;
import com.revolsys.jts.geom.CoordinatesList;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.LineSegment;
import com.revolsys.jts.geom.LineString;

/**
 * Computes the minimum clearance of a geometry or 
 * set of geometries.
 * <p>
 * The <b>Minimum Clearance</b> is a measure of
 * what magnitude of perturbation of its vertices can be tolerated
 * by a geometry before it becomes topologically invalid.
 * <p>
 * This class uses an inefficient O(N^2) scan.  
 * It is primarily for testing purposes.
 * 
 * 
 * @see MinimumClearance
 * @author Martin Davis
 *
 */
public class SimpleMinimumClearance {
  private class ComputeMCCoordinateSequenceFilter implements
    CoordinateSequenceFilter {
    private final Coordinates queryPt;

    public ComputeMCCoordinateSequenceFilter(final Coordinates queryPt) {
      this.queryPt = queryPt;
    }

    private void checkSegmentDistance(final Coordinates seg0,
      final Coordinates seg1) {
      if (queryPt.equals2d(seg0) || queryPt.equals2d(seg1)) {
        return;
      }
      final double segDist = CGAlgorithms.distancePointLine(queryPt, seg1, seg0);
      if (segDist > 0) {
        updateClearance(segDist, queryPt, seg1, seg0);
      }
    }

    private void checkVertexDistance(final Coordinates vertex) {
      final double vertexDist = vertex.distance(queryPt);
      if (vertexDist > 0) {
        updateClearance(vertexDist, queryPt, vertex);
      }
    }

    @Override
    public void filter(final CoordinatesList seq, final int i) {
      // compare to vertex
      checkVertexDistance(seq.getCoordinate(i));

      // compare to segment, if this is one
      if (i > 0) {
        checkSegmentDistance(seq.getCoordinate(i - 1), seq.getCoordinate(i));
      }
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

  private class VertexCoordinateFilter implements CoordinateFilter {
    public VertexCoordinateFilter() {

    }

    @Override
    public void filter(final Coordinates coord) {
      inputGeom.apply(new ComputeMCCoordinateSequenceFilter(coord));
    }
  }

  public static double getDistance(final Geometry g) {
    final SimpleMinimumClearance rp = new SimpleMinimumClearance(g);
    return rp.getDistance();
  }

  public static Geometry getLine(final Geometry g) {
    final SimpleMinimumClearance rp = new SimpleMinimumClearance(g);
    return rp.getLine();
  }

  private final Geometry inputGeom;

  private double minClearance;

  private Coordinates[] minClearancePts;

  public SimpleMinimumClearance(final Geometry geom) {
    inputGeom = geom;
  }

  private void compute() {
    if (minClearancePts != null) {
      return;
    }
    minClearancePts = new Coordinates[2];
    minClearance = Double.MAX_VALUE;
    inputGeom.apply(new VertexCoordinateFilter());
  }

  public double getDistance() {
    compute();
    return minClearance;
  }

  public LineString getLine() {
    compute();
    return inputGeom.getGeometryFactory().createLineString(minClearancePts);
  }

  private void updateClearance(final double candidateValue,
    final Coordinates p0, final Coordinates p1) {
    if (candidateValue < minClearance) {
      minClearance = candidateValue;
      minClearancePts[0] = new Coordinate(p0);
      minClearancePts[1] = new Coordinate(p1);
    }
  }

  private void updateClearance(final double candidateValue,
    final Coordinates p, final Coordinates seg0, final Coordinates seg1) {
    if (candidateValue < minClearance) {
      minClearance = candidateValue;
      minClearancePts[0] = new Coordinate(p);
      final LineSegment seg = new LineSegment(seg0, seg1);
      minClearancePts[1] = new Coordinate(seg.closestPoint(p));
    }
  }
}
