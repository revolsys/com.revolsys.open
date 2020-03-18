package com.revolsys.swing.table.record.renderer;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.table.TableCellRenderer;

import org.jdesktop.swingx.JXTable;
import org.jeometry.common.number.Numbers;

import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.swing.SwingUtil;
import com.revolsys.swing.table.record.model.AbstractSingleRecordTableModel;

public class SingleRecordTableCellRenderer implements TableCellRenderer {
  private final JLabel labelComponent = new JLabel();

  private final JLabel valueComponent = new JLabel();

  public SingleRecordTableCellRenderer() {
    this.labelComponent.setFont(SwingUtil.BOLD_FONT);
    this.labelComponent.setOpaque(true);

    this.valueComponent.setFont(SwingUtil.FONT);
    this.valueComponent.setOpaque(true);
  }

  @Override
  public Component getTableCellRendererComponent(final JTable table, final Object value,
    final boolean isSelected, final boolean hasFocus, int rowIndex, int columnIndex) {
    if (table instanceof JXTable) {
      final JXTable jxTable = (JXTable)table;
      rowIndex = jxTable.convertRowIndexToModel(rowIndex);
      columnIndex = jxTable.convertColumnIndexToModel(columnIndex);
    }
    final AbstractSingleRecordTableModel model = (AbstractSingleRecordTableModel)table.getModel();
    final RecordDefinition recordDefinition = model.getRecordDefinition();

    JComponent component = null;
    final String name = model.getColumnFieldName(rowIndex, columnIndex);
    final boolean required = recordDefinition.isFieldRequired(name);
    this.valueComponent.setBorder(new EmptyBorder(1, 2, 1, 2));
    if (columnIndex == 0) {
      this.valueComponent.setText(String.valueOf(rowIndex + 1));
      this.valueComponent.setHorizontalAlignment(SwingConstants.RIGHT);
      this.valueComponent.setHorizontalTextPosition(SwingConstants.RIGHT);
      component = this.valueComponent;
    } else if (columnIndex == 1) {
      final String title = model.getFieldTitle(name);
      this.labelComponent.setText(title);
      this.labelComponent.setBorder(new EmptyBorder(1, 2, 1, 2));
      component = this.labelComponent;
    } else {
      final String text = model.toDisplayValue(rowIndex, rowIndex, value);
      this.valueComponent.setText(text);
      if (Numbers.isNumber(text)) {
        this.valueComponent.setHorizontalAlignment(SwingConstants.RIGHT);
        this.valueComponent.setHorizontalTextPosition(SwingConstants.RIGHT);
      } else {
        this.valueComponent.setHorizontalAlignment(SwingConstants.LEFT);
        this.valueComponent.setHorizontalTextPosition(SwingConstants.LEFT);
      }
      component = this.valueComponent;
    }
    if (required && model.getObjectValue(rowIndex, columnIndex) == null) {
      component.setBackground(new Color(255, 0, 0, 100));
      component.setForeground(table.getForeground());
    }
    component.setToolTipText(null);
    return component;
  }

}
