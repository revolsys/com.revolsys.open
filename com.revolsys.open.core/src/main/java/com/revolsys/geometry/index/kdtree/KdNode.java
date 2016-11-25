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

import java.util.function.Consumer;

import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.impl.PointDoubleXY;
import com.revolsys.geometry.util.BoundingBoxUtil;

/**
 * A node of a {@link KdTree}, which represents one or more points in the same location.
 *
 * @author dskea
 */
public class KdNode extends PointDoubleXY {
  private static final long serialVersionUID = 1L;

  private int count;

  private KdNode left;

  private KdNode right;

  /**
   * Creates a new KdNode.
   *
   * @param x coordinate of point
   * @param y coordinate of point
   * @param data a data objects to associate with this node
   */
  public KdNode(final double x, final double y) {
    super(x, y);
    this.left = null;
    this.right = null;
    this.count = 1;
  }

  /**
  * Creates a new KdNode.
  *
  * @param point point location of new node
  * @param data a data objects to associate with this node
  */
  public KdNode(final Point point) {
    this(point.getX(), point.getY());
  }

  @SuppressWarnings("unchecked")
  <N extends KdNode> void forEachNode(final boolean odd, final double minX, final double minY,
    final double maxX, final double maxY, final Consumer<N> result) {

    double min;
    double max;
    double discriminant;
    if (odd) {
      min = minX;
      max = maxX;
      discriminant = this.x;
    } else {
      min = minY;
      max = maxY;
      discriminant = this.y;
    }
    if (min < discriminant && this.left != null) {
      this.left.forEachNode(!odd, minX, minY, maxX, maxY, result);
    }
    if (BoundingBoxUtil.intersects(minX, minY, maxX, maxY, this.x, this.y)) {
      result.accept((N)this);
    }
    if (discriminant <= max && this.right != null) {
      this.right.forEachNode(!odd, minX, minY, maxX, maxY, result);
    }
  }

  /**
   * Returns the number of inserted points that are coincident at this location.
   *
   * @return number of inserted points that this node represents
   */
  public int getCount() {
    return this.count;
  }

  /**
   * Returns the left node of the tree
   *
   * @return left node
   */
  public KdNode getLeft() {
    return this.left;
  }

  /**
   * Returns the right node of the tree
   *
   * @return right node
   */
  public KdNode getRight() {
    return this.right;
  }

  // Increments counts of points at this location
  void increment() {
    this.count = this.count + 1;
  }

  /**
   * Tests whether more than one point with this value have been inserted (up to the tolerance)
   *
   * @return true if more than one point have been inserted with this value
   */
  public boolean isRepeated() {
    return this.count > 1;
  }

  // Sets left node value
  void setLeft(final KdNode left) {
    this.left = left;
  }

  // Sets right node value
  void setRight(final KdNode right) {
    this.right = right;
  }
}
