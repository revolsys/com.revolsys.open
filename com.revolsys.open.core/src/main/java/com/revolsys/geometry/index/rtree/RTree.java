package com.revolsys.geometry.index.rtree;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

import com.revolsys.geometry.index.BoundingBoxSpatialIndex;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.impl.BoundingBoxDoubleXY;
import com.revolsys.util.ExitLoopException;

public class RTree<T> implements BoundingBoxSpatialIndex<T> {

  private int maxEntries;

  private RTreeNode<T> root = new RTreeLeaf<>(this.maxEntries);

  private int size;

  public RTree() {
    this(12, 32);
  }

  public RTree(final int minEntries, final int maxEntries) {
    this.maxEntries = maxEntries;
    this.root = new RTreeLeaf<>(maxEntries);
  }

  private RTreeLeaf<T> chooseLeaf(final List<RTreeBranch<T>> path, final RTreeNode<T> node,
    final BoundingBox boundingBox) {
    if (node instanceof RTreeLeaf) {
      return (RTreeLeaf<T>)node;
    } else {
      final RTreeBranch<T> branch = (RTreeBranch<T>)node;
      branch.expandBoundingBox(boundingBox);
      path.add(branch);
      double minExpansion = Float.MAX_VALUE;
      RTreeNode<T> next = null;
      for (final RTreeNode<T> childNode : branch) {
        final double expansion = getRequiredExpansion(childNode, boundingBox);
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
      return chooseLeaf(path, next, boundingBox);
    }
  }

  @Override
  public void forEach(final BoundingBox boundingBox, final Consumer<? super T> action) {
    final double minX = boundingBox.getMinX();
    final double minY = boundingBox.getMinY();
    final double maxX = boundingBox.getMaxX();
    final double maxY = boundingBox.getMaxY();
    try {
      this.root.forEach(minX, minY, maxX, maxY, action);
    } catch (final ExitLoopException e) {
    }
  }

  @Override
  public void forEach(final BoundingBox boundingBox, final Predicate<? super T> filter,
    final Consumer<? super T> action) {
    final double minX = boundingBox.getMinX();
    final double minY = boundingBox.getMinY();
    final double maxX = boundingBox.getMaxX();
    final double maxY = boundingBox.getMaxY();
    try {
      this.root.forEach(minX, minY, maxX, maxY, filter, action);
    } catch (final ExitLoopException e) {
    }
  }

  @Override
  public void forEach(final Consumer<? super T> action) {
    try {
      this.root.forEachValue(action);
    } catch (final ExitLoopException e) {
    }
  }

  @Override
  public void forEach(final double x, final double y, final Predicate<? super T> filter,
    final Consumer<? super T> action) {
    try {
      this.root.forEach(x, y, filter, action);
    } catch (final ExitLoopException e) {
    }
  }

  @Override
  public void forEach(final Predicate<? super T> filter, final Consumer<? super T> action) {
    try {
      this.root.forEachValue(filter, action);
    } catch (final ExitLoopException e) {
    }
  }

  private double getRequiredExpansion(final RTreeNode<T> node, final BoundingBox boundingBox) {
    double areaExpansion = 0;

    final double minX1 = node.getMinX();
    final double minY1 = node.getMinY();
    final double maxX1 = node.getMaxX();
    final double maxY1 = node.getMaxY();

    final double minX2 = boundingBox.getMinX();
    final double minY2 = boundingBox.getMinY();
    final double maxX2 = boundingBox.getMaxX();
    final double maxY2 = boundingBox.getMaxY();

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

  @Override
  public int getSize() {
    return this.size;
  }

  @Override
  public void put(final BoundingBox boundingBox, final T object) {
    final BoundingBox objectBoundingBox = new BoundingBoxDoubleXY(boundingBox);
    final LinkedList<RTreeBranch<T>> path = new LinkedList<>();
    final RTreeLeaf<T> leaf = chooseLeaf(path, this.root, objectBoundingBox);
    if (leaf.getSize() == this.maxEntries) {
      final List<RTreeNode<T>> newNodes = leaf.split(object, objectBoundingBox);
      replace(path, leaf, newNodes);
    } else {
      leaf.add(objectBoundingBox, object);
    }
    this.size++;
  }

  @Override
  public boolean remove(final BoundingBox boundingBox, final T object) {
    final double minX = boundingBox.getMinX();
    final double minY = boundingBox.getMinY();
    final double maxX = boundingBox.getMaxX();
    final double maxY = boundingBox.getMaxY();
    if (this.root.remove(null, minX, minY, maxX, maxY, object)) {
      this.size--;
      return true;
    } else {
      return false;
    }
  }

  private void replace(final LinkedList<RTreeBranch<T>> path, final RTreeNode<T> oldNode,
    final List<RTreeNode<T>> newNodes) {
    if (path.isEmpty()) {
      this.root = new RTreeBranch<>(this.maxEntries, newNodes);
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
