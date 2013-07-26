package com.revolsys.swing.tree.file;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeCellRenderer;

import com.revolsys.io.file.FolderConnection;
import com.revolsys.swing.SwingUtil;
import com.revolsys.swing.menu.MenuFactory;
import com.revolsys.swing.tree.ObjectTree;
import com.revolsys.swing.tree.TreeItemPropertyEnableCheck;
import com.revolsys.swing.tree.model.ObjectTreeModel;
import com.revolsys.swing.tree.model.node.AbstractObjectTreeNodeModel;

public class FolderConnectionModel extends
  AbstractObjectTreeNodeModel<FolderConnection, File> {

  public FolderConnectionModel() {
    setSupportedClasses(FolderConnection.class);
    setSupportedChildClasses(File.class);
    setObjectTreeNodeModels(new FileTreeNodeModel());
    setLazyLoad(true);
    final DefaultTreeCellRenderer renderer = getRenderer();
    renderer.setOpenIcon(FileTreeNodeModel.ICON_FOLDER_LINK);
    renderer.setClosedIcon(FileTreeNodeModel.ICON_FOLDER_LINK);
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
  protected List<File> getChildren(final FolderConnection connection) {
    final File file = connection.getFile();
    if (file.isDirectory()) {
      final File[] files = file.listFiles();
      if (files != null) {
        return Arrays.asList(files);
      }
    }
    return Collections.emptyList();
  }

  @Override
  public TreeCellRenderer getRenderer(final FolderConnection connection) {
    final File file = connection.getFile();
    final DefaultTreeCellRenderer renderer = (DefaultTreeCellRenderer)super.getRenderer(connection);
    if (file == null || !file.exists()) {
      renderer.setOpenIcon(FileTreeNodeModel.ICON_FOLDER_MISSING);
      renderer.setClosedIcon(FileTreeNodeModel.ICON_FOLDER_MISSING);

    } else if (file == null || !file.exists()) {
      renderer.setOpenIcon(FileTreeNodeModel.ICON_FOLDER_MISSING);
      renderer.setClosedIcon(FileTreeNodeModel.ICON_FOLDER_MISSING);
    } else {
      renderer.setOpenIcon(FileTreeNodeModel.ICON_FOLDER_LINK);
      renderer.setClosedIcon(FileTreeNodeModel.ICON_FOLDER_LINK);
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
