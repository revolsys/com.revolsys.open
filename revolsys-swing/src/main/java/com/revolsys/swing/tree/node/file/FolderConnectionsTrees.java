package com.revolsys.swing.tree.node.file;

import java.nio.file.Path;

import javax.swing.JFileChooser;

import com.revolsys.connection.ConnectionRegistry;
import com.revolsys.connection.file.FileConnectionManager;
import com.revolsys.connection.file.FolderConnection;
import com.revolsys.connection.file.FolderConnectionRegistry;
import com.revolsys.io.file.Paths;
import com.revolsys.swing.Borders;
import com.revolsys.swing.SwingUtil;
import com.revolsys.swing.component.ValueField;
import com.revolsys.swing.field.FileField;
import com.revolsys.swing.field.TextField;
import com.revolsys.swing.layout.GroupLayouts;
import com.revolsys.swing.menu.MenuFactory;
import com.revolsys.swing.tree.BaseTreeNode;
import com.revolsys.swing.tree.TreeNodes;
import com.revolsys.swing.tree.node.ConnectionManagerTrees;

public class FolderConnectionsTrees extends ConnectionManagerTrees {
  static {
    // FolderConnectionRegistry
    MenuFactory.addMenuInitializer(FolderConnectionRegistry.class, menu -> {
      TreeNodes.addMenuItemNodeValue(menu, "default", 0, "Add Connection", "folder:add",
        ConnectionRegistry::isEditable, FolderConnectionsTrees::addConnection);

      TreeNodes.addMenuItemNodeValue(menu, "default", 1, "Import Connection...", "folder:import",
        FolderConnectionRegistry::isEditable, FolderConnectionsTrees::importConnection);
    });

    // FolderConnection
    MenuFactory.addMenuInitializer(FolderConnection.class,
      menu -> TreeNodes.<FolderConnection> addMenuItemNodeValue(menu, "default", 1,
        "Export Connection", "folder:export", ConnectionManagerTrees::exportConnection));
  }

  private static void addConnection(final FolderConnectionRegistry registry) {
    final ValueField panel = new ValueField();
    panel.setTitle("Add Folder Connection");
    Borders.titled(panel, "Folder Connection");
    SwingUtil.addLabel(panel, "Name");
    final TextField nameField = new TextField(20);
    panel.add(nameField);

    SwingUtil.addLabel(panel, "Folder");
    final FileField folderField = new FileField(JFileChooser.DIRECTORIES_ONLY);
    panel.add(folderField);

    GroupLayouts.makeColumns(panel, 2, true);
    panel.showDialog();
    if (panel.isSaved()) {
      final Path file = folderField.getPath();
      if (file != null && Paths.exists(file)) {
        final String connectionName = nameField.getText();
        registry.addConnection(connectionName, file);
      }
    }
  }

  public static BaseTreeNode newFolderConnectionsTreeNode() {
    final FileConnectionManager connectionManager = FileConnectionManager.get();
    final BaseTreeNode node = BaseTreeNode.newTreeNode(connectionManager);
    node.setOpen(true);
    return node;
  }
}
