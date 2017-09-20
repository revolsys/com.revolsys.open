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
package com.revolsys.geometry.test.old.geom.prep;

import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.LineString;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.impl.PointDoubleXY;
import com.revolsys.geometry.model.prep.PreparedPolygon;
import com.revolsys.geometry.model.util.SineStarFactory;
import com.revolsys.geometry.util.GeometricShapeFactory;

import junit.framework.TestCase;
import junit.textui.TestRunner;

/**
 * Stress tests {@link PreparedPolygon#intersects(Geometry)}
 * to confirm it finds intersections correctly.
 *
 * @author Martin Davis
 *
 */
public class PreparedPolygonIntersectsStressTest extends TestCase {
  private static final GeometryFactory geometryFactory = GeometryFactory.floating2d(0);

  static final int MAX_ITER = 10000;

  public static void main(final String args[]) {
    TestRunner.run(PreparedPolygonIntersectsStressTest.class);
  }

  boolean testFailed = false;

  public PreparedPolygonIntersectsStressTest(final String name) {
    super(name);
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

  Geometry newSineStar(final Point origin, final double size, final int nPts) {
    final SineStarFactory gsf = new SineStarFactory();
    gsf.setCentre(origin);
    gsf.setSize(size);
    gsf.setNumPoints(nPts);
    gsf.setArmLengthRatio(0.1);
    gsf.setNumArms(20);
    final Geometry poly = gsf.newSineStar();
    return poly;
  }

  LineString newTestLine(final BoundingBox env, final double size, final int nPts) {
    final double width = env.getWidth();
    final double xOffset = width * Math.random();
    final double yOffset = env.getHeight() * Math.random();
    final Point basePt = new PointDoubleXY(env.getMinX() + xOffset, env.getMinY() + yOffset);
    final LineString line = newTestLine(basePt, size, nPts);
    return line;
  }

  LineString newTestLine(final Point base, final double size, final int nPts) {
    final GeometricShapeFactory gsf = new GeometricShapeFactory();
    gsf.setCentre(base);
    gsf.setSize(size);
    gsf.setNumPoints(nPts);
    final Geometry circle = gsf.newCircle();
    // System.out.println(circle);
    return (LineString)circle.getBoundary();
  }

  public void run(final int nPts) {
    // Geometry poly = newCircle(new BaseLasPoint((double)0, 0), 100, nPts);
    final Geometry poly = newSineStar(new PointDoubleXY(0, 0), 100, nPts);
    // System.out.println(poly);
    //
    // System.out.println();
    // System.out.println("Running with " + nPts + " points");
    test(poly);
  }

  public void test() {
    run(1000);
  }

  public void test(final Geometry g) {
    int count = 0;
    while (count < MAX_ITER) {
      count++;
      final LineString line = newTestLine(g.getBoundingBox(), 10, 20);

      // System.out.println("Test # " + count);
      // System.out.println(line);
      testResultsEqual(g, line);
    }
  }

  public void testResultsEqual(final Geometry g, final LineString line) {
    final boolean slowIntersects = g.intersects(line);

    final Geometry prepGeom = g.prepare();

    final boolean fastIntersects = prepGeom.intersects(line);

    if (slowIntersects != fastIntersects) {
      // System.out.println(line);
      // System.out.println("Slow = " + slowIntersects + ", Fast = "
      // + fastIntersects);
      throw new RuntimeException("Different results found for intersects() !");
    }
  }
}
