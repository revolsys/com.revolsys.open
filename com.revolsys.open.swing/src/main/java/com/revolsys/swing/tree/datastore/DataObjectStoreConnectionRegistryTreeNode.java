package com.revolsys.swing.tree.datastore;

import java.util.ArrayList;
import java.util.List;

import javax.swing.tree.TreeNode;

import com.revolsys.io.datastore.DataObjectStoreConnection;
import com.revolsys.io.datastore.DataObjectStoreConnectionRegistry;
import com.revolsys.swing.tree.file.FileModel;
import com.revolsys.swing.tree.model.node.LazyLoadTreeNode;

public class DataObjectStoreConnectionRegistryTreeNode extends LazyLoadTreeNode {

  public DataObjectStoreConnectionRegistryTreeNode(
    final DataObjectStoreConnectionsTreeNode parent,
    final DataObjectStoreConnectionRegistry registry) {
    super(parent, registry);
    setType("DataObjectStore Connections");
    setName(registry.getName());
    setIcon(FileModel.ICON_FOLDER_LINK);
    setAllowsChildren(true);
  }

  @Override
  protected List<TreeNode> doLoadChildren() {
    final List<TreeNode> children = new ArrayList<TreeNode>();
    final DataObjectStoreConnectionRegistry registry = getUserObject();
    final List<DataObjectStoreConnection> conections = registry.getConections();
    for (final DataObjectStoreConnection connection : conections) {
      final DataObjectStoreConnectionTreeNode child = new DataObjectStoreConnectionTreeNode(
        this, connection);
      children.add(child);
    }
    return children;
  }
}
