package com.revolsys.swing.tree.renderer;

import java.awt.Component;

import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreePath;

import com.revolsys.swing.tree.model.ObjectTreeModel;
import com.revolsys.swing.tree.model.node.ObjectTreeNodeModel;

public class ObjectModelTreeCellRenderer extends DefaultTreeCellRenderer {

  /**
   * 
   */
  private static final long serialVersionUID = -4266326319932775964L;

  private final ObjectTreeModel model;

  public ObjectModelTreeCellRenderer(final ObjectTreeModel model) {
    this.model = model;
  }

  @Override
  public Component getTreeCellRendererComponent(
    final JTree tree,
    final Object value,
    final boolean selected,
    final boolean expanded,
    final boolean leaf,
    final int row,
    final boolean hasFocus) {
    final TreePath path = model.getPath(value);
    ObjectTreeNodeModel<Object, Object> nodeModel;
    if (path == null) {
      nodeModel = model.getNodeModel(value);
    } else {
      nodeModel = model.getNodeModel(path);
    }
    if (nodeModel != null) {
      final TreeCellRenderer renderer = nodeModel.getRenderer(value);
      if (renderer != null) {
        return renderer.getTreeCellRendererComponent(tree, value, selected,
          expanded, leaf, row, hasFocus);
      }
    }
    return super.getTreeCellRendererComponent(tree, value, selected, expanded,
      leaf, row, hasFocus);
  }

}
