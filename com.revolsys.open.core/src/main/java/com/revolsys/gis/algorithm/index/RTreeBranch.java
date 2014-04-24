package com.revolsys.gis.algorithm.index;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.revolsys.collection.ArrayUtil;
import com.revolsys.collection.Visitor;
import com.revolsys.filter.Filter;
import com.revolsys.jts.geom.BoundingBox;
import com.revolsys.jts.geom.Envelope;

public class RTreeBranch<T> extends RTreeNode<T> implements
  Iterable<RTreeNode<T>> {

  /**
   * 
   */
  private static final long serialVersionUID = -6766833009366142439L;

  private RTreeNode<T>[] nodes;

  private int size;

  public RTreeBranch() {
  }

  @SuppressWarnings("unchecked")
  public RTreeBranch(final int size) {
    nodes = ArrayUtil.create(RTreeNode.class, size);
  }

  protected RTreeBranch(final int size, final List<RTreeNode<T>> nodes) {
    this(size);
    for (final RTreeNode<T> node : nodes) {
      add(node);
    }
  }

  private void add(final RTreeNode<T> node) {
    nodes[size] = node;
    size++;
    setBoundingBox(getBoundingBox().expandToInclude(node.getBoundingBox()));
  }

  public List<RTreeNode<T>> getNodes() {
    final List<RTreeNode<T>> nodes = new ArrayList<RTreeNode<T>>();
    for (int i = 0; i < size; i++) {
      final RTreeNode<T> node = this.nodes[i];
      nodes.add(node);
    }
    return nodes;
  }

  public int getSize() {
    return size;
  }

  @Override
  public Iterator<RTreeNode<T>> iterator() {
    return getNodes().iterator();
  }

  @Override
  public boolean remove(final LinkedList<RTreeNode<T>> path,
    final BoundingBox envelope, final T object) {
    for (int i = 0; i < size; i++) {
      final RTreeNode<T> node = nodes[i];
      if (node.contains(envelope)) {
        if (node.remove(path, envelope, object)) {
          path.addFirst(this);
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
    for (int i = 0; i < size - newNodes.size() + 1; i++) {
      final RTreeNode<T> childNode = nodes[i];
      if (childNode == node) {
        nodes[i] = newNodes.get(0);
        return;
      }
    }
  }

  @SuppressWarnings("unchecked")
  public List<RTreeNode<T>> split(final RTreeNode<T> node,
    final List<RTreeNode<T>> newNodes) {
    final RTreeBranch<T> branch1 = new RTreeBranch<T>(nodes.length);
    final RTreeBranch<T> branch2 = new RTreeBranch<T>(nodes.length);

    // TODO Add some ordering to the results
    final int midPoint = (int)Math.ceil(size / 2.0);
    for (int i = 0; i <= midPoint; i++) {
      final RTreeNode<T> childNode = nodes[i];
      if (childNode == node) {
        branch1.add(newNodes.get(0));
      } else {
        branch1.add(childNode);
      }
    }
    for (int i = midPoint + 1; i < size; i++) {
      final RTreeNode<T> childNode = nodes[i];
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
    BoundingBox boundingBox = new Envelope();
    for (int i = 0; i < size; i++) {
      final BoundingBox envelope = nodes[i].getBoundingBox();
      boundingBox = boundingBox.expandToInclude(envelope);
    }
    setBoundingBox(boundingBox);

  }

  @Override
  public boolean visit(final BoundingBox envelope, final Filter<T> filter,
    final Visitor<T> visitor) {
    for (int i = 0; i < size; i++) {
      final RTreeNode<T> node = nodes[i];
      if (envelope.intersects(node.getBoundingBox())) {
        if (!node.visit(envelope, filter, visitor)) {
          return false;
        }
      }
    }
    return true;
  }

  @Override
  public boolean visit(final BoundingBox envelope, final Visitor<T> visitor) {
    for (int i = 0; i < size; i++) {
      final RTreeNode<T> node = nodes[i];
      if (envelope.intersects(node.getBoundingBox())) {
        if (!node.visit(envelope, visitor)) {
          return false;
        }
      }
    }
    return true;
  }

  @Override
  public boolean visit(final Visitor<T> visitor) {
    for (int i = 0; i < size; i++) {
      final RTreeNode<T> node = nodes[i];
      if (!node.visit(visitor)) {
        return false;
      }
    }
    return true;
  }
}
