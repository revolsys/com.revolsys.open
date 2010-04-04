package com.revolsys.jump.ui.model;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Rectangle;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.tree.TreeCellRenderer;

import com.revolsys.jump.ui.style.BasicStyleComponent;
import com.vividsolutions.jts.util.Assert;

/**
 * Implements a {@link TreeCellRenderer}for the {@link
 * com.vividsolutions.jump.workbench.model.Layer Layer} tree. This class may be
 * renamed to LayerRenderer in the future.
 */

public class LayerThemeTreeCellRenderer implements TreeCellRenderer {
  private static final Color SELECTED_FOREGROUND_COLOR = UIManager.getColor("Tree.selectionForeground");

  private TreePanel treePanel = new TreePanel();

  public LayerThemeTreeCellRenderer() {

  }

  public Component getTreeCellRendererComponent(final JTree tree,
    final Object value, final boolean selected, final boolean expanded,
    final boolean leaf, final int row, final boolean hasFocus) {
    if (value instanceof Theme) {
      final Theme theme = (Theme)value;
      if (selected) {
        treePanel.setForeground(SELECTED_FOREGROUND_COLOR);
      } else {
        treePanel.setForeground(tree.getForeground());
      }
      treePanel.setTheme(theme);
      return treePanel;
    } else {
      Assert.shouldNeverReachHere(value.getClass().toString());
      return null;
    }
  }

  public class TreePanel extends JPanel {

    /**
     * 
     */
    private static final long serialVersionUID = -5549042749898164432L;

    private JCheckBox checkBox;

    private GridBagLayout gridBagLayout = new GridBagLayout();

    private JLabel label;

    private BasicStyleComponent stylePanel;

    public TreePanel() {
      setLayout(gridBagLayout);

      setOpaque(false);

      stylePanel = new BasicStyleComponent(new Dimension(10, 10));
      add(stylePanel, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0,
        GridBagConstraints.WEST, GridBagConstraints.NONE,
        new Insets(0, 0, 0, 5), 0, 0));

      checkBox = new JCheckBox();

      add(checkBox, new GridBagConstraints(3, 0, 1, 1, 0.0, 0.0,
        GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0,
          0), 0, 0));

      label = new JLabel();
      label.setOpaque(false);
      add(label, new GridBagConstraints(4, 0, 1, 1, 1.0, 0.0,
        GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0,
          0, 0, 0), 0, 0));
    }

    /**
     * @return relative to this panel
     */
    public Rectangle getCheckBoxBounds() {
      int i = gridBagLayout.getConstraints(checkBox).gridx;
      int x = 0;
      for (int j = 0; j < i; j++) {
        x += getColumnWidth(j);
      }
      return new Rectangle(x, 0, getColumnWidth(i), getRowHeight());
    }

    /**
     * @param i zero-based
     */
    protected int getColumnWidth(final int i) {
      validateTree();
      return gridBagLayout.getLayoutDimensions()[0][i];
    }

    protected int getRowHeight() {
      validateTree();
      return gridBagLayout.getLayoutDimensions()[1][0];
    }

    public void setTheme(final Theme theme) {
      label.setText(theme.getLabel());
      setToolTipText(theme.getLabel());
      checkBox.setSelected(theme.isVisible());
      stylePanel.setBasicStyle(theme.getBasicStyle());
    }
  }

}
