package com.revolsys.swing.tree.file;

import java.awt.TextField;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.tree.TreeNode;

import com.revolsys.io.file.FolderConnection;
import com.revolsys.io.file.FolderConnectionRegistry;
import com.revolsys.swing.SwingUtil;
import com.revolsys.swing.component.ValueField;
import com.revolsys.swing.field.DirectoryNameField;
import com.revolsys.swing.layout.GroupLayoutUtil;
import com.revolsys.swing.menu.MenuFactory;
import com.revolsys.swing.tree.BaseTree;
import com.revolsys.swing.tree.model.node.LazyLoadTreeNode;

public class FolderConnectionRegistryTreeNode extends LazyLoadTreeNode {

  private static final MenuFactory MENU = new MenuFactory();

  public FolderConnectionRegistryTreeNode(
    final FolderConnectionsTreeNode parent,
    final FolderConnectionRegistry registry) {
    super(parent, registry);
    setType("Folder Connections");
    setName(registry.getName());
    setIcon(FileTreeUtil.ICON_FOLDER_LINK);
    setAllowsChildren(true);
  }

  public void addConnection() {
    final FolderConnectionRegistryTreeNode object = BaseTree.getMouseClickItem();
    final FolderConnectionRegistryTreeNode node = object;
    final FolderConnectionRegistry registry = node.getUserObject();
    final ValueField panel = new ValueField();
    panel.setTitle("Add Folder Connection");
    SwingUtil.setTitledBorder(panel, "Folder Connection");
    SwingUtil.addLabel(panel, "Name");
    final TextField nameField = new TextField(20);
    panel.add(nameField);

    SwingUtil.addLabel(panel, "Folder");
    final DirectoryNameField folderField = new DirectoryNameField();
    panel.add(folderField);

    GroupLayoutUtil.makeColumns(panel, 2, true);
    panel.showDialog();
    if (panel.isSaved()) {
      final File file = folderField.getDirectoryFile();
      if (file != null && file.exists()) {
        registry.addConnection(nameField.getText(), file);
      }
    }
  }

  @Override
  protected List<TreeNode> doLoadChildren() {
    final List<TreeNode> children = new ArrayList<TreeNode>();
    final FolderConnectionRegistry registry = getUserObject();
    final List<FolderConnection> conections = registry.getConections();
    for (final FolderConnection connection : conections) {
      final FolderConnectionTreeNode child = new FolderConnectionTreeNode(this,
        connection);
      children.add(child);
    }
    return children;
  }

  @Override
  public MenuFactory getMenu() {
    return MENU;
  }
}
