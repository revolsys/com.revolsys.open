package com.revolsys.swing.tree.node.record;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.JOptionPane;

import com.revolsys.io.PathName;
import com.revolsys.record.io.RecordStoreConnection;
import com.revolsys.record.io.RecordStoreConnectionMapProxy;
import com.revolsys.record.io.RecordStoreConnectionRegistry;
import com.revolsys.record.io.RecordStoreProxy;
import com.revolsys.record.schema.FieldDefinition;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.record.schema.RecordStore;
import com.revolsys.record.schema.RecordStoreSchema;
import com.revolsys.record.schema.RecordStoreSchemaElement;
import com.revolsys.swing.Icons;
import com.revolsys.swing.SwingUtil;
import com.revolsys.swing.action.InvokeMethodAction;
import com.revolsys.swing.action.enablecheck.EnableCheck;
import com.revolsys.swing.map.form.RecordStoreConnectionDialog;
import com.revolsys.swing.menu.MenuFactory;
import com.revolsys.swing.tree.TreeNodePropertyEnableCheck;
import com.revolsys.swing.tree.TreeNodeRunnable;
import com.revolsys.swing.tree.node.BaseTreeNode;
import com.revolsys.swing.tree.node.LazyLoadTreeNode;
import com.revolsys.util.OS;

public class RecordStoreConnectionTreeNode extends LazyLoadTreeNode
  implements RecordStoreProxy, RecordStoreConnectionMapProxy {
  public static final Icon ICON = Icons.getIcon("database_link");

  private static final MenuFactory MENU = new MenuFactory("Record Store Connection");

  static {
    final EnableCheck notReadOnly = new TreeNodePropertyEnableCheck("readOnly", false);

    final InvokeMethodAction refresh = TreeNodeRunnable.createAction("Refresh", "arrow_refresh",
      "refresh");
    MENU.addMenuItem("default", refresh);
    if (OS.isMac()) {
      MENU.addMenuItem("default", TreeNodeRunnable.createAction("Edit Connection", "database_edit",
        notReadOnly, "editConnection"));
    }
    MENU.addMenuItem("default", TreeNodeRunnable.createAction("Delete Record Store Connection",
      "database_delete", notReadOnly, "deleteConnection"));
  }

  public static List<BaseTreeNode> getChildren(final Map<String, Object> connectionMap,
    final RecordStore recordStore) {
    if (recordStore == null) {
      return Collections.emptyList();
    } else {
      final RecordStoreSchema schema = recordStore.getRootSchema();
      return getChildren(connectionMap, schema);
    }
  }

  public static List<BaseTreeNode> getChildren(final Map<String, Object> connectionMap,
    final RecordStoreSchema schema) {
    final List<BaseTreeNode> children = new ArrayList<>();
    if (schema != null) {
      schema.refresh();
      for (final RecordStoreSchemaElement element : schema.getElements()) {
        final PathName path = element.getPathName();

        if (element instanceof RecordStoreSchema) {
          final RecordStoreSchemaTreeNode childNode = new RecordStoreSchemaTreeNode(connectionMap,
            path);
          children.add(childNode);
        } else if (element instanceof RecordDefinition) {
          final RecordDefinition recordDefinition = (RecordDefinition)element;
          String geometryType = null;
          final FieldDefinition geometryField = recordDefinition.getGeometryField();
          if (geometryField != null) {
            geometryType = geometryField.getType().toString();
          }
          final RecordStoreTableTreeNode childNode = new RecordStoreTableTreeNode(connectionMap,
            path, geometryType);
          children.add(childNode);
        }
      }
    }
    return children;
  }

  public RecordStoreConnectionTreeNode(final RecordStoreConnection connection) {
    super(connection);
    setType("Record Store Connection");
    setName(connection.getName());
    setIcon(ICON);
  }

  public void deleteConnection() {
    final RecordStoreConnection connection = getConnection();
    final int confirm = JOptionPane.showConfirmDialog(SwingUtil.getActiveWindow(),
      "Delete record store connection '" + connection.getName()
        + "'? This action cannot be undone.",
      "Delete Record Store Connection", JOptionPane.OK_CANCEL_OPTION, JOptionPane.ERROR_MESSAGE);
    if (confirm == JOptionPane.OK_OPTION) {
      connection.delete();
    }
  }

  @Override
  protected List<BaseTreeNode> doLoadChildren() {
    final RecordStore recordStore = getRecordStore();
    return getChildren(getRecordStoreConnectionMap(), recordStore);
  }

  public void editConnection() {
    final RecordStoreConnectionRegistry registry = ((RecordStoreConnectionRegistryTreeNode)getParent())
      .getRegistry();
    final RecordStoreConnection connection = getConnection();
    final RecordStoreConnectionDialog dialog = new RecordStoreConnectionDialog(registry,
      connection);
    dialog.setVisible(true);
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
