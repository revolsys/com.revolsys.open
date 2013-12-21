package com.revolsys.swing.tree.file;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.ListIterator;

import javax.swing.Icon;
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
  private static final MenuFactory MENU = new MenuFactory();
  static {
    final InvokeMethodAction refresh = TreeItemRunnable.createAction("Refresh",
      "arrow_refresh", "refresh");
    MENU.addMenuItem("default", refresh);

    final TreeItemPropertyEnableCheck readOnly = new TreeItemPropertyEnableCheck(
      "readOnly", false);
    MENU.addMenuItemTitleIcon("default", "Delete Folder Connection", "delete",
      readOnly, FolderConnectionTreeNode.class, "deleteConnection");
  }

  public static void deleteConnection() {
    final FolderConnectionTreeNode node = BaseTree.getMouseClickItem();
    final FolderConnection connection = node.getUserObject();
    final int confirm = JOptionPane.showConfirmDialog(
      SwingUtil.getActiveWindow(),
      "Delete folder connection '" + connection.getName()
        + "'? This action cannot be undone.", "Delete Layer",
      JOptionPane.OK_CANCEL_OPTION, JOptionPane.ERROR_MESSAGE);
    if (confirm == JOptionPane.OK_OPTION) {
      connection.delete();
    }
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
    final FolderConnection connection = getUserObject();
    return FileTreeNode.getFileNodes(this, connection.getFile());
  }

  protected File getFile() {
    final FolderConnection connection = getUserObject();
    final File file = connection.getFile();
    return file;
  }

  @Override
  public Icon getIcon() {
    final File file = getFile();
    if (file.exists()) {
      return super.getIcon();
    } else {
      return FileTreeNode.ICON_FOLDER_MISSING;
    }
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

  public boolean isReadOnly() {
    final FolderConnection connection = getUserObject();
    return connection.isReadOnly();
  }

  // TODO doesn't work
  @Override
  @SuppressWarnings({
    "unchecked", "rawtypes"
  })
  public void refresh() {
    final File file = getFile();
    final List<TreeNode> children = getChildren();
    if (file.exists()) {
      if (file.isDirectory()) {
        final List<FileTreeNode> oldNodes = (List)children;
        final ListIterator<FileTreeNode> oldIterator = oldNodes.listIterator();

        final List<FileTreeNode> newNodes = (List)FileTreeNode.getFileNodes(
          this, file);
        final ListIterator<FileTreeNode> newIterator = newNodes.listIterator();
        int i = 0;

        while (oldIterator.hasNext() && newIterator.hasNext()) {
          FileTreeNode oldNode = oldIterator.next();
          File oldFile = oldNode.getFile();

          FileTreeNode newNode = newIterator.next();
          File newFile = newNode.getFile();
          while (oldFile != null && oldFile.compareTo(newFile) < 0) {
            oldIterator.remove();
            nodeRemoved(i, oldNode);
            if (oldIterator.hasNext()) {
              oldNode = oldIterator.next();
              oldFile = oldNode.getFile();
            } else {
              oldFile = null;
            }
          }
          if (oldFile != null) {
            while (newFile != null && newFile.compareTo(oldFile) < 0) {
              oldIterator.previous();
              oldIterator.add(newNode);
              oldIterator.next();
              nodesInserted(i);
              i++;
              if (newIterator.hasNext()) {
                newNode = newIterator.next();
                newFile = newNode.getFile();
              } else {
                newFile = null;
              }
            }
            if (newFile != null) {
              i++;
            }
          }
        }
        while (oldIterator.hasNext()) {
          oldIterator.next();
          oldIterator.remove();
        }

        while (newIterator.hasNext()) {
          final FileTreeNode newNode = newIterator.next();
          oldIterator.add(newNode);
          nodesInserted(i);
          i++;
        }
      }
    } else {
      for (final TreeNode node : children) {
        removeNode(node);
      }
    }
  }
}
