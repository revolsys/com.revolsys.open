package com.revolsys.swing.tree.node.record;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import com.revolsys.record.io.RecordStoreConnection;
import com.revolsys.record.io.RecordStoreConnectionRegistry;
import com.revolsys.swing.map.form.RecordStoreConnectionForm;
import com.revolsys.swing.menu.MenuFactory;
import com.revolsys.swing.tree.BaseTreeNode;
import com.revolsys.swing.tree.LazyLoadTreeNode;
import com.revolsys.swing.tree.OpenStateTreeNode;
import com.revolsys.swing.tree.TreeNodes;
import com.revolsys.swing.tree.node.file.PathTreeNode;

public class RecordStoreConnectionRegistryTreeNode extends LazyLoadTreeNode
  implements PropertyChangeListener, OpenStateTreeNode {

  private static final MenuFactory MENU = new MenuFactory("Record Store Connections");

  static {
    addRefreshMenuItem(MENU);

    TreeNodes.addMenuItem(MENU, "default", "Add Connection", "database_add",
      RecordStoreConnectionRegistryTreeNode::addConnection);
  }

  public RecordStoreConnectionRegistryTreeNode(final RecordStoreConnectionRegistry registry) {
    super(registry);
    setType("Record Store Connections");
    setName(registry.getName());
    setIcon(PathTreeNode.ICON_FOLDER_LINK);
    setOpen(true);
  }

  private void addConnection() {
    final RecordStoreConnectionRegistry registry = getRegistry();
    final RecordStoreConnectionForm form = new RecordStoreConnectionForm(registry);
    form.showDialog();
  }

  @Override
  public MenuFactory getMenu() {
    return MENU;
  }

  protected RecordStoreConnectionRegistry getRegistry() {
    final RecordStoreConnectionRegistry registry = getUserData();
    return registry;
  }

  @Override
  protected List<BaseTreeNode> loadChildrenDo() {
    final List<BaseTreeNode> children = new ArrayList<>();
    final RecordStoreConnectionRegistry registry = getRegistry();
    final List<RecordStoreConnection> conections = registry.getConections();
    for (final RecordStoreConnection connection : conections) {
      final RecordStoreConnectionTreeNode child = new RecordStoreConnectionTreeNode(connection);
      children.add(child);
    }
    return children;
  }

  @Override
  public void propertyChangeDo(final PropertyChangeEvent event) {
    final Object source = event.getSource();
    if (source == getRegistry()) {
      final String propertyName = event.getPropertyName();
      if (propertyName.equals("connections")) {
        refresh();
      }
    }
  }
}
