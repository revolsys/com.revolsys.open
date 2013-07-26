package com.revolsys.swing.tree.datastore;

import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.tree.DefaultTreeCellRenderer;

import com.revolsys.famfamfam.silk.SilkIconLoader;
import com.revolsys.gis.data.io.DataObjectStoreSchema;
import com.revolsys.io.datastore.DataObjectStoreConnection;
import com.revolsys.swing.SwingUtil;
import com.revolsys.swing.menu.MenuFactory;
import com.revolsys.swing.tree.ObjectTree;
import com.revolsys.swing.tree.TreeItemPropertyEnableCheck;
import com.revolsys.swing.tree.model.ObjectTreeModel;
import com.revolsys.swing.tree.model.node.AbstractObjectTreeNodeModel;

public class DataObjectStoreConnectionTreeNodeModel extends
  AbstractObjectTreeNodeModel<DataObjectStoreConnection, DataObjectStoreSchema> {
  public DataObjectStoreConnectionTreeNodeModel() {
    setLazyLoad(true);
    setSupportedClasses(DataObjectStoreConnection.class);
    setSupportedChildClasses(DataObjectStoreSchema.class);
    setObjectTreeNodeModels(new DataObjectStoreSchemaTreeNodeModel());
    final ImageIcon icon = SilkIconLoader.getIcon("database_link");
    final DefaultTreeCellRenderer renderer = getRenderer();
    renderer.setLeafIcon(icon);
    renderer.setOpenIcon(icon);
    renderer.setClosedIcon(icon);
  }

  public void deleteConnection() {
    final Object object = ObjectTree.getMouseClickItem();
    if (object instanceof DataObjectStoreConnection) {
      final DataObjectStoreConnection connection = (DataObjectStoreConnection)object;
      final int confirm = JOptionPane.showConfirmDialog(
        SwingUtil.getActiveWindow(), "Delete data store connection '"
          + connection.getName() + "'? This action cannot be undone.",
        "Delete Layer", JOptionPane.OK_CANCEL_OPTION, JOptionPane.ERROR_MESSAGE);
      if (confirm == JOptionPane.OK_OPTION) {
        connection.delete();
      }
    }
  }

  @Override
  protected List<DataObjectStoreSchema> getChildren(
    final DataObjectStoreConnection connection) {
    return connection.getSchemas();
  }

  @Override
  public void initialize(final DataObjectStoreConnection connection) {
    getChildren(connection);
  }

  @Override
  public void setObjectTreeModel(final ObjectTreeModel objectTreeModel) {
    super.setObjectTreeModel(objectTreeModel);
    final MenuFactory menu = ObjectTreeModel.getMenu(DataObjectStoreConnection.class);
    final TreeItemPropertyEnableCheck readOnly = new TreeItemPropertyEnableCheck(
      "readOnly", false);
    menu.addMenuItemTitleIcon("default", "Delete Data Store Connection",
      "delete", readOnly, this, "deleteConnection");
  }
}
