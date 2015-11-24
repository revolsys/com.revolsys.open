package com.revolsys.swing.table.record.renderer;

import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;

import com.revolsys.swing.table.record.model.RecordRowTableModel;
import com.revolsys.util.number.BigDecimals;

public class RecordRowTableCellRenderer extends DefaultTableCellRenderer {
  private static final long serialVersionUID = 1L;

  private static final JLabel EMPTY_LABEL = new JLabel("\u2026");

  public RecordRowTableCellRenderer() {
    setOpaque(true);
  }

  @Override
  public Component getTableCellRendererComponent(final JTable table, final Object value,
    final boolean isSelected, final boolean hasFocus, final int rowIndex, final int columnIndex) {
    final RecordRowTableModel model = (RecordRowTableModel)table.getModel();
    if (model.getRecord(rowIndex) == null) {
      return EMPTY_LABEL;
    } else {
      final boolean selected = model.isSelected(isSelected, rowIndex, columnIndex);
      final Object displayValue;
      final int fieldsOffset = model.getFieldsOffset();
      if (columnIndex < fieldsOffset) {
        displayValue = value;
      } else {
        displayValue = model.toDisplayValue(rowIndex, columnIndex, value);
      }
      super.getTableCellRendererComponent(table, displayValue, selected, hasFocus, rowIndex,
        columnIndex);
      if (BigDecimals.isNumber(displayValue)) {
        setHorizontalAlignment(SwingConstants.RIGHT);
        setHorizontalTextPosition(SwingConstants.RIGHT);
      } else {
        setHorizontalAlignment(SwingConstants.LEFT);
        setHorizontalTextPosition(SwingConstants.LEFT);
      }
      setToolTipText(null);
      return this;
    }
  }
}
