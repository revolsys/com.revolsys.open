package com.revolsys.swing.tree.model.node;

import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

import javax.swing.Icon;
import javax.swing.tree.TreeNode;

import com.revolsys.collection.IteratorEnumeration;
import com.revolsys.gis.model.data.equals.EqualsRegistry;

public abstract class AbstractTreeNode implements TreeNode, Iterable<TreeNode> {

  private final boolean allowsChildren;

  private final Object userObject;

  private final TreeNode parent;

  private Icon icon;

  public AbstractTreeNode(final Object userObject) {
    this(userObject, false);
  }

  public AbstractTreeNode(final Object userObject, final boolean allowsChildren) {
    this(null, userObject, allowsChildren);
  }

  public AbstractTreeNode(final TreeNode parent, final Object userObject) {
    this(parent, userObject, false);
  }

  public AbstractTreeNode(final TreeNode parent, final Object userObject,
    final boolean allowsChildren) {
    this.parent = parent;
    this.userObject = userObject;
    this.allowsChildren = allowsChildren;
  }

  @SuppressWarnings("rawtypes")
  @Override
  public Enumeration children() {
    return IteratorEnumeration.create(getChildren());
  }

  @Override
  public boolean equals(final Object object) {
    if (object == this) {
      return true;
    } else if (object == null) {
      return false;
    } else if (object.getClass().equals(getClass())) {
      final AbstractTreeNode node = (AbstractTreeNode)object;
      return EqualsRegistry.equal(getUserObject(), node.getUserObject());
    } else {
      return false;
    }
  }

  @Override
  public boolean getAllowsChildren() {
    return isAllowsChildren();
  }

  @Override
  public TreeNode getChildAt(final int index) {
    final List<TreeNode> children = getChildren();
    final TreeNode child = children.get(index);
    return child;
  }

  @Override
  public int getChildCount() {
    final List<TreeNode> children = getChildren();
    return children.size();
  }

  protected abstract List<TreeNode> getChildren();

  public Icon getIcon() {
    return icon;
  }

  @Override
  public int getIndex(final TreeNode node) {
    final List<TreeNode> children = getChildren();
    return children.indexOf(node);
  }

  @Override
  public TreeNode getParent() {
    return parent;
  }

  @SuppressWarnings("unchecked")
  public <V extends TreeNode> V getParentNode() {
    return (V)parent;
  }

  @SuppressWarnings("unchecked")
  public <V> V getUserObject() {
    return (V)userObject;
  }

  @Override
  public int hashCode() {
    final Object object = getUserObject();
    if (object == null) {
      return 1;
    } else {
      return object.hashCode();
    }
  }

  public boolean isAllowsChildren() {
    return allowsChildren;
  }

  @Override
  public boolean isLeaf() {
    return (getChildCount() == 0);
  }

  @Override
  public Iterator<TreeNode> iterator() {
    final List<TreeNode> children = getChildren();
    return children.iterator();
  }

  protected void setIcon(final Icon icon) {
    this.icon = icon;
  }
}
