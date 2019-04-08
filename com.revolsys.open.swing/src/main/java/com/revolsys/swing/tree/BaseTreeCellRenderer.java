package com.revolsys.swing.tree;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.image.ImageObserver;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;

import org.jeometry.common.awt.WebColors;

import com.revolsys.swing.Icons;
import com.revolsys.swing.tree.node.file.PathTreeNode;
import com.revolsys.util.ToolTipProxy;

public class BaseTreeCellRenderer extends DefaultTreeCellRenderer implements ImageObserver {
  private static final Icon ICON_MISSING = Icons.getIcon("error");

  private static final long serialVersionUID = 1L;

  private final JLabel hiddenRenderer = new JLabel();

  private final Icon loadingIcon = BaseTreeNodeLoadingIcon.getIcon();

  public BaseTreeCellRenderer() {
    setOpenIcon(PathTreeNode.getIconFolder());
    setClosedIcon(PathTreeNode.getIconFolder());
    setBorder(BorderFactory.createEmptyBorder(1, 0, 0, 0));
    final Dimension zeroSize = new Dimension(0, 0);
    this.hiddenRenderer.setMinimumSize(zeroSize);
    this.hiddenRenderer.setMaximumSize(zeroSize);
    this.hiddenRenderer.setPreferredSize(zeroSize);
    this.hiddenRenderer.setSize(zeroSize);
  }

  @Override
  public Component getTreeCellRendererComponent(final JTree tree, final Object value,
    final boolean selected, final boolean expanded, final boolean leaf, final int row,
    final boolean hasFocus) {
    final Component renderer = super.getTreeCellRendererComponent(tree, value, selected, expanded,
      leaf, row, hasFocus);

    if (value instanceof BaseTreeNode) {
      final BaseTreeNode node = (BaseTreeNode)value;
      BaseTreeNodeLoadingIcon.removeNode(node);
      if (node.isVisible()) {
        if (node.isUserObjectInitialized()) {
          if (node.isExists()) {
            return node.getTreeCellRendererComponent(renderer, tree, value, selected, expanded,
              leaf, row, hasFocus);
          } else {
            Icon disabledIcon = node.getDisabledIcon();
            if (disabledIcon == null) {
              disabledIcon = ICON_MISSING;
            }
            setIcon(disabledIcon);
            setForeground(WebColors.Red);

            final Object userData = node.getUserData();
            final JComponent component = (JComponent)renderer;
            if (userData instanceof ToolTipProxy) {
              final ToolTipProxy toolTipProxy = (ToolTipProxy)userData;
              final String toolTip = toolTipProxy.getToolTip();
              component.setToolTipText(toolTip);
            } else {
              component.setToolTipText(null);
            }
          }
        } else {
          setIcon(this.loadingIcon);
          setForeground(WebColors.Gray);
          BaseTreeNodeLoadingIcon.addNode(node);
        }
      } else {
        return this.hiddenRenderer;
      }
    }
    return renderer;
  }
}
