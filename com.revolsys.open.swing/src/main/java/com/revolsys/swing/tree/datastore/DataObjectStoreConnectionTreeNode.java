package com.revolsys.swing.tree.datastore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.JOptionPane;
import javax.swing.tree.TreeNode;

import com.revolsys.famfamfam.silk.SilkIconLoader;
import com.revolsys.gis.data.io.DataObjectStore;
import com.revolsys.gis.data.io.DataObjectStoreConnectionMapProxy;
import com.revolsys.gis.data.io.DataObjectStoreProxy;
import com.revolsys.gis.data.io.DataObjectStoreSchema;
import com.revolsys.io.datastore.DataObjectStoreConnection;
import com.revolsys.swing.SwingUtil;
import com.revolsys.swing.menu.MenuFactory;
import com.revolsys.swing.tree.BaseTree;
import com.revolsys.swing.tree.TreeItemPropertyEnableCheck;
import com.revolsys.swing.tree.model.node.LazyLoadTreeNode;

public class DataObjectStoreConnectionTreeNode extends LazyLoadTreeNode
  implements DataObjectStoreProxy, DataObjectStoreConnectionMapProxy {
  public static final Icon ICON = SilkIconLoader.getIcon("database_link");

  private static final MenuFactory MENU = new MenuFactory();

  static {
    final TreeItemPropertyEnableCheck readOnly = new TreeItemPropertyEnableCheck(
      "readOnly", false);
    MENU.addMenuItemTitleIcon("default", "Delete Data Store Connection",
      "delete", readOnly, DataObjectStoreConnectionTreeNode.class,
      "deleteConnection");
  }

  public static void deleteConnection() {
    final DataObjectStoreConnectionTreeNode node = BaseTree.getMouseClickItem();
    final DataObjectStoreConnection connection = node.getConnection();
    final int confirm = JOptionPane.showConfirmDialog(
      SwingUtil.getActiveWindow(), "Delete data store connection '"
        + connection.getName() + "'? This action cannot be undone.",
      "Delete Layer", JOptionPane.OK_CANCEL_OPTION, JOptionPane.ERROR_MESSAGE);
    if (confirm == JOptionPane.OK_OPTION) {
      connection.delete();
    }
  }

  public DataObjectStoreConnectionTreeNode(final TreeNode parent,
    final DataObjectStoreConnection connection) {
    super(parent, connection);
    setType("Data Store Connection");
    setName(connection.getName());
    setIcon(ICON);
    setAllowsChildren(true);
  }

  @Override
  protected List<TreeNode> doLoadChildren() {
    final List<TreeNode> children = new ArrayList<TreeNode>();
    final DataObjectStore dataStore = getDataStore();
    if (dataStore != null) {
      for (final DataObjectStoreSchema schema : dataStore.getSchemas()) {
        final String schemaPath = schema.getPath();

        final DataObjectStoreSchemaTreeNode schemaNode = new DataObjectStoreSchemaTreeNode(
          this, schemaPath);
        children.add(schemaNode);
      }
    }
    return children;
  }

  public DataObjectStoreConnection getConnection() {
    final DataObjectStoreConnection connection = getUserObject();
    return connection;
  }

  @Override
  @SuppressWarnings("unchecked")
  public <V extends DataObjectStore> V getDataStore() {
    final DataObjectStoreConnection connection = getConnection();
    return (V)connection.getDataStore();
  }

  @Override
  public Map<String, Object> getDataStoreConnectionMap() {
    return Collections.<String, Object> singletonMap("name", getName());
  }

  @Override
  public MenuFactory getMenu() {
    return MENU;
  }

  public boolean isReadOnly() {
    final DataObjectStoreConnection connection = getConnection();
    return connection.isReadOnly();
  }

}
