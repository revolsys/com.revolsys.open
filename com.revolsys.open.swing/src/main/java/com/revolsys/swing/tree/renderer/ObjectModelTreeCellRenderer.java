package com.revolsys.swing.tree.renderer;

import java.awt.Component;
import java.awt.Dimension;

import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreePath;

import com.revolsys.famfamfam.silk.SilkIconLoader;
import com.revolsys.swing.tree.model.ObjectTreeModel;
import com.revolsys.swing.tree.model.node.ObjectTreeNodeModel;

public class ObjectModelTreeCellRenderer extends DefaultTreeCellRenderer {
  private static final long serialVersionUID = -4266326319932775964L;

  private final ObjectTreeModel model;

  private final JLabel hiddenRenderer = new JLabel();

  public ObjectModelTreeCellRenderer(final ObjectTreeModel model) {
    this.model = model;
    setOpenIcon(SilkIconLoader.getIcon("folder"));
    setClosedIcon(SilkIconLoader.getIcon("folder"));
    final Dimension zeroSize = new Dimension(0, 0);
    hiddenRenderer.setMinimumSize(zeroSize);
    hiddenRenderer.setMaximumSize(zeroSize);
    hiddenRenderer.setPreferredSize(zeroSize);
    hiddenRenderer.setSize(zeroSize);
  }

  @Override
  public Component getTreeCellRendererComponent(final JTree tree,
    final Object value, final boolean selected, final boolean expanded,
    final boolean leaf, final int row, final boolean hasFocus) {
    if (model.isVisible(value)) {
      final TreePath path = this.model.getPath(value);
      ObjectTreeNodeModel<Object, Object> nodeModel;
      if (path == null) {
        nodeModel = this.model.getNodeModel(value);
      } else {
        nodeModel = this.model.getNodeModel(path);
      }
      Object label = value;
      if (nodeModel != null) {
        final Component renderer = nodeModel.getRenderer(value, tree, selected,
          expanded, leaf, row, hasFocus);
        if (renderer != null) {
          return renderer;
        }
        label = nodeModel.getLabel(value);
      }
      return super.getTreeCellRendererComponent(tree, label, selected,
        expanded, leaf, row, hasFocus);
    } else {
      return hiddenRenderer;
    }
  }
}
