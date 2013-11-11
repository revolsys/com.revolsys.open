package com.revolsys.swing.tree.datastore;

import java.beans.IndexedPropertyChangeEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.tree.TreeNode;

import com.revolsys.io.datastore.DataObjectStoreConnection;
import com.revolsys.io.datastore.DataObjectStoreConnectionRegistry;
import com.revolsys.swing.tree.file.FileTreeNode;
import com.revolsys.swing.tree.model.node.LazyLoadTreeNode;
import com.revolsys.util.Property;

public class DataObjectStoreConnectionRegistryTreeNode extends LazyLoadTreeNode
  implements PropertyChangeListener {

  public DataObjectStoreConnectionRegistryTreeNode(
    final DataObjectStoreConnectionsTreeNode parent,
    final DataObjectStoreConnectionRegistry registry) {
    super(parent, registry);
    setType("DataObjectStore Connections");
    setName(registry.getName());
    setIcon(FileTreeNode.ICON_FOLDER_LINK);
    setAllowsChildren(true);
    Property.addListener(registry, this);
  }

  @Override
  public void doDelete() {
    final DataObjectStoreConnectionRegistry registry = getRegistry();
    Property.removeListener(registry, this);
    super.doDelete();
  }

  @Override
  protected List<TreeNode> doLoadChildren() {
    final List<TreeNode> children = new ArrayList<TreeNode>();
    final DataObjectStoreConnectionRegistry registry = getRegistry();
    final List<DataObjectStoreConnection> conections = registry.getConections();
    for (final DataObjectStoreConnection connection : conections) {
      final DataObjectStoreConnectionTreeNode child = new DataObjectStoreConnectionTreeNode(
        this, connection);
      children.add(child);
    }
    return children;
  }

  protected DataObjectStoreConnectionRegistry getRegistry() {
    final DataObjectStoreConnectionRegistry registry = getUserObject();
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
          final DataObjectStoreConnectionTreeNode node = new DataObjectStoreConnectionTreeNode(
            this, (DataObjectStoreConnection)newValue);
          addNode(index, node);

          nodesInserted(index);
        } else {
          nodesChanged(index);
        }
      }
    }
  }
}
