package com.revolsys.swing.tree.node.record;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import com.revolsys.data.io.RecordStoreConnectionMapProxy;
import com.revolsys.data.io.RecordStoreProxy;
import com.revolsys.data.record.schema.RecordStore;
import com.revolsys.data.record.schema.RecordStoreSchema;
import com.revolsys.io.Path;
import com.revolsys.swing.Icons;
import com.revolsys.swing.action.InvokeMethodAction;
import com.revolsys.swing.menu.MenuFactory;
import com.revolsys.swing.tree.TreeNodeRunnable;
import com.revolsys.swing.tree.node.BaseTreeNode;
import com.revolsys.swing.tree.node.LazyLoadTreeNode;
import com.revolsys.util.Property;

public class RecordStoreSchemaTreeNode extends LazyLoadTreeNode implements
  RecordStoreConnectionMapProxy {

  public static final ImageIcon ICON_SCHEMA = Icons.getIcon("folder_table");

  public static final ImageIcon ICON_SCHEMA_OPEN = Icons.getIcon("folder_table_open");

  private static final MenuFactory MENU = new MenuFactory("Record Store Schema");

  static {
    final InvokeMethodAction refresh = TreeNodeRunnable.createAction("Refresh",
      "arrow_refresh", "refresh");
    MENU.addMenuItem("default", refresh);
  }

  private final Map<String, Object> connectionMap;

  private final String schemaPath;

  public RecordStoreSchemaTreeNode(final Map<String, Object> connectionMap,
    final String schemaPath) {
    super(schemaPath);
    this.connectionMap = connectionMap;
    setType("Record Store Record Schema");
    setIcon(ICON_SCHEMA);
    this.schemaPath = schemaPath;
    String name = Path.getName(schemaPath);
    if (!Property.hasValue(name)) {
      name = "/";
    }
    setName(name);
  }

  @Override
  protected List<BaseTreeNode> doLoadChildren() {
    final RecordStoreSchema schema = getSchema();
    return RecordStoreConnectionTreeNode.getChildren(this.connectionMap, schema);
  }

  @Override
  public MenuFactory getMenu() {
    return MENU;
  }

  @Override
  public Icon getOpenIcon() {
    return ICON_SCHEMA_OPEN;
  }

  public RecordStore getRecordStore() {
    final BaseTreeNode parent = getParentNode();
    if (parent instanceof RecordStoreProxy) {
      final RecordStoreProxy proxy = (RecordStoreProxy)parent;
      return proxy.getRecordStore();
    } else {
      return null;
    }
  }

  @Override
  public Map<String, Object> getRecordStoreConnectionMap() {
    final BaseTreeNode parent = getParent();
    if (parent instanceof RecordStoreConnectionMapProxy) {
      final RecordStoreConnectionMapProxy proxy = (RecordStoreConnectionMapProxy)parent;
      return proxy.getRecordStoreConnectionMap();
    } else {
      return Collections.emptyMap();
    }
  }

  public RecordStoreSchema getSchema() {
    final RecordStore recordStore = getRecordStore();
    if (recordStore == null) {
      return null;
    } else {
      final RecordStoreSchema schema = recordStore.getSchema(this.schemaPath);
      return schema;
    }
  }

}
