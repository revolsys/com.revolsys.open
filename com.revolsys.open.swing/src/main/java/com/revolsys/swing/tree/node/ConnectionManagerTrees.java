package com.revolsys.swing.tree.node;

import java.awt.Window;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.revolsys.io.FileUtil;
import com.revolsys.io.connection.AbstractConnection;
import com.revolsys.io.connection.AbstractConnectionRegistry;
import com.revolsys.io.connection.Connection;
import com.revolsys.io.connection.ConnectionRegistry;
import com.revolsys.swing.SwingUtil;
import com.revolsys.swing.menu.MenuFactory;
import com.revolsys.swing.parallel.Invoke;
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

  protected static void exportConnection(final Connection connection) {
    final ConnectionRegistry<?> connectionRegistry = connection.getRegistry();
    String connectionType = connectionRegistry.getConnectionManager().getName();
    if (connectionType.endsWith("s")) {
      connectionType = connectionType.substring(0, connectionType.length() - 1);
    }
    if (!connectionType.endsWith(" Connection")) {
      connectionType += " Connection";
    }

    final Window window = SwingUtil.getActiveWindow();

    final Class<?> chooserClass = connectionRegistry.getClass();
    final JFileChooser fileChooser = SwingUtil.newFileChooser(chooserClass, "currentDirectory");
    fileChooser.setDialogTitle("Export " + connectionType);
    fileChooser.setMultiSelectionEnabled(false);

    final String fileExtension = connectionRegistry.getFileExtension();
    final FileNameExtensionFilter allFilter = new FileNameExtensionFilter("*." + fileExtension,
      fileExtension);
    fileChooser.addChoosableFileFilter(allFilter);

    fileChooser.setAcceptAllFileFilterUsed(false);
    fileChooser.setFileFilter(allFilter);

    final String connectionName = connection.getName();
    fileChooser.setSelectedFile(
      new File(fileChooser.getCurrentDirectory(), connectionName + "." + fileExtension));

    final int status = fileChooser.showSaveDialog(window);
    if (status == JFileChooser.APPROVE_OPTION) {
      Invoke.background("Export " + connectionType, () -> {
        File file = fileChooser.getSelectedFile();
        file = FileUtil.getFileWithExtension(file, fileExtension);
        connection.writeToFile(file);
      });
    }
    SwingUtil.saveFileChooserDirectory(chooserClass, "currentDirectory", fileChooser);
  }

  protected static void importConnection(
    final ConnectionRegistry<? extends Connection> connectionRegistry) {
    String name = connectionRegistry.getConnectionManager().getName();
    if (name.endsWith("s")) {
      name = name.substring(0, name.length() - 1);
    }
    if (!name.endsWith(" Connection")) {
      name += " Connection";
    }

    final Window window = SwingUtil.getActiveWindow();

    final Class<?> chooserClass = connectionRegistry.getClass();
    final JFileChooser fileChooser = SwingUtil.newFileChooser(chooserClass, "currentDirectory");
    fileChooser.setDialogTitle("Import " + name);
    fileChooser.setMultiSelectionEnabled(true);

    final String fileExtension = connectionRegistry.getFileExtension();
    final FileNameExtensionFilter allFilter = new FileNameExtensionFilter("*." + fileExtension,
      fileExtension);
    fileChooser.addChoosableFileFilter(allFilter);

    fileChooser.setAcceptAllFileFilterUsed(false);
    fileChooser.setFileFilter(allFilter);

    final int status = fileChooser.showDialog(window, "Import");
    if (status == JFileChooser.APPROVE_OPTION) {
      Invoke.background("Import " + name, () -> {
        for (final File file : fileChooser.getSelectedFiles()) {
          connectionRegistry.importConnection(file);
        }
      });
    }
    SwingUtil.saveFileChooserDirectory(chooserClass, "currentDirectory", fileChooser);
  }

}
