package com.revolsys.swing.tree.file;

import java.io.File;
import java.util.List;
import java.util.ListIterator;

import javax.swing.Icon;
import javax.swing.tree.TreeNode;

import com.revolsys.famfamfam.silk.SilkIconLoader;
import com.revolsys.swing.action.InvokeMethodAction;
import com.revolsys.swing.menu.MenuFactory;
import com.revolsys.swing.tree.TreeItemRunnable;
import com.revolsys.swing.tree.model.node.AbstractTreeNode;

public class FileSystemsTreeNode extends AbstractTreeNode {
  private List<TreeNode> children;

  public static final Icon ICON_FOLDER_DRIVE = SilkIconLoader.getIcon("folder_drive");

  private static final MenuFactory MENU = new MenuFactory();

  static {
    final InvokeMethodAction refresh = TreeItemRunnable.createAction("Refresh",
      "arrow_refresh", "refresh");
    MENU.addMenuItem("default", refresh);
  }

  public FileSystemsTreeNode(final TreeNode parent) {
    super(parent, null);
    setType("File Systems");
    setName("File Systems");
    setIcon(FileSystemsTreeNode.ICON_FOLDER_DRIVE);
    setAllowsChildren(true);
    init();
  }

  @Override
  public boolean equals(final Object other) {
    if (other instanceof FileSystemsTreeNode) {
      return true;
    }
    return false;
  }

  @Override
  public List<TreeNode> getChildren() {
    return children;
  }

  @Override
  public MenuFactory getMenu() {
    return MENU;
  }

  @Override
  public int hashCode() {
    return 0;
  }

  protected void init() {
    final File[] roots = File.listRoots();
    children = FileTreeNode.getFileNodes(this, roots);
  }

  @SuppressWarnings({
    "unchecked", "rawtypes"
  })
  public void refresh() {
    final List<FileTreeNode> oldNodes = (List)getChildren();
    final ListIterator<FileTreeNode> oldIterator = oldNodes.listIterator();
    final File[] roots = File.listRoots();

    final List<FileTreeNode> newNodes = (List)FileTreeNode.getFileNodes(this,
      roots);
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
}
