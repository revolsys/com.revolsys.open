package com.revolsys.swing.tree.node;

import java.io.File;
import java.nio.file.Path;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import com.revolsys.connection.AbstractConnection;
import com.revolsys.connection.AbstractConnectionRegistry;
import com.revolsys.connection.Connection;
import com.revolsys.connection.ConnectionRegistry;
import com.revolsys.io.FileUtil;
import com.revolsys.io.filter.FileNameExtensionFilter;
import com.revolsys.swing.Dialogs;
import com.revolsys.swing.SwingUtil;
import com.revolsys.swing.menu.MenuFactory;
import com.revolsys.swing.parallel.Invoke;
import com.revolsys.swing.tree.TreeNodes;

public class ConnectionManagerTrees {

  static {
    // AbstractConnectionRegistry
    MenuFactory.addMenuInitializer(AbstractConnectionRegistry.class, (menu) -> {
      LazyLoadTreeNode.addRefreshMenuItem(menu);
    });

    // AbstractConnection
    MenuFactory.addMenuInitializer(AbstractConnection.class, (menu) -> {
      LazyLoadTreeNode.addRefreshMenuItem(menu);
      TreeNodes.addMenuItemNodeValue(menu, "default", "Delete Connection", "delete",
        Connection::isEditable, ConnectionManagerTrees::deleteConnection);
    });
  }

  private static void deleteConnection(final Connection connection) {
    if (!connection.isReadOnly()) {
      final int confirm = Dialogs.showConfirmDialog(
        "Delete connection '" + connection.getName() + "'? This action cannot be undone.",
        "Delete Connection", JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE);
      if (confirm == JOptionPane.YES_OPTION) {
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

    final int status = Dialogs.showSaveDialog(fileChooser);
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

    final int status = Dialogs.showDialog(fileChooser, "Import");
    if (status == JFileChooser.APPROVE_OPTION) {
      Invoke.background("Import " + name, () -> {
        for (final File file : fileChooser.getSelectedFiles()) {
          final Path path = file.toPath();
          connectionRegistry.importConnection(path);
        }
      });
    }
    SwingUtil.saveFileChooserDirectory(chooserClass, "currentDirectory", fileChooser);
  }

}
