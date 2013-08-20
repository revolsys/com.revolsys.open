package com.revolsys.gis.algorithm.index.quadtree;

import java.util.ArrayList;
import java.util.List;

import com.revolsys.collection.Visitor;
import com.revolsys.gis.model.coordinates.Coordinates;
import com.vividsolutions.jts.geom.Envelope;

public abstract class NodeBase<T> {
  public static int getSubnodeIndex(final Envelope envelope,
    final Coordinates centre) {
    int subnodeIndex = -1;
    final double minX = envelope.getMinX();
    final double minY = envelope.getMinY();
    final double maxX = envelope.getMaxX();
    final double maxY = envelope.getMaxY();

    final double centreX = centre.getX();
    final double centreY = centre.getY();

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

  private final List<T> items = new ArrayList<T>();

  private final List<Envelope> envelopes = new ArrayList<Envelope>();

  private final List<Node<T>> nodes = new ArrayList<Node<T>>();

  public NodeBase() {
    for (int i = 0; i < 4; i++) {
      nodes.add(null);
    }
  }

  public void add(final Envelope envelope, final T item) {
    for (int i = 0; i < items.size(); i++) {
      final T oldItem = items.get(i);
      if (oldItem == item) {
        envelopes.set(i, envelope);
        return;
      }
    }
    envelopes.add(envelope);
    items.add(item);
  }

  public int depth() {
    int depth = 0;
    for (int i = 0; i < 4; i++) {
      final Node<T> node = getNode(i);
      if (node != null) {
        final int nodeDepth = node.depth();
        if (nodeDepth > depth) {
          depth = nodeDepth;
        }
      }
    }
    return depth + 1;
  }

  public List<T> getItems() {
    return items;
  }

  protected Node<T> getNode(final int i) {
    return nodes.get(i);
  }

  protected int getNodeCount() {
    int nodeCount = 0;
    for (int i = 0; i < 4; i++) {
      final Node<T> node = getNode(i);
      if (node != null) {
        nodeCount += node.size();
      }
    }
    return nodeCount + 1;
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
    return !items.isEmpty();
  }

  public boolean isEmpty() {
    boolean isEmpty = true;
    if (!items.isEmpty()) {
      isEmpty = false;
    }
    for (int i = 0; i < 4; i++) {
      final Node<T> node = getNode(i);
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

  protected abstract boolean isSearchMatch(Envelope searchEnv);

  public boolean remove(final Envelope envelope, final T item) {
    if (isSearchMatch(envelope)) {
      for (int i = 0; i < 4; i++) {
        final Node<T> node = getNode(i);
        if (node != null) {
          if (node.remove(envelope, item)) {
            if (node.isPrunable()) {
              nodes.set(i, null);
            }
            return true;
          }
        }
      }
      final int index = items.indexOf(item);
      if (index == -1) {
        return false;
      } else {
        envelopes.remove(index);
        items.remove(index);
        return true;
      }
    } else {
      return false;
    }
  }

  protected void setNode(final int i, final Node<T> node) {
    nodes.set(i, node);
  }

  protected int size() {
    int subSize = 0;
    for (int i = 0; i < 4; i++) {
      final Node<T> node = getNode(i);
      if (node != null) {
        subSize += node.size();
      }
    }
    return subSize + items.size();
  }

  public boolean visit(final Envelope envelope, final Visitor<T> visitor) {
    if (isSearchMatch(envelope)) {
      for (int i = 0; i < items.size(); i++) {
        final Envelope itemEnvelope = envelopes.get(i);
        if (isSearchMatch(itemEnvelope)) {
          final T item = items.get(i);
          if (!visitor.visit(item)) {
            return false;
          }
        }
      }

      for (int i = 0; i < 4; i++) {
        final Node<T> node = getNode(i);
        if (node != null) {
          if (!node.visit(envelope, visitor)) {
            return false;
          }
        }
      }
    }
    return true;
  }

  public boolean visit(final Visitor<T> visitor) {
    for (final T item : items) {
      if (!visitor.visit(item)) {
        return false;
      }
    }

    for (int i = 0; i < 4; i++) {
      final Node<T> node = getNode(i);
      if (node != null) {
        if (!node.visit(visitor)) {
          return false;
        }
      }
    }
    return true;
  }
}
