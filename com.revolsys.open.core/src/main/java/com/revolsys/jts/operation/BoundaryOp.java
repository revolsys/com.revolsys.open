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
package com.revolsys.jts.operation;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.revolsys.jts.algorithm.BoundaryNodeRule;
import com.revolsys.jts.geom.CoordinateArrays;
import com.revolsys.jts.geom.PointList;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.GeometryCollection;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.jts.geom.LineString;
import com.revolsys.jts.geom.MultiLineString;
import com.revolsys.jts.geom.MultiPoint;
import com.revolsys.jts.geom.Point;

/**
 * Computes the boundary of a {@link Geometry}.
 * Allows specifying the {@link BoundaryNodeRule} to be used.
 * This operation will always return a {@link Geometry} of the appropriate
 * dimension for the boundary (even if the input geometry is empty).
 * The boundary of zero-dimensional geometries (Points) is
 * always the empty {@link GeometryCollection}.
 *
 * @author Martin Davis
 * @version 1.7
 */

public class BoundaryOp {
  private final Geometry geom;

  private final GeometryFactory geomFact;

  private final BoundaryNodeRule bnRule;

  private Map endpointMap;

  public BoundaryOp(final Geometry geom) {
    this(geom, BoundaryNodeRule.MOD2_BOUNDARY_RULE);
  }

  public BoundaryOp(final Geometry geom, final BoundaryNodeRule bnRule) {
    this.geom = geom;
    geomFact = geom.getGeometryFactory();
    this.bnRule = bnRule;
  }

  private void addEndpoint(final Point pt) {
    Counter counter = (Counter)endpointMap.get(pt);
    if (counter == null) {
      counter = new Counter();
      endpointMap.put(pt, counter);
    }
    counter.count++;
  }

  private Geometry boundaryLineString(final LineString line) {
    if (geom.isEmpty()) {
      return getEmptyMultiPoint();
    }

    if (line.isClosed()) {
      // check whether endpoints of valence 2 are on the boundary or not
      final boolean closedEndpointOnBoundary = bnRule.isInBoundary(2);
      if (closedEndpointOnBoundary) {
        return line.getStartPoint();
      } else {
        return geomFact.multiPoint((Point[])null);
      }
    }
    return geomFact.multiPoint(new Point[] {
      line.getStartPoint(), line.getEndPoint()
    });
  }

  /*
   * // MD - superseded private Point[]
   * computeBoundaryFromGeometryGraph(MultiLineString mLine) { GeometryGraph g =
   * new GeometryGraph(0, mLine, bnRule); Point[] bdyPts =
   * g.getBoundaryPoints(); return bdyPts; }
   */

  private Geometry boundaryMultiLineString(final MultiLineString mLine) {
    if (geom.isEmpty()) {
      return getEmptyMultiPoint();
    }

    final Point[] bdyPts = computeBoundaryCoordinates(mLine);

    // return Point or MultiPoint
    if (bdyPts.length == 1) {
      return geomFact.point(bdyPts[0]);
    }
    // this handles 0 points case as well
    return geomFact.multiPoint(bdyPts);
  }

  private Point[] computeBoundaryCoordinates(final MultiLineString mLine) {
    final List bdyPts = new ArrayList();
    endpointMap = new TreeMap();
    for (int i = 0; i < mLine.getGeometryCount(); i++) {
      final LineString line = (LineString)mLine.getGeometry(i);
      if (line.getVertexCount() == 0) {
        continue;
      }
      addEndpoint(line.getCoordinate(0));
      addEndpoint(line.getCoordinate(line.getVertexCount() - 1));
    }

    for (final Iterator it = endpointMap.entrySet().iterator(); it.hasNext();) {
      final Map.Entry entry = (Map.Entry)it.next();
      final Counter counter = (Counter)entry.getValue();
      final int valence = counter.count;
      if (bnRule.isInBoundary(valence)) {
        bdyPts.add(entry.getKey());
      }
    }

    return CoordinateArrays.toCoordinateArray(bdyPts);
  }

  public Geometry getBoundary() {
    if (geom instanceof LineString) {
      return boundaryLineString((LineString)geom);
    }
    if (geom instanceof MultiLineString) {
      return boundaryMultiLineString((MultiLineString)geom);
    }
    return geom.getBoundary();
  }

  private MultiPoint getEmptyMultiPoint() {
    return geomFact.multiPoint((PointList)null);
  }
}

/**
 * Stores an integer count, for use as a Map entry.
 *
 * @author Martin Davis
 * @version 1.7
 */
class Counter {
  /**
   * The value of the count
   */
  int count;
}
