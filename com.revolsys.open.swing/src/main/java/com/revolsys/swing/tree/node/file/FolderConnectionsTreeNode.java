package com.revolsys.swing.tree.node.file;

import java.util.ArrayList;
import java.util.List;

import com.revolsys.io.file.FileConnectionManager;
import com.revolsys.io.file.FolderConnectionRegistry;
import com.revolsys.swing.tree.BaseTreeNode;
import com.revolsys.swing.tree.node.AbstractConnectionRegistryManagerTreeNode;

public class FolderConnectionsTreeNode extends
  AbstractConnectionRegistryManagerTreeNode<FileConnectionManager, FolderConnectionRegistry> {

  public FolderConnectionsTreeNode() {
    super(FileConnectionManager.get(), PathTreeNode.ICON_FOLDER_LINK);
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

  @Override
  protected BaseTreeNode newTreeNode(final FolderConnectionRegistry registry) {
    return new FolderConnectionRegistryTreeNode(registry);
  }
}
