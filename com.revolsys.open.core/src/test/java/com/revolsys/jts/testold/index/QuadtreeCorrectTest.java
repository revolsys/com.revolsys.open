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
package com.revolsys.jts.testold.index;

import java.util.ArrayList;
import java.util.List;

import com.revolsys.geometry.algorithm.index.quadtree.QuadTree;
import com.revolsys.geometry.model.impl.BoundingBoxDoubleGf;
import com.revolsys.geometry.util.Stopwatch;

/**
 * @version 1.7
 */
public class QuadtreeCorrectTest {

  static final double MAX_EXTENT = 1000.0;

  static final double MIN_EXTENT = -1000.0;

  /*
   * public static void testBinaryPower() { printBinaryPower(1004573397.0);
   * printBinaryPower(100.0); printBinaryPower(0.234);
   * printBinaryPower(0.000003455); } public static void printBinaryPower(double
   * num) { BinaryPower pow2 = new BinaryPower(); int exp =
   * BinaryPower.exponent(num); double p2 = pow2.power(exp);
   * System.out.println(num + " : pow2 = " + Math.pow(2.0, exp) + "   exp = " +
   * exp + "   2^exp = " + p2); }
   */
  static final int NUM_ITEMS = 2000;

  public static void main(final String[] args) throws Exception {
    // testBinaryPower();
    final QuadtreeCorrectTest test = new QuadtreeCorrectTest();
    test.run();
  }

  private final EnvelopeList envList = new EnvelopeList();

  private final QuadTree<BoundingBoxDoubleGf> index = new QuadTree<>();

  public QuadtreeCorrectTest() {
  }

  void createGrid(final int nGridCells) {
    int gridSize = (int)Math.sqrt(nGridCells);
    gridSize += 1;
    final double extent = MAX_EXTENT - MIN_EXTENT;
    final double gridInc = extent / gridSize;
    final double cellSize = 2 * gridInc;

    for (int i = 0; i < gridSize; i++) {
      for (int j = 0; j < gridSize; j++) {
        final double x = MIN_EXTENT + gridInc * i;
        final double y = MIN_EXTENT + gridInc * j;
        final BoundingBoxDoubleGf env = new BoundingBoxDoubleGf(2, x, y, x + cellSize,
          y + cellSize);
        this.index.insert(env, env);
        this.envList.add(env);
      }
    }
  }

  void fill() {
    createGrid(NUM_ITEMS);
  }

  private List getOverlapping(final List items, final BoundingBoxDoubleGf searchEnv) {
    final List result = new ArrayList();
    for (int i = 0; i < items.size(); i++) {
      final BoundingBoxDoubleGf env = (BoundingBoxDoubleGf)items.get(i);
      if (env.intersects(searchEnv)) {
        result.add(env);
      }
    }
    return result;
  }

  void queryGrid(final int nGridCells, final double cellSize) {
    final Stopwatch sw = new Stopwatch();
    sw.start();

    int gridSize = (int)Math.sqrt(nGridCells);
    gridSize += 1;
    final double extent = MAX_EXTENT - MIN_EXTENT;
    final double gridInc = extent / gridSize;

    for (int i = 0; i < gridSize; i++) {
      for (int j = 0; j < gridSize; j++) {
        final double x = MIN_EXTENT + gridInc * i;
        final double y = MIN_EXTENT + gridInc * j;
        final BoundingBoxDoubleGf env = new BoundingBoxDoubleGf(2, x, y, x + cellSize,
          y + cellSize);
        queryTest(env);
        // queryTime(env);
      }
    }
    // System.out.println("Time = " + sw.getTimeString());
  }

  void queryTest(final BoundingBoxDoubleGf env) {
    final List candidateList = this.index.query(env);
    final List finalList = getOverlapping(candidateList, env);

    final List eList = this.envList.query(env);
    // System.out.println(finalList.size());

    if (finalList.size() != eList.size()) {
      throw new RuntimeException("queries do not match");
    }
  }

  void queryTime(final BoundingBoxDoubleGf env) {
    // List finalList = getOverlapping(q.query(env), env);

    final List eList = this.envList.query(env);
  }

  public void run() {
    fill();
    // System.out.println("depth = " + this.q.depth() + " size = "
    // + this.q.size());
    runQueries();
  }

  void runQueries() {
    final int nGridCells = 100;
    final int cellSize = (int)Math.sqrt(NUM_ITEMS);
    final double extent = MAX_EXTENT - MIN_EXTENT;
    final double queryCellSize = 2.0 * extent / cellSize;

    queryGrid(nGridCells, queryCellSize);

    // queryGrid(200);
  }

}
