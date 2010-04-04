package com.revolsys.jump.ui.swing.table;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.border.EmptyBorder;
import javax.swing.table.TableCellRenderer;

import com.revolsys.jump.ui.swing.FeatureTypeUiBuilderRegistry;
import com.revolsys.jump.ui.swing.ValueUiBuilder;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureSchema;

public class FeatureTableCellRenderer implements TableCellRenderer {
  private JLabel titleComponent;

  private JLabel valueComponent;

  private FeatureTypeUiBuilderRegistry uiBuilderRegistry;

  public FeatureTableCellRenderer(final FeatureTypeUiBuilderRegistry uiBuilderRegistry) {
    this.uiBuilderRegistry = uiBuilderRegistry;
    titleComponent = new JLabel();
    titleComponent.setOpaque(true);
    titleComponent.setFont(titleComponent.getFont().deriveFont(Font.BOLD));
    valueComponent = new JLabel();
    valueComponent.setBorder(new EmptyBorder(1, 2, 1, 2));
    valueComponent.setOpaque(true);
  }

  public Component getTableCellRendererComponent(final JTable table, final Object value,
    final boolean isSelected, final boolean hasFocus, final int row, final int column) {
    Component component = null;
    if (column == 0) {
      FeatureTableModel model = (FeatureTableModel)table.getModel();
      int indent = model.getIndentAt(row);
      titleComponent.setBorder(new EmptyBorder(0, 2 + 15 * indent, 2, 0));
      if (value == null) {
        titleComponent.setText("-");
      } else {
        titleComponent.setText(value.toString());
      }
      component = titleComponent;
    } else if (row == 0) {
      valueComponent.setText(value.toString());
      component = valueComponent;
    } else {
      FeatureTableModel model = (FeatureTableModel)table.getModel();
      FeatureTableRow featureRow = model.getFeatureTableRow(row);
      Feature feature = featureRow.getFeature();
      ValueUiBuilder uiBuilder = null;
      if (feature != null) {
        FeatureSchema schema = feature.getSchema();
        int attributeIndex = featureRow.getAttributeIndex();
        uiBuilder = uiBuilderRegistry.getValueUiBuilder(schema, attributeIndex);
      }
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
    if (row % 2 == 0) {
      component.setBackground(Color.WHITE);
    } else {
      component.setBackground(Color.LIGHT_GRAY);
    }
    return component;
  }
}
