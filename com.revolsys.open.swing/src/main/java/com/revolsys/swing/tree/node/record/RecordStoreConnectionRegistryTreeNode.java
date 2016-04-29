package com.revolsys.swing.tree.node.record;

import java.beans.PropertyChangeListener;

import com.revolsys.record.io.RecordStoreConnection;
import com.revolsys.record.io.RecordStoreConnectionRegistry;
import com.revolsys.swing.map.form.RecordStoreConnectionForm;
import com.revolsys.swing.menu.MenuFactory;
import com.revolsys.swing.tree.BaseTreeNode;
import com.revolsys.swing.tree.TreeNodes;
import com.revolsys.swing.tree.node.AbstractConnectionRegistryTreeNode;
import com.revolsys.swing.tree.node.OpenStateTreeNode;
import com.revolsys.swing.tree.node.file.PathTreeNode;

public class RecordStoreConnectionRegistryTreeNode
  extends AbstractConnectionRegistryTreeNode<RecordStoreConnectionRegistry, RecordStoreConnection>
  implements PropertyChangeListener, OpenStateTreeNode {

  static {
    final MenuFactory menu = MenuFactory.getMenu(RecordStoreConnectionRegistry.class);

    TreeNodes.addMenuItem(menu, "default", "Add Connection", "database_add",
      RecordStoreConnectionRegistryTreeNode::addConnection);
  }

  public RecordStoreConnectionRegistryTreeNode(final RecordStoreConnectionRegistry registry) {
    super(PathTreeNode.ICON_FOLDER_LINK, registry);
  }

  private void addConnection() {
    final RecordStoreConnectionRegistry registry = getRegistry();
    final RecordStoreConnectionForm form = new RecordStoreConnectionForm(registry);
    form.showDialog();
  }

  @Override
  protected BaseTreeNode newChildTreeNode(final RecordStoreConnection connection) {
    return new RecordStoreConnectionTreeNode(connection);
  }
}
