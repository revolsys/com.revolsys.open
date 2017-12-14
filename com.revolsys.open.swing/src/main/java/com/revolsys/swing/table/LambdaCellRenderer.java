package com.revolsys.swing.table;

import java.awt.Component;
import java.util.function.Function;

import javax.swing.JTable;

import org.jdesktop.swingx.renderer.DefaultTableRenderer;

public class LambdaCellRenderer<R> extends DefaultTableRenderer {

  private static final long serialVersionUID = 1L;

  private final Function<R, ? extends Object> renderFunction;

  public LambdaCellRenderer(final Function<R, ? extends Object> renderFunction) {
    this.renderFunction = renderFunction;
  }

  @SuppressWarnings("unchecked")
  @Override
  public Component getTableCellRendererComponent(final JTable table, Object value,
    final boolean isSelected, final boolean hasFocus, final int row, final int column) {
    value = this.renderFunction.apply((R)value);
    if (value == null) {
      value = "-";
    }
    return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
  }
}
