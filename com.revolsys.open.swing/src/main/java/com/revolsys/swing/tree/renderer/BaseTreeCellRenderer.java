package com.revolsys.swing.tree.renderer;

import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;

import com.revolsys.util.JavaBeanUtil;

public class BaseTreeCellRenderer extends DefaultTreeCellRenderer {

  /**
   * 
   */
  private static final long serialVersionUID = -281281683669758372L;

  private String labelPropertyName;

  public BaseTreeCellRenderer() {
  }

  public BaseTreeCellRenderer(final String labelPropertyName) {
    this.labelPropertyName = labelPropertyName;
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
    return label;
  }

}
