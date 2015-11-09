package com.revolsys.swing.table.object;

import java.awt.Component;

import javax.swing.AbstractCellEditor;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.TableCellEditor;

import com.revolsys.converter.string.StringConverter;

public class ObjectListTableCellEditor extends AbstractCellEditor implements TableCellEditor {

  /**
   *
   */
  private static final long serialVersionUID = 1L;

  private final JTextField editorComponent = new JTextField();

  @Override
  public Object getCellEditorValue() {
    return this.editorComponent.getText();
  }

  @Override
  public Component getTableCellEditorComponent(final JTable table, final Object value,
    final boolean isSelected, final int row, final int column) {
    if (value == null) {
      this.editorComponent.setText(null);
    } else {
      this.editorComponent.setText(StringConverter.toString(value));
    }
    return this.editorComponent;
  }
}
