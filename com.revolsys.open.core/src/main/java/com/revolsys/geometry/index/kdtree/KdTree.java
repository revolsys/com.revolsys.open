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

package com.revolsys.geometry.index.kdtree;

import java.util.ArrayList;
import java.util.List;

import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.Point;
import com.revolsys.util.Emptyable;

/**
 * An implementation of a 2-D KD-Tree. KD-trees provide fast range searching on
 * point data.
 * <p>
 * This implementation supports detecting and snapping points which are closer than a given
 * tolerance value. If the same point (up to tolerance) is inserted more than once a new node is
 * not created but the count of the existing node is incremented.
 *
 *
 * @author David Skea
 * @author Martin Davis
 */
public class KdTree implements Emptyable {
  private final KdNode last = null;

  private long numberOfNodes;

  private KdNode root = null;

  private final double tolerance;

  /**
   * Creates a new instance of a KdTree
   * with a snapping tolerance of 0.0.
   * (I.e. distinct points will <i>not</i> be snapped)
   */
  public KdTree() {
    this(0.0);
  }

  /**
   * Creates a new instance of a KdTree, specifying a snapping distance tolerance.
   * Point which lie closer than the tolerance to a point already
   * in the tree will be treated as identical to the existing point.
   *
   * @param tolerance
   *          the tolerance distance for considering two points equal
   */
  public KdTree(final double tolerance) {
    this.tolerance = tolerance;
  }

  /**
   * Inserts a new point in the kd-tree, with no data.
   *
   * @param p
   *          the point to insert
   * @return the kdnode containing the point
   */
  public KdNode insert(final Point p) {
    return insert(p, null);
  }

  /**
   * Inserts a new point into the kd-tree.
   *
   * @param p
   *          the point to insert
   * @param data
   *          a data item for the point
   * @return returns a new KdNode if a new point is inserted, else an existing
   *         node is returned with its counter incremented. This can be checked
   *         by testing returnedNode.getCount() > 1.
   */
  public KdNode insert(final Point p, final Object data) {
    if (this.root == null) {
      this.root = new KdNode(p, data);
      return this.root;
    }

    KdNode currentNode = this.root;
    KdNode leafNode = this.root;
    boolean isOddLevel = true;
    boolean isLessThan = true;

    /**
     * Traverse the tree,
     * first cutting the plane left-right (by X ordinate)
     * then top-bottom (by Y ordinate)
     */
    while (currentNode != this.last) {
      // test if point is already a node
      if (currentNode != null) {
        final boolean isInTolerance = p.distance(currentNode) <= this.tolerance;

        // check if point is already in tree (up to tolerance) and if so simply
        // return existing node
        if (isInTolerance) {
          currentNode.increment();
          return currentNode;
        }
      }

      if (isOddLevel) {
        isLessThan = p.getX() < currentNode.getX();
      } else {
        isLessThan = p.getY() < currentNode.getY();
      }
      leafNode = currentNode;
      if (isLessThan) {
        currentNode = currentNode.getLeft();
      } else {
        currentNode = currentNode.getRight();
      }

      isOddLevel = !isOddLevel;
    }

    // no node found, add new leaf node to tree
    this.numberOfNodes = this.numberOfNodes + 1;
    final KdNode node = new KdNode(p, data);
    node.setLeft(this.last);
    node.setRight(this.last);
    if (isLessThan) {
      leafNode.setLeft(node);
    } else {
      leafNode.setRight(node);
    }
    return node;
  }

  /**
   * Tests whether the index contains any items.
   *
   * @return true if the index does not contain any items
   */
  @Override
  public boolean isEmpty() {
    if (this.root == null) {
      return true;
    }
    return false;
  }

  /**
   * Performs a range search of the points in the index.
   *
   * @param queryEnv
   *          the range rectangle to query
   * @return a list of the KdNodes found
   */
  public List query(final BoundingBox queryEnv) {
    final List result = new ArrayList();
    queryNode(this.root, this.last, queryEnv, true, result);
    return result;
  }

  /**
   * Performs a range search of the points in the index.
   *
   * @param queryEnv
   *          the range rectangle to query
   * @param result
   *          a list to accumulate the result nodes into
   */
  public void query(final BoundingBox queryEnv, final List result) {
    queryNode(this.root, this.last, queryEnv, true, result);
  }

  private void queryNode(final KdNode currentNode, final KdNode bottomNode,
    final BoundingBox queryEnv, final boolean odd, final List result) {
    if (currentNode == bottomNode) {
      return;
    }

    double min;
    double max;
    double discriminant;
    if (odd) {
      min = queryEnv.getMinX();
      max = queryEnv.getMaxX();
      discriminant = currentNode.getX();
    } else {
      min = queryEnv.getMinY();
      max = queryEnv.getMaxY();
      discriminant = currentNode.getY();
    }
    final boolean searchLeft = min < discriminant;
    final boolean searchRight = discriminant <= max;

    if (searchLeft) {
      queryNode(currentNode.getLeft(), bottomNode, queryEnv, !odd, result);
    }
    if (queryEnv.covers(currentNode)) {
      result.add(currentNode);
    }
    if (searchRight) {
      queryNode(currentNode.getRight(), bottomNode, queryEnv, !odd, result);
    }

  }
}
