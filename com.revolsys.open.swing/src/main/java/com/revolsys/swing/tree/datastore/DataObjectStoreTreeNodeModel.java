package com.revolsys.swing.tree.datastore;

import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.tree.DefaultTreeCellRenderer;

import com.revolsys.famfamfam.silk.SilkIconLoader;
import com.revolsys.gis.data.io.DataObjectStore;
import com.revolsys.gis.data.io.DataObjectStoreSchema;
import com.revolsys.gis.data.io.DelegatingDataObjectStoreHandler;
import com.revolsys.jdbc.io.AbstractJdbcDataObjectStore;
import com.revolsys.jdbc.io.JdbcDataObjectStore;
import com.revolsys.swing.tree.model.node.AbstractObjectTreeNodeModel;

public class DataObjectStoreTreeNodeModel extends
  AbstractObjectTreeNodeModel<DataObjectStore, DataObjectStoreSchema> {
  public DataObjectStoreTreeNodeModel() {
    setLazyLoad(true);
    setSupportedClasses(DataObjectStore.class, JdbcDataObjectStore.class,
      AbstractJdbcDataObjectStore.class, DelegatingDataObjectStoreHandler.class);
    setSupportedChildClasses(DataObjectStoreSchema.class);
    setObjectTreeNodeModels(new DataObjectStoreSchemaTreeNodeModel());
    final ImageIcon icon = SilkIconLoader.getIcon("database_link");
    DefaultTreeCellRenderer renderer = getRenderer();
    renderer.setLeafIcon(icon);
    renderer.setOpenIcon(icon);
    renderer.setClosedIcon(icon);
  }

  @Override
  protected List<DataObjectStoreSchema> getChildren(
    final DataObjectStore dataObjectStore) {
    return dataObjectStore.getSchemas();
  }

  @Override
  public void initialize(final DataObjectStore dataObjectStore) {
    getChildren(dataObjectStore);
  }

}
