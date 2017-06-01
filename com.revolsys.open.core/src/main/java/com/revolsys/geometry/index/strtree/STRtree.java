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
package com.revolsys.geometry.index.strtree;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

import com.revolsys.geometry.index.SpatialIndex;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.BoundingBoxProxy;
import com.revolsys.geometry.util.Assert;
import com.revolsys.geometry.util.PriorityQueue;
import com.revolsys.util.Pair;

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
public class STRtree<I> extends AbstractSTRtree<BoundingBox, I, BoundingBoxNode<I>>
  implements SpatialIndex<I>, Serializable, Comparator<Boundable<BoundingBox, I>> {

  private static final int DEFAULT_NODE_CAPACITY = 10;

  private static final long serialVersionUID = 259274702368956900L;

  private static double avg(final double a, final double b) {
    return (a + b) / 2d;
  }

  private static double centreX(final BoundingBox e) {
    return avg(e.getMinX(), e.getMaxX());
  };

  private static double centreY(final BoundingBox e) {
    return avg(e.getMinY(), e.getMaxY());
  }

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
  public int compare(final Boundable<BoundingBox, I> o1, final Boundable<BoundingBox, I> o2) {
    return compareDoubles(centreY(o1.getBounds()), centreY(o2.getBounds()));
  }

  public int compareX(final Boundable<BoundingBox, I> o1, final Boundable<BoundingBox, I> o2) {
    return compareDoubles(centreX(o1.getBounds()), centreX(o2.getBounds()));
  }

  /**
   * Returns the number of items in the tree.
   *
   * @return the number of items in the tree
   */
  @Override
  public int depth() {
    return super.depth();
  };

  @Override
  public void forEach(final BoundingBoxProxy boundingBox, final Consumer<? super I> action) {
    query(boundingBox.getBoundingBox(), action);
  }

  @Override
  public void forEach(final Consumer<? super I> action) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void forEach(final double x, final double y, final Consumer<? super I> action) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void forEach(final double minX, final double minY, final double maxX, final double maxY,
    final Consumer<? super I> action) {
    final BoundingBox boundingBox = getGeometryFactory().newBoundingBox(minX, minY, maxX, maxY);
    query(boundingBox, action);
  }

  @Override
  protected Comparator<Boundable<BoundingBox, I>> getComparator() {
    return this;
  }

  @Override
  public int getSize() {
    return this.root.getItemCount();
  }

  /**
   * Inserts an item having the given bounds into the tree.
   */
  @Override
  public void insertItem(final BoundingBox itemEnv, final I item) {
    if (!itemEnv.isEmpty()) {
      super.insert(itemEnv, item);
    }
  }

  @Override
  protected boolean intersects(final BoundingBox aBounds, final BoundingBox bBounds) {
    return aBounds.intersectsFast(bBounds);
  }

  private Pair<I, I> nearestNeighbour(final BoundablePair<I> initBndPair,
    final ItemDistance<I> itemDistance) {
    return nearestNeighbour(initBndPair, itemDistance, Double.POSITIVE_INFINITY);
  }

  private Pair<I, I> nearestNeighbour(final BoundablePair<I> initBndPair,
    final ItemDistance<I> itemDistance, final double maxDistance) {
    double distanceLowerBound = maxDistance;
    BoundablePair<I> minPair = null;

    // initialize internal structures
    final PriorityQueue<BoundablePair<I>> priorityQueue = new PriorityQueue<>();

    // initialize queue
    priorityQueue.add(initBndPair);

    while (!priorityQueue.isEmpty() && distanceLowerBound > 0.0) {
      // pop head of queue and expand one side of pair
      final BoundablePair<I> bndPair = priorityQueue.poll();
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
         * double maxDist = bndPair.getMaximumDistance(); if (maxDist * .99 < lastComputedDistance)
         * return; //
         */

        /**
         * Otherwise, expand one side of the pair,
         * (the choice of which side to expand is heuristically determined)
         * and insert the new expanded pairs into the queue
         */
        bndPair.expandToQueue(priorityQueue, itemDistance, distanceLowerBound);
      }
    }

    final Boundable<BoundingBox, I> boundable1 = minPair.getBoundable(0);
    final Boundable<BoundingBox, I> boundable2 = minPair.getBoundable(1);
    final I item1 = boundable1.getItem();
    final I item2 = boundable2.getItem();
    return new Pair<>(item1, item2);
  }

  /**
   * Finds the item in this tree which is nearest to the given {@link Object},
   * using {@link ItemDistance} as the distance metric.
   * A Branch-and-Bound tree traversal algorithm is used
   * to provide an efficient search.
   * <p>
   * The query <tt>object</tt> does <b>not</b> have to be
   * contained in the tree, but it does
   * have to be compatible with the <tt>itemDistance</tt>
   * distance metric.
   *
   * @param env the envelope of the query item
   * @param item the item to find the nearest neighbour of
   * @param itemDistance a distance metric applicable to the items in this tree and the query item
   * @return the nearest item in this tree
   */
  public I nearestNeighbour(final BoundingBox env, final I item,
    final ItemDistance<I> itemDistance) {
    final Boundable<BoundingBox, I> bnd = new ItemBoundable<>(env, item);
    final BoundingBoxNode<I> root = getRoot();
    final BoundablePair<I> bp = new BoundablePair<>(root, bnd, itemDistance);
    return nearestNeighbour(bp, itemDistance).getValue1();
  }

  /**
   * Finds the two nearest items in the tree,
   * using {@link ItemDistance} as the distance metric.
   * A Branch-and-Bound tree traversal algorithm is used
   * to provide an efficient search.
   *
   * @param itemDistance a distance metric applicable to the items in this tree
   * @return the pair of the nearest items
   */
  public Pair<I, I> nearestNeighbour(final ItemDistance<I> itemDistance) {
    final BoundingBoxNode<I> root = getRoot();
    final BoundablePair<I> bp = new BoundablePair<>(root, root, itemDistance);
    return nearestNeighbour(bp, itemDistance);
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
   * @param itemDistance a distance metric applicable to the items in the trees
   * @return the pair of the nearest items, one from each tree
   */
  public Pair<I, I> nearestNeighbour(final STRtree<I> tree, final ItemDistance<I> itemDistance) {
    final BoundablePair<I> bp = new BoundablePair<>(getRoot(), tree.getRoot(), itemDistance);
    return nearestNeighbour(bp, itemDistance);
  }

  @Override
  protected BoundingBoxNode<I> newNode(final int level) {
    return new BoundingBoxNode<>(level);
  }

  /**
   * Creates the parent level for the given child level. First, orders the items
   * by the x-values of the midpoints, and groups them into vertical slices.
   * For each slice, orders the items by the y-values of the midpoints, and
   * group them into runs of size M (the node capacity). For each run, creates
   * a new (parent) node.
   */
  @Override
  protected List<BoundingBoxNode<I>> newParentBoundables(
    final List<? extends Boundable<BoundingBox, I>> childBoundables, final int newLevel) {
    Assert.isTrue(!childBoundables.isEmpty());
    final int minLeafCount = (int)Math.ceil(childBoundables.size() / (double)getNodeCapacity());
    final List<Boundable<BoundingBox, I>> sortedChildBoundables = new ArrayList<>(childBoundables);
    Collections.sort(sortedChildBoundables, this::compareX);
    final List<List<Boundable<BoundingBox, I>>> verticalSlices = verticalSlices(
      sortedChildBoundables, (int)Math.ceil(Math.sqrt(minLeafCount)));
    return newParentBoundablesFromVerticalSlices(verticalSlices, newLevel);
  }

  protected List<BoundingBoxNode<I>> newParentBoundablesFromVerticalSlice(
    final List<? extends Boundable<BoundingBox, I>> childBoundables, final int newLevel) {
    return super.newParentBoundables(childBoundables, newLevel);
  }

  private List<BoundingBoxNode<I>> newParentBoundablesFromVerticalSlices(
    final List<List<Boundable<BoundingBox, I>>> verticalSlices, final int newLevel) {
    Assert.isTrue(verticalSlices.size() > 0);
    final List<BoundingBoxNode<I>> parentBoundables = new ArrayList<>();
    for (final List<? extends Boundable<BoundingBox, I>> verticalSlice : verticalSlices) {
      parentBoundables.addAll(newParentBoundablesFromVerticalSlice(verticalSlice, newLevel));
    }
    return parentBoundables;
  }

  /**
   * Removes a single item from the tree.
   *
   * @param itemEnv the BoundingBox of the item to remove
   * @param item the item to remove
   * @return <code>true</code> if the item was found
   */
  @Override
  public boolean removeItem(final BoundingBox itemEnv, final I item) {
    return super.remove(itemEnv, item);
  }

  /**
   * @param childBoundables Must be sorted by the x-value of the envelope midpoints
   */
  protected List<List<Boundable<BoundingBox, I>>> verticalSlices(
    final List<Boundable<BoundingBox, I>> childBoundables, final int sliceCount) {
    final int sliceCapacity = (int)Math.ceil(childBoundables.size() / (double)sliceCount);
    final List<List<Boundable<BoundingBox, I>>> slices = new ArrayList<>(sliceCapacity);
    final Iterator<Boundable<BoundingBox, I>> i = childBoundables.iterator();
    for (int j = 0; j < sliceCount; j++) {
      final List<Boundable<BoundingBox, I>> slice = new ArrayList<>();
      slices.add(slice);
      int boundablesAddedToSlice = 0;
      while (i.hasNext() && boundablesAddedToSlice < sliceCapacity) {
        final Boundable<BoundingBox, I> childBoundable = i.next();
        slice.add(childBoundable);
        boundablesAddedToSlice++;
      }
    }
    return slices;
  }

}
