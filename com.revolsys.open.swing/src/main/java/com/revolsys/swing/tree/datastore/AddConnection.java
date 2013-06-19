package com.revolsys.swing.tree.datastore;

import java.awt.event.ActionEvent;

import com.revolsys.famfamfam.silk.SilkIconLoader;
import com.revolsys.gis.data.io.DataObjectStore;
import com.revolsys.gis.data.store.ConnectionRegistry;
import com.revolsys.swing.action.I18nAction;
import com.revolsys.swing.tree.TreeUtil;

public class AddConnection extends I18nAction {
  private static final long serialVersionUID = 2750736040832727823L;

  public AddConnection() {
    super(SilkIconLoader.getIcon("database_add"));
  }

  @Override
  public void actionPerformed(final ActionEvent e) {
    final Object source = e.getSource();
    @SuppressWarnings("unchecked")
    final ConnectionRegistry<DataObjectStore> registry = TreeUtil.getFirstSelectedNode(
      source, ConnectionRegistry.class);
    if (registry != null) {
      new AddDataStoreConnectionPanel(registry);
    }
  }

}
