package com.revolsys.swing.tree.datastore;

import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.tree.DefaultTreeCellRenderer;

import com.revolsys.famfamfam.silk.SilkIconLoader;
import com.revolsys.io.datastore.DataObjectStoreConnectionManager;
import com.revolsys.io.datastore.DataObjectStoreConnectionRegistry;
import com.revolsys.swing.tree.model.node.AbstractObjectTreeNodeModel;

public class DataObjectStoreConnectionManagerModel
  extends
  AbstractObjectTreeNodeModel<DataObjectStoreConnectionManager, DataObjectStoreConnectionRegistry> {

  public DataObjectStoreConnectionManagerModel() {
    setSupportedClasses(DataObjectStoreConnectionManager.class);
    setSupportedChildClasses(DataObjectStoreConnectionRegistry.class);
    setObjectTreeNodeModels(new DataObjectStoreConnectionRegistryModel());
    final ImageIcon icon = SilkIconLoader.getIconWithBadge("folder", "database");
    final DefaultTreeCellRenderer renderer = getRenderer();
    renderer.setOpenIcon(icon);
    renderer.setClosedIcon(icon);
  }

  @Override
  protected List<DataObjectStoreConnectionRegistry> getChildren(
    final DataObjectStoreConnectionManager connectionRegistry) {
    final List<DataObjectStoreConnectionRegistry> registries = connectionRegistry.getVisibleConnectionRegistries();
    return registries;
  }

  @Override
  public void initialize(
    final DataObjectStoreConnectionManager connectionRegistry) {
    getChildren(connectionRegistry);
  }

  @Override
  public boolean isLeaf(final DataObjectStoreConnectionManager node) {
    return false;
  }

}
