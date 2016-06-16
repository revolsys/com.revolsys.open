package com.revolsys.swing.tree.node;

import javax.swing.JOptionPane;

import com.revolsys.io.connection.AbstractConnection;
import com.revolsys.io.connection.AbstractConnectionRegistry;
import com.revolsys.io.connection.Connection;
import com.revolsys.swing.SwingUtil;
import com.revolsys.swing.menu.MenuFactory;
import com.revolsys.swing.tree.TreeNodes;

public class ConnectionManagerTrees {

  static {
    // AbstractConnectionRegistry
    final MenuFactory connectionRegistryMenu = MenuFactory
      .getMenu(AbstractConnectionRegistry.class);
    LazyLoadTreeNode.addRefreshMenuItem(connectionRegistryMenu);

    // AbstractConnection
    final MenuFactory connectionMenu = MenuFactory.getMenu(AbstractConnection.class);
    LazyLoadTreeNode.addRefreshMenuItem(connectionMenu);
    TreeNodes.addMenuItemNodeValue(connectionMenu, "default", "Delete Connection", "delete",
      Connection::isEditable, ConnectionManagerTrees::deleteConnection);
  }

  private static void deleteConnection(final Connection connection) {
    if (!connection.isReadOnly()) {
      final int confirm = JOptionPane.showConfirmDialog(SwingUtil.getActiveWindow(),
        "Delete connection '" + connection.getName() + "'? This action cannot be undone.",
        "Delete Connection", JOptionPane.OK_CANCEL_OPTION, JOptionPane.ERROR_MESSAGE);
      if (confirm == JOptionPane.OK_OPTION) {
        connection.deleteConnection();
      }
    }
  }
}
