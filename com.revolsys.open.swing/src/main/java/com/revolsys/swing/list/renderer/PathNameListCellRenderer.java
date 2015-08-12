package com.revolsys.swing.list.renderer;

import java.awt.Component;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

import org.jdesktop.swingx.autocomplete.ObjectToStringConverter;

import com.revolsys.io.Path;
import com.revolsys.io.PathName;
import com.revolsys.util.Property;

@SuppressWarnings("rawtypes")
public class PathNameListCellRenderer extends ObjectToStringConverter implements ListCellRenderer {

  private final DefaultListCellRenderer renderer = new DefaultListCellRenderer();

  public PathNameListCellRenderer() {
  }

  @Override
  public Component getListCellRendererComponent(final JList list, final Object value,
    final int index, final boolean isSelected, final boolean cellHasFocus) {
    final String title = getPreferredStringForItem(value);
    return this.renderer.getListCellRendererComponent(list, title, index, isSelected, cellHasFocus);
  }

  @Override
  public String getPreferredStringForItem(final Object item) {
    if (item instanceof PathName) {
      final PathName path = (PathName)item;
      return path.getName();
    } else if (Property.hasValue(item)) {
      final String path = item.toString();
      return Path.getName(path);
    } else {
      return "-";
    }
  }
}
