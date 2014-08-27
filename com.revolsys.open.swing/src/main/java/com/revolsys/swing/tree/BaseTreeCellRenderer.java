package com.revolsys.swing.tree;

import java.awt.Component;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;

import com.revolsys.awt.WebColors;
import com.revolsys.famfamfam.silk.SilkIconLoader;
import com.revolsys.swing.tree.node.BaseTreeNode;
import com.revolsys.swing.tree.node.file.FileTreeNode;

public class BaseTreeCellRenderer extends DefaultTreeCellRenderer {
  private static final ImageIcon ICON_MISSING = SilkIconLoader.getIcon("error");

  private static final long serialVersionUID = 1L;

  private final JLabel hiddenRenderer = new JLabel();

  public BaseTreeCellRenderer() {
    setOpenIcon(FileTreeNode.ICON_FOLDER);
    setClosedIcon(FileTreeNode.ICON_FOLDER);
    setBorder(BorderFactory.createEmptyBorder(1, 0, 0, 0));
    final Dimension zeroSize = new Dimension(0, 0);
    this.hiddenRenderer.setMinimumSize(zeroSize);
    this.hiddenRenderer.setMaximumSize(zeroSize);
    this.hiddenRenderer.setPreferredSize(zeroSize);
    this.hiddenRenderer.setSize(zeroSize);
  }

  @Override
  public Component getTreeCellRendererComponent(final JTree tree,
    final Object value, final boolean selected, final boolean expanded,
    final boolean leaf, final int row, final boolean hasFocus) {
    Component renderer = super.getTreeCellRendererComponent(tree, value,
      selected, expanded, leaf, row, hasFocus);

    if (value instanceof BaseTreeNode) {
      final BaseTreeNode node = (BaseTreeNode)value;
      if (node.isVisible()) {
        renderer = node.getTreeCellRendererComponent(renderer, tree, value,
          selected, expanded, leaf, row, hasFocus);

        if (node.isExists()) {
        } else if (!node.isEnabled()) {
          renderer.setEnabled(false);
        } else {
          setIcon(ICON_MISSING);
          setForeground(WebColors.Red);
        }
      } else {
        return this.hiddenRenderer;
      }
    }
    return renderer;
  }
}
