package com.revolsys.swing.tree.node.record;

import java.util.ArrayList;
import java.util.List;

import javax.swing.Icon;

import com.revolsys.io.datastore.RecordStoreConnectionManager;
import com.revolsys.io.datastore.RecordStoreConnectionRegistry;
import com.revolsys.swing.EventQueue;
import com.revolsys.swing.Icons;
import com.revolsys.swing.tree.node.BaseTreeNode;
import com.revolsys.swing.tree.node.ListTreeNode;

public class RecordStoreConnectionsTreeNode extends ListTreeNode {
  public static final Icon ICON = Icons.getIcon("folder_database");

  public static final Icon ICON_OPEN = Icons.getIcon("folder_database_open");

  public RecordStoreConnectionsTreeNode() {
    setName("Record Stores");
    setType("Record Stores");
    setIcon(ICON);
    final RecordStoreConnectionManager connectionManager = RecordStoreConnectionManager.get();
    EventQueue.addPropertyChange(connectionManager, () -> refresh());
  }

  @Override
  protected List<BaseTreeNode> doLoadChildren() {
    final RecordStoreConnectionManager recordStoreConnectionManager = RecordStoreConnectionManager.get();
    final List<BaseTreeNode> children = new ArrayList<>();
    final List<RecordStoreConnectionRegistry> registries = recordStoreConnectionManager.getVisibleConnectionRegistries();
    for (final RecordStoreConnectionRegistry registry : registries) {
      final BaseTreeNode child = new RecordStoreConnectionRegistryTreeNode(registry);
      children.add(child);
    }
    return children;
  }

  @Override
  public Icon getOpenIcon() {
    return ICON_OPEN;
  }
}
