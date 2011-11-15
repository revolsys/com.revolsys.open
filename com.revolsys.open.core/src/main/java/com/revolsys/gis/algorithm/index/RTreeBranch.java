package com.revolsys.gis.algorithm.index;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.revolsys.collection.ArrayUtil;
import com.revolsys.collection.Visitor;
import com.revolsys.filter.Filter;
import com.vividsolutions.jts.geom.Envelope;

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
    expandToInclude(node);
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

  public Iterator<RTreeNode<T>> iterator() {
    return getNodes().iterator();
  }

  public void replace(RTreeNode<T> node, List<RTreeNode<T>> newNodes) {
    for (int i = 1; i < newNodes.size(); i++) {
      RTreeNode<T> newNode = newNodes.get(i);
      add(newNode);
    }
    for (int i = 0; i < size - newNodes.size() + 1; i++) {
      RTreeNode<T> childNode = nodes[i];
      if (childNode == node) {
        nodes[i] = newNodes.get(0);
        return;
      }
    }
  }

  protected void updateEnvelope() {
    init();
    for (int i = 0; i < size; i++) {
      Envelope envelope = nodes[i];
      expandToInclude(envelope);
    }
  }

  public boolean remove(LinkedList<RTreeNode<T>> path, Envelope envelope,
    T object) {
    for (int i = 0; i < size; i++) {
      RTreeNode<T> node = nodes[i];
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

  @SuppressWarnings("unchecked")
  public List<RTreeNode<T>> split(RTreeNode<T> node, List<RTreeNode<T>> newNodes) {
    RTreeBranch<T> branch1 = new RTreeBranch<T>(nodes.length);
    RTreeBranch<T> branch2 = new RTreeBranch<T>(nodes.length);

    // TODO Add some ordering to the results
    final int midPoint = (int)Math.ceil(size / 2.0);
    for (int i = 0; i <= midPoint; i++) {
      RTreeNode<T> childNode = nodes[i];
      if (childNode == node) {
        branch1.add(newNodes.get(0));
      } else {
        branch1.add(childNode);
      }
    }
    for (int i = midPoint + 1; i < size; i++) {
      RTreeNode<T> childNode = nodes[i];
      if (childNode == node) {
        branch1.add(newNodes.get(0));
      } else {
        branch2.add(childNode);
      }
    }
    RTreeNode<T> newNode = newNodes.get(1);
    branch2.add(newNode);
    return Arrays.<RTreeNode<T>> asList(branch1, branch2);
  }

  @Override
  public boolean visit(Envelope envelope, Visitor<T> visitor) {
    for (int i = 0; i < size; i++) {
      RTreeNode<T> node = nodes[i];
      if (envelope.intersects(node)) {
        if (!node.visit(envelope, visitor)) {
          return false;
        }
      }
    }
    return true;
  }

  @Override
  public boolean visit(Envelope envelope, Filter<T> filter, Visitor<T> visitor) {
    for (int i = 0; i < size; i++) {
      RTreeNode<T> node = nodes[i];
      if (envelope.intersects(node)) {
        if (!node.visit(envelope, filter, visitor)) {
          return false;
        }
      }
    }
    return true;
  }

  @Override
  public boolean visit(Visitor<T> visitor) {
    for (int i = 0; i < size; i++) {
      RTreeNode<T> node = nodes[i];
      if (!node.visit(visitor)) {
        return false;
      }
    }
    return true;
  }
}
