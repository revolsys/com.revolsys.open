package com.revolsys.swing.map.table;

import java.awt.Component;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

import org.jdesktop.swingx.autocomplete.ObjectToStringConverter;

import com.revolsys.converter.string.StringConverterRegistry;
import com.revolsys.gis.data.model.Attribute;
import com.revolsys.gis.data.model.DataObjectMetaData;

public class AttributeTitleStringConveter extends ObjectToStringConverter
  implements ListCellRenderer {
  private final DataObjectMetaData metaData;

  private final DefaultListCellRenderer renderer = new DefaultListCellRenderer();

  public AttributeTitleStringConveter(final DataObjectMetaData metaData) {
    this.metaData = metaData;
  }

  @Override
  public Component getListCellRendererComponent(final JList list,
    final Object value, final int index, final boolean isSelected,
    final boolean cellHasFocus) {
    final String title = getPreferredStringForItem(value);
    return renderer.getListCellRendererComponent(list, title, index,
      isSelected, cellHasFocus);
  }

  @Override
  public String getPreferredStringForItem(final Object item) {
    if (item instanceof Attribute) {
      final Attribute attribute = (Attribute)item;
      return attribute.getTitle();
    } else if (item instanceof String) {
      final String attributeName = (String)item;
      final Attribute attribute = metaData.getAttribute(attributeName);
      if (attribute == null) {
        return attributeName;
      } else {
        return attribute.getTitle();
      }
    }
    return StringConverterRegistry.toString(item);
  }
}
