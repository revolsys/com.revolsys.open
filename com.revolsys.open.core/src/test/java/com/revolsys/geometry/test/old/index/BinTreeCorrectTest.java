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
package com.revolsys.geometry.test.old.index;

import java.util.ArrayList;
import java.util.List;

import com.revolsys.geometry.index.bintree.Bintree;
import com.revolsys.geometry.index.bintree.Interval;
import com.revolsys.geometry.util.Stopwatch;

/**
 * @version 1.7
 */
public class BinTreeCorrectTest {

  static final double MAX_EXTENT = 1000.0;

  static final double MIN_EXTENT = -1000.0;

  static final int NUM_ITEMS = 20000;

  public static void main(final String[] args) throws Exception {
    // testBinaryPower();
    final BinTreeCorrectTest test = new BinTreeCorrectTest();
    test.run();
  }

  Bintree btree = new Bintree();

  IntervalList intervalList = new IntervalList();

  public BinTreeCorrectTest() {
  }

  void createGrid(final int nGridCells) {
    int gridSize = (int)Math.sqrt(nGridCells);
    gridSize += 1;
    final double extent = MAX_EXTENT - MIN_EXTENT;
    final double gridInc = extent / gridSize;
    final double cellSize = 2 * gridInc;

    for (int i = 0; i < gridSize; i++) {
      final double x = MIN_EXTENT + gridInc * i;
      final Interval interval = new Interval(x, x + cellSize);
      this.btree.insert(interval, interval);
      this.intervalList.add(interval);
    }
  }

  void fill() {
    createGrid(NUM_ITEMS);
  }

  private List getOverlapping(final List items, final Interval searchInterval) {
    final List result = new ArrayList();
    for (int i = 0; i < items.size(); i++) {
      final Interval interval = (Interval)items.get(i);
      if (interval.overlaps(searchInterval)) {
        result.add(interval);
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
      final double x = MIN_EXTENT + gridInc * i;
      final Interval interval = new Interval(x, x + cellSize);
      queryTest(interval);
      // queryTime(env);
    }
    // System.out.println("Time = " + sw.getTimeString());
  }

  void queryTest(final Interval interval) {
    final List candidateList = this.btree.query(interval);
    final List finalList = getOverlapping(candidateList, interval);

    final List eList = this.intervalList.query(interval);
    // System.out.println(finalList.size());

    if (finalList.size() != eList.size()) {
      throw new RuntimeException("queries do not match");
    }
  }

  void queryTime(final Interval interval) {
    // List finalList = getOverlapping(q.query(env), env);

    final List eList = this.intervalList.query(interval);
  }

  public void run() {
    fill();
    // System.out.println("depth = " + this.btree.depth() + " size = "
    // + this.btree.size());
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
