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

package com.revolsys.jts.operation.distance;

import com.revolsys.gis.model.coordinates.LineSegmentUtil;
import com.revolsys.jts.algorithm.CGAlgorithms;
import com.revolsys.jts.geom.BoundingBox;
import com.revolsys.jts.geom.PointList;
import com.revolsys.jts.geom.Envelope;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.Point;
import com.revolsys.jts.geom.impl.PointDouble;

/**
 * Represents a sequence of facets (points or line segments)
 * of a {@link Geometry}
 * specified by a subsequence of a {@link PointList}.
 * 
 * @author Martin Davis
 *
 */
public class FacetSequence {
  private final PointList pts;

  private final int start;

  private final int end;

  // temporary Point to materialize points from the PointList
  private Point pt = new PointDouble();

  private Point seqPt = new PointDouble();

  // temporary Point to materialize points from the PointList
  private Point p0 = new PointDouble();

  private Point p1 = new PointDouble();

  private Point q0 = new PointDouble();

  private Point q1 = new PointDouble();

  /**
   * Creates a new sequence for a single point from a PointList.
   * 
   * @param pts the sequence holding the points in the facet sequence
   * @param start the index of the point
   */
  public FacetSequence(final PointList pts, final int start) {
    this.pts = pts;
    this.start = start;
    this.end = start + 1;
  }

  /**
   * Creates a new section based on a PointList.
   * 
   * @param pts the sequence holding the points in the section
   * @param start the index of the start point
   * @param end the index of the end point + 1
   */
  public FacetSequence(final PointList pts, final int start, final int end) {
    this.pts = pts;
    this.start = start;
    this.end = end;
  }

  private double computeLineLineDistance(final FacetSequence facetSeq) {
    // both linear - compute minimum segment-segment distance
    double minDistance = Double.MAX_VALUE;

    for (int i = start; i < end - 1; i++) {
      for (int j = facetSeq.start; j < facetSeq.end - 1; j++) {
        p0 = pts.getPoint(i);
        p1 = pts.getPoint(i + 1);
        q0 = facetSeq.pts.getPoint(j);
        q1 = facetSeq.pts.getPoint(j + 1);

        final double dist = LineSegmentUtil.distanceLineLine(p0, p1, q0, q1);
        if (dist == 0.0) {
          return 0.0;
        }
        if (dist < minDistance) {
          minDistance = dist;
        }
      }
    }
    return minDistance;
  }

  private double computePointLineDistance(final Point pt,
    final FacetSequence facetSeq) {
    double minDistance = Double.MAX_VALUE;

    for (int i = facetSeq.start; i < facetSeq.end - 1; i++) {
      q0 = facetSeq.pts.getPoint(i);
      q1 = facetSeq.pts.getPoint(i + 1);
      final double dist = LineSegmentUtil.distanceLinePoint(q0, q1, pt);
      if (dist == 0.0) {
        return 0.0;
      }
      if (dist < minDistance) {
        minDistance = dist;
      }
    }
    return minDistance;
  }

  public double distance(final FacetSequence facetSeq) {
    final boolean isPoint = isPoint();
    final boolean isPointOther = facetSeq.isPoint();

    if (isPoint && isPointOther) {
      pt = pts.getPoint(start);
      seqPt = facetSeq.pts.getPoint(facetSeq.start);
      return pt.distance(seqPt);
    } else if (isPoint) {
      pt = pts.getPoint(start);
      return computePointLineDistance(pt, facetSeq);
    } else if (isPointOther) {
      seqPt = facetSeq.pts.getPoint(facetSeq.start);
      return computePointLineDistance(seqPt, this);
    }
    return computeLineLineDistance(facetSeq);

  }

  public Point getCoordinate(final int index) {
    return pts.getPoint(start + index);
  }

  public BoundingBox getEnvelope() {
    final Envelope env = new Envelope(pts);
    return env;
  }

  public boolean isPoint() {
    return end - start == 1;
  }

  public int size() {
    return end - start;
  }

  @Override
  public String toString() {
    final StringBuffer buf = new StringBuffer();
    buf.append("LINESTRING ( ");
    for (int i = start; i < end; i++) {
      if (i > start) {
        buf.append(", ");
      }
      buf.append(pts.getX(i) + " " + pts.getY(i));
    }
    buf.append(" )");
    return buf.toString();
  }
}
