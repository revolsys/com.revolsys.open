package com.revolsys.swing.tree.dynamic;

import javax.swing.JTree;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;

public class DefaultMutableTreeModelChangedTask implements Runnable {
  private final JTree tree;

  private final TreeNode node;

  public DefaultMutableTreeModelChangedTask(final JTree tree,
    final TreeNode node) {
    this.tree = tree;
    this.node = node;
  }

  @Override
  public void run() {
    ((DefaultTreeModel)this.tree.getModel()).nodeStructureChanged(this.node);
  }
}
