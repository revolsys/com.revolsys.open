package com.revolsys.swing.tree.datastore;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JPanel;
import javax.swing.JScrollPane;

import com.revolsys.gis.data.io.DataObjectStore;
import com.revolsys.gis.data.io.DataObjectStoreSchema;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectMetaDataImpl;
import com.revolsys.jdbc.io.JdbcDataObjectStore;
import com.revolsys.swing.tree.ObjectTree;
import com.revolsys.swing.tree.model.ObjectTreeModel;

public class DataObjectStoreTreePanel<T extends DataObject> extends JPanel {
  /**
   * 
   */
  private static final long serialVersionUID = -8596307058450127838L;

  public DataObjectStoreTreePanel(final DataObjectStore dataObjectStore) {
    final ObjectTreeModel model = new ObjectTreeModel(dataObjectStore);
    model.addNodeModel(DataObjectStore.class,
      new DataObjectStoreTreeNodeModel());
    model.addNodeModel(JdbcDataObjectStore.class,
      new DataObjectStoreTreeNodeModel());
    model.addNodeModel(DataObjectStoreSchema.class,
      new DataObjectStoreSchemaTreeNodeModel());
    model.addNodeModel(DataObjectMetaDataImpl.class,
      new DataObjectMetaDataTreeNodeModel());

    final ObjectTree tree = new ObjectTree(model);

    setLayout(new BorderLayout());
    final JScrollPane scrollPane = new JScrollPane(tree);
    scrollPane.setPreferredSize(new Dimension(200, 600));
    add(scrollPane, BorderLayout.CENTER);

  }
}
