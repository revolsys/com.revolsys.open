package com.revolsys.swing.tree.node.record;

import javax.swing.Icon;

import com.revolsys.record.io.RecordStoreConnectionManager;
import com.revolsys.record.io.RecordStoreConnectionRegistry;
import com.revolsys.swing.Icons;
import com.revolsys.swing.tree.BaseTreeNode;
import com.revolsys.swing.tree.node.AbstractConnectionRegistryManagerTreeNode;
import com.revolsys.swing.tree.node.file.PathTreeNode;

public class RecordStoreConnectionsTreeNode extends
  AbstractConnectionRegistryManagerTreeNode<RecordStoreConnectionManager, RecordStoreConnectionRegistry> {
  public static final Icon ICON = Icons.getIconWithBadge(PathTreeNode.ICON_FOLDER, "database");

  public RecordStoreConnectionsTreeNode() {
    super(RecordStoreConnectionManager.get(), ICON);
    setIcon(ICON);
  }

  @Override
  protected BaseTreeNode newTreeNode(final RecordStoreConnectionRegistry registry) {
    return new RecordStoreConnectionRegistryTreeNode(registry);
  }
}
