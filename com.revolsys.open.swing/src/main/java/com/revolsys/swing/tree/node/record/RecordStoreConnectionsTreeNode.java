package com.revolsys.swing.tree.node.record;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Icon;

import com.revolsys.record.io.RecordStoreConnectionManager;
import com.revolsys.record.io.RecordStoreConnectionRegistry;
import com.revolsys.swing.Icons;
import com.revolsys.swing.tree.BaseTreeNode;
import com.revolsys.swing.tree.LazyLoadTreeNode;
import com.revolsys.swing.tree.node.file.PathTreeNode;

public class RecordStoreConnectionsTreeNode extends LazyLoadTreeNode {
  public static final Icon ICON = Icons.getIconWithBadge(PathTreeNode.ICON_FOLDER, "database");

  public RecordStoreConnectionsTreeNode() {
    super(RecordStoreConnectionManager.get());
    setName("Record Stores");
    setType("Record Stores");
    setIcon(ICON);
  }

  @Override
  protected List<BaseTreeNode> loadChildrenDo() {
    final RecordStoreConnectionManager recordStoreConnectionManager = RecordStoreConnectionManager
      .get();
    final List<BaseTreeNode> children = new ArrayList<>();
    final List<RecordStoreConnectionRegistry> registries = recordStoreConnectionManager
      .getVisibleConnectionRegistries();
    for (final RecordStoreConnectionRegistry registry : registries) {
      final BaseTreeNode child = new RecordStoreConnectionRegistryTreeNode(registry);
      children.add(child);
    }
    return children;
  }

  @Override
  public void propertyChangeDo(final PropertyChangeEvent event) {
    final Object source = event.getSource();
    if (source == getUserObject()) {
      final String propertyName = event.getPropertyName();
      if (propertyName.equals("registries")) {
        refresh();
      }
    }
  }
}
