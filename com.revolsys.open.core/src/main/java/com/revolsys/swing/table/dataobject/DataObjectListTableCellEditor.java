package com.revolsys.swing.table.dataobject;

import java.awt.Component;

import javax.swing.AbstractCellEditor;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.TableCellEditor;

import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.swing.builder.DataObjectMetaDataUiBuilderRegistry;
import com.revolsys.swing.builder.ValueUiBuilder;

@SuppressWarnings("serial")
public class DataObjectListTableCellEditor extends AbstractCellEditor
  implements TableCellEditor {
  private final DataObjectMetaDataUiBuilderRegistry uiBuilderRegistry;

  private final JTextField editorComponent = new JTextField();

  private ValueUiBuilder uiBuilder;

  public DataObjectListTableCellEditor(
    final DataObjectMetaDataUiBuilderRegistry uiBuilderRegistry) {
    this.uiBuilderRegistry = uiBuilderRegistry;
  }

  @Override
  public Object getCellEditorValue() {
    if (uiBuilder != null) {
      return uiBuilder.getCellEditorValue();
    } else {
      return editorComponent.getText();
    }
  }

  @Override
  public Component getTableCellEditorComponent(
    final JTable table,
    final Object value,
    final boolean isSelected,
    final int row,
    final int column) {
    final DataObjectListTableModel model = (DataObjectListTableModel)table.getModel();
    final DataObjectMetaData schema = model.getMetaData();
    uiBuilder = uiBuilderRegistry.getValueUiBuilder(schema, column);
    if (uiBuilder != null) {
      return uiBuilder.getEditorComponent(value);
    } else if (value == null) {
      editorComponent.setText(null);
    } else {
      editorComponent.setText(value.toString());
    }
    return editorComponent;
  }
}
