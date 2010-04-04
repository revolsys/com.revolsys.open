package com.revolsys.jump.ui.swing.table;

import java.awt.Component;

import javax.swing.AbstractCellEditor;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.TableCellEditor;

import com.revolsys.jump.ui.swing.FeatureTypeUiBuilderRegistry;
import com.revolsys.jump.ui.swing.ValueUiBuilder;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureSchema;

@SuppressWarnings("serial")
public class FeatureTableCellEditor extends AbstractCellEditor implements
  TableCellEditor {
  private FeatureTypeUiBuilderRegistry uiBuilderRegistry;

  private JTextField editorComponent = new JTextField();

  private ValueUiBuilder uiBuilder;

  public FeatureTableCellEditor(
    final FeatureTypeUiBuilderRegistry uiBuilderRegistry) {
    this.uiBuilderRegistry = uiBuilderRegistry;
  }

  public Component getTableCellEditorComponent(final JTable table,
    final Object value, final boolean isSelected, final int row,
    final int column) {
    if (column == 0) {
      return null;
    } else {
      FeatureTableModel model = (FeatureTableModel)table.getModel();
      FeatureTableRow featureRow = model.getFeatureTableRow(row);
      Feature feature = featureRow.getFeature();
      FeatureSchema schema = feature.getSchema();
      int attributeIndex = featureRow.getAttributeIndex();
      uiBuilder = uiBuilderRegistry.getValueUiBuilder(schema, attributeIndex);
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

  public Object getCellEditorValue() {
    if (uiBuilder != null) {
      return uiBuilder.getCellEditorValue();
    } else {
      return editorComponent.getText();
    }
  }

}
