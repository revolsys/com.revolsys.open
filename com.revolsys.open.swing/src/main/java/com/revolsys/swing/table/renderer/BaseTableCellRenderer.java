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
    return component;
  }
}
