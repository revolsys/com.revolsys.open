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
import com.revolsys.util.JavaBeanUtil;

public class ResultPagerListCellRenderer extends DefaultListCellRenderer {
  private static final long serialVersionUID = 1L;

  private List<String> attributeNames = Collections.emptyList();

  public ResultPagerListCellRenderer() {
  }

  public ResultPagerListCellRenderer(final List<String> attributeNames) {
    this.attributeNames = attributeNames;
  }

  public ResultPagerListCellRenderer(final String... attributeNames) {
    this(Arrays.asList(attributeNames));
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
    } else if (attributeNames.isEmpty()) {
      label = StringConverterRegistry.toString(value);
    } else {
      final List<String> values = new ArrayList<String>();
      for (final String attributeName : attributeNames) {
        final String text = StringConverterRegistry.toString(JavaBeanUtil.getValue(
          value, attributeName));
        values.add(text);
      }
      label = CollectionUtil.toString(values);
    }
    setText(label);
    return component;
  }
}
