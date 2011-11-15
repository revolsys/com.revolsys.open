package com.revolsys.gis.algorithm.index;

import java.util.LinkedList;
import java.util.List;

import com.revolsys.collection.Visitor;
import com.revolsys.filter.Filter;
import com.vividsolutions.jts.geom.Envelope;

public class RTree<T> extends AbstractSpatialIndex<T> {

  private int maxEntries;

  private int minEntries;

  private RTreeNode<T> root = new RTreeLeaf<T>(maxEntries);

  private int size;

  public RTree() {
    this(12, 32);
  }

  public RTree(int minEntries, int maxEntries) {
    this.minEntries = minEntries;
    this.maxEntries = maxEntries;
    root = new RTreeLeaf<T>(maxEntries);
  }

  public void put(Envelope envelope, T object) {
    LinkedList<RTreeBranch<T>> path = new LinkedList<RTreeBranch<T>>();
    RTreeLeaf<T> leaf = chooseLeaf(path, root, envelope);
    if (leaf.getSize() == maxEntries) {
      List<RTreeNode<T>> newNodes = leaf.split(envelope, object);
      replace(path, leaf, newNodes);
    } else {
      leaf.add(envelope, object);
    }
    size++;
  }

  private void replace(LinkedList<RTreeBranch<T>> path, RTreeNode<T> oldNode,
    List<RTreeNode<T>> newNodes) {
    if (path.isEmpty()) {
      root = new RTreeBranch<T>(maxEntries, newNodes);
    } else {
      RTreeBranch<T> parentNode = path.removeLast();
      if (parentNode.getSize() + newNodes.size() - 1 >= maxEntries) {
        List<RTreeNode<T>> newParentNodes = parentNode.split(oldNode, newNodes);
        replace(path, parentNode, newParentNodes);
      } else {
        parentNode.replace(oldNode, newNodes);
      }
    }

  }

  public int getSize() {
    return size;
  }

  public boolean remove(Envelope envelope, T object) {
    // TODO rebalance after remove
    LinkedList<RTreeNode<T>> path = new LinkedList<RTreeNode<T>>();
    if (root.remove(path, envelope, object)) {
      size--;
      return true;
    } else {
      return false;
    }
  }

  private double getRequiredExpansion(RTreeNode<T> node, Envelope envelope) {
    double areaExpansion = 0;

    final double minX1 = node.getMinX();
    final double minX2 = envelope.getMinX();
    final double minY1 = node.getMinY();
    final double minY2 = envelope.getMinY();

    final double maxX1 = node.getMaxX();
    final double maxX2 = envelope.getMaxX();
    final double maxY1 = node.getMaxY();
    final double maxY2 = envelope.getMaxY();

    double maxWidth = Math.max(maxX1, maxX2) - Math.min(minX1, minX2);
    double maxHeight = Math.max(maxY1, maxY2) - Math.min(minY1, minY2);
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

  private RTreeLeaf<T> chooseLeaf(List<RTreeBranch<T>> path, RTreeNode<T> node,
    Envelope envelope) {
    if (node instanceof RTreeLeaf) {
      return (RTreeLeaf<T>)node;
    } else {
      RTreeBranch<T> branch = (RTreeBranch<T>)node;
      branch.expandToInclude(envelope);
      path.add(branch);
      double minExpansion = Float.MAX_VALUE;
      RTreeNode<T> next = null;
      for (RTreeNode<T> childNode : branch) {
        double expansion = getRequiredExpansion(childNode, envelope);
        if (expansion < minExpansion) {
          minExpansion = expansion;
          next = childNode;
        } else if (expansion == minExpansion) {
          double childArea = childNode.getArea();
          double minArea = next.getArea();
          if (childArea < minArea) {
            next = childNode;
          }
        }
      }
      return chooseLeaf(path, next, envelope);
    }
  }

  public void visit(final Envelope envelope, final Visitor<T> visitor) {
    root.visit(envelope, visitor);
  }

  public void visit(final Envelope envelope, final Filter<T> filter, final Visitor<T> visitor) {
    root.visit(envelope, filter, visitor);
  }
  public void visit(final Visitor<T> visitor) {
    root.visit(visitor);
  }

}
