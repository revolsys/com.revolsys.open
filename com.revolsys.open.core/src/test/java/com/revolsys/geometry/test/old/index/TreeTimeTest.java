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

import com.revolsys.geometry.index.quadtree.QuadTree;
import com.revolsys.geometry.index.strtree.STRtree;
import com.revolsys.geometry.model.impl.BoundingBoxDoubleGf;

/**
 * @version 1.7
 */
public class TreeTimeTest {
  class EnvelopeListIndex implements Index {
    EnvelopeList index = new EnvelopeList();

    @Override
    public void finishInserting() {
    }

    @Override
    public void insert(final BoundingBoxDoubleGf itemEnv, final Object item) {
      this.index.add(itemEnv);
    }

    @Override
    public List query(final BoundingBoxDoubleGf searchEnv) {
      return this.index.query(searchEnv);
    }

    @Override
    public String toString() {
      return "Env";
    }
  }

  class QuadtreeIndex implements Index {
    QuadTree<Object> index = new QuadTree<>();

    @Override
    public void finishInserting() {
    }

    @Override
    public void insert(final BoundingBoxDoubleGf itemEnv, final Object item) {
      this.index.insertItem(itemEnv, item);
    }

    @Override
    public List query(final BoundingBoxDoubleGf searchEnv) {
      return this.index.getItems(searchEnv);
    }

    @Override
    public String toString() {
      return "Quad";
    }
  }

  class STRtreeIndex implements Index {
    STRtree index;

    // public String toString() { return "" + index.getNodeCapacity() + ""; }
    public STRtreeIndex(final int nodeCapacity) {
      this.index = new STRtree(nodeCapacity);
    }

    @Override
    public void finishInserting() {
      this.index.build();
    }

    @Override
    public void insert(final BoundingBoxDoubleGf itemEnv, final Object item) {
      this.index.insertItem(itemEnv, item);
    }

    @Override
    public List query(final BoundingBoxDoubleGf searchEnv) {
      return this.index.getItems(searchEnv);
    }

    @Override
    public String toString() {
      return "STR[M=" + this.index.getNodeCapacity() + "]";
    }
  }

  public static final int NUM_ITEMS = 10000;

  public static void main(final String[] args) throws Exception {
    final int n = 10000;
    final TreeTimeTest test = new TreeTimeTest();
    final List items = IndexTester.newGridItems(n);
    // System.out.println("----------------------------------------------");
    // System.out.println("Dummy run to ensure classes are loaded before real
    // run");
    // System.out.println("----------------------------------------------");
    test.run(items);
    // System.out.println("----------------------------------------------");
    // System.out.println("Real run");
    // System.out.println("----------------------------------------------");
    test.run(items);
  }

  public TreeTimeTest() {
  }

  public IndexTester.IndexResult run(final Index index, final List items) throws Exception {
    return new IndexTester(index).testAll(items);
  }

  public List run(final List items) throws Exception {
    final ArrayList indexResults = new ArrayList();
    // System.out.println("# items = " + items.size());
    indexResults.add(run(new QuadtreeIndex(), items));
    indexResults.add(run(new STRtreeIndex(10), items));
    // indexResults.add(run(new QXtreeIndex(), n));
    // indexResults.add(run(new EnvelopeListIndex(), n));
    return indexResults;
  }

}
