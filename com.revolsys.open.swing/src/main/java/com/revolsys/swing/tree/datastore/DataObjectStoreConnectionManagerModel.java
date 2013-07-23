package com.revolsys.swing.tree.datastore;

import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.tree.DefaultTreeCellRenderer;

import com.revolsys.famfamfam.silk.SilkIconLoader;
import com.revolsys.gis.data.store.DataObjectStoreConnectionManager;
import com.revolsys.gis.data.store.DataObjectStoreConnectionRegistry;
import com.revolsys.swing.tree.model.node.AbstractObjectTreeNodeModel;

public class DataObjectStoreConnectionManagerModel
  extends
  AbstractObjectTreeNodeModel<DataObjectStoreConnectionManager, DataObjectStoreConnectionRegistry> {

  public DataObjectStoreConnectionManagerModel() {
    setSupportedClasses(DataObjectStoreConnectionManager.class);
    setSupportedChildClasses(DataObjectStoreConnectionRegistry.class);
    setObjectTreeNodeModels(new DataObjectStoreConnectionRegistryModel());
    setLazyLoad(true);
    final ImageIcon icon = SilkIconLoader.getIconWithBadge("folder", "database");
    final DefaultTreeCellRenderer renderer = getRenderer();
    renderer.setLeafIcon(icon);
    renderer.setOpenIcon(icon);
    renderer.setClosedIcon(icon);
  }

  @Override
  protected List<DataObjectStoreConnectionRegistry> getChildren(
    final DataObjectStoreConnectionManager connectionRegistry) {
    final List<DataObjectStoreConnectionRegistry> registries = connectionRegistry.getConnectionRegistries();
    return registries;
  }

  @Override
  public void initialize(
    final DataObjectStoreConnectionManager connectionRegistry) {
    getChildren(connectionRegistry);
  }

}
