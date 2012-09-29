package com.revolsys.swing;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.text.JTextComponent;

public class SwingUtil {

  @SuppressWarnings("unchecked")
  public static <V> V getValue(JComponent component) {
    if (component instanceof JTextComponent) {
      JTextComponent textComponent = (JTextComponent)component;
      return (V)textComponent.getText();
    } else  if (component instanceof JComboBox) {
      JComboBox comboBox = (JComboBox)component;
      return (V)comboBox.getSelectedItem();
    } else  if (component instanceof JList) {
      JList list = (JList)component;
      return (V)list.getSelectedValue();
    } else  if (component instanceof JCheckBox) {
      JCheckBox checkBox = (JCheckBox)component;
      return (V)(Object)checkBox.isSelected();
    } else {
      return null;
    }
  }
}
