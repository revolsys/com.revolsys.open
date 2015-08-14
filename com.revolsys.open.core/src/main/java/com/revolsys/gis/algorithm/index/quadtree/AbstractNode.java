package com.revolsys.gis.algorithm.index.quadtree;

import java.io.Serializable;
import java.util.Arrays;
import java.util.function.Consumer;

import com.revolsys.jts.index.DoubleBits;
import com.revolsys.jts.index.IntervalSize;
import com.revolsys.jts.util.BoundingBoxUtil;
import com.revolsys.util.Emptyable;

public abstract class AbstractNode<T> implements Emptyable, Serializable {
  /**
   *
   */
  private static final long serialVersionUID = 1L;

  private static int computeQuadLevel(final double... bounds) {
    final double dx = bounds[2] - bounds[0];
    final double dy = bounds[3] - bounds[1];
    final double dMax = dx > dy ? dx : dy;
    final int level = DoubleBits.exponent(dMax) + 1;
    return level;
  }

  private static int getSubnodeIndex(final double centreX, final double centreY,
    final double... bounds) {
    final double minX = bounds[0];
    final double minY = bounds[1];
    final double maxX = bounds[2];
    final double maxY = bounds[3];
    int subnodeIndex = -1;

    if (minX >= centreX) {
      if (minY >= centreY) {
        subnodeIndex = 3;
      }
      if (maxY <= centreY) {
        subnodeIndex = 1;
      }
    }
    if (maxX <= centreX) {
      if (minY >= centreY) {
        subnodeIndex = 2;
      }
      if (maxY <= centreY) {
        subnodeIndex = 0;
      }
    }
    return subnodeIndex;
  }

  private static void setBounds(final double minX, final double minY, final double[] newBounds,
    final int level) {
    final double quadSize = DoubleBits.powerOf2(level);
    final double x1 = Math.floor(minX / quadSize) * quadSize;
    final double y1 = Math.floor(minY / quadSize) * quadSize;
    final double x2 = x1 + quadSize;
    final double y2 = y1 + quadSize;
    newBounds[0] = x1;
    newBounds[1] = y1;
    newBounds[2] = x2;
    newBounds[3] = y2;
  }

  private AbstractNode<T>[] nodes;

  private double minX;

  private double minY;

  private double maxX;

  private double maxY;

  private final int level;

  protected AbstractNode() {
    this.level = Integer.MIN_VALUE;
  }

  public AbstractNode(final int level, final double... bounds) {
    this.level = level;
    this.minX = bounds[0];
    this.minY = bounds[1];
    this.maxX = bounds[2];
    this.maxY = bounds[3];
  }

  public void add(final QuadTree<T> tree, final double[] bounds, final T item) {
    for (int i = 0; i < getItemCount(); i++) {
      final T oldItem = getItem(tree, i);
      if (oldItem == item) {
        doRemove(i);
      }
    }
    doAdd(tree, bounds, item);
  }

  public void clear() {
    this.nodes = null;
  }

  public AbstractNode<T> createExpanded(final AbstractNode<T> node, double[] bounds) {
    bounds = bounds.clone();
    if (node != null) {
      BoundingBoxUtil.expand(bounds, 2, 0, node.minX);
      BoundingBoxUtil.expand(bounds, 2, 0, node.maxX);
      BoundingBoxUtil.expand(bounds, 2, 1, node.minY);
      BoundingBoxUtil.expand(bounds, 2, 1, node.maxY);
    }

    final AbstractNode<T> largerNode = createNode(bounds);
    if (node != null) {
      largerNode.insertNode(node);
    }
    return largerNode;
  }

  protected AbstractNode<T> createNode(final double[] bounds) {
    final double[] newBounds = new double[4];
    final double minX = bounds[0];
    final double minY = bounds[1];
    final double maxX = bounds[2];
    final double maxY = bounds[3];
    int level = computeQuadLevel(bounds);
    setBounds(minX, minY, newBounds, level);
    while (!BoundingBoxUtil.covers(newBounds[0], newBounds[1], newBounds[2], newBounds[3], minX,
      minY, maxX, maxY)) {
      level++;
      setBounds(minX, minY, newBounds, level);
    }

    return createNode(level, newBounds);
  }

  protected abstract AbstractNode<T> createNode(int level, double... newBounds);

