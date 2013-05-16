package com.revolsys.swing.tree.datastore;

import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.tree.DefaultTreeCellRenderer;

import com.revolsys.famfamfam.silk.SilkIconLoader;
import com.revolsys.gis.data.io.DataObjectStore;
import com.revolsys.gis.data.io.DataObjectStoreConnections;
import com.revolsys.jdbc.io.JdbcDataObjectStore;
import com.revolsys.swing.menu.MenuFactory;
import com.revolsys.swing.tree.model.ObjectTreeModel;
import com.revolsys.swing.tree.model.node.AbstractObjectTreeNodeModel;

public class DataObjectStoreConnectionsModel extends
  AbstractObjectTreeNodeModel<DataObjectStoreConnections, DataObjectStore> {

  public DataObjectStoreConnectionsModel() {
    setSupportedClasses(DataObjectStoreConnections.class);
    setSupportedChildClasses(JdbcDataObjectStore.class);
    setObjectTreeNodeModels(new DataObjectStoreTreeNodeModel());
    setLazyLoad(true);
    final ImageIcon icon = SilkIconLoader.getIcon("folder_database");
    final DefaultTreeCellRenderer renderer = getRenderer();
    renderer.setLeafIcon(icon);
    renderer.setOpenIcon(icon);
    renderer.setClosedIcon(icon);
   }

  @Override
  public void setObjectTreeModel(ObjectTreeModel objectTreeModel) {
    super.setObjectTreeModel(objectTreeModel);
    final MenuFactory menu = objectTreeModel.getMenu(DataObjectStoreConnections.class);
    menu.addMenuItem(new AddConnection());
  }

  @Override
  protected List<DataObjectStore> getChildren(
    final DataObjectStoreConnections connectionManager) {
    final List<DataObjectStore> dataObjectStores = connectionManager.getDataObjectStores();
    return dataObjectStores;
  }

  @Override
  public void initialize(final DataObjectStoreConnections connectionManager) {
    getChildren(connectionManager);
  }
}
