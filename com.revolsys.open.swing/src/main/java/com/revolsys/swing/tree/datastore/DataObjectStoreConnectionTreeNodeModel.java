package com.revolsys.swing.tree.datastore;

import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.tree.DefaultTreeCellRenderer;

import com.revolsys.famfamfam.silk.SilkIconLoader;
import com.revolsys.gis.data.io.DataObjectStoreSchema;
import com.revolsys.gis.data.store.DataObjectStoreConnection;
import com.revolsys.swing.tree.model.node.AbstractObjectTreeNodeModel;

public class DataObjectStoreConnectionTreeNodeModel extends
  AbstractObjectTreeNodeModel<DataObjectStoreConnection, DataObjectStoreSchema> {
  public DataObjectStoreConnectionTreeNodeModel() {
    setLazyLoad(true);
    setSupportedClasses(DataObjectStoreConnection.class);
    setSupportedChildClasses(DataObjectStoreSchema.class);
    setObjectTreeNodeModels(new DataObjectStoreSchemaTreeNodeModel());
    final ImageIcon icon = SilkIconLoader.getIcon("database_link");
    final DefaultTreeCellRenderer renderer = getRenderer();
    renderer.setLeafIcon(icon);
    renderer.setOpenIcon(icon);
    renderer.setClosedIcon(icon);
  }

  @Override
  protected List<DataObjectStoreSchema> getChildren(
    final DataObjectStoreConnection connection) {
    return connection.getSchemas();
  }

  @Override
  public void initialize(final DataObjectStoreConnection connection) {
    getChildren(connection);
  }

}
