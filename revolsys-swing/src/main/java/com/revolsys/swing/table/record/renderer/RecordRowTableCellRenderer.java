package com.revolsys.swing.table.record.renderer;

import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;

import org.jeometry.common.number.Numbers;

import com.revolsys.record.Record;
import com.revolsys.swing.table.record.model.RecordRowTableModel;

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
    final Record record = model.getRecord(rowIndex);
    if (record == null) {
      return EMPTY_LABEL;
    } else {
      final boolean selected = isSelected || model.isSelected(isSelected, rowIndex, columnIndex);
      final Object displayValue;
      final int fieldsOffset = model.getColumnFieldsOffset();
      if (columnIndex < fieldsOffset) {
        displayValue = value;
      } else {
        displayValue = model.toDisplayValue(rowIndex, columnIndex, value);
      }
      super.getTableCellRendererComponent(table, displayValue, selected, hasFocus, rowIndex,
        columnIndex);
      if (Numbers.isNumber(displayValue)) {
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
