package com.revolsys.swing.table.dataobject.row;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.border.EmptyBorder;
import javax.swing.table.TableCellRenderer;

import com.revolsys.converter.string.StringConverterRegistry;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.swing.builder.DataObjectMetaDataUiBuilderRegistry;
import com.revolsys.swing.builder.ValueUiBuilder;

public class DataObjectRowTableCellRenderer implements TableCellRenderer {
  private final JLabel valueComponent;

  private final DataObjectMetaDataUiBuilderRegistry uiBuilderRegistry;

  public DataObjectRowTableCellRenderer() {
    this(DataObjectMetaDataUiBuilderRegistry.getInstance());
  }

  public DataObjectRowTableCellRenderer(
    final DataObjectMetaDataUiBuilderRegistry uiBuilderRegistry) {
    this.uiBuilderRegistry = uiBuilderRegistry;
    valueComponent = new JLabel();
    valueComponent.setBorder(new EmptyBorder(1, 2, 1, 2));
    valueComponent.setOpaque(true);
  }

  @Override
  public Component getTableCellRendererComponent(final JTable table,
    final Object value, final boolean isSelected, final boolean hasFocus,
    final int row, final int column) {
    Component component = null;

    final DataObjectRowTableModel model = (DataObjectRowTableModel)table.getModel();

    final DataObjectMetaData metaData = model.getMetaData();
    final boolean required = metaData.isAttributeRequired(column);
    if (uiBuilderRegistry != null) {
      final ValueUiBuilder uiBuilder = uiBuilderRegistry.getValueUiBuilder(
        metaData, column);
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
    final int[] selectedRows = table.getSelectedRows();
    boolean selected = false;
    for (final int selectedRow : selectedRows) {
      if (row == selectedRow) {
        selected = true;
      }
    }
    if (required && value == null) {
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
