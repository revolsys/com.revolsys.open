package com.revolsys.swing.tree.record;

import java.util.ArrayList;
import java.util.List;

import javax.swing.Icon;
import javax.swing.tree.TreeNode;

import com.revolsys.famfamfam.silk.SilkIconLoader;
import com.revolsys.io.datastore.RecordStoreConnectionManager;
import com.revolsys.io.datastore.RecordStoreConnectionRegistry;
import com.revolsys.swing.tree.model.node.AbstractTreeNode;

public class RecordStoreConnectionsTreeNode extends AbstractTreeNode {
  public static final Icon ICON = SilkIconLoader.getIcon("folder_database");

  private ArrayList<TreeNode> children;

  public RecordStoreConnectionsTreeNode(final TreeNode parent) {
    super(parent, null);
    setName("Data Stores");
    setType("Data Stores");
    setIcon(ICON);
    setAllowsChildren(true);
    init();
  }

  @Override
  public List<TreeNode> getChildren() {
    return children;
  }

  protected void init() {
    children = new ArrayList<TreeNode>();
    final List<RecordStoreConnectionRegistry> registries = RecordStoreConnectionManager.get()
      .getVisibleConnectionRegistries();
    for (final RecordStoreConnectionRegistry registry : registries) {
      final RecordStoreConnectionRegistryTreeNode child = new RecordStoreConnectionRegistryTreeNode(
        this, registry);
      children.add(child);
    }
  }

}
