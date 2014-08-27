package com.revolsys.swing.tree.node.record;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import com.revolsys.io.datastore.RecordStoreConnection;
import com.revolsys.io.datastore.RecordStoreConnectionRegistry;
import com.revolsys.swing.tree.node.BaseTreeNode;
import com.revolsys.swing.tree.node.LazyLoadTreeNode;
import com.revolsys.swing.tree.node.file.FileTreeNode;

public class RecordStoreConnectionRegistryTreeNode extends LazyLoadTreeNode
implements PropertyChangeListener {

  public RecordStoreConnectionRegistryTreeNode(
    final RecordStoreConnectionRegistry registry) {
    super(registry);
    setType("RecordStore Connections");
    setName(registry.getName());
    setIcon(FileTreeNode.ICON_FOLDER_LINK);
  }

  @Override
  protected List<BaseTreeNode> doLoadChildren() {
    final List<BaseTreeNode> children = new ArrayList<>();
    final RecordStoreConnectionRegistry registry = getRegistry();
    final List<RecordStoreConnection> conections = registry.getConections();
    for (final RecordStoreConnection connection : conections) {
      final RecordStoreConnectionTreeNode child = new RecordStoreConnectionTreeNode(
        connection);
      children.add(child);
    }
    return children;
  }

  @Override
  public void doPropertyChange(final PropertyChangeEvent event) {
    if (event.getSource() == getRegistry()) {
      final String propertyName = event.getPropertyName();
      if (propertyName.equals("connections")) {
        refresh();
      }
    }
  }

  protected RecordStoreConnectionRegistry getRegistry() {
    final RecordStoreConnectionRegistry registry = getUserData();
    return registry;
  }
}
