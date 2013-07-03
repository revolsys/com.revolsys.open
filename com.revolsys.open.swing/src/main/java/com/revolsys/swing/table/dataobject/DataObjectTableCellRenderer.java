package com.revolsys.swing.table.dataobject;

import java.awt.Color;
import java.awt.Component;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.border.EmptyBorder;
import javax.swing.table.TableCellRenderer;

import org.jdesktop.swingx.JXTable;

import com.revolsys.converter.string.StringConverterRegistry;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.gis.data.model.codes.CodeTable;
import com.revolsys.swing.SwingUtil;
import com.revolsys.swing.builder.DataObjectMetaDataUiBuilderRegistry;
import com.revolsys.util.CollectionUtil;

public class DataObjectTableCellRenderer implements TableCellRenderer {
  private final JLabel valueComponent = new JLabel();

  private final JLabel labelComponent = new JLabel();

  private DataObjectMetaDataUiBuilderRegistry uiBuilderRegistry;

  public DataObjectTableCellRenderer() {
    this(DataObjectMetaDataUiBuilderRegistry.getInstance());
  }

  public DataObjectTableCellRenderer(
    final DataObjectMetaDataUiBuilderRegistry uiBuilderRegistry) {
    this.uiBuilderRegistry = uiBuilderRegistry;
    labelComponent.setBorder(new EmptyBorder(1, 2, 1, 2));
    labelComponent.setFont(SwingUtil.BOLD_FONT);
    labelComponent.setOpaque(true);

    valueComponent.setBorder(new EmptyBorder(1, 2, 1, 2));
    valueComponent.setFont(SwingUtil.FONT);
    valueComponent.setOpaque(true);
  }

  @Override
  public Component getTableCellRendererComponent(final JTable table,
    final Object value, final boolean isSelected, final boolean hasFocus,
    final int row, final int column) {
    int attributeIndex;
    if (table instanceof JXTable) {
      final JXTable jxTable = (JXTable)table;
      attributeIndex = jxTable.convertRowIndexToModel(row);
    } else {
      attributeIndex = row;
    }
    final AbstractDataObjectTableModel model = (AbstractDataObjectTableModel)table.getModel();
    final DataObjectMetaData metaData = model.getMetaData();
    final boolean required = metaData.isAttributeRequired(attributeIndex);

    Component component = null;
    final String name = metaData.getAttributeName(attributeIndex);
    final String title = metaData.getAttributeTitle(name);
    if (column == 0) {
      valueComponent.setText(String.valueOf(attributeIndex));
      component = valueComponent;
    } else if (column == 1) {
      labelComponent.setText(title);
      component = labelComponent;
    } else {
      if (component == null) {
        String text;
        if (value == null) {
          text = "-";
        } else {
          CodeTable codeTable = null;
          if (!name.equals(metaData.getIdAttributeName())) {
            codeTable = metaData.getCodeTableByColumn(name);
          }
          if (codeTable == null) {
            text = StringConverterRegistry.toString(value);
          } else {
            final List<Object> values = codeTable.getValues(value);
            if (values == null || values.isEmpty()) {
              text = "-";
            } else {
              text = CollectionUtil.toString(values);
            }
          }
        }
        valueComponent.setText(text);
        component = valueComponent;
      }
    }
    if (required && model.getValue(attributeIndex) == null) {
      component.setBackground(new Color(255, 0, 0, 100));
      component.setForeground(table.getForeground());
    } else {
      setRowColor(table, component, row);
    }
    return component;
  }

  protected void setRowColor(final JTable table, final Component component,
    final int row) {
    if (component != null) {
      final int[] selectedRows = table.getSelectedRows();
      boolean selected = false;
      for (final int selectedRow : selectedRows) {
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
    }
  }

  public void setUiBuilderRegistry(
    final DataObjectMetaDataUiBuilderRegistry uiBuilderRegistry) {
    this.uiBuilderRegistry = uiBuilderRegistry;
  }
}
