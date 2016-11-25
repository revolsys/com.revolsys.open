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
import java.util.function.Consumer;

import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.Point;
import com.revolsys.util.Emptyable;
import com.revolsys.util.function.BiFunctionDouble;

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

  private long numberOfNodes;

  private KdNode root = null;

  private final double tolerance;

  private final BiFunctionDouble<KdNode> nodeFactory;

  /**
   * Creates a new instance of a KdTree
   * with a snapping tolerance of 0.0.
   * (I.e. distinct points will <i>not</i> be snapped)
   */
  public KdTree() {
    this(0.0);
  }

  public KdTree(final BiFunctionDouble<KdNode> nodeFactory) {
    this(nodeFactory, 0);
  }

  public KdTree(final BiFunctionDouble<KdNode> nodeFactory, final double tolerance) {
    this.nodeFactory = nodeFactory;
    this.tolerance = tolerance;
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
    this(KdNode::new, tolerance);
  }

  /**
   * Performs a range search of the points in the index.
   *
   * @param boundingBox
   *          the range rectangle to query
   * @param result
   *          a list to accumulate the result nodes into
   */
  public <N extends KdNode> void forEachNode(final BoundingBox boundingBox,
    final Consumer<N> result) {
    if (this.root != null) {
      final double minX = boundingBox.getMinX();
      final double minY = boundingBox.getMinY();
      final double maxX = boundingBox.getMaxX();
      final double maxY = boundingBox.getMaxY();
      this.root.forEachNode(true, minX, minY, maxX, maxY, result);
    }
  }

  /**
   * Inserts a new point into the kd-tree.
   *
   * @param point
   *          the point to insert
   * @return returns a new KdNode if a new point is inserted, else an existing
   *         node is returned with its counter incremented. This can be checked
   *         by testing returnedNode.getCount() > 1.
   */
  public <N extends KdNode> N insert(final Point point) {
    final double x = point.getX();
    final double y = point.getY();
    return insertPoint(x, y);
  }

  @SuppressWarnings("unchecked")
  public <N extends KdNode> N insertPoint(final double x, final double y) {
    if (this.root == null) {
      this.root = this.nodeFactory.accept(x, y);
      return (N)this.root;
    } else {

      KdNode currentNode = this.root;
      KdNode leafNode = this.root;
      boolean isOddLevel = true;
      boolean isLessThan = true;

      /**
       * Traverse the tree,
       * first cutting the plane left-right (by X ordinate)
       * then top-bottom (by Y ordinate)
       */
      while (currentNode != null) {
        // test if point is already a node
        if (currentNode != null) {
          final boolean isInTolerance = currentNode.distance(x, y) <= this.tolerance;

          // check if point is already in tree (up to tolerance) and if so simply
          // return existing node
          if (isInTolerance) {
            currentNode.increment();
            return (N)currentNode;
          }
        }

        if (isOddLevel) {
          isLessThan = x < currentNode.getX();
        } else {
          isLessThan = y < currentNode.getY();
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
      final KdNode node = this.nodeFactory.accept(x, y);
      if (isLessThan) {
        leafNode.setLeft(node);
      } else {
        leafNode.setRight(node);
      }
      return (N)node;
    }
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
    } else {
      return false;
    }
  }

  /**
   * Performs a range search of the points in the index.
   *
   * @param boundingBox
   *          the range rectangle to query
   * @return a list of the KdNodes found
   */
  public <N extends KdNode> List<N> query(final BoundingBox boundingBox) {
    final List<N> result = new ArrayList<>();
    final Consumer<N> action = result::add;
    forEachNode(boundingBox, action);
    return result;
  }

}
