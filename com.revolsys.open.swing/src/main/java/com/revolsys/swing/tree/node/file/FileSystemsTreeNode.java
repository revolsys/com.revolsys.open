package com.revolsys.swing.tree.node.file;

import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.List;

import com.revolsys.swing.action.InvokeMethodAction;
import com.revolsys.swing.menu.MenuFactory;
import com.revolsys.swing.tree.TreeNodeRunnable;
import com.revolsys.swing.tree.node.BaseTreeNode;
import com.revolsys.swing.tree.node.LazyLoadTreeNode;

public class FileSystemsTreeNode extends LazyLoadTreeNode {

  private static final MenuFactory MENU = new MenuFactory("File Systems");

  static {
    final InvokeMethodAction refresh = TreeNodeRunnable.createAction("Refresh", "arrow_refresh",
      "refresh");
    MENU.addMenuItem("default", refresh);
  }

  public FileSystemsTreeNode() {
    setType("File Systems");
    setName("File Systems");
    setIcon(FileTreeNode.ICON_FOLDER_DRIVE);
  }

  @Override
  protected List<BaseTreeNode> doLoadChildren() {
    final FileSystem fileSystem = FileSystems.getDefault();

    final Iterable<Path> roots = fileSystem.getRootDirectories();
    return FileTreeNode.getPathNodes(this, roots);
  }

  @Override
  public MenuFactory getMenu() {
    return MENU;
  }
}
