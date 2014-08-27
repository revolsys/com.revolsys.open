package com.revolsys.swing.tree.node.record;

import java.util.ArrayList;
import java.util.List;

import javax.swing.Icon;

import com.revolsys.famfamfam.silk.SilkIconLoader;
import com.revolsys.io.datastore.RecordStoreConnectionManager;
import com.revolsys.io.datastore.RecordStoreConnectionRegistry;
import com.revolsys.swing.tree.node.BaseTreeNode;
import com.revolsys.swing.tree.node.ListTreeNode;

public class RecordStoreConnectionsTreeNode extends ListTreeNode {
  public static final Icon ICON = SilkIconLoader.getIcon("folder_database");

  public RecordStoreConnectionsTreeNode() {
    setName("Data Stores");
    setType("Data Stores");
    setIcon(ICON);
  }

  @Override
  protected List<BaseTreeNode> doLoadChildren() {
    final List<BaseTreeNode> children = new ArrayList<>();
    final List<RecordStoreConnectionRegistry> registries = RecordStoreConnectionManager.get()
        .getVisibleConnectionRegistries();
    for (final RecordStoreConnectionRegistry registry : registries) {
      final BaseTreeNode child = new RecordStoreConnectionRegistryTreeNode(
        registry);
      children.add(child);
    }
    return children;
  }
}
