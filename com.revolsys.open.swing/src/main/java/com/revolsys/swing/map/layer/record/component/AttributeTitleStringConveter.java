package com.revolsys.swing.map.layer.record.component;

import java.awt.Component;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

import org.jdesktop.swingx.autocomplete.ObjectToStringConverter;

import com.revolsys.converter.string.StringConverterRegistry;
import com.revolsys.data.record.schema.FieldDefinition;
import com.revolsys.swing.map.layer.record.AbstractRecordLayer;

public class AttributeTitleStringConveter extends ObjectToStringConverter
  implements ListCellRenderer {
  private final AbstractRecordLayer layer;

  private final DefaultListCellRenderer renderer = new DefaultListCellRenderer();

  public AttributeTitleStringConveter(final AbstractRecordLayer layer) {
    this.layer = layer;
  }

  @Override
  public Component getListCellRendererComponent(final JList list, final Object value,
    final int index, final boolean isSelected, final boolean cellHasFocus) {
    final String title = getPreferredStringForItem(value);
    return this.renderer.getListCellRendererComponent(list, title, index, isSelected, cellHasFocus);
  }

  @Override
  public String getPreferredStringForItem(final Object item) {
    if (item instanceof FieldDefinition) {
      final FieldDefinition attribute = (FieldDefinition)item;
      return this.layer.getFieldTitle(attribute.getName());
    } else if (item instanceof String) {
      final String fieldName = (String)item;
      return this.layer.getFieldTitle(fieldName);
    }
    return StringConverterRegistry.toString(item);
  }
}
