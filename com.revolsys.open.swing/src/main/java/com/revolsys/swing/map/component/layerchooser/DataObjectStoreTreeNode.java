package com.revolsys.swing.map.component.layerchooser;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.tree.TreeNode;

import com.revolsys.gis.data.io.DataObjectStore;
import com.revolsys.gis.data.io.DataObjectStoreFactoryRegistry;
import com.revolsys.gis.data.io.DataObjectStoreSchema;
import com.revolsys.swing.tree.file.FileModel;
import com.revolsys.swing.tree.model.node.LazyLoadTreeNode;

public class DataObjectStoreTreeNode extends LazyLoadTreeNode {

  private final Map<String, Object> config;

  public DataObjectStoreTreeNode(final String name,
    final Map<String, Object> config) {
    super(config);
    setTitle(name);
    setIcon(FileModel.ICON_FILE_DATABASE);
    setAllowsChildren(true);
    this.config = config;
  }

  public DataObjectStore getDataStore() {
    return DataObjectStoreFactoryRegistry.getDataObjectStore(config);
  }

  @Override
  protected List<TreeNode> loadChildren() {
    final List<TreeNode> children = new ArrayList<TreeNode>();
    final DataObjectStore dataStore = getDataStore();
    for (final DataObjectStoreSchema schema : dataStore.getSchemas()) {
      final String schemaPath = schema.getPath();

      final DataObjectStoreSchemaTreeNode schemaNode = new DataObjectStoreSchemaTreeNode(
        this, schemaPath);
      children.add(schemaNode);
    }
    return children;
  }

}
