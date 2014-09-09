package com.revolsys.swing.tree;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.image.ImageObserver;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;

import com.revolsys.awt.WebColors;
import com.revolsys.swing.Icons;
import com.revolsys.swing.tree.node.BaseTreeNode;
import com.revolsys.swing.tree.node.file.FileTreeNode;

public class BaseTreeCellRenderer extends DefaultTreeCellRenderer implements
ImageObserver {
  private static final ImageIcon ICON_MISSING = Icons.getIcon("error");

  private static final long serialVersionUID = 1L;

  private final JLabel hiddenRenderer = new JLabel();

  private final BaseTreeNodeLoadingIcon loading = new BaseTreeNodeLoadingIcon();

  private final Icon loadingIcon = this.loading.getIcon();

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
    final Component renderer = super.getTreeCellRendererComponent(tree, value,
      selected, expanded, leaf, row, hasFocus);

    if (value instanceof BaseTreeNode) {
      final BaseTreeNode node = (BaseTreeNode)value;
      this.loading.removeNode(node);
      if (node.isVisible()) {
        if (node.isUserObjectInitialized()) {

          if (node.isExists()) {
            return node.getTreeCellRendererComponent(renderer, tree, value,
              selected, expanded, leaf, row, hasFocus);
          } else {
            setIcon(ICON_MISSING);
            setForeground(WebColors.Red);
          }
        } else {
          setIcon(this.loadingIcon);
          setForeground(WebColors.Gray);
          this.loading.addNode(node);
        }
      } else {
        return this.hiddenRenderer;
      }
    }
    return renderer;
  }
}
