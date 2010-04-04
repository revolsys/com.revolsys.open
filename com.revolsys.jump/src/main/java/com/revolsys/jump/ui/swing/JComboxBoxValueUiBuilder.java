package com.revolsys.jump.ui.swing;

import java.awt.Component;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.JComboBox;
import javax.swing.JLabel;

public class JComboxBoxValueUiBuilder extends AbstractValueUIBuilder {
  private Map<String, String> keyValueMap = new HashMap<String, String>();

  private Map<String, String> valueKeyMap = new HashMap<String, String>();

  private JLabel rendererComponent = new JLabel();

  private JComboBox editorComponent;

  public JComboxBoxValueUiBuilder(final Map<String, String> values) {
    rendererComponent.setOpaque(true);
    String[] listValues = new String[values.size()];
    int i = 0;
    for (Entry<String, String> entry : values.entrySet()) {
      String key = entry.getKey();
      String value = entry.getValue();
      keyValueMap.put(key, value);
      valueKeyMap.put(value, key);
      listValues[i] = value;
      i++;
    }
    editorComponent = new JComboBox(listValues);
  }

  public Component getEditorComponent(final Object key) {
    Object value = null;
    if (key != null) {
      value = keyValueMap.get(key.toString());
    }
    editorComponent.setSelectedItem(value);
    return editorComponent;
  }

  public Component getRendererComponent(final Object key) {
    Object value = null;
    if (key != null) {
      value = keyValueMap.get(key.toString());
    }
    if (value != null) {
      rendererComponent.setText(value.toString());
    } else {
      rendererComponent.setText("-");
    }
    return rendererComponent;
  }

  public Object getCellEditorValue() {
    Object value = editorComponent.getSelectedItem();
    Object key = valueKeyMap.get(value);
    return key;
  }

}
