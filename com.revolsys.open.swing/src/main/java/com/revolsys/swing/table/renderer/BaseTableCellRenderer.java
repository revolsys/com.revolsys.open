package com.revolsys.swing.table.renderer;

import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;

import org.jdesktop.swingx.renderer.ComponentProvider;
import org.jdesktop.swingx.renderer.DefaultTableRenderer;
import org.jdesktop.swingx.renderer.IconValue;
import org.jdesktop.swingx.renderer.StringValue;
import org.jeometry.common.data.type.DataTypes;

public class BaseTableCellRenderer extends DefaultTableRenderer {

  private static final long serialVersionUID = 1L;

  public BaseTableCellRenderer() {
    super();
  }

  public BaseTableCellRenderer(final ComponentProvider<?> componentProvider) {
    super(componentProvider);
  }

  public BaseTableCellRenderer(final StringValue converter) {
    super(converter);
  }

  public BaseTableCellRenderer(final StringValue stringValue, final IconValue iconValue) {
    super(stringValue, iconValue);
  }

  public BaseTableCellRenderer(final StringValue stringValue, final IconValue iconValue,
    final int alignment) {
    super(stringValue, iconValue, alignment);
  }

  public BaseTableCellRenderer(final StringValue converter, final int alignment) {
    super(converter, alignment);
  }

  @Override
  public Component getTableCellRendererComponent(final JTable table, final Object value,
    final boolean isSelected, final boolean hasFocus, final int row, final int columnIndex) {
    final String text = DataTypes.toString(value);
    final Component component = super.getTableCellRendererComponent(table, text, isSelected,
      hasFocus, row, columnIndex);
    if (Number.class.isAssignableFrom(table.getModel().getColumnClass(columnIndex))) {
      if (component instanceof JLabel) {
        final JLabel label = (JLabel)component;
        label.setAlignmentX(Component.RIGHT_ALIGNMENT);
        label.setHorizontalTextPosition(SwingConstants.RIGHT);
        label.setHorizontalAlignment(SwingConstants.RIGHT);
      }
    }
    return component;
  }
}