  private AbstractNode<T> createSubnode(final int index) {
    // create a new subquad in the appropriate quadrant

    double minX = 0.0;
    double maxX = 0.0;
    double minY = 0.0;
    double maxY = 0.0;

    final double centreX = getCentreX();
    final double centreY = getCentreY();
    switch (index) {
      case 0:
        minX = this.minX;
        maxX = centreX;
        minY = this.minY;
        maxY = centreY;
      break;
      case 1:
        minX = centreX;
        maxX = this.maxX;
        minY = this.minY;
        maxY = centreY;
      break;
      case 2:
        minX = this.minX;
        maxX = centreX;
        minY = centreY;
        maxY = this.maxY;
      break;
      case 3:
        minX = centreX;
        maxX = this.maxX;
        minY = centreY;
        maxY = this.maxY;
      break;
    }
    final AbstractNode<T> node = createNode(this.level - 1, minX, minY, maxX, maxY);
    return node;
  }

  public int depth() {
    int depth = 0;
    for (int i = 0; i < 4; i++) {
      final AbstractNode<T> node = getNode(i);
      if (node != null) {
        final int nodeDepth = node.depth();
        if (nodeDepth > depth) {
          depth = nodeDepth;
        }
      }
    }
    return depth + 1;
  }

  protected abstract void doAdd(QuadTree<T> tree, double[] bounds, T item);

  protected abstract void doRemove(int index);

  public AbstractNode<T> find(final double[] bounds) {
    final int subnodeIndex = getSubnodeIndex(getCentreX(), getCentreY(), bounds);
    if (subnodeIndex == -1) {
      return this;
    }
    if (getNode(subnodeIndex) != null) {
      final AbstractNode<T> node = getNode(subnodeIndex);
      return node.find(bounds);
    }
    return this;
  }

  public void forEach(final QuadTree<T> tree, final Consumer<T> action) {
    for (int i = 0; i < getItemCount(); i++) {
      final T item = getItem(tree, i);
      action.accept(item);
    }

    for (int i = 0; i < 4; i++) {
      final AbstractNode<T> node = getNode(i);
      if (node != null) {
        node.forEach(tree, action);
      }
    }
  }

  public void forEach(final QuadTree<T> tree, final double[] bounds, final Consumer<T> action) {
    if (isSearchMatch(bounds)) {
      final int itemCount = getItemCount();
      for (int i = 0; i < itemCount; i++) {
        final double[] itemBounds = getBounds(tree, i);
        if (BoundingBoxUtil.intersects(bounds, itemBounds)) {
          final T item = getItem(tree, i);
          action.accept(item);
        }
      }

      for (int i = 0; i < 4; i++) {
        final AbstractNode<T> node = getNode(i);
        if (node != null) {
          node.forEach(tree, bounds, action);
        }
      }
    }
  }

  protected abstract double[] getBounds(QuadTree<T> tree, int i);

  private double getCentreX() {
    if (isRoot()) {
      return 0;
    } else {
      return (this.minX + this.maxX) / 2;
    }
  }

  private double getCentreY() {
    if (isRoot()) {
      return 0;
    } else {
      return (this.minY + this.maxY) / 2;
    }
  }

  protected abstract T getItem(QuadTree<T> tree, int i);

  public abstract int getItemCount();

  public AbstractNode<T> getNode(final double[] bounds) {
    final int subnodeIndex = getSubnodeIndex(getCentreX(), getCentreY(), bounds);
    if (subnodeIndex != -1) {
      final AbstractNode<T> node = getSubnode(subnodeIndex);
      return node.getNode(bounds);
    } else {
      return this;
    }
  }

  protected AbstractNode<T> getNode(final int i) {
    if (this.nodes == null) {
      return null;
    } else {
      return this.nodes[i];
    }
  }

  protected int getNodeCount() {
    int nodeCount = 0;
    for (int i = 0; i < 4; i++) {
      final AbstractNode<T> node = getNode(i);
      if (node != null) {
        nodeCount += node.size();
      }
    }
    return nodeCount + 1;
  }

  private AbstractNode<T> getSubnode(final int index) {
    if (getNode(index) == null) {
      setNode(index, createSubnode(index));
    }
    return getNode(index);
  }

