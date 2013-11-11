package com.revolsys.swing.tree.renderer;

import java.awt.Component;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;

import com.revolsys.swing.tree.model.node.AbstractTreeNode;
import com.revolsys.util.JavaBeanUtil;

public class BaseTreeCellRenderer extends DefaultTreeCellRenderer {
  private static final long serialVersionUID = 1L;

  private final String labelPropertyName;

  public BaseTreeCellRenderer() {
    this(null);
  }

  public BaseTreeCellRenderer(final String labelPropertyName) {
    this.labelPropertyName = labelPropertyName;
    setBorder(BorderFactory.createEmptyBorder(1, 0, 0, 0));
  }

  @Override
  public Component getTreeCellRendererComponent(final JTree tree,
    final Object value, final boolean selected, final boolean expanded,
    final boolean leaf, final int row, final boolean hasFocus) {
    final JLabel label = (JLabel)super.getTreeCellRendererComponent(tree,
      value, selected, expanded, leaf, row, hasFocus);
    if (this.labelPropertyName != null) {
      final String text = JavaBeanUtil.getProperty(value,
        this.labelPropertyName);
      label.setText(text);
    }
    if (value instanceof AbstractTreeNode) {
      final AbstractTreeNode node = (AbstractTreeNode)value;
      final Icon icon = node.getIcon();
      if (icon != null) {
        setIcon(icon);
      }
    }
    return label;
  }

}
