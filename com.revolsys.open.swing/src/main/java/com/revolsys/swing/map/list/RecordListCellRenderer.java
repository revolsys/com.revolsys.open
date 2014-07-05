package com.revolsys.swing.map.list;

import java.awt.Component;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;

import com.revolsys.converter.string.StringConverterRegistry;
import com.revolsys.data.record.Record;

@SuppressWarnings("serial")
public class RecordListCellRenderer extends DefaultListCellRenderer {
  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  private final String attributeName;

  public RecordListCellRenderer(final String attributeName) {
    this.attributeName = attributeName;
  }

  @Override
  public Component getListCellRendererComponent(final JList list,
    final Object cellValue, final int index, final boolean isSelected,
    final boolean cellHasFocus) {
    super.getListCellRendererComponent(list, cellValue, index, isSelected,
      cellHasFocus);
    if (cellValue instanceof Record) {
      final Record object = (Record)cellValue;
      final Object value = object.getValue(this.attributeName);
      final String text = StringConverterRegistry.toString(value);
      setText(text);
    }
    return this;
  }
}
