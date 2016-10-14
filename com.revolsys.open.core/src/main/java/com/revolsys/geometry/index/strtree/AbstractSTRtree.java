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
import java.util.List;
import java.util.function.Consumer;

import com.revolsys.geometry.util.Assert;
import com.revolsys.util.Emptyable;
import com.revolsys.util.ExitLoopException;

/**
 * Base class for STRtree and SIRtree. STR-packed R-trees are described in:
 * P. Rigaux, Michel Scholl and Agnes Voisard. <i>Spatial Databases With
 * Application To GIS.</i> Morgan Kaufmann, San Francisco, 2002.
 * <p>
 * This implementation is based on {@link Boundable}s rather than {@link AbstractNode}s,
 * because the STR algorithm operates on both nodes and
 * data, both of which are treated as Boundables.
 *
 * @see STRtree
 * @see SIRtree
 *
 * @version 1.7
 */
public abstract class AbstractSTRtree<B, I, N extends AbstractNode<B, I>>
  implements Emptyable, Serializable {

  private static final int DEFAULT_NODE_CAPACITY = 10;

  /**
   *
   */
  private static final long serialVersionUID = -3886435814360241337L;

  protected static int compareDoubles(final double a, final double b) {
    return a > b ? 1 : a < b ? -1 : 0;
  }

  private boolean built = false;

  /**
   * Set to <tt>null</tt> when index is built, to avoid retaining memory.
   */
  private List<ItemBoundable<B, I>> itemBoundables = new ArrayList<>();

  private final int nodeCapacity;

  protected N root;

  /**
   * Constructs an AbstractSTRtree with the
   * default node capacity.
   */
  public AbstractSTRtree() {
    this(DEFAULT_NODE_CAPACITY);
  }

  /**
   * Constructs an AbstractSTRtree with the specified maximum number of child
   * nodes that a node may have
   *
   * @param nodeCapacity the maximum number of child nodes in a node
   */
  public AbstractSTRtree(final int nodeCapacity) {
    Assert.isTrue(nodeCapacity > 1, "Node capacity must be greater than 1");
    this.nodeCapacity = nodeCapacity;
  }

  protected List<Boundable<B, I>> boundablesAtLevel(final int level) {
    final List<Boundable<B, I>> boundables = new ArrayList<>();
    this.root.boundablesAtLevel(level, boundables);
    return boundables;
  }

  /**
   * Creates parent nodes, grandparent nodes, and so forth up to the root
   * node, for the data that has been inserted into the tree. Can only be
   * called once, and thus can be called only after all of the data has been
   * inserted into the tree.
   */
  public synchronized void build() {
    if (this.built) {
      return;
    }
    this.root = this.itemBoundables.isEmpty() ? newNode(0)
      : newNodeHigherLevels(this.itemBoundables, -1);
    // the item list is no longer needed
    this.itemBoundables = null;
    this.built = true;
  }

  protected int depth() {
    if (isEmpty()) {
      return 0;
    } else {
      build();
      return this.root.getDepth();
    }
  }

  protected abstract Comparator<Boundable<B, I>> getComparator();

  /**
   * Returns the maximum number of child nodes that a node may have
   */
  public int getNodeCapacity() {
    return this.nodeCapacity;
  }

  public N getRoot() {
    build();
    return this.root;
  }

  protected void insert(final B bounds, final I item) {
    Assert.isTrue(!this.built,
      "Cannot insert items into an STR packed R-tree after it has been built.");
    final ItemBoundable<B, I> itemBoundable = new ItemBoundable<>(bounds, item);
    this.itemBoundables.add(itemBoundable);
  }

  /**
   * For STRtrees, the bounds will be Envelopes; for SIRtrees, Intervals;
   * for other subclasses of AbstractSTRtree, some other class.
   * @param aBounds the bounds of one spatial object
   * @param bBounds the bounds of another spatial object
   * @return whether the two bounds intersect
   */
  protected abstract boolean intersects(B bounds1, B bounds2);

  /**
   * Tests whether the index contains any items.
   * This method does not build the index,
   * so items can still be inserted after it has been called.
   *
   * @return true if the index does not contain any items
   */
  @Override
  public boolean isEmpty() {
    if (!this.built) {
      return this.itemBoundables.isEmpty();
    }
    return this.root.isEmpty();
  }

  /**
   * Gets a tree structure (as a nested list)
   * corresponding to the structure of the items and nodes in this tree.
   * <p>
   * The returned {@link List}s contain either {@link Object} items,
   * or Lists which correspond to subtrees of the tree
   * Subtrees which do not contain any items are not included.
   * <p>
   * Builds the tree if necessary.
   *
   * @return a List of items and/or Lists
   */
  public List<?> itemsTree() {
    build();

    final List<?> valuesTree = itemsTree(this.root);
    if (valuesTree == null) {
      return new ArrayList<>();
    }
    return valuesTree;
  }

  @SuppressWarnings({
    "rawtypes", "unchecked"
  })
  private List itemsTree(final N node) {
    final List valuesTreeForNode = new ArrayList();
    for (final Boundable<B, I> childBoundable : node.getChildren()) {
      if (childBoundable instanceof AbstractNode) {
        final List valuesTreeForChild = itemsTree((N)childBoundable);
        // only add if not null (which indicates an item somewhere in this tree
        if (valuesTreeForChild != null) {
          valuesTreeForNode.add(valuesTreeForChild);
        }
      } else if (childBoundable instanceof ItemBoundable) {
        valuesTreeForNode.add(((ItemBoundable)childBoundable).getItem());
      } else {
        Assert.shouldNeverReachHere();
      }
    }
    if (valuesTreeForNode.size() <= 0) {
      return null;
    }
    return valuesTreeForNode;
  }

  protected N lastNode(final List<N> nodes) {
    return nodes.get(nodes.size() - 1);
  }

  protected abstract N newNode(int level);

  /**
   * Creates the levels higher than the given level
   *
   * @param boundablesOfALevel
   *            the level to build on
   * @param level
   *            the level of the Boundables, or -1 if the boundables are item
   *            boundables (that is, below level 0)
   * @return the root, which may be a ParentNode or a LeafNode
   */
  private N newNodeHigherLevels(final List<? extends Boundable<B, I>> boundablesOfALevel,
    final int level) {
    Assert.isTrue(!boundablesOfALevel.isEmpty());
    final List<N> parentBoundables = newParentBoundables(boundablesOfALevel, level + 1);
    if (parentBoundables.size() == 1) {
      return parentBoundables.get(0);
    }
    return newNodeHigherLevels(parentBoundables, level + 1);
  }

  /**
   * Sorts the childBoundables then divides them into groups of size M, where
   * M is the node capacity.
   */
  protected List<N> newParentBoundables(final List<? extends Boundable<B, I>> childBoundables,
    final int newLevel) {
    Assert.isTrue(!childBoundables.isEmpty());
    final List<N> parentBoundables = new ArrayList<>();
    parentBoundables.add(newNode(newLevel));
    final List<Boundable<B, I>> sortedChildBoundables = new ArrayList<>(childBoundables);
    Collections.sort(sortedChildBoundables, getComparator());
    for (final Boundable<B, I> childBoundable : sortedChildBoundables) {
      if (lastNode(parentBoundables).getChildCount() == getNodeCapacity()) {
        parentBoundables.add(newNode(newLevel));
      }
      lastNode(parentBoundables).addChild(childBoundable);
    }
    return parentBoundables;
  }

  /**
   *  Also builds the tree, if necessary.
   */
  public List<I> query(final B searchBounds) {
    build();
    final List<I> matches = new ArrayList<>();
    if (!isEmpty()) {
      query(searchBounds, matches::add);
    }
    return matches;
  }

  /**
   *  Also builds the tree, if necessary.
   */
  public void query(final B searchBounds, final Consumer<? super I> visitor) {
    build();
    if (!isEmpty()) {
      try {
        this.root.query(this, searchBounds, visitor);
      } catch (final ExitLoopException e) {
      }
    }
  }

  /**
   * Removes an item from the tree.
   * (Builds the tree, if necessary.)
   */
  protected boolean remove(final B searchBounds, final I item) {
    build();
    if (this.itemBoundables.isEmpty()) {
      Assert.isTrue(this.root.getBounds() == null);
    }
    if (intersects(this.root.getBounds(), searchBounds)) {
      return this.root.remove(this, searchBounds, item);
    }
    return false;
  }

  public int size() {
    if (isEmpty()) {
      return 0;
    } else {
      build();
      return this.root.getItemCount();
    }
  }
}
