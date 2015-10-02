package com.revolsys.swing.field;

import java.awt.Component;
import java.util.function.Function;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

import org.jdesktop.swingx.autocomplete.ObjectToStringConverter;

public class FunctionStringConverter extends ObjectToStringConverter implements ListCellRenderer {

  private int horizontalAlignment = JLabel.LEFT;

  private final DefaultListCellRenderer renderer = new DefaultListCellRenderer();

  private final Function<Object, String> function;

  public FunctionStringConverter(final Function<Object, String> function) {
    this.function = function;
  }

  public int getHorizontalAlignment() {
    return this.horizontalAlignment;
  }

  @Override
  public Component getListCellRendererComponent(final JList list, final Object value,
    final int index, final boolean isSelected, final boolean cellHasFocus) {
    this.renderer.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
    final String text = getPreferredStringForItem(value);
    this.renderer.setText(text);
    this.renderer.setHorizontalAlignment(this.horizontalAlignment);
    return this.renderer;
  }

  @Override
  public String getPreferredStringForItem(final Object item) {
    if (item == null) {
      return "";
    } else {
      return this.function.apply(item);
    }
  }

  public void setHorizontalAlignment(final int horizontalAlignment) {
    this.horizontalAlignment = horizontalAlignment;
  }
}
