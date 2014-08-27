package com.revolsys.swing.tree;

import java.awt.Component;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;

import com.revolsys.awt.WebColors;
import com.revolsys.famfamfam.silk.SilkIconLoader;
import com.revolsys.swing.tree.node.BaseTreeNode;

public class BaseTreeCellRenderer extends DefaultTreeCellRenderer {
  private static final ImageIcon ICON_MISSING = SilkIconLoader.getIcon("error");

  private static final long serialVersionUID = 1L;

  public BaseTreeCellRenderer() {
    setBorder(BorderFactory.createEmptyBorder(1, 0, 0, 0));
  }

  @Override
  public Component getTreeCellRendererComponent(final JTree tree,
    final Object value, final boolean selected, final boolean expanded,
    final boolean leaf, final int row, final boolean hasFocus) {
    Component renderer = super.getTreeCellRendererComponent(tree, value,
      selected, expanded, leaf, row, hasFocus);

    if (value instanceof BaseTreeNode) {
      final BaseTreeNode node = (BaseTreeNode)value;
      renderer = node.getTreeCellRendererComponent(renderer, tree, value,
        selected, expanded, leaf, row, hasFocus);

      if (node.isExists()) {
      } else if (!node.isEnabled()) {
        renderer.setEnabled(false);
      } else {
        setIcon(ICON_MISSING);
        setForeground(WebColors.Red);
      }
    }
    return renderer;
  }
}
