package com.revolsys.swing.tree.model.node;

import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;

import com.revolsys.collection.IteratorEnumeration;

public abstract class AbstractMutableTreeNode implements MutableTreeNode,
  Iterable<MutableTreeNode> {

  private boolean allowsChildren;

  private Object userObject;

  public AbstractMutableTreeNode() {
  }

  public AbstractMutableTreeNode(final boolean allowsChildren) {
    this(null, true);
  }

  public AbstractMutableTreeNode(final Object userObject) {
    this(userObject, false);
  }

  public AbstractMutableTreeNode(final Object userObject, final boolean allowsChildren) {
    this.allowsChildren = allowsChildren;
    this.userObject = userObject;
  }

  @SuppressWarnings("rawtypes")
  @Override
  public Enumeration children() {
    return IteratorEnumeration.create(getChildren());
  }

  @Override
  public boolean getAllowsChildren() {
    return allowsChildren;
  }

  @Override
  public TreeNode getChildAt(final int index) {
    final List<MutableTreeNode> children = getChildren();
    return children.get(index);
  }

  @Override
  public int getChildCount() {
    final List<MutableTreeNode> children = getChildren();
    return children.size();
  }

  protected abstract List<MutableTreeNode> getChildren();

  @Override
  public int getIndex(final TreeNode node) {
    final List<MutableTreeNode> children = getChildren();
    return children.indexOf(node);
  }

  @Override
  public abstract MutableTreeNode getParent();

  public Object getUserObject() {
    return userObject;
  }

  @Override
  public void insert(final MutableTreeNode child, final int index) {
    if (!allowsChildren) {
      throw new IllegalStateException("node does not allow children");
    } else if (child == null) {
      throw new IllegalArgumentException("new child is null");
    } else if (isNodeAncestor(child)) {
      throw new IllegalArgumentException("new child is an ancestor");
    } else {
      final MutableTreeNode parent = (MutableTreeNode)child.getParent();
      if (parent != null) {
        parent.remove(child);
      }
      child.setParent(this);
      final List<MutableTreeNode> children = getChildren();
      children.add(index, child);
    }
  }

  public boolean isAllowsChildren() {
    return allowsChildren;
  }

  @Override
  public boolean isLeaf() {
    return (getChildCount() == 0);
  }

  public boolean isNodeAncestor(final TreeNode node) {
    if (node == null) {
      return false;
    } else {
      for (TreeNode ancestor = this; ancestor != null; ancestor = ancestor.getParent()) {
        if (ancestor == node) {
          return true;
        }
      }
      return false;
    }
  }

  @Override
  public Iterator<MutableTreeNode> iterator() {
    final List<MutableTreeNode> children = getChildren();
    return children.iterator();
  }

  @Override
  public void remove(final int index) {
    final List<MutableTreeNode> children = getChildren();
    final MutableTreeNode child = children.remove(index);
    if (child != null) {
      child.setParent(null);
    }
  }

  @Override
  public void remove(final MutableTreeNode node) {
    if (node != null) {
      final List<MutableTreeNode> children = getChildren();
      if (children.remove(node)) {
        node.setParent(null);
      }
    }
  }

  @Override
  public void removeFromParent() {
    final MutableTreeNode parent = getParent();
    if (parent != null) {
      parent.remove(this);
    }
  }

  @Override
  public void setUserObject(final Object userObject) {
    this.userObject = userObject;
  }

}
