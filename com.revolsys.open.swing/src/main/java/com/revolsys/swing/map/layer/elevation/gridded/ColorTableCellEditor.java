package com.revolsys.swing.map.layer.elevation.gridded;

import java.awt.Color;
import java.awt.Component;

import javax.swing.AbstractCellEditor;
import javax.swing.JColorChooser;
import javax.swing.JDialog;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;

import com.revolsys.swing.table.AbstractTableModel;

public class ColorTableCellEditor extends AbstractCellEditor implements TableCellEditor {
  private static final long serialVersionUID = 1L;

  private final JColorChooser colorChooser = new JColorChooser();

  @Override
  public void cancelCellEditing() {
    this.colorChooser.setVisible(false);
  }

  @Override
  public Object getCellEditorValue() {
    return this.colorChooser.getColor();
  }

  @Override
  public Component getTableCellEditorComponent(final JTable table, final Object value,
    final boolean isSelected, final int row, final int column) {
    final AbstractTableModel tableModel = (AbstractTableModel)table.getModel();
    this.colorChooser.setColor((Color)value);
    this.colorChooser.setVisible(true);
    final JDialog dialog = JColorChooser.createDialog(table, "Select Color", true,
      this.colorChooser, e -> tableModel.setValueUndo(this.colorChooser.getColor(), row, column),
      null);
    dialog.setVisible(true);
    return null;
  }

  @Override
  public boolean stopCellEditing() {
    this.colorChooser.setVisible(false);
    return true;
  }

}
