package com.revolsys.swing.tree.datastore;

import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.tree.DefaultTreeCellRenderer;

import com.revolsys.famfamfam.silk.SilkIconLoader;
import com.revolsys.gis.data.io.DataObjectStore;
import com.revolsys.gis.data.store.ConnectionRegistry;
import com.revolsys.gis.data.store.DataObjectStoreConnectionRegistry;
import com.revolsys.swing.menu.MenuFactory;
import com.revolsys.swing.tree.ObjectTree;
import com.revolsys.swing.tree.TreeItemPropertyEnableCheck;
import com.revolsys.swing.tree.model.ObjectTreeModel;
import com.revolsys.swing.tree.model.node.AbstractObjectTreeNodeModel;

public class DataObjectStoreConnectionRegistryModel
  extends
  AbstractObjectTreeNodeModel<ConnectionRegistry<DataObjectStore>, DataObjectStore> {

  public DataObjectStoreConnectionRegistryModel() {
    setSupportedClasses(DataObjectStoreConnectionRegistry.class);
    setSupportedChildClasses(DataObjectStore.class);
    setObjectTreeNodeModels(new DataObjectStoreConnectionTreeNodeModel());
    setLazyLoad(true);
    final ImageIcon icon = SilkIconLoader.getIconWithBadge("folder", "database");
    final DefaultTreeCellRenderer renderer = getRenderer();
    renderer.setLeafIcon(icon);
    renderer.setOpenIcon(icon);
    renderer.setClosedIcon(icon);
  }

  public void addConnection() {
    final Object object = ObjectTree.getMouseClickItem();
    if (object instanceof DataObjectStoreConnectionRegistry) {
      final DataObjectStoreConnectionRegistry registry = (DataObjectStoreConnectionRegistry)object;
      new AddDataStoreConnectionPanel(registry).showDialog();
    }

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
    final MenuFactory menu = ObjectTreeModel.getMenu(DataObjectStoreConnectionRegistry.class);
    final TreeItemPropertyEnableCheck readOnly = new TreeItemPropertyEnableCheck(
      "readOnly", false);
    menu.addMenuItemTitleIcon("default", "Add Data Store Connection", "add",
      readOnly, this, "addConnection");
  }
}
