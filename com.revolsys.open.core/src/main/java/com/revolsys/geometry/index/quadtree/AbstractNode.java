package com.revolsys.geometry.index.quadtree;

import java.io.Serializable;
import java.util.Arrays;
import java.util.function.Consumer;

import com.revolsys.geometry.index.DoubleBits;
import com.revolsys.geometry.index.IntervalSize;
import com.revolsys.geometry.util.RectangleUtil;
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

  private final int level;

  private double maxX;

  private double maxY;

  private double minX;

  private double minY;

  @SuppressWarnings("unchecked")
  protected final AbstractNode<T>[] nodes = new AbstractNode[4];

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
    if (!changeItem(tree, bounds, item)) {
      addDo(tree, bounds, item);
    }
  }

  protected abstract void addDo(QuadTree<T> tree, double[] bounds, T item);

  protected abstract boolean changeItem(final QuadTree<T> tree, final double[] bounds,
    final T item);

  public void clear() {
    Arrays.fill(this.nodes, null);
  }

  public int depth() {

    int depth = 0;
    for (final AbstractNode<T> node : this.nodes) {
      if (node != null) {
        final int nodeDepth = node.depth();
        if (nodeDepth > depth) {
          depth = nodeDepth;
        }
      }
    }
    return depth + 1;
  }

  public AbstractNode<T> find(final double[] bounds) {
    final int subnodeIndex = getSubnodeIndex(getCentreX(), getCentreY(), bounds);
    if (subnodeIndex == -1) {
      return this;
    }
    final AbstractNode<T> node = this.nodes[subnodeIndex];
    if (node != null) {
      return node.find(bounds);
    }
    return this;
  }

  public void forEach(final QuadTree<T> tree, final Consumer<? super T> action) {
    forEachItem(tree, action);

    for (final AbstractNode<T> node : this.nodes) {
      if (node != null) {
        node.forEach(tree, action);
      }
    }
  }

  public void forEach(final QuadTree<T> tree, final double[] bounds,
    final Consumer<? super T> action) {
    if (isSearchMatch(bounds)) {
      forEachItem(tree, bounds, action);

      for (final AbstractNode<T> node : this.nodes) {
        if (node != null) {
          node.forEach(tree, bounds, action);
        }
      }
    }
  }

  protected abstract void forEachItem(QuadTree<T> tree, Consumer<? super T> action);

  protected abstract void forEachItem(final QuadTree<T> tree, final double[] bounds,
    final Consumer<? super T> action);

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

  protected int getNodeCount() {
    int nodeCount = 0;
    for (final AbstractNode<T> node : this.nodes) {
      if (node != null) {
        nodeCount += node.size();
      }
    }
    return nodeCount + 1;
  }

  private AbstractNode<T> getSubnode(final int index) {
    final AbstractNode<T>[] nodes = this.nodes;
    final AbstractNode<T> node = nodes[index];
    if (node == null) {
      final AbstractNode<T> newNode = newSubnode(index);
      nodes[index] = newNode;
      return newNode;
    }
    return node;
  }

  public boolean hasChildren() {
    for (final AbstractNode<T> node : this.nodes) {
      if (node != null) {
        return true;
      }
    }
    return false;
  }

  protected abstract boolean hasItems();

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
      this.nodes[index] = node;
    } else {
      final AbstractNode<T> childNode = newSubnode(index);
      childNode.insertNode(node);
      this.nodes[index] = childNode;
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
      final AbstractNode<T>[] nodes = this.nodes;
      AbstractNode<T> node = nodes[index];
      if (node == null || !RectangleUtil.covers(node.minX, node.minY, node.maxX, node.maxY, minX2,
        minY2, maxX2, maxY2)) {
        node = newNodeExpanded(node, bounds);
        nodes[index] = node;
      }
      insertContained(tree, node, bounds, item);
    }
  }

  @Override
  public boolean isEmpty() {
    final boolean isEmpty = !hasItems();
    for (final AbstractNode<T> node : this.nodes) {
      if (node != null) {
        if (!node.isEmpty()) {
          return false;
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

  protected AbstractNode<T> newNode(final double[] bounds) {
    final double[] newBounds = new double[4];
    final double minX = bounds[0];
    final double minY = bounds[1];
    final double maxX = bounds[2];
    final double maxY = bounds[3];
    int level = computeQuadLevel(bounds);
    setBounds(minX, minY, newBounds, level);
    while (!RectangleUtil.covers(newBounds[0], newBounds[1], newBounds[2], newBounds[3], minX, minY,
      maxX, maxY)) {
      level++;
      setBounds(minX, minY, newBounds, level);
    }

    return newNode(level, newBounds);
  }

  protected abstract AbstractNode<T> newNode(int level, double... newBounds);

  public AbstractNode<T> newNodeExpanded(final AbstractNode<T> node, double[] bounds) {
    bounds = bounds.clone();
    if (node != null) {
      RectangleUtil.expand(bounds, 2, 0, node.minX);
      RectangleUtil.expand(bounds, 2, 0, node.maxX);
      RectangleUtil.expand(bounds, 2, 1, node.minY);
      RectangleUtil.expand(bounds, 2, 1, node.maxY);
    }

    final AbstractNode<T> largerNode = newNode(bounds);
    if (node != null) {
      largerNode.insertNode(node);
    }
    return largerNode;
  }

  private AbstractNode<T> newSubnode(final int index) {
    // Construct a new new subquad in the appropriate quadrant

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
    final AbstractNode<T> node = newNode(this.level - 1, minX, minY, maxX, maxY);
    return node;
  }

  public boolean removeItem(final QuadTree<T> tree, final double[] bounds, final T item) {
    boolean removed = false;
    if (isSearchMatch(bounds)) {
      int nodeIndex = 0;
      final AbstractNode<T>[] nodes = this.nodes;
      for (final AbstractNode<T> node : nodes) {
        if (node != null) {
          if (node.removeItem(tree, bounds, item)) {
            if (node.isPrunable()) {
              nodes[nodeIndex] = null;
            }
            removed = true;
          }
        }
        nodeIndex++;
      }
      removed |= removeItem(tree, item);
    }
    return removed;

  }

  protected abstract boolean removeItem(final QuadTree<T> tree, final T item);

  protected int size() {
    int subSize = 0;
    for (final AbstractNode<T> node : this.nodes) {
      if (node != null) {
        subSize += node.size();
      }
    }
    return subSize + getItemCount();
  }

  @Override
  public String toString() {
    return Arrays.toString(this.nodes) + "=" + getItemCount();
  }
}
