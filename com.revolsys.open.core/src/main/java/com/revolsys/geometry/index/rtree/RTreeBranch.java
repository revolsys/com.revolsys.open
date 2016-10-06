package com.revolsys.geometry.index.rtree;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

import com.revolsys.collection.ArrayUtil;

public class RTreeBranch<T> extends RTreeNode<T> implements Iterable<RTreeNode<T>> {

  private RTreeNode<T>[] nodes;

  private int size;

  public RTreeBranch() {
  }

  @SuppressWarnings("unchecked")
  public RTreeBranch(final int size) {
    this.nodes = ArrayUtil.newArray(RTreeNode.class, size);
  }

  protected RTreeBranch(final int size, final List<RTreeNode<T>> nodes) {
    this(size);
    for (final RTreeNode<T> node : nodes) {
      add(node);
    }
  }

  private void add(final RTreeNode<T> node) {
    this.nodes[this.size] = node;
    this.size++;
    expandBoundingBox(node);
  }

  @Override
  protected void expandBoundingBox(final double... bounds) {
    super.expandBoundingBox(bounds);
  }

  @Override
  protected void expandBoundingBox(final double minX, final double minY, final double maxX,
    final double maxY) {
    super.expandBoundingBox(minX, minY, maxX, maxY);
  }

  @Override
  public void forEach(final double minX, final double minY, final double maxX, final double maxY,
    final Consumer<T> action) {
    for (int i = 0; i < this.size; i++) {
      final RTreeNode<T> node = this.nodes[i];
      if (node.intersectsBoundingBox(minX, minY, maxX, maxY)) {
        node.forEach(minX, minY, maxX, maxY, action);
        ;
      }
    }
  }

  @Override
  public void forEach(final double minX, final double minY, final double maxX, final double maxY,
    final Predicate<T> filter, final Consumer<T> action) {
    for (int i = 0; i < this.size; i++) {
      final RTreeNode<T> node = this.nodes[i];
      if (node.intersectsBoundingBox(minX, minY, maxX, maxY)) {
        node.forEach(minX, minY, maxX, maxY, filter, action);
        ;
      }
    }
  }

  @Override
  public void forEach(final double x, final double y, final Predicate<T> filter,
    final Consumer<T> action) {
    for (int i = 0; i < this.size; i++) {
      final RTreeNode<T> node = this.nodes[i];
      if (node.intersectsBoundingBox(x, y)) {
        node.forEach(x, y, filter, action);
      }
    }
  }

  @Override
  public void forEachValue(final Consumer<? super T> action) {
    for (int i = 0; i < this.size; i++) {
      final RTreeNode<T> node = this.nodes[i];
      node.forEachValue(action);
    }
  }

  public List<RTreeNode<T>> getNodes() {
    final List<RTreeNode<T>> nodes = new ArrayList<>();
    for (int i = 0; i < this.size; i++) {
      final RTreeNode<T> node = this.nodes[i];
      nodes.add(node);
    }
    return nodes;
  }

  public int getSize() {
    return this.size;
  }

  @Override
  public Iterator<RTreeNode<T>> iterator() {
    return getNodes().iterator();
  }

  @Override
  public boolean remove(final LinkedList<RTreeNode<T>> path, final double minX, final double minY,
    final double maxX, final double maxY, final T object) {
    for (int i = 0; i < this.size; i++) {
      final RTreeNode<T> node = this.nodes[i];
      if (node.covers(minX, minY, maxX, maxY)) {
        if (node.remove(path, minX, minY, maxX, maxY, object)) {
          if (path != null) {
            path.addFirst(this);
          }
          updateEnvelope();
          return true;
        }
      }
    }
    return false;
  }

  public void replace(final RTreeNode<T> node, final List<RTreeNode<T>> newNodes) {
    for (int i = 1; i < newNodes.size(); i++) {
      final RTreeNode<T> newNode = newNodes.get(i);
      add(newNode);
    }
    for (int i = 0; i < this.size - newNodes.size() + 1; i++) {
      final RTreeNode<T> childNode = this.nodes[i];
      if (childNode == node) {
        this.nodes[i] = newNodes.get(0);
        return;
      }
    }
  }

  public List<RTreeNode<T>> split(final RTreeNode<T> node, final List<RTreeNode<T>> newNodes) {
    final RTreeBranch<T> branch1 = new RTreeBranch<>(this.nodes.length);
    final RTreeBranch<T> branch2 = new RTreeBranch<>(this.nodes.length);

    // TODO Add some ordering to the results
    final int midPoint = (int)Math.ceil(this.size / 2.0);
    for (int i = 0; i <= midPoint; i++) {
      final RTreeNode<T> childNode = this.nodes[i];
      if (childNode == node) {
        branch1.add(newNodes.get(0));
      } else {
        branch1.add(childNode);
      }
    }
    for (int i = midPoint + 1; i < this.size; i++) {
      final RTreeNode<T> childNode = this.nodes[i];
      if (childNode == node) {
        branch1.add(newNodes.get(0));
      } else {
        branch2.add(childNode);
      }
    }
    final RTreeNode<T> newNode = newNodes.get(1);
    branch2.add(newNode);
    return Arrays.<RTreeNode<T>> asList(branch1, branch2);
  }

  @Override
  protected void updateEnvelope() {
    double minX = Double.MAX_VALUE;
    double maxX = -Double.MAX_VALUE;
    double minY = Double.MAX_VALUE;
    double maxY = -Double.MAX_VALUE;

    for (int i = 0; i < this.size; i++) {
      final RTreeNode<T> node = this.nodes[i];
      final double nodeMinX = node.getMinX();
      if (nodeMinX < minX) {
        minX = nodeMinX;
      }
      final double nodeMinY = node.getMinY();
      if (nodeMinY < minY) {
        minY = nodeMinY;
      }

      final double nodeMaxX = node.getMaxX();
      if (nodeMaxX > maxX) {
        maxX = nodeMaxX;
      }
      final double nodeMaxY = node.getMaxY();
      if (nodeMaxY > maxY) {
        maxY = nodeMaxY;
      }
    }
    setBoundingBox(minX, maxX, minY, maxY);
  }
}
