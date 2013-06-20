package com.revolsys.swing.tree.datastore;

import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.tree.DefaultTreeCellRenderer;

import com.revolsys.famfamfam.silk.SilkIconLoader;
import com.revolsys.gis.data.io.DataObjectStore;
import com.revolsys.gis.data.store.ConnectionRegistry;
import com.revolsys.gis.data.store.DataObjectStoreConnectionManager;
import com.revolsys.swing.tree.model.node.AbstractObjectTreeNodeModel;

public class DataObjectStoreConnectionManagerModel
  extends
  AbstractObjectTreeNodeModel<DataObjectStoreConnectionManager, ConnectionRegistry<DataObjectStore>> {

  public DataObjectStoreConnectionManagerModel() {
    setSupportedClasses(DataObjectStoreConnectionManager.class);
    setSupportedChildClasses(ConnectionRegistry.class);
    setObjectTreeNodeModels(new DataObjectStoreConnectionRegistryModel());
    setLazyLoad(true);
    final ImageIcon icon = SilkIconLoader.getIconWidthBadge("folder",
      "database");
    final DefaultTreeCellRenderer renderer = getRenderer();
    renderer.setLeafIcon(icon);
    renderer.setOpenIcon(icon);
    renderer.setClosedIcon(icon);
  }

  @Override
  protected List<ConnectionRegistry<DataObjectStore>> getChildren(
    final DataObjectStoreConnectionManager connectionRegistry) {
    final List<ConnectionRegistry<DataObjectStore>> registries = connectionRegistry.getConnectionRegistries();
    return registries;
  }

  @Override
  public void initialize(
    final DataObjectStoreConnectionManager connectionRegistry) {
    getChildren(connectionRegistry);
  }

}
