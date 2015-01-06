package com.revolsys.swing.tree.node.file;

import java.io.File;
import java.net.URL;
import java.util.List;

import javax.swing.Icon;
import javax.swing.JOptionPane;

import com.revolsys.io.file.FolderConnection;
import com.revolsys.swing.SwingUtil;
import com.revolsys.swing.action.InvokeMethodAction;
import com.revolsys.swing.action.enablecheck.EnableCheck;
import com.revolsys.swing.menu.MenuFactory;
import com.revolsys.swing.tree.TreeNodePropertyEnableCheck;
import com.revolsys.swing.tree.TreeNodeRunnable;
import com.revolsys.swing.tree.node.BaseTreeNode;
import com.revolsys.swing.tree.node.LazyLoadTreeNode;
import com.revolsys.util.UrlProxy;
import com.revolsys.util.UrlUtil;

public class FolderConnectionTreeNode extends LazyLoadTreeNode implements
UrlProxy {
  private static final MenuFactory MENU = new MenuFactory("Folder Connection");

  static {
    final InvokeMethodAction refresh = TreeNodeRunnable.createAction("Refresh",
      "arrow_refresh", NODE_EXISTS, "refresh");
    MENU.addMenuItem("default", refresh);

    final EnableCheck readOnly = new TreeNodePropertyEnableCheck("readOnly",
      false);
    MENU.addMenuItem("default", TreeNodeRunnable.createAction(
      "Delete Folder Connection", "delete", readOnly, "deleteConnection"));
  }

  public FolderConnectionTreeNode(final FolderConnection connection) {
    super(connection);
    setType("Folder Connection");
    setName(connection.getName());
    setIcon(FileTreeNode.ICON_FOLDER_LINK);
  }

  public void deleteConnection() {
    final FolderConnection connection = getUserData();
    final int confirm = JOptionPane.showConfirmDialog(
      SwingUtil.getActiveWindow(),
      "Delete folder connection '" + connection.getName()
      + "'? This action cannot be undone.", "Delete Layer",
      JOptionPane.OK_CANCEL_OPTION, JOptionPane.ERROR_MESSAGE);
    if (confirm == JOptionPane.OK_OPTION) {
      connection.delete();
    }
  }

  @Override
  protected List<BaseTreeNode> doLoadChildren() {
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
  public Icon getOpenIcon() {
    return FileTreeNode.ICON_FOLDER_LINK_OPEN;
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
