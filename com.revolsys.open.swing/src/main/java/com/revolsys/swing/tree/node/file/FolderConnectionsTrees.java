package com.revolsys.swing.tree.node.file;

import java.awt.TextField;
import java.io.File;

import javax.swing.JFileChooser;

import com.revolsys.io.connection.ConnectionRegistry;
import com.revolsys.io.file.FileConnectionManager;
import com.revolsys.io.file.FolderConnectionRegistry;
import com.revolsys.swing.Borders;
import com.revolsys.swing.SwingUtil;
import com.revolsys.swing.component.ValueField;
import com.revolsys.swing.field.FileField;
import com.revolsys.swing.layout.GroupLayouts;
import com.revolsys.swing.menu.MenuFactory;
import com.revolsys.swing.tree.BaseTreeNode;
import com.revolsys.swing.tree.TreeNodes;
import com.revolsys.swing.tree.node.ConnectionManagerTrees;

public class FolderConnectionsTrees extends ConnectionManagerTrees {
  static {
    // FolderConnectionRegistry
    final MenuFactory menu = MenuFactory.getMenu(FolderConnectionRegistry.class);
    TreeNodes.addMenuItemNodeValue(menu, "default", 0, "Add Connection", "folder:add",
      ConnectionRegistry::isEditable, FolderConnectionsTrees::addConnection);
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
      final File file = folderField.getFile();
      if (file != null && file.exists()) {
        registry.addConnection(nameField.getText(), file);
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
