package com.revolsys.swing.tree.datastore;

import java.util.ArrayList;
import java.util.List;

import javax.swing.Icon;
import javax.swing.tree.TreeNode;

import com.revolsys.famfamfam.silk.SilkIconLoader;
import com.revolsys.io.datastore.DataObjectStoreConnectionManager;
import com.revolsys.io.datastore.DataObjectStoreConnectionRegistry;
import com.revolsys.swing.tree.model.node.LazyLoadTreeNode;

public class DataObjectStoreConnectionsTreeNode extends LazyLoadTreeNode {
  public static final Icon ICON = SilkIconLoader.getIcon("folder_database");

  public DataObjectStoreConnectionsTreeNode(final TreeNode parent) {
    super(parent, null);
    setName("Data Stores");
    setType("Data Stores");
    setIcon(ICON);
    setAllowsChildren(true);
  }

  @Override
  protected List<TreeNode> doLoadChildren() {
    final List<TreeNode> children = new ArrayList<TreeNode>();
    final List<DataObjectStoreConnectionRegistry> registries = DataObjectStoreConnectionManager.get()
      .getVisibleConnectionRegistries();
    for (final DataObjectStoreConnectionRegistry registry : registries) {
      final DataObjectStoreConnectionRegistryTreeNode child = new DataObjectStoreConnectionRegistryTreeNode(
        this, registry);
      children.add(child);
    }
    return children;
  }
}
