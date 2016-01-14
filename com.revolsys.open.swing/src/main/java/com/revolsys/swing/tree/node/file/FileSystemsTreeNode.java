package com.revolsys.swing.tree.node.file;

import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.List;

import com.revolsys.swing.menu.MenuFactory;
import com.revolsys.swing.tree.BaseTreeNode;
import com.revolsys.swing.tree.LazyLoadTreeNode;

public class FileSystemsTreeNode extends LazyLoadTreeNode {
  private static final MenuFactory MENU = new MenuFactory("File Systems");

  static {
    addRefreshMenuItem(MENU);
  }

  public FileSystemsTreeNode() {
    setType("File Systems");
    setName("File Systems");
    setIcon(PathTreeNode.ICON_FOLDER_DRIVE);
  }

  @Override
  public MenuFactory getMenu() {
    return MENU;
  }

  @Override
  protected List<BaseTreeNode> loadChildrenDo() {
    final FileSystem fileSystem = FileSystems.getDefault();

    final Iterable<Path> roots = fileSystem.getRootDirectories();
    return PathTreeNode.getPathNodes(this, roots, true);
  }
}
