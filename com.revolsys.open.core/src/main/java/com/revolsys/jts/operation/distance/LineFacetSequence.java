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
import com.revolsys.jts.geom.BoundingBox;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.LineString;
import com.revolsys.jts.geom.Point;
import com.revolsys.jts.geom.impl.BoundingBoxDoubleGf;
import com.revolsys.util.MathUtil;

/**
 * Represents a sequence of facets (points or line segments)
 * of a {@link Geometry}
 * specified by a subsequence of a {@link LineString}.
 * 
 * @author Martin Davis
 *
 */
public class LineFacetSequence implements FacetSequence {
  private final LineString line;

  private final int start;

  private final int end;

  /**
   * Creates a new sequence for a single point from a LineString.
   * 
   * @param line the sequence holding the points in the facet sequence
   * @param start the index of the point
   */
  public LineFacetSequence(final LineString line, final int start) {
    this.line = line;
    this.start = start;
    this.end = start + 1;
  }

  /**
   * Creates a new section based on a LineString.
   * 
   * @param line the sequence holding the points in the section
   * @param start the index of the start point
   * @param end the index of the end point + 1
   */
  public LineFacetSequence(final LineString line, final int start, final int end) {
    this.line = line;
    this.start = start;
    this.end = end;
  }

  private double computeLineLineDistance(final FacetSequence facetSeq) {
    // both linear - compute minimum segment-segment distance
    double minDistance = Double.MAX_VALUE;

    for (int i = 0; i < getVertexCount() - 1; i++) {
      final double line1X1 = getCoordinate(i, 0);
      final double line1Y1 = getCoordinate(i, 1);
      final double line1X2 = getCoordinate(i + 1, 0);
      final double line1Y2 = getCoordinate(i + 1, 1);
      for (int j = 0; j < facetSeq.getVertexCount() - 1; j++) {
        final double line2X1 = facetSeq.getCoordinate(i, 0);
        final double line2Y1 = facetSeq.getCoordinate(i, 1);
        final double line2X2 = facetSeq.getCoordinate(i + 1, 0);
        final double line2Y2 = facetSeq.getCoordinate(i + 1, 1);

        final double dist = LineSegmentUtil.distanceLineLine(line1X1, line1Y1,
          line1X2, line1Y2, line2X1, line2Y1, line2X2, line2Y2);
        if (dist == 0.0) {
          return 0.0;
        } else if (dist < minDistance) {
          minDistance = dist;
        }
      }
    }
    return minDistance;
  }

  @Override
  public double distance(final FacetSequence facetSeq) {
    final boolean isPoint = isPoint();
    final boolean isPointOther = facetSeq.isPoint();

    if (isPoint) {
      final double x = getCoordinate(0, 0);
      final double y = getCoordinate(0, 1);
      if (isPointOther) {
        final double x2 = facetSeq.getCoordinate(0, 0);
        final double y2 = facetSeq.getCoordinate(0, 1);

        return MathUtil.distance(x, x, x2, y2);
      } else {
        return PointFacetSequence.computePointLineDistance(x, y, facetSeq);
      }
    } else if (isPointOther) {
      final double x = facetSeq.getCoordinate(0, 0);
      final double y = facetSeq.getCoordinate(0, 1);
      return PointFacetSequence.computePointLineDistance(x, y, this);
    } else {
      return computeLineLineDistance(facetSeq);
    }

  }

  @Override
  public Point getCoordinate(final int index) {
    return line.getPoint(start + index);
  }

  @Override
  public double getCoordinate(final int vertexIndex, final int axisIndex) {
    return line.getCoordinate(start + vertexIndex, axisIndex);
  }

  @Override
  public BoundingBox getEnvelope() {
    final BoundingBoxDoubleGf env = new BoundingBoxDoubleGf(line);
    return env;
  }

  @Override
  public int getVertexCount() {
    return end - start;
  }

  @Override
  public boolean isPoint() {
    return end - start == 1;
  }

  @Override
  public String toString() {
    final StringBuilder buf = new StringBuilder();
    buf.append("LINESTRING ( ");
    for (int i = 0; i < getVertexCount(); i++) {
      if (i > 0) {
        buf.append(", ");
      }
      buf.append(getCoordinate(i, 0) + " " + getCoordinate(i, 0));
    }
    buf.append(" )");
    return buf.toString();
  }
}
