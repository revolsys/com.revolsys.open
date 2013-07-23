package com.revolsys.swing.tree.datastore;

import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.tree.DefaultTreeCellRenderer;

import com.revolsys.famfamfam.silk.SilkIconLoader;
import com.revolsys.gis.data.store.ConnectionRegistry;
import com.revolsys.gis.data.store.DataObjectStoreConnection;
import com.revolsys.gis.data.store.DataObjectStoreConnectionManager;
import com.revolsys.swing.tree.model.node.AbstractObjectTreeNodeModel;

public class DataObjectStoreConnectionManagerModel
  extends
  AbstractObjectTreeNodeModel<DataObjectStoreConnectionManager, ConnectionRegistry<DataObjectStoreConnection>> {

  public DataObjectStoreConnectionManagerModel() {
    setSupportedClasses(DataObjectStoreConnectionManager.class);
    setSupportedChildClasses(ConnectionRegistry.class);
    setObjectTreeNodeModels(new DataObjectStoreConnectionRegistryModel());
    setLazyLoad(true);
    final ImageIcon icon = SilkIconLoader.getIconWithBadge("folder",
      "database");
    final DefaultTreeCellRenderer renderer = getRenderer();
    renderer.setLeafIcon(icon);
    renderer.setOpenIcon(icon);
    renderer.setClosedIcon(icon);
  }

  @Override
  protected List<ConnectionRegistry<DataObjectStoreConnection>> getChildren(
    final DataObjectStoreConnectionManager connectionRegistry) {
    final List<ConnectionRegistry<DataObjectStoreConnection>> registries = connectionRegistry.getConnectionRegistries();
    return registries;
  }

  @Override
  public void initialize(
    final DataObjectStoreConnectionManager connectionRegistry) {
    getChildren(connectionRegistry);
  }

}
