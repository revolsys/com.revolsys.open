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
package com.revolsys.gis.model.geometry.index;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.revolsys.collection.Visitor;
import com.revolsys.jts.geom.Coordinate;
import com.revolsys.jts.io.WKTWriter;

/**
 * A static index on a set of 1-dimensional intervals,
 * using an R-Tree packed based on the order of the interval midpoints.
 * It supports range searching,
 * where the range is an interval of the real line (which may be a single point).
 * A common use is to index 1-dimensional intervals which 
 * are the projection of 2-D objects onto an axis of the coordinate system.
 * <p>
 * This index structure is <i>static</i> 
 * - items cannot be added or removed once the first query has been made.
 * The advantage of this characteristic is that the index performance 
 * can be optimized based on a fixed set of items.
 * 
 * @author Martin Davis
 */
public class SortedPackedIntervalRTree<V> {
  private final List<IntervalRTreeNode<V>> leaves = new ArrayList<IntervalRTreeNode<V>>();

  private IntervalRTreeNode<V> root = null;

  private int level = 0;

  public SortedPackedIntervalRTree() {

  }

  private void buildLevel(final List<IntervalRTreeNode<V>> src,
    final List<IntervalRTreeNode<V>> dest) {
    level++;
    dest.clear();
    for (int i = 0; i < src.size(); i += 2) {
      final IntervalRTreeNode<V> n1 = src.get(i);
      final IntervalRTreeNode<V> n2 = (i + 1 < src.size()) ? (IntervalRTreeNode<V>)src.get(i)
        : null;
      if (n2 == null) {
        dest.add(n1);
      } else {
        final IntervalRTreeNode<V> node = new IntervalRTreeBranchNode<V>(
          src.get(i), src.get(i + 1));
        // printNode(node);
        // System.out.println(node);
        dest.add(node);
      }
    }
  }

  private IntervalRTreeNode<V> buildTree() {
    // sort the leaf nodes
    Collections.sort(leaves, new NodeComparator<V>());

    // now group nodes into blocks of two and build tree up recursively
    List<IntervalRTreeNode<V>> src = leaves;
    List<IntervalRTreeNode<V>> temp = null;
    List<IntervalRTreeNode<V>> dest = new ArrayList<>();

    while (true) {
      buildLevel(src, dest);
      if (dest.size() == 1) {
        return dest.get(0);
      }

      temp = src;
      src = dest;
      dest = temp;
    }
  }

  private void init() {
    if (root != null) {
      return;
    }
    root = buildTree();
  }

  /**
   * Adds an item to the index which is associated with the given interval
   * 
   * @param min the lower bound of the item interval
   * @param max the upper bound of the item interval
   * @param item the item to insert
   * 
   * @throws IllegalStateException if the index has already been queried
   */
  public void insert(final double min, final double max, final Object item) {
    if (root != null) {
      throw new IllegalStateException(
        "Index cannot be added to once it has been queried");
    }
    leaves.add(new IntervalRTreeLeafNode(min, max, item));
  }

  private void printNode(final IntervalRTreeNode node) {
    System.out.println(WKTWriter.toLineString(new Coordinate(node.getMin(), level),
      new Coordinate(node.getMax(), level)));
  }

  /**
   * Search for intervals in the index which intersect the given closed interval
   * and apply the visitor to them.
   * 
   * @param min the lower bound of the query interval
   * @param max the upper bound of the query interval
   * @param visitor the visitor to pass any matched items to
   */
  public void query(final double min, final double max, final Visitor<V> visitor) {
    init();

    root.query(min, max, visitor);
  }

}