  public boolean hasChildren() {
    for (int i = 0; i < 4; i++) {
      if (getNode(i) != null) {
        return true;
      }
    }
    return false;
  }

  public boolean hasItems() {
    return getItemCount() > 0;
  }

  private void insertContained(final QuadTree<T> tree, final AbstractNode<T> root,
    final double[] bounds, final T item) {
    final boolean isZeroX = IntervalSize.isZeroWidth(bounds[2], bounds[0]);
    final boolean isZeroY = IntervalSize.isZeroWidth(bounds[3], bounds[1]);
    AbstractNode<T> node;
    if (isZeroX || isZeroY) {
      node = root.find(bounds);
    } else {
      node = root.getNode(bounds);
    }
    node.add(tree, bounds, item);
  }

  void insertNode(final AbstractNode<T> node) {
    final double centreX = getCentreX();
    final double centreY = getCentreY();
    final int index = getSubnodeIndex(centreX, centreY, node.minX, node.minY, node.maxX, node.maxY);
    if (node.level == this.level - 1) {
      setNode(index, node);
    } else {
      final AbstractNode<T> childNode = createSubnode(index);
      childNode.insertNode(node);
      setNode(index, childNode);
    }
  }

  protected void insertRoot(final QuadTree<T> tree, final double[] bounds, final T item) {
    final int index = getSubnodeIndex(0, 0, bounds);
    if (index == -1) {
      add(tree, bounds, item);
    } else {
      final double minX2 = bounds[0];
      final double minY2 = bounds[1];
      final double maxX2 = bounds[2];
      final double maxY2 = bounds[3];
      AbstractNode<T> node = getNode(index);
      if (node == null || !BoundingBoxUtil.covers(node.minX, node.minY, node.maxX, node.maxY, minX2,
        minY2, maxX2, maxY2)) {
        final AbstractNode<T> largerNode = createExpanded(node, bounds);
        setNode(index, largerNode);
        node = largerNode;
      }
      insertContained(tree, node, bounds, item);
    }
  }

  @Override
  public boolean isEmpty() {
    boolean isEmpty = !hasItems();
    for (int i = 0; i < 4; i++) {
      final AbstractNode<T> node = getNode(i);
      if (node != null) {
        if (!node.isEmpty()) {
          isEmpty = false;
        }
      }
    }
    return isEmpty;
  }

  public boolean isPrunable() {
    return !(hasChildren() || hasItems());
  }

  public boolean isRoot() {
    return this.level == Integer.MIN_VALUE;
  }

  protected boolean isSearchMatch(final double[] bounds) {
    if (isRoot()) {
      return true;
    } else if (bounds == null) {
      return false;
    } else {
      final double minX2 = bounds[0];
      final double minY2 = bounds[1];
      final double maxX2 = bounds[2];
      final double maxY2 = bounds[3];
      return !(minX2 > this.maxX || maxX2 < this.minX || minY2 > this.maxY || maxY2 < this.minY);
    }
  }

  public boolean remove(final QuadTree<T> tree, final double[] bounds, final T item) {
    boolean removed = false;
    if (isSearchMatch(bounds)) {
      for (int i = 0; i < 4; i++) {
        final AbstractNode<T> node = getNode(i);
        if (node != null) {
          if (node.remove(tree, bounds, item)) {
            if (node.isPrunable()) {
              setNode(i, null);
            }
            removed = true;
          }
        }
      }
      for (int i = 0; i < getItemCount(); i++) {
        final T value = getItem(tree, i);
        if (value == item) {
          doRemove(i);
          removed = true;
        }
      }
    }
    return removed;

  }

  @SuppressWarnings("unchecked")
  protected void setNode(final int i, final AbstractNode<T> node) {
    if (this.nodes == null) {
      if (node == null) {
        return;
      }
      this.nodes = new AbstractNode[4];
    }
    this.nodes[i] = node;
  }

  protected int size() {
    int subSize = 0;
    for (int i = 0; i < 4; i++) {
      final AbstractNode<T> node = getNode(i);
      if (node != null) {
        subSize += node.size();
      }
    }
    return subSize + getItemCount();
  }

  @Override
  public String toString() {
    if (this.nodes == null) {
      return "[]=" + getItemCount();
    } else {
      return Arrays.toString(this.nodes) + "=" + getItemCount();
    }
  }
}
