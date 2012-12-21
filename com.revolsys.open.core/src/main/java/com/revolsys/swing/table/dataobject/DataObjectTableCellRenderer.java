package com.revolsys.swing.table.dataobject;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.border.EmptyBorder;
import javax.swing.table.TableCellRenderer;

import com.revolsys.converter.string.StringConverterRegistry;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.swing.builder.DataObjectMetaDataUiBuilderRegistry;
import com.revolsys.swing.builder.ValueUiBuilder;

public class DataObjectTableCellRenderer implements TableCellRenderer {
  private final JLabel valueComponent = new JLabel();

  private final JLabel labelComponent = new JLabel();

  private final DataObjectMetaDataUiBuilderRegistry uiBuilderRegistry;

  public DataObjectTableCellRenderer() {
    this(DataObjectMetaDataUiBuilderRegistry.getInstance());
  }

  public DataObjectTableCellRenderer(
    final DataObjectMetaDataUiBuilderRegistry uiBuilderRegistry) {
    this.uiBuilderRegistry = uiBuilderRegistry;
    labelComponent.setBorder(new EmptyBorder(1, 2, 1, 2));
    labelComponent.setFont(labelComponent.getFont().deriveFont(Font.BOLD));
    labelComponent.setOpaque(true);

    valueComponent.setBorder(new EmptyBorder(1, 2, 1, 2));
    valueComponent.setOpaque(true);

  }

  @Override
  public Component getTableCellRendererComponent(final JTable table,
    final Object value, final boolean isSelected, final boolean hasFocus,
    final int row, final int column) {
    final AbstractDataObjectTableModel model = (AbstractDataObjectTableModel)table.getModel();
    final DataObjectMetaData metaData = model.getMetaData();
    final boolean required = metaData.isAttributeRequired(row);

    Component component = null;
    if (column == 1) {
      if (uiBuilderRegistry != null) {
        final ValueUiBuilder uiBuilder = uiBuilderRegistry.getValueUiBuilder(
          metaData, row);
        if (uiBuilder != null) {
          component = uiBuilder.getRendererComponent(value);
        }
      }
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
    } else {
      String name = metaData.getAttributeName(row);
      labelComponent.setText(name);
      component = labelComponent;
    }
    final int[] selectedRows = table.getSelectedRows();
    boolean selected = false;
    for (final int selectedRow : selectedRows) {
      if (row == selectedRow) {
        selected = true;
      }
    }
    if (required && model.getValue(row) == null) {
      component.setBackground(new Color(255, 0, 0, 100));
      component.setForeground(table.getForeground());
    } else if (selected) {
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
