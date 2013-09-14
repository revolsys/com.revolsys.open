package com.revolsys.swing.tree.file;

import java.util.List;

import javax.swing.tree.DefaultTreeCellRenderer;

import com.revolsys.io.file.FolderConnectionManager;
import com.revolsys.io.file.FolderConnectionRegistry;
import com.revolsys.swing.tree.model.node.AbstractObjectTreeNodeModel;

public class FolderConnectionManagerModel
  extends
  AbstractObjectTreeNodeModel<FolderConnectionManager, FolderConnectionRegistry> {

  public FolderConnectionManagerModel() {
    setSupportedClasses(FolderConnectionManager.class);
    setSupportedChildClasses(FolderConnectionRegistry.class);
    setObjectTreeNodeModels(new FolderConnectionRegistryModel());
    final DefaultTreeCellRenderer renderer = getRenderer();
    renderer.setOpenIcon(FileModel.ICON_FOLDER_LINK);
    renderer.setClosedIcon(FileModel.ICON_FOLDER_LINK);
  }

  @Override
  protected List<FolderConnectionRegistry> getChildren(
    final FolderConnectionManager connectionRegistry) {
    final List<FolderConnectionRegistry> registries = connectionRegistry.getVisibleConnectionRegistries();
    return registries;
  }

  @Override
  public void initialize(final FolderConnectionManager connectionRegistry) {
    getChildren(connectionRegistry);
  }

  @Override
  public boolean isLeaf(final FolderConnectionManager node) {
    return false;
  }

}
