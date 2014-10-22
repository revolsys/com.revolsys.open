package com.revolsys.swing.list;

import java.awt.Component;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;

import com.revolsys.converter.string.StringConverterRegistry;
import com.revolsys.swing.field.ResultPagerComboBoxModel;
import com.revolsys.util.CollectionUtil;
import com.revolsys.util.Property;

public class ResultPagerListCellRenderer extends DefaultListCellRenderer {
  private static final long serialVersionUID = 1L;

  private List<String> fieldNames = Collections.emptyList();

  public ResultPagerListCellRenderer() {
  }

  public ResultPagerListCellRenderer(final List<String> fieldNames) {
    this.fieldNames = fieldNames;
  }

  public ResultPagerListCellRenderer(final String... fieldNames) {
    this(Arrays.asList(fieldNames));
  }

  @Override
  public Component getListCellRendererComponent(final JList list,
    final Object value, final int index, final boolean isSelected,
    final boolean cellHasFocus) {
    final Component component = super.getListCellRendererComponent(list, value,
      index, isSelected, cellHasFocus);
    String label;
    if (value == ResultPagerComboBoxModel.NULL || value == null) {
      label = "-";
    } else if (this.fieldNames.isEmpty()) {
      label = StringConverterRegistry.toString(value);
    } else {
      final List<String> values = new ArrayList<String>();
      for (final String fieldName : this.fieldNames) {
        final String text = StringConverterRegistry.toString(Property.get(
          value, fieldName));
        values.add(text);
      }
      label = CollectionUtil.toString(values);
    }
    setText(label);
    return component;
  }
}
