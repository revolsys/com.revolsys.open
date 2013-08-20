package com.revolsys.swing.tree.datastore;

import javax.swing.ImageIcon;
import javax.swing.tree.DefaultTreeCellRenderer;

import com.revolsys.famfamfam.silk.SilkIconLoader;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.gis.data.model.DataObjectMetaDataImpl;
import com.revolsys.swing.menu.MenuFactory;
import com.revolsys.swing.tree.model.ObjectTreeModel;
import com.revolsys.swing.tree.model.node.AbstractObjectTreeNodeModel;

public class DataObjectMetaDataTreeNodeModel extends
  AbstractObjectTreeNodeModel<DataObjectMetaData, Object> {

  static {
    final MenuFactory menu = ObjectTreeModel.getMenu(DataObjectMetaDataImpl.class);
    menu.addMenuItem(new AddLayer());
  }

  public DataObjectMetaDataTreeNodeModel() {
    setLazyLoad(true);
    setSupportedClasses(DataObjectMetaData.class, DataObjectMetaDataImpl.class);
    final ImageIcon icon = SilkIconLoader.getIcon("database_table");
    final DefaultTreeCellRenderer renderer = getRenderer();
    renderer.setLeafIcon(icon);
    renderer.setOpenIcon(icon);
    renderer.setClosedIcon(icon);
  }

  @Override
  public boolean isLeaf(final DataObjectMetaData metaData) {
    return true;
  }
}
