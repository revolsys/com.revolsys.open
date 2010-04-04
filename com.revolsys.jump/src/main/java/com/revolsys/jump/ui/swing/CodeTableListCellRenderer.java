package com.revolsys.jump.ui.swing;

import java.awt.Component;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;

public class CodeTableListCellRenderer extends DefaultListCellRenderer {

  private static final long serialVersionUID = -3005635657852385255L;

  @SuppressWarnings("unchecked")
  public Component getListCellRendererComponent(final JList list,
    final Object value, final int index, final boolean isSelected,
    final boolean cellHasFocus) {
    super.getListCellRendererComponent(list, value, index, isSelected,
      cellHasFocus);
    if (value instanceof Entry) {
      Entry<Number, List<Object>> entry = (Entry<Number, List<Object>>)value;
      setText(toString(entry.getValue()));
    }
    return this;
  }

  private String toString(final List<Object> values) {
    StringBuffer sb = new StringBuffer();
    for (Iterator<Object> valueIter = values.iterator(); valueIter.hasNext();) {
      Object object = valueIter.next();
      sb.append(object);
      if (valueIter.hasNext()) {
        sb.append(",");
      }
    }
    return sb.toString();
  }

}
