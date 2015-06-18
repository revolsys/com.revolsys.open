package com.revolsys.swing.tree.node.record;

import java.util.ArrayList;
import java.util.List;

import javax.swing.Icon;

import com.revolsys.data.record.io.RecordStoreConnectionManager;
import com.revolsys.data.record.io.RecordStoreConnectionRegistry;
import com.revolsys.swing.EventQueue;
import com.revolsys.swing.Icons;
import com.revolsys.swing.tree.node.BaseTreeNode;
import com.revolsys.swing.tree.node.LazyLoadTreeNode;
import com.revolsys.swing.tree.node.file.FileTreeNode;

public class RecordStoreConnectionsTreeNode extends LazyLoadTreeNode {
  public static final Icon ICON = Icons.getIconWithBadge(FileTreeNode.ICON_FOLDER, "database");

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
}
