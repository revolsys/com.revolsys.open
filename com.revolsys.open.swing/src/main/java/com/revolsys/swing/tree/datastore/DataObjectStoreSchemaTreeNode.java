package com.revolsys.swing.tree.datastore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.swing.ImageIcon;
import javax.swing.tree.TreeNode;

import org.springframework.util.StringUtils;

import com.revolsys.famfamfam.silk.SilkIconLoader;
import com.revolsys.gis.data.io.DataObjectStore;
import com.revolsys.gis.data.io.DataObjectStoreConnectionMapProxy;
import com.revolsys.gis.data.io.DataObjectStoreProxy;
import com.revolsys.gis.data.io.DataObjectStoreSchema;
import com.revolsys.gis.data.model.Attribute;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.io.PathUtil;
import com.revolsys.swing.tree.model.node.LazyLoadTreeNode;

public class DataObjectStoreSchemaTreeNode extends LazyLoadTreeNode implements
  DataObjectStoreConnectionMapProxy {

  public static final ImageIcon ICON_SCHEMA = SilkIconLoader.getIcon("folder_table");

  private final String schemaPath;

  public DataObjectStoreSchemaTreeNode(final TreeNode parent,
    final String schemaPath) {
    super(parent, schemaPath);
    setType("Data Store Schema");
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
    final DataObjectStore dataStore = getDataStore();
    if (dataStore != null) {
      final DataObjectStoreSchema schema = dataStore.getSchema(schemaPath);
      if (schema != null) {
        for (final DataObjectMetaData metaData : schema.getTypes()) {
          final String typeName = metaData.getPath();
          String geometryType = null;
          final Attribute geometryAttribute = metaData.getGeometryAttribute();
          if (geometryAttribute != null) {
            geometryType = geometryAttribute.getType().toString();
          }
          final DataObjectStoreTableTreeNode tableNode = new DataObjectStoreTableTreeNode(
            this, typeName, geometryType);
          children.add(tableNode);
        }
      }
    }
    return children;
  }

  public DataObjectStore getDataStore() {
    final TreeNode parent = getParentNode();
    if (parent instanceof DataObjectStoreProxy) {
      final DataObjectStoreProxy proxy = (DataObjectStoreProxy)parent;
      return proxy.getDataStore();
    } else {
      return null;
    }
  }

  @Override
  public Map<String, Object> getDataStoreConnection() {
    final TreeNode parent = getParent();
    if (parent instanceof DataObjectStoreConnectionMapProxy) {
      final DataObjectStoreConnectionMapProxy proxy = (DataObjectStoreConnectionMapProxy)parent;
      return proxy.getDataStoreConnection();
    } else {
      return Collections.emptyMap();
    }
  }

}
