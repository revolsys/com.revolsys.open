package com.revolsys.swing.table;

import java.awt.Component;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;

public class NumberTableCellRenderer extends DefaultTableCellRenderer {

  /**
   *
   */
  private static final long serialVersionUID = 1L;

  private final String format;

  public NumberTableCellRenderer() {
    this("#,###");
  }

  public NumberTableCellRenderer(final String format) {
    this.format = format;
  }

  private NumberFormat getFormat() {
    return new DecimalFormat(this.format);
  }

  @Override
  public Component getTableCellRendererComponent(final JTable table, Object value,
    final boolean isSelected, final boolean hasFocus, final int row, final int column) {
    if (value instanceof Number) {
      final Number number = (Number)value;
      value = getFormat().format(number);
    }
    final Component label = super.getTableCellRendererComponent(table, value, isSelected, hasFocus,
      row, column);
    setHorizontalAlignment(SwingConstants.RIGHT);
    return label;
  }
}
