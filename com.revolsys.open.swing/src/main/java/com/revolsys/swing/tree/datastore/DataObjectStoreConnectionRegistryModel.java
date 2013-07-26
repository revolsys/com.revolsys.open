package com.revolsys.swing.tree.datastore;

import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.tree.DefaultTreeCellRenderer;

import com.revolsys.famfamfam.silk.SilkIconLoader;
import com.revolsys.io.datastore.DataObjectStoreConnection;
import com.revolsys.io.datastore.DataObjectStoreConnectionRegistry;
import com.revolsys.swing.menu.MenuFactory;
import com.revolsys.swing.tree.ObjectTree;
import com.revolsys.swing.tree.TreeItemPropertyEnableCheck;
import com.revolsys.swing.tree.model.ObjectTreeModel;
import com.revolsys.swing.tree.model.node.AbstractObjectTreeNodeModel;

public class DataObjectStoreConnectionRegistryModel
  extends
  AbstractObjectTreeNodeModel<DataObjectStoreConnectionRegistry, DataObjectStoreConnection> {

  public DataObjectStoreConnectionRegistryModel() {
    setSupportedClasses(DataObjectStoreConnectionRegistry.class);
    setSupportedChildClasses(DataObjectStoreConnection.class);
    setObjectTreeNodeModels(new DataObjectStoreConnectionTreeNodeModel());
    final ImageIcon icon = SilkIconLoader.getIconWithBadge("folder", "database");
    final DefaultTreeCellRenderer renderer = getRenderer();
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
  protected List<DataObjectStoreConnection> getChildren(
    final DataObjectStoreConnectionRegistry connectionRegistry) {
    final List<DataObjectStoreConnection> dataObjectStores = connectionRegistry.getConections();
    return dataObjectStores;
  }

  @Override
  public void initialize(
    final DataObjectStoreConnectionRegistry connectionRegistry) {
    getChildren(connectionRegistry);
  }

  @Override
  public boolean isLeaf(final DataObjectStoreConnectionRegistry node) {
    return false;
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
