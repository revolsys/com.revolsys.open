package com.revolsys.swing.tree.node;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Icon;

import com.revolsys.io.connection.ConnectionRegistry;
import com.revolsys.io.connection.ConnectionRegistryManager;
import com.revolsys.swing.EventQueue;
import com.revolsys.swing.tree.BaseTreeNode;

public abstract class AbstractConnectionRegistryManagerTreeNode<M extends ConnectionRegistryManager<R>, R extends ConnectionRegistry<?>>
  extends LazyLoadTreeNode {

  public AbstractConnectionRegistryManagerTreeNode(final M connectionManager, final Icon icon) {
    super(connectionManager);
    final String name = connectionManager.getName();
    setName(name);
    setIcon(icon);
    EventQueue.addPropertyChange(connectionManager, this::refresh);
  }

  @SuppressWarnings("unchecked")
  public M getConnectionManager() {
    return (M)super.getUserObject();
  }

  @Override
  protected List<BaseTreeNode> loadChildrenDo() {
    final M connectionManager = getConnectionManager();
    final List<BaseTreeNode> children = new ArrayList<>();
    final List<R> registries = connectionManager.getVisibleConnectionRegistries();
    for (final R registry : registries) {
      final BaseTreeNode child = newTreeNode(registry);
      children.add(child);
    }
    return children;
  }

  protected abstract BaseTreeNode newTreeNode(final R registry);

  @Override
  public void propertyChangeDo(final PropertyChangeEvent event) {
    final Object source = event.getSource();
    if (source == getUserObject()) {
      final String propertyName = event.getPropertyName();
      if (propertyName.equals("registries")) {
        refresh();
      }
    }
  }
}
