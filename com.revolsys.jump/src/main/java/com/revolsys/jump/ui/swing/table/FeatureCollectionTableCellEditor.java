package com.revolsys.jump.ui.swing.table;

import java.awt.Component;

import javax.swing.AbstractCellEditor;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.TableCellEditor;

import com.revolsys.jump.ui.swing.FeatureTypeUiBuilderRegistry;
import com.revolsys.jump.ui.swing.ValueUiBuilder;
import com.vividsolutions.jump.feature.FeatureSchema;

@SuppressWarnings("serial")
public class FeatureCollectionTableCellEditor extends AbstractCellEditor
  implements TableCellEditor {
  private FeatureTypeUiBuilderRegistry uiBuilderRegistry;

  private JTextField editorComponent = new JTextField();

  private ValueUiBuilder uiBuilder;

  public FeatureCollectionTableCellEditor(
    final FeatureTypeUiBuilderRegistry uiBuilderRegistry) {
    this.uiBuilderRegistry = uiBuilderRegistry;
  }

  public Component getTableCellEditorComponent(final JTable table,
    final Object value, final boolean isSelected, final int row,
    final int column) {
    FeatureListTableModel model = (FeatureListTableModel)table.getModel();
    FeatureSchema schema = model.getSchema();
    uiBuilder = uiBuilderRegistry.getValueUiBuilder(schema, getAttributeIndex(
      schema, column));
    if (uiBuilder != null) {
      return uiBuilder.getEditorComponent(value);
    } else if (value == null) {
      editorComponent.setText(null);
    } else {
      editorComponent.setText(value.toString());
    }
    return editorComponent;
  }

  public Object getCellEditorValue() {
    if (uiBuilder != null) {
      return uiBuilder.getCellEditorValue();
    } else {
      return editorComponent.getText();
    }
  }

  private int getAttributeIndex(final FeatureSchema schema, final int column) {
    int attributeIndex = column - 1;
    int geometryIndex = schema.getGeometryIndex();
    if (geometryIndex != -1) {
      if (attributeIndex >= geometryIndex) {
        attributeIndex++;
      }
    }
    return attributeIndex;
  }
}
