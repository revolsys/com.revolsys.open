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
package com.revolsys.jts.index.strtree;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import com.revolsys.jts.geom.BoundingBox;
import com.revolsys.jts.geom.impl.BoundingBoxDoubleGf;
import com.revolsys.jts.index.ItemVisitor;
import com.revolsys.jts.index.SpatialIndex;
import com.revolsys.jts.util.Assert;
import com.revolsys.jts.util.PriorityQueue;

/**
 *  A query-only R-tree created using the Sort-Tile-Recursive (STR) algorithm.
 *  For two-dimensional spatial data.
 * <P>
 *  The STR packed R-tree is simple to implement and maximizes space
 *  utilization; that is, as many leaves as possible are filled to capacity.
 *  Overlap between nodes is far less than in a basic R-tree. However, once the
 *  tree has been built (explicitly or on the first call to #query), items may
 *  not be added or removed.
 * <P>
 * Described in: P. Rigaux, Michel Scholl and Agnes Voisard.
 * <i>Spatial Databases With Application To GIS</i>.
 * Morgan Kaufmann, San Francisco, 2002.
 *
 * @version 1.7
 */
public class STRtree extends AbstractSTRtree implements SpatialIndex,
Serializable {

  private static final class STRtreeNode extends AbstractNode {
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private STRtreeNode(final int level) {
      super(level);
    }

    @Override
    protected Object computeBounds() {
      BoundingBox bounds = null;
      for (final Iterator i = getChildBoundables().iterator(); i.hasNext();) {
        final Boundable childBoundable = (Boundable)i.next();
        if (bounds == null) {
          bounds = (BoundingBox)childBoundable.getBounds();
        } else {
          bounds = bounds.expandToInclude((BoundingBox)childBoundable.getBounds());
        }
      }
      return bounds;
    }
  }

  private static double avg(final double a, final double b) {
    return (a + b) / 2d;
  }

  private static double centreX(final BoundingBox e) {
    return avg(e.getMinX(), e.getMaxX());
  }

  private static double centreY(final BoundingBox e) {
    return avg(e.getMinY(), e.getMaxY());
  }

  /**
   *
   */
  private static final long serialVersionUID = 259274702368956900L;

  private static Comparator xComparator = new Comparator() {
    @Override
    public int compare(final Object o1, final Object o2) {
      return compareDoubles(centreX((BoundingBox)((Boundable)o1).getBounds()),
        centreX((BoundingBox)((Boundable)o2).getBounds()));
    }
  };

  private static Comparator yComparator = new Comparator() {
    @Override
    public int compare(final Object o1, final Object o2) {
      return compareDoubles(centreY((BoundingBox)((Boundable)o1).getBounds()),
        centreY((BoundingBox)((Boundable)o2).getBounds()));
    }
  };

  private static IntersectsOp intersectsOp = new IntersectsOp() {
    @Override
    public boolean intersects(final Object aBounds, final Object bBounds) {
      return ((BoundingBox)aBounds).intersects((BoundingBoxDoubleGf)bBounds);
    }
  };

  private static final int DEFAULT_NODE_CAPACITY = 10;

  /**
   * Constructs an STRtree with the default node capacity.
   */
  public STRtree() {
    this(DEFAULT_NODE_CAPACITY);
  }

  /**
   * Constructs an STRtree with the given maximum number of child nodes that
   * a node may have.
   * <p>
   * The minimum recommended capacity setting is 4.
   *
   */
  public STRtree(final int nodeCapacity) {
    super(nodeCapacity);
  }

  @Override
  protected AbstractNode createNode(final int level) {
    return new STRtreeNode(level);
  }

  /**
   * Creates the parent level for the given child level. First, orders the items
   * by the x-values of the midpoints, and groups them into vertical slices.
   * For each slice, orders the items by the y-values of the midpoints, and
   * group them into runs of size M (the node capacity). For each run, creates
   * a new (parent) node.
   */
  @Override
  protected List createParentBoundables(final List childBoundables,
    final int newLevel) {
    Assert.isTrue(!childBoundables.isEmpty());
    final int minLeafCount = (int)Math.ceil(childBoundables.size() / (double)getNodeCapacity());
    final ArrayList sortedChildBoundables = new ArrayList(childBoundables);
    Collections.sort(sortedChildBoundables, xComparator);
    final List[] verticalSlices = verticalSlices(sortedChildBoundables,
      (int)Math.ceil(Math.sqrt(minLeafCount)));
    return createParentBoundablesFromVerticalSlices(verticalSlices, newLevel);
  }

  protected List createParentBoundablesFromVerticalSlice(
    final List childBoundables, final int newLevel) {
    return super.createParentBoundables(childBoundables, newLevel);
  }

  private List createParentBoundablesFromVerticalSlices(
    final List[] verticalSlices, final int newLevel) {
    Assert.isTrue(verticalSlices.length > 0);
    final List parentBoundables = new ArrayList();
    for (final List verticalSlice : verticalSlices) {
      parentBoundables.addAll(createParentBoundablesFromVerticalSlice(
        verticalSlice, newLevel));
    }
    return parentBoundables;
  }

  /**
   * Returns the number of items in the tree.
   *
   * @return the number of items in the tree
   */
  @Override
  public int depth() {
    return super.depth();
  }

  @Override
  protected Comparator getComparator() {
    return yComparator;
  }

  @Override
  protected IntersectsOp getIntersectsOp() {
    return intersectsOp;
  }

  /**
   * Inserts an item having the given bounds into the tree.
   */
  @Override
  public void insert(final BoundingBox itemEnv, final Object item) {
    if (itemEnv.isEmpty()) {
      return;
    }
    super.insert(itemEnv, item);
  }

  private Object[] nearestNeighbour(final BoundablePair initBndPair) {
    return nearestNeighbour(initBndPair, Double.POSITIVE_INFINITY);
  }

  private Object[] nearestNeighbour(final BoundablePair initBndPair,
    final double maxDistance) {
    double distanceLowerBound = maxDistance;
    BoundablePair minPair = null;

    // initialize internal structures
    final PriorityQueue priQ = new PriorityQueue();

    // initialize queue
    priQ.add(initBndPair);

    while (!priQ.isEmpty() && distanceLowerBound > 0.0) {
      // pop head of queue and expand one side of pair
      final BoundablePair bndPair = (BoundablePair)priQ.poll();
      final double currentDistance = bndPair.getDistance();

      /**
       * If the distance for the first node in the queue
       * is >= the current minimum distance, all other nodes
       * in the queue must also have a greater distance.
       * So the current minDistance must be the true minimum,
       * and we are done.
       */
      if (currentDistance >= distanceLowerBound) {
        break;
      }

      /**
       * If the pair members are leaves
       * then their distance is the exact lower bound.
       * Update the distanceLowerBound to reflect this
       * (which must be smaller, due to the test
       * immediately prior to this).
       */
      if (bndPair.isLeaves()) {
        // assert: currentDistance < minimumDistanceFound
        distanceLowerBound = currentDistance;
        minPair = bndPair;
      } else {
        // testing - does allowing a tolerance improve speed?
        // Ans: by only about 10% - not enough to matter
        /*
         * double maxDist = bndPair.getMaximumDistance(); if (maxDist * .99 <
         * lastComputedDistance) return; //
         */

        /**
         * Otherwise, expand one side of the pair,
         * (the choice of which side to expand is heuristically determined)
         * and insert the new expanded pairs into the queue
         */
        bndPair.expandToQueue(priQ, distanceLowerBound);
      }
    }
    // done - return items with min distance
    return new Object[] {
      ((ItemBoundable)minPair.getBoundable(0)).getItem(),
      ((ItemBoundable)minPair.getBoundable(1)).getItem()
    };
  }

  /**
   * Finds the item in this tree which is nearest to the given {@link Object},
   * using {@link ItemDistance} as the distance metric.
   * A Branch-and-Bound tree traversal algorithm is used
   * to provide an efficient search.
   * <p>
   * The query <tt>object</tt> does <b>not</b> have to be
   * contained in the tree, but it does
   * have to be compatible with the <tt>itemDist</tt>
   * distance metric.
   *
   * @param env the envelope of the query item
   * @param item the item to find the nearest neighbour of
   * @param itemDist a distance metric applicable to the items in this tree and the query item
   * @return the nearest item in this tree
   */
  public Object nearestNeighbour(final BoundingBox env, final Object item,
    final ItemDistance itemDist) {
    final Boundable bnd = new ItemBoundable(env, item);
    final BoundablePair bp = new BoundablePair(this.getRoot(), bnd, itemDist);
    return nearestNeighbour(bp)[0];
  }

  /**
   * Finds the two nearest items in the tree,
   * using {@link ItemDistance} as the distance metric.
   * A Branch-and-Bound tree traversal algorithm is used
   * to provide an efficient search.
   *
   * @param itemDist a distance metric applicable to the items in this tree
   * @return the pair of the nearest items
   */
  public Object[] nearestNeighbour(final ItemDistance itemDist) {
    final BoundablePair bp = new BoundablePair(this.getRoot(), this.getRoot(),
      itemDist);
    return nearestNeighbour(bp);
  }

  /**
   * Finds the two nearest items from this tree
   * and another tree,
   * using {@link ItemDistance} as the distance metric.
   * A Branch-and-Bound tree traversal algorithm is used
   * to provide an efficient search.
   * The result value is a pair of items,
   * the first from this tree and the second
   * from the argument tree.
   *
   * @param tree another tree
   * @param itemDist a distance metric applicable to the items in the trees
   * @return the pair of the nearest items, one from each tree
   */
  public Object[] nearestNeighbour(final STRtree tree,
    final ItemDistance itemDist) {
    final BoundablePair bp = new BoundablePair(this.getRoot(), tree.getRoot(),
      itemDist);
    return nearestNeighbour(bp);
  }

  /**
   * Returns items whose bounds intersect the given envelope.
   */
  @Override
  public List query(final BoundingBox searchEnv) {
    // Yes this method does something. It specifies that the bounds is an
    // BoundingBoxDoubleGf. super.query takes an Object, not an BoundingBoxDoubleGf. [Jon Aquino
    // 10/24/2003]
    return super.query(searchEnv);
  }

  /**
   * Returns items whose bounds intersect the given envelope.
   */
  public void query(final BoundingBox searchEnv, final ItemVisitor visitor) {
    // Yes this method does something. It specifies that the bounds is an
    // BoundingBoxDoubleGf. super.query takes an Object, not an BoundingBoxDoubleGf. [Jon Aquino
    // 10/24/2003]
    super.query(searchEnv, visitor);
  }

  /**
   * Removes a single item from the tree.
   *
   * @param itemEnv the BoundingBoxDoubleGf of the item to remove
   * @param item the item to remove
   * @return <code>true</code> if the item was found
   */
  @Override
  public boolean remove(final BoundingBox itemEnv, final Object item) {
    return super.remove(itemEnv, item);
  }

  /**
   * Returns the number of items in the tree.
   *
   * @return the number of items in the tree
   */
  @Override
  public int size() {
    return super.size();
  }

  /**
   * @param childBoundables Must be sorted by the x-value of the envelope midpoints
   */
  protected List[] verticalSlices(final List childBoundables,
    final int sliceCount) {
    final int sliceCapacity = (int)Math.ceil(childBoundables.size()
      / (double)sliceCount);
    final List[] slices = new List[sliceCount];
    final Iterator i = childBoundables.iterator();
    for (int j = 0; j < sliceCount; j++) {
      slices[j] = new ArrayList();
      int boundablesAddedToSlice = 0;
      while (i.hasNext() && boundablesAddedToSlice < sliceCapacity) {
        final Boundable childBoundable = (Boundable)i.next();
        slices[j].add(childBoundable);
        boundablesAddedToSlice++;
      }
    }
    return slices;
  }

}
