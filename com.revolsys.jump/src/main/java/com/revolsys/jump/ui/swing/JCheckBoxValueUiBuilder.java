package com.revolsys.jump.ui.swing;

import java.awt.Component;

import javax.swing.JCheckBox;
import javax.swing.JLabel;

public class JCheckBoxValueUiBuilder extends AbstractValueUIBuilder {
  private String falseValue = "0";

  private String trueValue = "1";

  private JLabel rendererComponent = new JLabel();

  private JCheckBox editorComponent = new JCheckBox();

  public JCheckBoxValueUiBuilder() {
    rendererComponent.setOpaque(true);
  }

  public Component getEditorComponent(final Object key) {
    if (key == null || falseValue.equals(key.toString())) {
      editorComponent.setSelected(false);
    } else {
      editorComponent.setSelected(true);
    }
    return editorComponent;
  }

  public Component getRendererComponent(final Object key) {
    if (key == null || falseValue.equals(key.toString())) {
      rendererComponent.setText("false");
    } else {
      rendererComponent.setText("true");
    }
    return rendererComponent;
  }

  public Object getCellEditorValue() {
    if (editorComponent.isSelected()) {
      return trueValue;
    } else {
      return falseValue;
    }
  }

}
