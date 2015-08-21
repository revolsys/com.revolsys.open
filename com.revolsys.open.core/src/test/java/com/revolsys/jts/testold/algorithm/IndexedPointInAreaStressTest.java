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
package com.revolsys.jts.testold.algorithm;

import com.revolsys.geometry.algorithm.locate.IndexedPointInAreaLocator;
import com.revolsys.geometry.algorithm.locate.PointOnGeometryLocator;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.jts.testold.perf.algorithm.PointInAreaStressTester;

import junit.framework.TestCase;
import junit.textui.TestRunner;

public class IndexedPointInAreaStressTest extends TestCase {

  public static void main(final String args[]) {
    TestRunner.run(IndexedPointInAreaStressTest.class);
  }

  public IndexedPointInAreaStressTest(final String name) {
    super(name);
  }

  public void testGrid() {
    // Use fixed PM to try and get at least some points hitting the boundary
    final GeometryFactory geomFactory = GeometryFactory.fixed(0, 1.0);
    // GeometryFactoryI geomFactory = new GeometryFactoryI();

    final PerturbedGridPolygonBuilder gridBuilder = new PerturbedGridPolygonBuilder(geomFactory);
    gridBuilder.setNumLines(20);
    gridBuilder.setLineWidth(10.0);
    gridBuilder.setSeed(1185072199562L);
    final Geometry area = gridBuilder.getGeometry();

    // PointInAreaLocator pia = new
    // GeometryFactoryIndexedPointInAreaLocator(area);
    final PointOnGeometryLocator pia = new IndexedPointInAreaLocator(area);

    final PointInAreaStressTester gridTester = new PointInAreaStressTester(geomFactory, area);
    gridTester.setNumPoints(100000);
    gridTester.setPIA(pia);

    final boolean isCorrect = gridTester.run();
    assertTrue(isCorrect);
  }
}
