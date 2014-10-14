package com.revolsys.swing.tree.node.file;

import java.util.ArrayList;
import java.util.List;

import com.revolsys.io.file.FolderConnectionManager;
import com.revolsys.io.file.FolderConnectionRegistry;
import com.revolsys.swing.listener.InvokeMethodListener;
import com.revolsys.swing.tree.node.BaseTreeNode;
import com.revolsys.swing.tree.node.ListTreeNode;
import com.revolsys.util.Property;

public class FolderConnectionsTreeNode extends ListTreeNode {

  public FolderConnectionsTreeNode() {
    setName("Folder Connections");
    setType("Folder Connections");
    setIcon(FileTreeNode.ICON_FOLDER_LINK);
    final FolderConnectionManager connectionManager = FolderConnectionManager.get();
    Property.addListener(connectionManager, new InvokeMethodListener(this,
      "refresh"));
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

}
