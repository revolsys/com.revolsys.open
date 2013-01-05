package com.revolsys.swing.table.object;

import java.awt.Component;

import javax.swing.AbstractCellEditor;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.TableCellEditor;

import com.revolsys.converter.string.StringConverterRegistry;

@SuppressWarnings("serial")
public class ObjectListTableCellEditor extends AbstractCellEditor implements
  TableCellEditor {

  private final JTextField editorComponent = new JTextField();

  @Override
  public Object getCellEditorValue() {
    return editorComponent.getText();
  }

  @Override
  public Component getTableCellEditorComponent(final JTable table,
    final Object value, final boolean isSelected, final int row,
    final int column) {
    if (value == null) {
      editorComponent.setText(null);
    } else {
      editorComponent.setText(StringConverterRegistry.toString(value));
    }
    return editorComponent;
  }
}
