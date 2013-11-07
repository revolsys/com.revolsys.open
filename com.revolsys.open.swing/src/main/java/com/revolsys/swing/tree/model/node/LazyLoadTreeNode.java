package com.revolsys.swing.tree.model.node;

import java.util.ArrayList;
import java.util.List;

import javax.swing.tree.TreeNode;

public abstract class LazyLoadTreeNode extends AbstractTreeNode {

  private List<TreeNode> children;

  public LazyLoadTreeNode(final Object userObject) {
    super(userObject);
  }

  public LazyLoadTreeNode(final Object userObject,
    final boolean allowsChildren) {
    super(userObject, allowsChildren);
  }

  public LazyLoadTreeNode(final TreeNode parent, final Object userObject) {
    super(parent, userObject);
  }

  public LazyLoadTreeNode(final TreeNode parent,
    final Object userObject, final boolean allowsChildren) {
    super(parent, userObject, allowsChildren);
  }

  @Override
  public int getChildCount() {
    return getChildren().size();
  }

  @Override
  protected List<TreeNode> getChildren() {
    if (children == null) {
      children = loadChildren();
    }
    return children;
  }

  protected List<TreeNode> loadChildren() {
    return new ArrayList<TreeNode>();
  }
}
