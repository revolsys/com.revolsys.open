package com.revolsys.swing.tree.node.record;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.swing.Icon;

import com.revolsys.io.PathName;
import com.revolsys.record.io.RecordStoreConnectionMapProxy;
import com.revolsys.record.io.RecordStoreProxy;
import com.revolsys.record.schema.RecordStore;
import com.revolsys.record.schema.RecordStoreSchema;
import com.revolsys.swing.Icons;
import com.revolsys.swing.menu.MenuFactory;
import com.revolsys.swing.tree.BaseTreeNode;
import com.revolsys.swing.tree.LazyLoadTreeNode;
import com.revolsys.swing.tree.node.file.PathTreeNode;
import com.revolsys.util.Property;

public class RecordStoreSchemaTreeNode extends LazyLoadTreeNode
  implements RecordStoreConnectionMapProxy {

  public static final Icon ICON_SCHEMA = Icons.getIconWithBadge(PathTreeNode.ICON_FOLDER, "table");

  private static final MenuFactory MENU = new MenuFactory("Record Store Schema");

  static {
    addRefreshMenuItem(MENU);
  }

  private final Map<String, Object> connectionMap;

  private final PathName schemaPath;

  public RecordStoreSchemaTreeNode(final Map<String, Object> connectionMap,
    final PathName schemaPath) {
    super(schemaPath);
    this.connectionMap = connectionMap;
    setType("Record Store Record Schema");
    setIcon(ICON_SCHEMA);
    this.schemaPath = schemaPath;
    String name = schemaPath.getName();
    if (!Property.hasValue(name)) {
      name = "/";
    }
    setName(name);
  }

  @Override
  public MenuFactory getMenu() {
    return MENU;
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

  @Override
  protected List<BaseTreeNode> loadChildrenDo() {
    final RecordStoreSchema schema = getSchema();
    return RecordStoreConnectionTreeNode.getChildren(this.connectionMap, schema);
  }

}
