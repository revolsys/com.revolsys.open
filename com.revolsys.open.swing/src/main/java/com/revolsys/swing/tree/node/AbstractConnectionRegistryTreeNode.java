package com.revolsys.swing.tree.node;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Icon;

import com.revolsys.io.connection.AbstractConnectionRegistry;
import com.revolsys.io.connection.Connection;
import com.revolsys.io.connection.ConnectionRegistry;
import com.revolsys.swing.menu.MenuFactory;
import com.revolsys.swing.tree.BaseTreeNode;

public abstract class AbstractConnectionRegistryTreeNode<R extends ConnectionRegistry<C>, C extends Connection>
  extends LazyLoadTreeNode implements PropertyChangeListener, OpenStateTreeNode {

  static {
    final MenuFactory menu = MenuFactory.getMenu(AbstractConnectionRegistry.class);
    addRefreshMenuItem(menu);
  }

  public AbstractConnectionRegistryTreeNode(final Icon icon, final R registry) {
    super(registry);
    setName(registry.getName());
    setIcon(icon);
    setOpen(true);
  }

  @SuppressWarnings("unchecked")
  public R getRegistry() {
    return (R)getUserData();
  }

  @Override
  protected List<BaseTreeNode> loadChildrenDo() {
    final List<BaseTreeNode> children = new ArrayList<>();
    final R registry = getRegistry();
    final List<C> conections = registry.getConnections();
    for (final C connection : conections) {
      final BaseTreeNode child = newChildTreeNode(connection);
      children.add(child);
    }
    return children;
  }

  protected abstract BaseTreeNode newChildTreeNode(final C connection);

  @Override
  public void propertyChangeDo(final PropertyChangeEvent event) {
    final Object source = event.getSource();
    if (source == getRegistry()) {
      final String propertyName = event.getPropertyName();
      if (propertyName.equals("connections")) {
        refresh();
      }
    }
  }
}
