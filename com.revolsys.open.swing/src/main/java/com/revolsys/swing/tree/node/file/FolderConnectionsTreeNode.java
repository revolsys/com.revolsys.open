package com.revolsys.swing.tree.node.file;

import java.util.ArrayList;
import java.util.List;

import com.revolsys.io.file.FileConnectionManager;
import com.revolsys.io.file.FolderConnectionRegistry;
import com.revolsys.swing.EventQueue;
import com.revolsys.swing.menu.MenuFactory;
import com.revolsys.swing.tree.BaseTreeNode;
import com.revolsys.swing.tree.LazyLoadTreeNode;

public class FolderConnectionsTreeNode extends LazyLoadTreeNode {
  private static final MenuFactory MENU = new MenuFactory("Folder Connections");

  static {
    addRefreshMenuItem(MENU);
  }

  public FolderConnectionsTreeNode() {
    setName("Folder Connections");
    setType("Folder Connections");
    setIcon(PathTreeNode.ICON_FOLDER_LINK);
    final FileConnectionManager connectionManager = FileConnectionManager.get();
    EventQueue.addPropertyChange(connectionManager, () -> refresh());
  }

  @Override
  public MenuFactory getMenu() {
    return MENU;
  }

  @Override
  protected List<BaseTreeNode> loadChildrenDo() {
    final List<BaseTreeNode> children = new ArrayList<>();
    final List<FolderConnectionRegistry> registries = FileConnectionManager.get()
      .getVisibleConnectionRegistries();
    for (final FolderConnectionRegistry childRegistry : registries) {
      final FolderConnectionRegistryTreeNode child = new FolderConnectionRegistryTreeNode(
        childRegistry);
      children.add(child);
    }
    return children;
  }
}
