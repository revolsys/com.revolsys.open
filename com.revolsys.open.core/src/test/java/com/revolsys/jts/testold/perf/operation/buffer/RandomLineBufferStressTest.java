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

import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.jts.io.WKTReader;
import com.revolsys.jts.operation.buffer.validate.BufferResultValidator;

/**
 * Test buffers generated around set of random linestrings.
 * Intended to stress-test the correctness of buffer generation.
 * The random linestring sets tend to have numerous holes when buffered, 
 * which is a good test.
 * 
 * @version 1.7
 */
public class RandomLineBufferStressTest {

  public static void main(final String args[]) {
    try {
      new RandomLineBufferStressTest().run();
    } catch (final Exception ex) {
      ex.printStackTrace();
    }

  }

  private final GeometryFactory geometryFactory = GeometryFactory.getFactory(0,
    2);

  WKTReader rdr = new WKTReader(this.geometryFactory);

  private int caseCount = 0;

  public RandomLineBufferStressTest() {
  }

  void checkBuffer(final Geometry g, final double distance) {
    final Geometry buf = g.buffer(distance);
    final String isValidMsg = BufferResultValidator.isValidMsg(g, distance, buf);
    if (isValidMsg != null) {
      System.out.println("Input: ");
      System.out.println(g);
      System.out.println("Buffer: ");
      System.out.println(buf);

      throw new IllegalStateException(isValidMsg);
    }
  }

  void run() throws Exception {
    while (true) {
      run(10);
      run(100);
      run(200);
    }
  }

  void run(final int numPts) throws Exception {
    final double lineScale = 1.0;

    final Geometry line = RandomOffsetLineStringGenerator.generate(lineScale,
      numPts, this.geometryFactory);
    System.out.println();
    System.out.println(line);

    runCase(line, 10, lineScale, numPts);
    runCase(line, 1, lineScale, numPts);
    runCase(line, .1, lineScale, numPts);
  }

  void runCase(final Geometry line, final double dist, final double lineScale,
    final int numPts) throws Exception {
    this.caseCount++;
    System.out.println("Running case " + this.caseCount + "  (line scale = "
      + lineScale + "  buffer dist = " + dist + "  num pts = " + numPts + " )");
    checkBuffer(line, dist);
  }

}
