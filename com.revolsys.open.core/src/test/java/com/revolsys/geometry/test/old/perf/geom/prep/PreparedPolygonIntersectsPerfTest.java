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
package com.revolsys.geometry.test.old.perf.geom.prep;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.LineString;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.impl.PointDoubleXY;
import com.revolsys.geometry.model.util.SineStarFactory;
import com.revolsys.geometry.util.GeometricShapeFactory;
import com.revolsys.geometry.util.Stopwatch;

public class PreparedPolygonIntersectsPerfTest {
  private static final GeometryFactory geometryFactory = GeometryFactory.floating(0, 2);

  static final int MAX_ITER = 10;

  static final int NUM_AOI_PTS = 2000;

  static final int NUM_LINE_PTS = 100;

  static final int NUM_LINES = 10000;

  public static void main(final String[] args) {
    final PreparedPolygonIntersectsPerfTest test = new PreparedPolygonIntersectsPerfTest();
    test.test();
  }

  Stopwatch sw = new Stopwatch();

  boolean testFailed = false;

  public PreparedPolygonIntersectsPerfTest() {
  }

  Geometry newCircle(final Point origin, final double size, final int nPts) {
    final GeometricShapeFactory gsf = new GeometricShapeFactory();
    gsf.setCentre(origin);
    gsf.setSize(size);
    gsf.setNumPoints(nPts);
    final Geometry circle = gsf.newCircle();
    // Polygon gRect = gsf.createRectangle();
    // Geometry g = gRect.getExteriorRing();
    return circle;
  }

  Geometry newLine(final Point base, final double size, final int nPts) {
    final SineStarFactory gsf = new SineStarFactory();
    gsf.setCentre(base);
    gsf.setSize(size);
    gsf.setNumPoints(nPts);
    final Geometry circle = gsf.newSineStar();
    // System.out.println(circle);
    return circle.getBoundary();
  }

  List newLines(final BoundingBox env, final int nItems, final double size, final int nPts) {
    final int nCells = (int)Math.sqrt(nItems);

    final List geoms = new ArrayList();
    final double width = env.getWidth();
    final double xInc = width / nCells;
    final double yInc = width / nCells;
    for (int i = 0; i < nCells; i++) {
      for (int j = 0; j < nCells; j++) {
        final Point base = new PointDoubleXY(env.getMinX() + i * xInc, env.getMinY() + j * yInc);
        final Geometry line = newLine(base, size, nPts);
        geoms.add(line);
      }
    }
    return geoms;
  }

  Geometry newSineStar(final Point origin, final double size, final int nPts) {
    final SineStarFactory gsf = new SineStarFactory();
    gsf.setCentre(origin);
    gsf.setSize(size);
    gsf.setNumPoints(nPts);
    gsf.setArmLengthRatio(0.1);
    gsf.setNumArms(50);
    final Geometry poly = gsf.newSineStar();
    return poly;
  }

  public void test() {
    test(5);
    test(10);
    test(500);
    test(1000);
    test(2000);
    test(4000);
    /*
     * test(4000); test(8000);
     */
  }

  public void test(final Geometry g, final List lines) {
    // System.out.println("AOI # pts: " + g.getVertexCount() + " # lines: "
    // + lines.size() + " # pts in line: " + NUM_LINE_PTS);

    final Stopwatch sw = new Stopwatch();
    int count = 0;
    for (int i = 0; i < MAX_ITER; i++) {
      // count = testPrepGeomNotCached(i, g, lines);
      count = testPrepGeomCached(i, g, lines);
      // count = testOriginal(i, g, lines);
    }
    // System.out.println("Count of intersections = " + count);
    // System.out.println("Finished in " + sw.getTimeString());
  }

  public void test(final int nPts) {
    // Geometry poly = newCircle(new BaseLasPoint((double)0, 0), 100, nPts);
    final Geometry sinePoly = newSineStar(new PointDoubleXY(0, 0), 100, nPts);
    // System.out.println(poly);
    // Geometry target = sinePoly.getBoundary();
    final Geometry target = sinePoly;

    final List lines = newLines(target.getBoundingBox(), NUM_LINES, 1.0, NUM_LINE_PTS);

    // System.out.println();
    // System.out.println("Running with " + nPts + " points");
    test(target, lines);
  }

  public int testOriginal(final int iter, final Geometry g, final List lines) {
    if (iter == 0) {
      // System.out.println("Using orginal JTS algorithm");
    }
    int count = 0;
    for (final Iterator i = lines.iterator(); i.hasNext();) {
      final LineString line = (LineString)i.next();
      if (g.intersects(line)) {
        count++;
      }
    }
    return count;
  }

  public int testPrepGeomCached(final int iter, final Geometry g, final List lines) {
    if (iter == 0) {
      // System.out.println("Using cached Prepared Geometry");
    }
    final Geometry prepGeom = g.prepare();

    int count = 0;
    for (final Iterator i = lines.iterator(); i.hasNext();) {
      final LineString line = (LineString)i.next();

      if (prepGeom.intersects(line)) {
        count++;
      }
    }
    return count;
  }

  /**
   * Tests using PreparedGeometry, but creating a new
   * Geometry object each time.
   * This tests whether there is a penalty for using
   * the PG algorithm as a complete replacement for
   * the original algorithm.
   *
   * @param g
   * @param lines
   * @return the count
   */
  public int testPrepGeomNotCached(final int iter, final Geometry g, final List lines) {
    if (iter == 0) {
      // System.out.println("Using NON-CACHED Prepared Geometry");
    }
    // Geometry prepGeom = pgFact.create(g);

    int count = 0;
    for (final Iterator i = lines.iterator(); i.hasNext();) {
      final LineString line = (LineString)i.next();

      // test performance of creating the prepared geometry each time
      final Geometry prepGeom = g.prepare();

      if (prepGeom.intersects(line)) {
        count++;
      }
    }
    return count;
  }

}
