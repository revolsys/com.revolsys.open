package com.revolsys.swing.builder;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.border.EmptyBorder;

import com.revolsys.converter.string.StringConverterRegistry;
import com.revolsys.swing.SwingUtil;

public class DefaultValueUiBuilder implements ValueUiBuilder {
  private final JLabel defaultRenderer;

  private JComponent component;

  public DefaultValueUiBuilder() {
    this.defaultRenderer = new JLabel();
    this.defaultRenderer.setBorder(new EmptyBorder(1, 2, 1, 2));
    this.defaultRenderer.setOpaque(true);
  }

  @Override
  public Object getCellEditorValue() {
    return SwingUtil.getValue(this.component);
  }

  @Override
  public JComponent getEditorComponent(final Object value) {
    return SwingUtil.getValue(this.component);
  }

  @Override
  public JComponent getRendererComponent(final Object value) {
    final String text = getText(value);
    this.defaultRenderer.setText(text);
    return this.defaultRenderer;
  }

  public String getText(final Object value) {
    return StringConverterRegistry.toString(value);
  }

}
