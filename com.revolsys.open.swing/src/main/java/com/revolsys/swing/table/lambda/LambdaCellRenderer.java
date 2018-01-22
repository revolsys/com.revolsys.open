package com.revolsys.swing.table.lambda;

import java.awt.Component;

import javax.swing.JTable;

import org.jdesktop.swingx.renderer.DefaultTableRenderer;

public class LambdaCellRenderer<V> extends DefaultTableRenderer {

  private static final long serialVersionUID = 1L;

  private final LambdaStringValue<V> renderFunction;

  public LambdaCellRenderer(final LambdaStringValue<V> renderFunction) {
    super(renderFunction);
    this.renderFunction = renderFunction;
  }

  public LambdaCellRenderer(final LambdaStringValue<V> renderFunction, final int alignment) {
    super(renderFunction, alignment);
    this.renderFunction = renderFunction;
  }

  public LambdaStringValue<V> getRenderFunction() {
    return this.renderFunction;
  }

  @Override
  public Component getTableCellRendererComponent(final JTable table, final Object value,
    final boolean isSelected, final boolean hasFocus, final int row, final int column) {
    // if (value != null && this.renderFunction != null) {
    // value = this.renderFunction.getString(value);
    // }
    // if (value == null) {
    // value = "-";
    // }
    return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
  }
}
