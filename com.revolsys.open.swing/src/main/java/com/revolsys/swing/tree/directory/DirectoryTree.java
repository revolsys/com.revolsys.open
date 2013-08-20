package com.revolsys.swing.tree.directory;

import javax.swing.JTree;
import javax.swing.tree.DefaultTreeModel;

import com.revolsys.swing.tree.dynamic.DynamicTreeExpansionHandler;

public class DirectoryTree extends JTree {
  /**
   * 
   */
  private static final long serialVersionUID = 4939445868392913924L;

  public DirectoryTree() {
    putClientProperty("JTree.lineStyle", "Angled");

    final DirectoryTreeNode rootNode = new DirectoryTreeNode(null, "",
      new FileSystemLoader());
    rootNode.populateChildren(this);
    final DefaultTreeModel treeModel = new DefaultTreeModel(rootNode);
    setModel(treeModel);

    addTreeExpansionListener(new DynamicTreeExpansionHandler(this));
  }
}
