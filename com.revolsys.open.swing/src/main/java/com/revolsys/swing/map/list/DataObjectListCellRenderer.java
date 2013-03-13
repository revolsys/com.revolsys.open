package com.revolsys.swing.map.list;

import java.awt.Component;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;

import com.revolsys.converter.string.StringConverterRegistry;
import com.revolsys.gis.data.model.DataObject;

@SuppressWarnings("serial")
public class DataObjectListCellRenderer extends DefaultListCellRenderer {
  private final String attributeName;

  public DataObjectListCellRenderer(final String attributeName) {
    this.attributeName = attributeName;
  }

  @Override
  public Component getListCellRendererComponent(final JList list,
    final Object cellValue, final int index, final boolean isSelected,
    final boolean cellHasFocus) {
    super.getListCellRendererComponent(list, cellValue, index, isSelected,
      cellHasFocus);
    if (cellValue instanceof DataObject) {
      final DataObject object = (DataObject)cellValue;
      final Object value = object.getValue(attributeName);
      final String text = StringConverterRegistry.toString(value);
      setText(text);
    }
    return this;
  }
}
