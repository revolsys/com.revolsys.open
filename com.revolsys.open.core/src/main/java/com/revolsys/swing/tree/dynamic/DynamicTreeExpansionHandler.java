package com.revolsys.swing.tree.dynamic;

import javax.swing.JTree;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

public class DynamicTreeExpansionHandler implements TreeExpansionListener {
  private final JTree tree;

  private final DefaultTreeModel model;

  public DynamicTreeExpansionHandler(final JTree tree) {
    this.tree = tree;
    this.model = (DefaultTreeModel)tree.getModel();
  }

  @Override
  public void treeCollapsed(final TreeExpansionEvent event) {
    if (event.getSource() != tree) {
      return;
    }

    final TreePath path = event.getPath();
    final Object node = path.getLastPathComponent();
    if (node instanceof DynamicTreeNode) {
      final DynamicTreeNode dynamicNode = (DynamicTreeNode)node;
      dynamicNode.clearChildren();
    }
  }

  @Override
  public void treeExpanded(final TreeExpansionEvent event) {
    if (event.getSource() != tree) {
      return;
    }

    final TreePath path = event.getPath();
    final Object node = path.getLastPathComponent();
    if (node instanceof DynamicTreeNode) {
      final DynamicTreeNode dynamicNode = (DynamicTreeNode)node;
      dynamicNode.populateChildren(tree);
    }
  }
}
