package com.revolsys.swing.table.dataobject.renderer;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.table.TableCellRenderer;

import org.jdesktop.swingx.JXTable;

import com.revolsys.converter.string.BigDecimalStringConverter;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.swing.SwingUtil;
import com.revolsys.swing.table.dataobject.model.AbstractSingleDataObjectTableModel;

public class SingleDataObjectTableCellRenderer implements TableCellRenderer {
  private final JLabel valueComponent = new JLabel();

  private final JLabel labelComponent = new JLabel();

  public SingleDataObjectTableCellRenderer() {
    this.labelComponent.setBorder(new EmptyBorder(1, 2, 1, 2));
    this.labelComponent.setFont(SwingUtil.BOLD_FONT);
    this.labelComponent.setOpaque(true);

    this.valueComponent.setBorder(new EmptyBorder(1, 2, 1, 2));
    this.valueComponent.setFont(SwingUtil.FONT);
    this.valueComponent.setOpaque(true);
  }

  @Override
  public Component getTableCellRendererComponent(final JTable table,
    final Object value, final boolean isSelected, final boolean hasFocus,
    int rowIndex, int columnIndex) {
    if (table instanceof JXTable) {
      final JXTable jxTable = (JXTable)table;
      rowIndex = jxTable.convertRowIndexToModel(rowIndex);
      columnIndex = jxTable.convertColumnIndexToModel(columnIndex);
    }
    final AbstractSingleDataObjectTableModel model = (AbstractSingleDataObjectTableModel)table.getModel();
    final DataObjectMetaData metaData = model.getMetaData();

    Component component = null;
    final String name = model.getAttributeName(rowIndex, columnIndex);
    final boolean required = metaData.isAttributeRequired(name);
    final String title = metaData.getAttributeTitle(name);
    if (columnIndex == 0) {
      this.valueComponent.setText(String.valueOf(rowIndex));
      valueComponent.setHorizontalAlignment(SwingConstants.RIGHT);
      valueComponent.setHorizontalTextPosition(SwingConstants.RIGHT);
      component = this.valueComponent;
    } else if (columnIndex == 1) {
      this.labelComponent.setText(title);
      component = this.labelComponent;
    } else {
      final String text = model.toDisplayValue(rowIndex, value);
      this.valueComponent.setText(text);
      if (BigDecimalStringConverter.isNumber(text)) {
        valueComponent.setHorizontalAlignment(SwingConstants.RIGHT);
        valueComponent.setHorizontalTextPosition(SwingConstants.RIGHT);
      } else {
        valueComponent.setHorizontalAlignment(SwingConstants.LEFT);
        valueComponent.setHorizontalTextPosition(SwingConstants.LEFT);
      }
      component = this.valueComponent;
    }
    if (required && model.getObjectValue(rowIndex) == null) {
      component.setBackground(new Color(255, 0, 0, 100));
      component.setForeground(table.getForeground());
    } else {
      setRowColor(table, component, rowIndex);
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

}
