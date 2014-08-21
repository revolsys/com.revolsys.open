package com.revolsys.swing.tree.record;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.JOptionPane;
import javax.swing.tree.TreeNode;

import com.revolsys.data.io.RecordStoreConnectionMapProxy;
import com.revolsys.data.io.RecordStoreProxy;
import com.revolsys.data.io.RecordStoreSchemaElement;
import com.revolsys.data.record.schema.Attribute;
import com.revolsys.data.record.schema.RecordDefinition;
import com.revolsys.data.record.schema.RecordStore;
import com.revolsys.data.record.schema.RecordStoreSchema;
import com.revolsys.famfamfam.silk.SilkIconLoader;
import com.revolsys.io.datastore.RecordStoreConnection;
import com.revolsys.swing.SwingUtil;
import com.revolsys.swing.menu.MenuFactory;
import com.revolsys.swing.tree.BaseTree;
import com.revolsys.swing.tree.TreeItemPropertyEnableCheck;
import com.revolsys.swing.tree.model.node.LazyLoadTreeNode;

public class RecordStoreConnectionTreeNode extends LazyLoadTreeNode implements
RecordStoreProxy, RecordStoreConnectionMapProxy {
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

  public static List<TreeNode> getChildren(final TreeNode parent,
    final Map<String, Object> connectionMap, final RecordStore recordStore) {
    if (recordStore == null) {
      return Collections.emptyList();
    } else {
      final RecordStoreSchema schema = recordStore.getRootSchema();
      return getChildren(parent, connectionMap, schema);
    }
  }

  public static List<TreeNode> getChildren(final TreeNode parent,
    final Map<String, Object> connectionMap, final RecordStoreSchema schema) {
    final List<TreeNode> children = new ArrayList<TreeNode>();
    if (schema != null) {
      for (final RecordStoreSchemaElement element : schema.getElements()) {
        final String path = element.getPath();

        if (element instanceof RecordStoreSchema) {
          final RecordStoreSchemaTreeNode childNode = new RecordStoreSchemaTreeNode(
            parent, connectionMap, path);
          children.add(childNode);
        } else if (element instanceof RecordDefinition) {
          final RecordDefinition recordDefinition = (RecordDefinition)element;
          String geometryType = null;
          final Attribute geometryAttribute = recordDefinition.getGeometryAttribute();
          if (geometryAttribute != null) {
            geometryType = geometryAttribute.getType().toString();
          }
          final RecordStoreTableTreeNode childNode = new RecordStoreTableTreeNode(
            parent, connectionMap, path, geometryType);
          children.add(childNode);
        }
      }
    }
    return children;
  }

  public static final Icon ICON = SilkIconLoader.getIcon("database_link");

  private static final MenuFactory MENU = new MenuFactory();

  static {
    final TreeItemPropertyEnableCheck readOnly = new TreeItemPropertyEnableCheck(
      "readOnly", false);
    MENU.addMenuItemTitleIcon("default", "Delete Data Store Connection",
      "delete", readOnly, RecordStoreConnectionTreeNode.class,
        "deleteConnection");
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
    final RecordStore recordStore = getRecordStore();
    return getChildren(this, getRecordStoreConnectionMap(), recordStore);
  }

  public RecordStoreConnection getConnection() {
    final RecordStoreConnection connection = getUserData();
    return connection;
  }

  @Override
  public MenuFactory getMenu() {
    return MENU;
  }

  @Override
  @SuppressWarnings("unchecked")
  public <V extends RecordStore> V getRecordStore() {
    final RecordStoreConnection connection = getConnection();
    return (V)connection.getRecordStore();
  }

  @Override
  public Map<String, Object> getRecordStoreConnectionMap() {
    return Collections.<String, Object> singletonMap("name", getName());
  }

  public boolean isReadOnly() {
    final RecordStoreConnection connection = getConnection();
    return connection.isReadOnly();
  }

}
