package com.revolsys.swing.tree.datastore;

import java.awt.event.MouseListener;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.ImageIcon;
import javax.swing.tree.DefaultTreeCellRenderer;

import com.revolsys.famfamfam.silk.SilkIconLoader;
import com.revolsys.gis.data.io.DataObjectStoreSchema;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.swing.menu.MenuFactory;
import com.revolsys.swing.tree.model.node.AbstractObjectTreeNodeModel;

public class DataObjectStoreSchemaTreeNodeModel extends
  AbstractObjectTreeNodeModel<DataObjectStoreSchema, DataObjectMetaData> {
  private final MenuFactory menu = new MenuFactory();

  public DataObjectStoreSchemaTreeNodeModel() {
    setSupportedClasses(DataObjectStoreSchema.class);
    setSupportedChildClasses(DataObjectMetaData.class);
    setObjectTreeNodeModels(new DataObjectMetaDataTreeNodeModel());
    setLazyLoad(true);
    final DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer();
    setRenderer(renderer);
    final ImageIcon icon = SilkIconLoader.getIconWithBadge("folder", "database");
    renderer.setLeafIcon(icon);
    renderer.setOpenIcon(icon);
    renderer.setClosedIcon(icon);
  }

  @Override
  public int addChild(final DataObjectStoreSchema schema,
    final DataObjectMetaData child) {
    return -1;
  }

  @Override
  public int addChild(final DataObjectStoreSchema schema, final int index,
    final DataObjectMetaData child) {
    return -1;
  }

  @Override
  public DataObjectMetaData getChild(final DataObjectStoreSchema schema,
    final int index) {
    final List<DataObjectMetaData> types = schema.getTypes();
    return types.get(index);
  }

  @Override
  public int getChildCount(final DataObjectStoreSchema schema) {
    final List<DataObjectMetaData> types = schema.getTypes();
    return types.size();
  }

  @Override
  public int getIndexOfChild(final DataObjectStoreSchema schema,
    final DataObjectMetaData child) {
    final List<DataObjectMetaData> types = schema.getTypes();
    return types.indexOf(child);
  }

  @Override
  public MenuFactory getMenu(final DataObjectStoreSchema schema) {
    return this.menu;
  }

  @Override
  public MouseListener getMouseListener(final DataObjectStoreSchema schema) {
    return null;
  }

  @Override
  public Set<Class<?>> getSupportedChildClasses() {
    final HashSet<Class<?>> classes = new HashSet<Class<?>>();
    classes.add(DataObjectMetaData.class);
    return classes;
  }

  @Override
  public void initialize(final DataObjectStoreSchema schema) {
    schema.getTypes();
  }

  @Override
  public boolean isLeaf(final DataObjectStoreSchema schema) {
    return false;
  }

  @Override
  public boolean removeChild(final DataObjectStoreSchema schema,
    final DataObjectMetaData child) {
    return false;
  }
}
