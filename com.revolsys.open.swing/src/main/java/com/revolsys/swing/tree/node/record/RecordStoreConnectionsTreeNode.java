package com.revolsys.swing.tree.node.record;

import java.util.ArrayList;
import java.util.List;

import javax.swing.Icon;

import com.revolsys.io.datastore.RecordStoreConnectionManager;
import com.revolsys.io.datastore.RecordStoreConnectionRegistry;
import com.revolsys.swing.Icons;
import com.revolsys.swing.listener.InvokeMethodListener;
import com.revolsys.swing.tree.node.BaseTreeNode;
import com.revolsys.swing.tree.node.ListTreeNode;
import com.revolsys.util.Property;

public class RecordStoreConnectionsTreeNode extends ListTreeNode {
  public static final Icon ICON = Icons.getIcon("folder_database");

  public RecordStoreConnectionsTreeNode() {
    setName("Record Stores");
    setType("Record Stores");
    setIcon(ICON);
    final RecordStoreConnectionManager recordStoreConnectionManager = RecordStoreConnectionManager.get();
    Property.addListener(recordStoreConnectionManager,
      new InvokeMethodListener(this, "refresh"));
  }

  @Override
  protected List<BaseTreeNode> doLoadChildren() {
    final RecordStoreConnectionManager recordStoreConnectionManager = RecordStoreConnectionManager.get();
    final List<BaseTreeNode> children = new ArrayList<>();
    final List<RecordStoreConnectionRegistry> registries = recordStoreConnectionManager.getVisibleConnectionRegistries();
    for (final RecordStoreConnectionRegistry registry : registries) {
      final BaseTreeNode child = new RecordStoreConnectionRegistryTreeNode(
        registry);
      children.add(child);
    }
    return children;
  }

}
