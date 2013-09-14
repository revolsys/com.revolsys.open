package com.revolsys.swing.tree.file;

import java.awt.Component;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;

import com.revolsys.io.file.FolderConnection;
import com.revolsys.io.file.FolderConnectionFile;
import com.revolsys.swing.SwingUtil;
import com.revolsys.swing.menu.MenuFactory;
import com.revolsys.swing.tree.ObjectTree;
import com.revolsys.swing.tree.TreeItemPropertyEnableCheck;
import com.revolsys.swing.tree.model.ObjectTreeModel;
import com.revolsys.swing.tree.model.node.AbstractObjectTreeNodeModel;

public class FolderConnectionModel extends
  AbstractObjectTreeNodeModel<FolderConnection, FolderConnectionFile> {

  public FolderConnectionModel() {
    setSupportedClasses(FolderConnection.class);
    setSupportedChildClasses(FolderConnectionFile.class);
    setObjectTreeNodeModels(new FolderConnectionFileModel());
    setLazyLoad(true);
    final DefaultTreeCellRenderer renderer = getRenderer();
    renderer.setOpenIcon(FileModel.ICON_FOLDER_LINK);
    renderer.setClosedIcon(FileModel.ICON_FOLDER_LINK);
  }

  public void deleteConnection() {
    final Object object = ObjectTree.getMouseClickItem();
    if (object instanceof FolderConnection) {
      final FolderConnection connection = (FolderConnection)object;
      final int confirm = JOptionPane.showConfirmDialog(
        SwingUtil.getActiveWindow(),
        "Delete folder connection '" + connection.getName()
          + "'? This action cannot be undone.", "Delete Layer",
        JOptionPane.OK_CANCEL_OPTION, JOptionPane.ERROR_MESSAGE);
      if (confirm == JOptionPane.OK_OPTION) {
        connection.delete();
      }
    }
  }

  @Override
  protected List<FolderConnectionFile> getChildren(
    final FolderConnection connection) {
    final FolderConnectionFile file = connection.getFile();
    final List<FolderConnectionFile> files = file.getFiles();
    return files;
  }

  @Override
  public Component getRenderer(final FolderConnection connection,
    final JTree tree, final boolean selected, final boolean expanded,
    final boolean leaf, final int row, final boolean hasFocus) {
    final FolderConnectionFile file = connection.getFile();
    final JLabel renderer = (JLabel)super.getRenderer(connection, tree,
      selected, expanded, leaf, row, hasFocus);
    if (file == null || !file.exists()) {
      renderer.setIcon(FileModel.ICON_FOLDER_MISSING);
    } else if (file == null || !file.exists()) {
      renderer.setIcon(FileModel.ICON_FOLDER_MISSING);
    } else {
      renderer.setIcon(FileModel.ICON_FOLDER_LINK);
    }
    return renderer;
  }

  @Override
  public void initialize(final FolderConnection connection) {
    getChildren(connection);
  }

  @Override
  public boolean isLeaf(final FolderConnection node) {
    return false;
  }

  @Override
  public void setObjectTreeModel(final ObjectTreeModel objectTreeModel) {
    super.setObjectTreeModel(objectTreeModel);
    final MenuFactory menu = ObjectTreeModel.getMenu(FolderConnection.class);
    final TreeItemPropertyEnableCheck readOnly = new TreeItemPropertyEnableCheck(
      "readOnly", false);
    menu.addMenuItemTitleIcon("default", "Delete Folder Connection", "delete",
      readOnly, this, "deleteConnection");
  }
}
