package com.revolsys.swing.tree.node.file;

import java.util.ArrayList;
import java.util.List;

import javax.swing.Icon;

import com.revolsys.io.file.FolderConnectionManager;
import com.revolsys.io.file.FolderConnectionRegistry;
import com.revolsys.swing.EventQueue;
import com.revolsys.swing.tree.node.BaseTreeNode;
import com.revolsys.swing.tree.node.ListTreeNode;

public class FolderConnectionsTreeNode extends ListTreeNode {

  public FolderConnectionsTreeNode() {
    setName("Folder Connections");
    setType("Folder Connections");
    setIcon(FileTreeNode.ICON_FOLDER_LINK);
    final FolderConnectionManager connectionManager = FolderConnectionManager.get();
    EventQueue.addPropertyChange(connectionManager, () -> refresh());
  }

  @Override
  protected List<BaseTreeNode> doLoadChildren() {
    final List<BaseTreeNode> children = new ArrayList<>();
    final List<FolderConnectionRegistry> registries = FolderConnectionManager.get()
      .getVisibleConnectionRegistries();
    for (final FolderConnectionRegistry childRegistry : registries) {
      final FolderConnectionRegistryTreeNode child = new FolderConnectionRegistryTreeNode(
        childRegistry);
      children.add(child);
    }
    return children;
  }

  @Override
  public Icon getOpenIcon() {
    return FileTreeNode.ICON_FOLDER_LINK_OPEN;
  }
}
