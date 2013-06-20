package com.revolsys.swing.tree.datastore;

import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.tree.DefaultTreeCellRenderer;

import com.revolsys.famfamfam.silk.SilkIconLoader;
import com.revolsys.gis.data.io.DataObjectStore;
import com.revolsys.gis.data.store.ConnectionRegistry;
import com.revolsys.swing.menu.MenuFactory;
import com.revolsys.swing.tree.model.ObjectTreeModel;
import com.revolsys.swing.tree.model.node.AbstractObjectTreeNodeModel;

public class DataObjectStoreConnectionRegistryModel
  extends
  AbstractObjectTreeNodeModel<ConnectionRegistry<DataObjectStore>, DataObjectStore> {

  public DataObjectStoreConnectionRegistryModel() {
    setSupportedClasses(ConnectionRegistry.class);
    setSupportedChildClasses(DataObjectStore.class);
    setObjectTreeNodeModels(new DataObjectStoreTreeNodeModel());
    setLazyLoad(true);
    final ImageIcon icon = SilkIconLoader.getIconWidthBadge("folder",
        "database");
    final DefaultTreeCellRenderer renderer = getRenderer();
    renderer.setLeafIcon(icon);
    renderer.setOpenIcon(icon);
    renderer.setClosedIcon(icon);
  }

  @Override
  protected List<DataObjectStore> getChildren(
    final ConnectionRegistry<DataObjectStore> connectionRegistry) {
    final List<DataObjectStore> dataObjectStores = connectionRegistry.getConections();
    return dataObjectStores;
  }

  @Override
  public void initialize(
    final ConnectionRegistry<DataObjectStore> connectionRegistry) {
    getChildren(connectionRegistry);
  }

  @Override
  public void setObjectTreeModel(final ObjectTreeModel objectTreeModel) {
    super.setObjectTreeModel(objectTreeModel);
    final MenuFactory menu = ObjectTreeModel.getMenu(ConnectionRegistry.class);
    menu.addMenuItem(new AddConnection());
  }
}
