package com.revolsys.swing.builder;

import java.awt.Component;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.border.EmptyBorder;

import com.revolsys.converter.string.StringConverterRegistry;
import com.revolsys.swing.SwingUtil;

public class DefaultValueUiBuilder implements ValueUiBuilder {
  private final JLabel defaultRenderer;

  public DefaultValueUiBuilder() {
    defaultRenderer = new JLabel();
    defaultRenderer.setBorder(new EmptyBorder(1, 2, 1, 2));
    defaultRenderer.setOpaque(true);
  }

  private JComponent component;

  @Override
  public Object getCellEditorValue() {
    return SwingUtil.getValue(component);
  }

  @Override
  public Component getEditorComponent(Object value) {
    return SwingUtil.getValue(component);
  }

  @Override
  public Component getRendererComponent(final Object value) {
    final String text = getText(value);
    defaultRenderer.setText(text);
    return defaultRenderer;
  }

  public String getText(final Object value) {
    return StringConverterRegistry.toString(value);
  }

}
