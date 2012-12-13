package com.revolsys.swing.table.object;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.border.EmptyBorder;
import javax.swing.table.TableCellRenderer;

import com.revolsys.converter.string.StringConverterRegistry;

public class ObjectListTableCellRenderer implements TableCellRenderer {
  private final JLabel valueComponent;

  public ObjectListTableCellRenderer() {
    valueComponent = new JLabel();
    valueComponent.setBorder(new EmptyBorder(1, 2, 1, 2));
    valueComponent.setOpaque(true);
  }

  @Override
  public Component getTableCellRendererComponent(final JTable table,
    final Object value, final boolean isSelected, final boolean hasFocus,
    final int row, final int column) {
    Component component = null;

  
    if (component == null) {
      String text;
      if (value == null) {
        text = "-";
      } else {
        text = StringConverterRegistry.toString(value);
      }
      valueComponent.setText(text);
      component = valueComponent;
    }
    final int[] selectedRows = table.getSelectedRows();
    boolean selected = false;
    for (final int selectedRow : selectedRows) {
      if (row == selectedRow) {
        selected = true;
      }
    }
    if (selected) {
      component.setBackground(table.getSelectionBackground());
      component.setForeground(table.getSelectionForeground());
    } else if (row % 2 == 0) {
      component.setBackground(Color.WHITE);
      component.setForeground(table.getForeground());
    } else {
      component.setBackground(Color.LIGHT_GRAY);
      component.setForeground(table.getForeground());
    }
    return component;
  }
}
