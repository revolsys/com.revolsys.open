package com.revolsys.swing.tree.record;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.JOptionPane;
import javax.swing.tree.TreeNode;

import com.revolsys.data.io.RecordStore;
import com.revolsys.data.io.RecordStoreConnectionMapProxy;
import com.revolsys.data.io.RecordStoreProxy;
import com.revolsys.data.io.RecordStoreSchema;
import com.revolsys.famfamfam.silk.SilkIconLoader;
import com.revolsys.io.datastore.RecordStoreConnection;
import com.revolsys.swing.SwingUtil;
import com.revolsys.swing.menu.MenuFactory;
import com.revolsys.swing.tree.BaseTree;
import com.revolsys.swing.tree.TreeItemPropertyEnableCheck;
import com.revolsys.swing.tree.model.node.LazyLoadTreeNode;

public class RecordStoreConnectionTreeNode extends LazyLoadTreeNode
  implements RecordStoreProxy, RecordStoreConnectionMapProxy {
  public static final Icon ICON = SilkIconLoader.getIcon("database_link");

  private static final MenuFactory MENU = new MenuFactory();

  static {
    final TreeItemPropertyEnableCheck readOnly = new TreeItemPropertyEnableCheck(
      "readOnly", false);
    MENU.addMenuItemTitleIcon("default", "Delete Data Store Connection",
      "delete", readOnly, RecordStoreConnectionTreeNode.class,
      "deleteConnection");
  }

  public static void deleteConnection() {
    final RecordStoreConnectionTreeNode node = BaseTree.getMouseClickItem();
    final RecordStoreConnection connection = node.getConnection();
    final int confirm = JOptionPane.showConfirmDialog(
      SwingUtil.getActiveWindow(), "Delete data store connection '"
        + connection.getName() + "'? This action cannot be undone.",
      "Delete Layer", JOptionPane.OK_CANCEL_OPTION, JOptionPane.ERROR_MESSAGE);
    if (confirm == JOptionPane.OK_OPTION) {
      connection.delete();
    }
  }

  public RecordStoreConnectionTreeNode(final TreeNode parent,
    final RecordStoreConnection connection) {
    super(parent, connection);
    setType("Data Store Connection");
    setName(connection.getName());
    setIcon(ICON);
    setAllowsChildren(true);
  }

  @Override
  protected List<TreeNode> doLoadChildren() {
    final List<TreeNode> children = new ArrayList<TreeNode>();
    final RecordStore recordStore = getDataStore();
    if (recordStore != null) {
      for (final RecordStoreSchema schema : recordStore.getSchemas()) {
        final String schemaPath = schema.getPath();

        final RecordStoreSchemaTreeNode schemaNode = new RecordStoreSchemaTreeNode(
          this, schemaPath);
        children.add(schemaNode);
      }
    }
    return children;
  }

  public RecordStoreConnection getConnection() {
    final RecordStoreConnection connection = getUserData();
    return connection;
  }

  @Override
  @SuppressWarnings("unchecked")
  public <V extends RecordStore> V getDataStore() {
    final RecordStoreConnection connection = getConnection();
    return (V)connection.getDataStore();
  }

  @Override
  public Map<String, Object> getRecordStoreConnectionMap() {
    return Collections.<String, Object> singletonMap("name", getName());
  }

  @Override
  public MenuFactory getMenu() {
    return MENU;
  }

  public boolean isReadOnly() {
    final RecordStoreConnection connection = getConnection();
    return connection.isReadOnly();
  }

}
