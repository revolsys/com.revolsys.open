package com.revolsys.swing.component;

import java.awt.Component;
import java.util.HashMap;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.JList;
import javax.swing.plaf.basic.BasicComboBoxRenderer;

public class JLabelListCellRenderer extends BasicComboBoxRenderer {
  /**
   *
   */
  private static final long serialVersionUID = -8014837570227669960L;

  private final Map<Object, Icon> icons = new HashMap<>();

  private final Map<Object, String> labels = new HashMap<>();

  @Override
  public Component getListCellRendererComponent(final JList list, final Object value,
    final int index, final boolean isSelected, final boolean cellHasFocus) {
    super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
    final String label = this.labels.get(value);
    if (label != null) {
      setText(label);
    }
    final Icon icon = this.icons.get(value);
    setIcon(icon);
    return this;
  }

  public void setIcon(final Object value, final Icon icon) {
    this.icons.put(value, icon);
  }

  public void setLabel(final Object value, final String label) {
    this.labels.put(value, label);

  }

  public void setLabel(final Object value, final String label, final Icon icon) {
    this.labels.put(value, label);
    this.icons.put(value, icon);
  }
}
