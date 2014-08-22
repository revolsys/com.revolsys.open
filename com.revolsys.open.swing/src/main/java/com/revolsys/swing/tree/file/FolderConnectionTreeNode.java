package com.revolsys.swing.tree.file;

import java.io.File;
import java.net.URL;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.tree.TreeNode;

import com.revolsys.io.file.FolderConnection;
import com.revolsys.swing.SwingUtil;
import com.revolsys.swing.action.InvokeMethodAction;
import com.revolsys.swing.menu.MenuFactory;
import com.revolsys.swing.tree.BaseTree;
import com.revolsys.swing.tree.TreeItemPropertyEnableCheck;
import com.revolsys.swing.tree.TreeItemRunnable;
import com.revolsys.swing.tree.model.node.LazyLoadTreeNode;
import com.revolsys.util.UrlProxy;
import com.revolsys.util.UrlUtil;

public class FolderConnectionTreeNode extends LazyLoadTreeNode implements
UrlProxy {
  public static void deleteConnection() {
    final FolderConnectionTreeNode node = BaseTree.getMouseClickItem();
    final FolderConnection connection = node.getUserData();
    final int confirm = JOptionPane.showConfirmDialog(
      SwingUtil.getActiveWindow(),
      "Delete folder connection '" + connection.getName()
      + "'? This action cannot be undone.", "Delete Layer",
      JOptionPane.OK_CANCEL_OPTION, JOptionPane.ERROR_MESSAGE);
    if (confirm == JOptionPane.OK_OPTION) {
      connection.delete();
    }
  }

  private static final MenuFactory MENU = new MenuFactory();

  static {
    final InvokeMethodAction refresh = TreeItemRunnable.createAction("Refresh",
      "arrow_refresh", NODE_EXISTS, "refresh");
    MENU.addMenuItem("default", refresh);

    final TreeItemPropertyEnableCheck readOnly = new TreeItemPropertyEnableCheck(
      "readOnly", false);
    MENU.addMenuItemTitleIcon("default", "Delete Folder Connection", "delete",
      readOnly, FolderConnectionTreeNode.class, "deleteConnection");
  }

  public FolderConnectionTreeNode(
    final FolderConnectionRegistryTreeNode parent,
    final FolderConnection connection) {
    super(parent, connection);
    setType("Folder Connection");
    setName(connection.getName());
    setIcon(FileTreeNode.ICON_FOLDER_LINK);
    setAllowsChildren(true);
  }

  @Override
  protected List<TreeNode> doLoadChildren() {
    final FolderConnection connection = getUserData();
    return FileTreeNode.getFileNodes(this, connection.getFile());
  }

  protected File getFile() {
    final FolderConnection connection = getUserData();
    final File file = connection.getFile();
    return file;
  }

  @Override
  public MenuFactory getMenu() {
    return MENU;
  }

  @Override
  public URL getUrl() {
    final String name = getName();
    final String urlstring = "folderconnection://"
        + UrlUtil.percentEncode(name) + "/";
    try {
      final URL url = new URL(urlstring);
      return url;
    } catch (final Throwable e) {
      throw new IllegalArgumentException("Invalid URL: " + urlstring, e);
    }
  }

  @Override
  public boolean isExists() {
    final File file = getFile();
    return file != null && file.exists() && super.isExists();
  }

  public boolean isReadOnly() {
    final FolderConnection connection = getUserData();
    return connection.isReadOnly();
  }

}
