package com.revolsys.swing.tree.node.file;

import java.io.File;
import java.util.List;

import javax.swing.Icon;

import com.revolsys.famfamfam.silk.SilkIconLoader;
import com.revolsys.swing.action.InvokeMethodAction;
import com.revolsys.swing.menu.MenuFactory;
import com.revolsys.swing.tree.TreeItemRunnable;
import com.revolsys.swing.tree.node.BaseTreeNode;
import com.revolsys.swing.tree.node.ListTreeNode;

public class FileSystemsTreeNode extends ListTreeNode {

  public static final Icon ICON_FOLDER_DRIVE = SilkIconLoader.getIcon("folder_drive");

  private static final MenuFactory MENU = new MenuFactory();

  static {
    final InvokeMethodAction refresh = TreeItemRunnable.createAction("Refresh",
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
    final File[] roots = File.listRoots();
    return FileTreeNode.getFileNodes(this, roots);
  }

  @Override
  public MenuFactory getMenu() {
    return MENU;
  }
}
