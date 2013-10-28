package com.revolsys.swing.tree.model.node;

import java.util.ArrayList;
import java.util.List;

import javax.swing.tree.TreeNode;

public class ListTreeNode extends AbstractTreeNode {
  public ListTreeNode() {
    this(null);
  }

  public ListTreeNode(final TreeNode parent) {
    this(parent, new ArrayList<TreeNode>());
  }

  public ListTreeNode(final TreeNode parent, final List<TreeNode> children) {
    super(parent, children, true);
  }

  public void add(final TreeNode child) {
    final List<TreeNode> children = getChildren();
    children.add(child);
  }

  @SuppressWarnings("unchecked")
  @Override
  public List<TreeNode> getChildren() {
    final List<TreeNode> children = (List<TreeNode>)getUserObject();
    return children;
  }
}
