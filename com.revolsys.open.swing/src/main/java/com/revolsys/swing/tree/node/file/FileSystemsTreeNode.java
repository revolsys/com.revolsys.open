package com.revolsys.swing.tree.node.file;

import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.List;

import javax.swing.Icon;

import com.revolsys.swing.Icons;
import com.revolsys.swing.action.InvokeMethodAction;
import com.revolsys.swing.menu.MenuFactory;
import com.revolsys.swing.tree.TreeNodeRunnable;
import com.revolsys.swing.tree.node.BaseTreeNode;
import com.revolsys.swing.tree.node.ListTreeNode;

public class FileSystemsTreeNode extends ListTreeNode {

  public static final Icon ICON_FOLDER_DRIVE = Icons.getIcon("folder_drive");

  public static final Icon ICON_FOLDER_DRIVE_OPEN = Icons.getIcon("folder_drive_open");

  private static final MenuFactory MENU = new MenuFactory("File Systems");

  static {
    final InvokeMethodAction refresh = TreeNodeRunnable.createAction("Refresh",
      "arrow_refresh", "refresh");
    MENU.addMenuItem("default", refresh);
  }

  public FileSystemsTreeNode() {
    setType("File Systems");
    setName("File Systems");
    setIcon(FileSystemsTreeNode.ICON_FOLDER_DRIVE);
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

  @Override
  public Icon getOpenIcon() {
    return ICON_FOLDER_DRIVE_OPEN;
  }
}
