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
import java.util.Iterator;
import java.util.List;

import com.revolsys.geometry.model.impl.BoundingBoxDoubleGf;
import com.revolsys.geometry.util.Assert;
import com.revolsys.geometry.util.Stopwatch;

/**
 * @version 1.7
 */
public class IndexTester {
  public static class IndexResult {
    public String indexName;

    public long loadMilliseconds;

    public long queryMilliseconds;

    public IndexResult(final String indexName) {
      this.indexName = indexName;
    }
  }

  static final double EXTENT_MAX = 1000.0;

  static final double EXTENT_MIN = -1000.0;

  static final int NUM_ITEMS = 2000;

  public static List newGridItems(final int nGridCells) {
    final ArrayList items = new ArrayList();
    int gridSize = (int)Math.sqrt(nGridCells);
    gridSize += 1;
    final double extent = EXTENT_MAX - EXTENT_MIN;
    final double gridInc = extent / gridSize;
    final double cellSize = gridInc;
    for (int i = 0; i < gridSize; i++) {
      for (int j = 0; j < gridSize; j++) {
        final double x = EXTENT_MIN + gridInc * i;
        final double y = EXTENT_MIN + gridInc * j;
        final BoundingBoxDoubleGf env = new BoundingBoxDoubleGf(2, x, y, x + cellSize,
          y + cellSize);
        items.add(env);
      }
    }
    return items;
  }

  Index index;

  public IndexTester(final Index index) {
    this.index = index;
  }

  void loadGrid(final List items) {
    for (final Iterator i = items.iterator(); i.hasNext();) {
      final BoundingBoxDoubleGf item = (BoundingBoxDoubleGf)i.next();
      this.index.insert(item, item);
    }
    this.index.finishInserting();
  }

  void queryGrid(final int nGridCells, final double cellSize) {

    int gridSize = (int)Math.sqrt(nGridCells);
    gridSize += 1;
    final double extent = EXTENT_MAX - EXTENT_MIN;
    final double gridInc = extent / gridSize;

    for (int i = 0; i < gridSize; i++) {
      for (int j = 0; j < gridSize; j++) {
        final double x = EXTENT_MIN + gridInc * i;
        final double y = EXTENT_MIN + gridInc * j;
        final BoundingBoxDoubleGf env = new BoundingBoxDoubleGf(2, x, y, x + cellSize,
          y + cellSize);
        this.index.query(env);
      }
    }
  }

  void runGridQuery() {
    final int nGridCells = 100;
    final int cellSize = (int)Math.sqrt(NUM_ITEMS);
    final double extent = EXTENT_MAX - EXTENT_MIN;
    final double queryCellSize = 2.0 * extent / cellSize;

    queryGrid(nGridCells, queryCellSize);
  }

  void runSelfQuery(final List items) {
    double querySize = 0.0;
    for (int i = 0; i < items.size(); i++) {
      final BoundingBoxDoubleGf env = (BoundingBoxDoubleGf)items.get(i);
      final List list = this.index.query(env);
      Assert.isTrue(!list.isEmpty());
      querySize += list.size();
    }
    // System.out.println("Avg query size = " + querySize / items.size());
  }

  public IndexResult testAll(final List items) {
    final IndexResult result = new IndexResult(this.index.toString());
    System.out.print(this.index.toString() + "           ");
    System.gc();
    final Stopwatch sw = new Stopwatch();
    sw.start();
    loadGrid(items);
    final String loadTime = sw.getTimeString();
    result.loadMilliseconds = sw.getTime();
    System.gc();
    sw.start();
    // runQueries();
    runSelfQuery(items);
    final String queryTime = sw.getTimeString();
    result.queryMilliseconds = sw.getTime();
    // System.out.println(" Load Time = " + loadTime + " Query Time = "
    // + queryTime);
    return result;
  }
}
