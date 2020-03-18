package com.revolsys.swing.map.layer.elevation.gridded;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

public class ColorTableCellRenderer extends DefaultTableCellRenderer implements TableCellRenderer {

  /**
   *
   */
  private static final long serialVersionUID = 1L;

  private Color color;

  @Override
  public Component getTableCellRendererComponent(final JTable table, final Object value,
    final boolean isSelected, final boolean hasFocus, final int row, final int column) {
    this.color = (Color)value;
    return super.getTableCellRendererComponent(table, "", isSelected, hasFocus, row, column);
  }

  @Override
  protected void paintComponent(final Graphics g) {
    super.paintComponent(g);
    g.setColor(this.color);
    g.fillRect(1, 1, getWidth() - 2, getHeight() - 2);
  }
}
