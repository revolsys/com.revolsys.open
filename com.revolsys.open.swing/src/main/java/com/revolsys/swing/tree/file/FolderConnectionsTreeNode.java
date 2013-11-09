package com.revolsys.swing.tree.file;

import java.util.ArrayList;
import java.util.List;

import javax.swing.tree.TreeNode;

import com.revolsys.io.file.FolderConnectionManager;
import com.revolsys.io.file.FolderConnectionRegistry;
import com.revolsys.swing.tree.model.node.AbstractTreeNode;

public class FolderConnectionsTreeNode extends AbstractTreeNode {

  private ArrayList<TreeNode> children;

  public FolderConnectionsTreeNode(final TreeNode parent) {
    super(parent, null);
    setName("Folder Connections");
    setType("Folder Connections");
    setIcon(FileTreeUtil.ICON_FOLDER_LINK);
    setAllowsChildren(true);
    init();
  }

  @Override
  public List<TreeNode> getChildren() {
    return children;
  }

  protected void init() {
    children = new ArrayList<TreeNode>();
    final List<FolderConnectionRegistry> registries = FolderConnectionManager.get()
      .getVisibleConnectionRegistries();
    for (final FolderConnectionRegistry childRegistry : registries) {
      final FolderConnectionRegistryTreeNode child = new FolderConnectionRegistryTreeNode(
        this, childRegistry);
      children.add(child);
    }
  }
}
