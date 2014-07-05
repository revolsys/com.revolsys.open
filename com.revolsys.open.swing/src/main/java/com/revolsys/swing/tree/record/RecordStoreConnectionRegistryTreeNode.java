package com.revolsys.swing.tree.record;

import java.beans.IndexedPropertyChangeEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.tree.TreeNode;

import com.revolsys.io.datastore.RecordStoreConnection;
import com.revolsys.io.datastore.RecordStoreConnectionRegistry;
import com.revolsys.swing.tree.file.FileTreeNode;
import com.revolsys.swing.tree.model.node.LazyLoadTreeNode;
import com.revolsys.util.Property;

public class RecordStoreConnectionRegistryTreeNode extends LazyLoadTreeNode
  implements PropertyChangeListener {

  public RecordStoreConnectionRegistryTreeNode(
    final RecordStoreConnectionsTreeNode parent,
    final RecordStoreConnectionRegistry registry) {
    super(parent, registry);
    setType("RecordStore Connections");
    setName(registry.getName());
    setIcon(FileTreeNode.ICON_FOLDER_LINK);
    setAllowsChildren(true);
    Property.addListener(registry, this);
  }

  @Override
  public void doDelete() {
    final RecordStoreConnectionRegistry registry = getRegistry();
    Property.removeListener(registry, this);
    super.doDelete();
  }

  @Override
  protected List<TreeNode> doLoadChildren() {
    final List<TreeNode> children = new ArrayList<TreeNode>();
    final RecordStoreConnectionRegistry registry = getRegistry();
    final List<RecordStoreConnection> conections = registry.getConections();
    for (final RecordStoreConnection connection : conections) {
      final RecordStoreConnectionTreeNode child = new RecordStoreConnectionTreeNode(
        this, connection);
      children.add(child);
    }
    return children;
  }

  protected RecordStoreConnectionRegistry getRegistry() {
    final RecordStoreConnectionRegistry registry = getUserData();
    return registry;
  }

  @Override
  public void propertyChange(final PropertyChangeEvent event) {
    if (event instanceof IndexedPropertyChangeEvent) {
      final IndexedPropertyChangeEvent indexEvent = (IndexedPropertyChangeEvent)event;
      final String propertyName = indexEvent.getPropertyName();
      if (propertyName.equals("connections")) {
        final int index = indexEvent.getIndex();
        final Object newValue = indexEvent.getNewValue();
        final Object oldValue = indexEvent.getOldValue();
        if (newValue == null) {
          if (oldValue != null) {
            removeNode(index);
            nodeRemoved(index, oldValue);
          }
        } else if (oldValue == null) {
          final RecordStoreConnectionTreeNode node = new RecordStoreConnectionTreeNode(
            this, (RecordStoreConnection)newValue);
          addNode(index, node);

          nodesInserted(index);
        } else {
          nodesChanged(index);
        }
      }
    }
  }
}
