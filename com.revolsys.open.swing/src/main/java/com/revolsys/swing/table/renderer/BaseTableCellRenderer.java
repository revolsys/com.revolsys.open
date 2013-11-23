package com.revolsys.swing.table.renderer;

import java.awt.Component;

import javax.swing.JTable;

import org.jdesktop.swingx.renderer.ComponentProvider;
import org.jdesktop.swingx.renderer.DefaultTableRenderer;
import org.jdesktop.swingx.renderer.IconValue;
import org.jdesktop.swingx.renderer.StringValue;

public class BaseTableCellRenderer extends DefaultTableRenderer {

  private int rowHeight;

  public BaseTableCellRenderer() {
    super();
  }

  public BaseTableCellRenderer(final ComponentProvider<?> componentProvider) {
    super(componentProvider);
  }

  public BaseTableCellRenderer(final StringValue converter) {
    super(converter);
  }

  public BaseTableCellRenderer(final StringValue stringValue,
    final IconValue iconValue) {
    super(stringValue, iconValue);
  }

  public BaseTableCellRenderer(final StringValue stringValue,
    final IconValue iconValue, final int alignment) {
    super(stringValue, iconValue, alignment);
  }

  public BaseTableCellRenderer(final StringValue converter, final int alignment) {
    super(converter, alignment);
  }

  @Override
  public Component getTableCellRendererComponent(final JTable table,
    final Object value, final boolean isSelected, final boolean hasFocus,
    final int row, final int columnIndex) {
    final Component component = super.getTableCellRendererComponent(table,
      value, isSelected, hasFocus, row, columnIndex);

    // final int cellSpacingWidth = table.getIntercellSpacing().width;
    // final int cellSpacingHeight = table.getIntercellSpacing().height;
    // final TableColumnModel columnModel = table.getColumnModel();
    // final int columnWidth = columnModel.getColumn(columnIndex).getWidth();
    // component.setMinimumSize(new Dimension(columnWidth - cellSpacingWidth,
    // 1));
    // // component.setBounds(new Rectangle(0, 0, columnWidth -
    // cellSpacingWidth,
    // // Integer.MAX_VALUE));
    // //
    // final int cellHeight = component.getPreferredSize().height
    // + cellSpacingHeight;
    // if (columnIndex == 0) {
    // rowHeight = cellHeight;
    // } else {
    // rowHeight = Math.max(rowHeight, cellHeight);
    // }
    // if (columnIndex == table.getColumnCount() - 1) {
    // if (table.getRowHeight(row) != rowHeight) {
    // // table.setRowHeight(row, rowHeight);
    // }
    // }
    // System.out.println(cellHeight);
    return component;
  }
}
