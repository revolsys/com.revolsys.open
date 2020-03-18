package com.revolsys.swing.map.list;

import java.awt.Component;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;

import org.jeometry.common.data.type.DataTypes;

import com.revolsys.record.Record;

public class RecordListCellRenderer extends DefaultListCellRenderer {
  private static final long serialVersionUID = 1L;

  private final String fieldName;

  public RecordListCellRenderer(final String fieldName) {
    this.fieldName = fieldName;
  }

  @Override
  public Component getListCellRendererComponent(final JList list, final Object cellValue,
    final int index, final boolean isSelected, final boolean cellHasFocus) {
    super.getListCellRendererComponent(list, cellValue, index, isSelected, cellHasFocus);
    if (cellValue instanceof Record) {
      final Record object = (Record)cellValue;
      final Object value = object.getValue(this.fieldName);
      final String text = DataTypes.toString(value);
      setText(text);
    }
    return this;
  }
}
