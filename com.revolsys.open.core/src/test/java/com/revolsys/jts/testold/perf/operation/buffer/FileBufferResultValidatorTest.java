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
package com.revolsys.jts.testold.perf.operation.buffer;

import java.util.Iterator;
import java.util.List;

import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.operation.buffer.validate.BufferResultValidator;
import com.revolsys.geometry.util.Stopwatch;
import com.revolsys.geometry.wkb.WKTReader;
import com.revolsys.jts.testold.algorithm.InteriorPointTest;

import junit.framework.TestCase;
import junit.framework.TestCase;

/**
 * @version 1.7
 */
public class FileBufferResultValidatorTest extends TestCase {

  public static void main(final String[] args) {
    junit.textui.TestRunner.run(FileBufferResultValidatorTest.class);
  }

  WKTReader rdr = new WKTReader();

  public FileBufferResultValidatorTest(final String name) {
    super(name);
  }

  void runAll(final List geoms, final double dist) {
    final Stopwatch sw = new Stopwatch();
    // System.out.println("Geom count = " + geoms.size() + " distance = " +
    // dist);
    for (final Iterator i = geoms.iterator(); i.hasNext();) {
      final Geometry g = (Geometry)i.next();
      runBuffer(g, dist);
      runBuffer(g.reverse(), dist);
      System.out.print(".");
    }
    // System.out.println(" " + sw.getTimeString());

  }

  void runBuffer(final Geometry g, final double dist) {
    final Geometry buf = g.buffer(dist);
    final BufferResultValidator validator = new BufferResultValidator(g, dist, buf);

    if (!validator.isValid()) {
      final String msg = validator.getErrorMessage();

      // System.out.println(msg);
      // System.out.println(WktWriter.point(validator.getErrorLocation()));
      // System.out.println(g);
    }
    assertTrue(validator.isValid());
  }

  void runTest(final String file) throws Exception {
    final List polys = InteriorPointTest.getTestGeometries(file);

    runAll(polys, 0.01);
    runAll(polys, 0.1);
    runAll(polys, 1.0);
    runAll(polys, 10.0);
    runAll(polys, 100.0);
    runAll(polys, 1000.0);

  }

  public void testAfrica() throws Exception {
    runTest("africa.wkt");
  }
}
