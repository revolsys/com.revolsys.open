package com.revolsys.swing.tree.record;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.swing.ImageIcon;
import javax.swing.tree.TreeNode;

import org.springframework.util.StringUtils;

import com.revolsys.data.io.RecordStore;
import com.revolsys.data.io.RecordStoreConnectionMapProxy;
import com.revolsys.data.io.RecordStoreProxy;
import com.revolsys.data.io.RecordStoreSchema;
import com.revolsys.data.record.schema.Attribute;
import com.revolsys.data.record.schema.RecordDefinition;
import com.revolsys.famfamfam.silk.SilkIconLoader;
import com.revolsys.io.PathUtil;
import com.revolsys.swing.tree.model.node.LazyLoadTreeNode;

public class RecordStoreSchemaTreeNode extends LazyLoadTreeNode implements
  RecordStoreConnectionMapProxy {

  public static final ImageIcon ICON_SCHEMA = SilkIconLoader.getIcon("folder_table");

  private final String schemaPath;

  public RecordStoreSchemaTreeNode(final TreeNode parent,
    final String schemaPath) {
    super(parent, schemaPath);
    setType("Data Store RecordDefinition");
    setAllowsChildren(true);
    setIcon(ICON_SCHEMA);
    setParent(parent);
    this.schemaPath = schemaPath;
    String name = PathUtil.getName(schemaPath);
    if (!StringUtils.hasText(name)) {
      name = "/";
    }
    setName(name);
  }

  @Override
  protected List<TreeNode> doLoadChildren() {
    final List<TreeNode> children = new ArrayList<TreeNode>();
    final RecordStore dataStore = getRecordStore();
    if (dataStore != null) {
      final RecordStoreSchema schema = dataStore.getSchema(schemaPath);
      if (schema != null) {
        for (final RecordDefinition metaData : schema.getTypes()) {
          final String typeName = metaData.getPath();
          String geometryType = null;
          final Attribute geometryAttribute = metaData.getGeometryAttribute();
          if (geometryAttribute != null) {
            geometryType = geometryAttribute.getType().toString();
          }
          final RecordStoreTableTreeNode tableNode = new RecordStoreTableTreeNode(
            this, typeName, geometryType);
          children.add(tableNode);
        }
      }
    }
    return children;
  }

  public RecordStore getRecordStore() {
    final TreeNode parent = getParentNode();
    if (parent instanceof RecordStoreProxy) {
      final RecordStoreProxy proxy = (RecordStoreProxy)parent;
      return proxy.getDataStore();
    } else {
      return null;
    }
  }

  @Override
  public Map<String, Object> getRecordStoreConnectionMap() {
    final TreeNode parent = getParent();
    if (parent instanceof RecordStoreConnectionMapProxy) {
      final RecordStoreConnectionMapProxy proxy = (RecordStoreConnectionMapProxy)parent;
      return proxy.getRecordStoreConnectionMap();
    } else {
      return Collections.emptyMap();
    }
  }

}
