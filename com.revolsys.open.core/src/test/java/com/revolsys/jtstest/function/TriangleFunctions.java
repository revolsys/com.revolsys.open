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
package com.revolsys.jtstest.function;

import com.revolsys.jts.geom.Coordinates;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.jts.geom.LineSegmentImpl;
import com.revolsys.jts.geom.LineString;
import com.revolsys.jts.geom.Triangle;
import com.revolsys.jts.geom.util.GeometryMapper;

public class TriangleFunctions {

  public static Geometry angleBisectors(final Geometry g) {
    final Coordinates[] pts = trianglePts(g);
    final Coordinates cc = Triangle.inCentre(pts[0], pts[1], pts[2]);
    final GeometryFactory geomFact = FunctionsUtil.getFactoryOrDefault(g);
    final LineString[] line = new LineString[3];
    line[0] = geomFact.lineString(pts[0], cc);
    line[1] = geomFact.lineString(pts[1], cc);
    line[2] = geomFact.lineString(pts[2], cc);
    return geomFact.multiLineString(line);
  }

  public static Geometry centroid(final Geometry g) {
    return GeometryMapper.map(g, new GeometryMapper.MapOp() {
      @Override
      public Geometry map(final Geometry g) {
        final Coordinates[] pts = trianglePts(g);
        final Coordinates cc = Triangle.centroid(pts[0], pts[1], pts[2]);
        final GeometryFactory geomFact = FunctionsUtil.getFactoryOrDefault(g);
        return geomFact.point(cc);
      }
    });
  }

  public static Geometry circumcentre(final Geometry g) {
    return GeometryMapper.map(g, new GeometryMapper.MapOp() {
      @Override
      public Geometry map(final Geometry g) {
        final Coordinates[] pts = trianglePts(g);
        final Coordinates cc = Triangle.circumcentre(pts[0], pts[1], pts[2]);
        final GeometryFactory geomFact = FunctionsUtil.getFactoryOrDefault(g);
        return geomFact.point(cc);
      }
    });
  }

  public static Geometry incentre(final Geometry g) {
    return GeometryMapper.map(g, new GeometryMapper.MapOp() {
      @Override
      public Geometry map(final Geometry g) {
        final Coordinates[] pts = trianglePts(g);
        final Coordinates cc = Triangle.inCentre(pts[0], pts[1], pts[2]);
        final GeometryFactory geomFact = FunctionsUtil.getFactoryOrDefault(g);
        return geomFact.point(cc);
      }
    });
  }

  public static Geometry perpendicularBisectors(final Geometry g) {
    final Coordinates[] pts = trianglePts(g);
    final Coordinates cc = Triangle.circumcentre(pts[0], pts[1], pts[2]);
    final GeometryFactory geomFact = FunctionsUtil.getFactoryOrDefault(g);
    final LineString[] line = new LineString[3];
    final Coordinates p0 = (new LineSegmentImpl(pts[1], pts[2])).closestPoint(cc);
    line[0] = geomFact.lineString(p0, cc);
    final Coordinates p1 = (new LineSegmentImpl(pts[0], pts[2])).closestPoint(cc);
    line[1] = geomFact.lineString(p1, cc);
    final Coordinates p2 = (new LineSegmentImpl(pts[0], pts[1])).closestPoint(cc);
    line[2] = geomFact.lineString(p2, cc);
    return geomFact.multiLineString(line);
  }

  private static Coordinates[] trianglePts(final Geometry g) {
    final Coordinates[] pts = g.getCoordinateArray();
    if (pts.length < 3) {
      throw new IllegalArgumentException(
        "Input geometry must have at least 3 points");
    }
    return pts;
  }
}
