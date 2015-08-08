package com.revolsys.gis.algorithm.index;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

import com.revolsys.jts.geom.BoundingBox;

public class RTree<T> extends AbstractSpatialIndex<T> {

  private int maxEntries;

  private RTreeNode<T> root = new RTreeLeaf<T>(this.maxEntries);

  private int size;

  public RTree() {
    this(12, 32);
  }

  public RTree(final int minEntries, final int maxEntries) {
    this.maxEntries = maxEntries;
    this.root = new RTreeLeaf<T>(maxEntries);
  }

  private RTreeLeaf<T> chooseLeaf(final List<RTreeBranch<T>> path, final RTreeNode<T> node,
    final BoundingBox envelope) {
    if (node instanceof RTreeLeaf) {
      return (RTreeLeaf<T>)node;
    } else {
      final RTreeBranch<T> branch = (RTreeBranch<T>)node;
      branch.setBoundingBox(branch.getBoundingBox().expandToInclude(envelope));
      path.add(branch);
      double minExpansion = Float.MAX_VALUE;
      RTreeNode<T> next = null;
      for (final RTreeNode<T> childNode : branch) {
        final double expansion = getRequiredExpansion(childNode, envelope);
        if (expansion < minExpansion) {
          minExpansion = expansion;
          next = childNode;
        } else if (expansion == minExpansion) {
          final double childArea = childNode.getArea();
          final double minArea = next.getArea();
          if (childArea < minArea) {
            next = childNode;
          }
        }
      }
      return chooseLeaf(path, next, envelope);
    }
  }

  @Override
  public void forEach(final BoundingBox envelope, final Consumer<T> action) {
    this.root.forEach(envelope, action);
  }

  @Override
  public void forEach(final BoundingBox envelope, final Predicate<T> filter,
    final Consumer<T> action) {
    this.root.forEach(envelope, filter, action);
  }

  @Override
  public void forEach(final Consumer<T> action) {
    this.root.forEachValue(action);
  }

  private double getRequiredExpansion(final RTreeNode<T> node, final BoundingBox envelope) {
    double areaExpansion = 0;

    final BoundingBox boundingBox = node.getBoundingBox();
    final double minX1 = boundingBox.getMinX();
    final double minX2 = envelope.getMinX();
    final double minY1 = boundingBox.getMinY();
    final double minY2 = envelope.getMinY();

    final double maxX1 = boundingBox.getMaxX();
    final double maxX2 = envelope.getMaxX();
    final double maxY1 = boundingBox.getMaxY();
    final double maxY2 = envelope.getMaxY();

    final double maxWidth = Math.max(maxX1, maxX2) - Math.min(minX1, minX2);
    final double maxHeight = Math.max(maxY1, maxY2) - Math.min(minY1, minY2);
    if (minX1 > minX2) {
      areaExpansion += (minX1 - minX2) * maxHeight;
    }
    if (maxX1 < maxX2) {
      areaExpansion += (maxX2 - maxX1) * maxHeight;
    }
    if (minY1 > minY2) {
      areaExpansion += (minY1 - minY2) * maxWidth;
    }
    if (maxY1 < maxY2) {
      areaExpansion += (maxY2 - maxY1) * maxWidth;
    }

    return areaExpansion;
  }

  public int getSize() {
    return this.size;
  }

  @Override
  public void put(final BoundingBox envelope, final T object) {
    final LinkedList<RTreeBranch<T>> path = new LinkedList<RTreeBranch<T>>();
    final RTreeLeaf<T> leaf = chooseLeaf(path, this.root, envelope);
    if (leaf.getSize() == this.maxEntries) {
      final List<RTreeNode<T>> newNodes = leaf.split(envelope, object);
      replace(path, leaf, newNodes);
    } else {
      leaf.add(envelope, object);
    }
    this.size++;
  }

  @Override
  public boolean remove(final BoundingBox envelope, final T object) {
    // TODO rebalance after remove
    final LinkedList<RTreeNode<T>> path = new LinkedList<RTreeNode<T>>();
    if (this.root.remove(path, envelope, object)) {
      this.size--;
      return true;
    } else {
      return false;
    }
  }

  private void replace(final LinkedList<RTreeBranch<T>> path, final RTreeNode<T> oldNode,
    final List<RTreeNode<T>> newNodes) {
    if (path.isEmpty()) {
      this.root = new RTreeBranch<T>(this.maxEntries, newNodes);
    } else {
      final RTreeBranch<T> parentNode = path.removeLast();
      if (parentNode.getSize() + newNodes.size() - 1 >= this.maxEntries) {
        final List<RTreeNode<T>> newParentNodes = parentNode.split(oldNode, newNodes);
        replace(path, parentNode, newParentNodes);
      } else {
        parentNode.replace(oldNode, newNodes);
      }
    }

  }

}
