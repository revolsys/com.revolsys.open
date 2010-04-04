package com.revolsys.jump.ui.swing.table;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.border.EmptyBorder;
import javax.swing.table.TableCellRenderer;

import com.revolsys.jump.ui.swing.FeatureTypeUiBuilderRegistry;
import com.revolsys.jump.ui.swing.ValueUiBuilder;
import com.vividsolutions.jump.feature.FeatureSchema;

public class FeatureCollectionTableCellRenderer implements TableCellRenderer {
  private JLabel valueComponent;

  private FeatureTypeUiBuilderRegistry uiBuilderRegistry;

  public FeatureCollectionTableCellRenderer(
    final FeatureTypeUiBuilderRegistry uiBuilderRegistry) {
    this.uiBuilderRegistry = uiBuilderRegistry;
    valueComponent = new JLabel();
    valueComponent.setBorder(new EmptyBorder(1, 2, 1, 2));
    valueComponent.setOpaque(true);
  }

  public Component getTableCellRendererComponent(final JTable table,
    final Object value, final boolean isSelected, final boolean hasFocus,
    final int row, final int column) {
    Component component = null;

    FeatureListTableModel model = (FeatureListTableModel)table.getModel();

    FeatureSchema schema = model.getSchema();
    if (column == 0) {
      valueComponent.setText(value.toString());
      component = valueComponent;
    } else {
      ValueUiBuilder uiBuilder = uiBuilderRegistry.getValueUiBuilder(schema,
        getAttributeIndex(schema, column));
      if (uiBuilder != null) {
        component = uiBuilder.getRendererComponent(value);
      } else if (value == null) {
        valueComponent.setText("-");
        component = valueComponent;
      } else {
        valueComponent.setText(value.toString());
        component = valueComponent;
      }
    }
    int[] selectedRows = table.getSelectedRows();
    boolean selected = false;
    for (int selectedRow : selectedRows) {
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
