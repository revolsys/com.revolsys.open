package com.revolsys.swing.tree.node.record;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

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
import com.revolsys.swing.map.form.RecordStoreConnectionForm;
import com.revolsys.swing.menu.MenuFactory;
import com.revolsys.swing.tree.BaseTreeNode;
import com.revolsys.swing.tree.LazyLoadTreeNode;
import com.revolsys.swing.tree.TreeNodes;
import com.revolsys.util.Exceptions;

public class RecordStoreConnectionTreeNode extends LazyLoadTreeNode
  implements RecordStoreProxy, RecordStoreConnectionMapProxy {
  public static final Icon ICON = Icons.getIcon("database_link");

  private static final MenuFactory MENU = new MenuFactory("Record Store Connection");

  static {
    addRefreshMenuItem(MENU);

    final Predicate<RecordStoreConnectionTreeNode> editableFilter = RecordStoreConnectionTreeNode::isEditable;
    TreeNodes.addMenuItem(MENU, "default", "Edit Connection", "database_edit", editableFilter,
      RecordStoreConnectionTreeNode::editConnection);

    TreeNodes.addMenuItem(MENU, "default", "Delete Record Store Connection", "database_delete",
      editableFilter, RecordStoreConnectionTreeNode::deleteConnection);
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
            geometryType = geometryField.getDataType().toString();
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

  private void deleteConnection() {
    final RecordStoreConnection connection = getConnection();
    final int confirm = JOptionPane.showConfirmDialog(SwingUtil.getActiveWindow(),
      "Delete record store connection '" + connection.getName()
        + "'? This action cannot be undone.",
      "Delete Record Store Connection", JOptionPane.OK_CANCEL_OPTION, JOptionPane.ERROR_MESSAGE);
    if (confirm == JOptionPane.OK_OPTION) {
      connection.delete();
    }
  }

  private void editConnection() {
    final RecordStoreConnectionRegistry registry = ((RecordStoreConnectionRegistryTreeNode)getParent())
      .getRegistry();
    final RecordStoreConnection connection = getConnection();
    final RecordStoreConnectionForm form = new RecordStoreConnectionForm(registry, connection);
    form.showDialog();
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

  public boolean isEditable() {
    return !isReadOnly();
  }

  public boolean isReadOnly() {
    final RecordStoreConnection connection = getConnection();
    return connection.isReadOnly();
  }

  @Override
  protected List<BaseTreeNode> loadChildrenDo() {
    try {
      final RecordStore recordStore = getRecordStore();
      return getChildren(getRecordStoreConnectionMap(), recordStore);
    } catch (final Exception e) {
      Exceptions.log(getClass(), "Cannot refresh: " + getName(), e);
      return getChildren();
    }
  }
}
