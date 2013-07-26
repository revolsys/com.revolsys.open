package com.revolsys.swing.tree.file;

import java.awt.TextField;
import java.io.File;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.tree.DefaultTreeCellRenderer;

import com.revolsys.io.file.FolderConnection;
import com.revolsys.io.file.FolderConnectionRegistry;
import com.revolsys.swing.SwingUtil;
import com.revolsys.swing.component.ValueField;
import com.revolsys.swing.field.DirectoryNameField;
import com.revolsys.swing.layout.GroupLayoutUtil;
import com.revolsys.swing.menu.MenuFactory;
import com.revolsys.swing.tree.ObjectTree;
import com.revolsys.swing.tree.TreeItemPropertyEnableCheck;
import com.revolsys.swing.tree.model.ObjectTreeModel;
import com.revolsys.swing.tree.model.node.AbstractObjectTreeNodeModel;

public class FolderConnectionRegistryModel extends
  AbstractObjectTreeNodeModel<FolderConnectionRegistry, FolderConnection> {

  public FolderConnectionRegistryModel() {
    setSupportedClasses(FolderConnectionRegistry.class);
    setSupportedChildClasses(File.class);
    setObjectTreeNodeModels(new FolderConnectionModel());
    final DefaultTreeCellRenderer renderer = getRenderer();
    renderer.setOpenIcon(FileTreeNodeModel.ICON_FOLDER_LINK);
    renderer.setClosedIcon(FileTreeNodeModel.ICON_FOLDER_LINK);
  }

  public void addConnection() {
    final Object object = ObjectTree.getMouseClickItem();
    if (object instanceof FolderConnectionRegistry) {
      final FolderConnectionRegistry registry = (FolderConnectionRegistry)object;
      final ValueField panel = new ValueField();
      panel.setTitle("Add Folder Connection");
      panel.setBorder(BorderFactory.createTitledBorder("Folder Connection"));
      SwingUtil.addLabel(panel, "Name");
      final TextField nameField = new TextField(20);
      panel.add(nameField);

      SwingUtil.addLabel(panel, "Folder");
      final DirectoryNameField folderField = new DirectoryNameField();
      panel.add(folderField);

      GroupLayoutUtil.makeColumns(panel, 2);
      panel.showDialog();
      if (panel.isSaved()) {
        final File file = folderField.getDirectoryFile();
        if (file != null && file.exists()) {
          registry.addConnection(nameField.getText(), file);
        }
      }
    }

  }

  @Override
  protected List<FolderConnection> getChildren(
    final FolderConnectionRegistry connectionRegistry) {
    final List<FolderConnection> connections = connectionRegistry.getConections();
    return connections;
  }

  @Override
  public void initialize(final FolderConnectionRegistry connectionRegistry) {
    getChildren(connectionRegistry);
  }

  @Override
  public boolean isLeaf(final FolderConnectionRegistry node) {
    return false;
  }

  @Override
  public void setObjectTreeModel(final ObjectTreeModel objectTreeModel) {
    super.setObjectTreeModel(objectTreeModel);
    final MenuFactory menu = ObjectTreeModel.getMenu(FolderConnectionRegistry.class);
    final TreeItemPropertyEnableCheck readOnly = new TreeItemPropertyEnableCheck(
      "readOnly", false);
    menu.addMenuItemTitleIcon("default", "Add Folder Connection", "add",
      readOnly, this, "addConnection");
  }

}
