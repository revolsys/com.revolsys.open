package com.revolsys.swing.tree.node.file;

import java.awt.TextField;
import java.beans.PropertyChangeListener;
import java.io.File;

import javax.swing.JFileChooser;

import com.revolsys.io.file.FolderConnection;
import com.revolsys.io.file.FolderConnectionRegistry;
import com.revolsys.swing.Borders;
import com.revolsys.swing.SwingUtil;
import com.revolsys.swing.component.ValueField;
import com.revolsys.swing.field.FileField;
import com.revolsys.swing.layout.GroupLayouts;
import com.revolsys.swing.menu.MenuFactory;
import com.revolsys.swing.tree.TreeNodes;
import com.revolsys.swing.tree.node.AbstractConnectionRegistryTreeNode;

public class FolderConnectionRegistryTreeNode
  extends AbstractConnectionRegistryTreeNode<FolderConnectionRegistry, FolderConnection>
  implements PropertyChangeListener {

  static {
    final MenuFactory menu = MenuFactory.getMenu(FolderConnectionRegistry.class);
    TreeNodes.addMenuItem(menu, "default", "Add Connection", "folder:add",
      FolderConnectionRegistryTreeNode::addConnection);
  }

  public FolderConnectionRegistryTreeNode(final FolderConnectionRegistry registry) {
    super(PathTreeNode.ICON_FOLDER_LINK, registry);
  }

  private void addConnection() {
    final FolderConnectionRegistry registry = getRegistry();
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

  @Override
  protected FolderConnectionTreeNode newChildTreeNode(final FolderConnection connection) {
    return new FolderConnectionTreeNode(connection);
  }
}
