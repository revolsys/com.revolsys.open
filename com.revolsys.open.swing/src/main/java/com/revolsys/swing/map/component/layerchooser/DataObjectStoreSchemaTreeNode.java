package com.revolsys.swing.map.component.layerchooser;

import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.tree.TreeNode;

import org.springframework.util.StringUtils;

import com.revolsys.famfamfam.silk.SilkIconLoader;
import com.revolsys.gis.data.io.DataObjectStore;
import com.revolsys.gis.data.io.DataObjectStoreSchema;
import com.revolsys.gis.data.model.Attribute;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.io.PathUtil;
import com.revolsys.swing.tree.model.node.LazyLoadTreeNode;

public class DataObjectStoreSchemaTreeNode extends LazyLoadTreeNode {

  public static final ImageIcon ICON_SCHEMA = SilkIconLoader.getIcon("folder_table");

  private final String schemaPath;

  public DataObjectStoreSchemaTreeNode(final DataObjectStoreTreeNode parent,
    final String schemaPath) {
    super(schemaPath);
    setAllowsChildren(true);
    setIcon(ICON_SCHEMA);
    setParent(parent);
    this.schemaPath = schemaPath;
    String title = PathUtil.getName(schemaPath);
    if (!StringUtils.hasText(title)) {
      title = "/";
    }
    setTitle(title);
  }

  private DataObjectStore getDataStore() {
    final DataObjectStoreTreeNode parent = getParentNode();
    return parent.getDataStore();
  }

  @Override
  protected List<TreeNode> loadChildren() {
    final List<TreeNode> children = new ArrayList<TreeNode>();
    final DataObjectStore dataStore = getDataStore();
    final DataObjectStoreSchema schema = dataStore.getSchema(schemaPath);
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

    return children;
  }

}
