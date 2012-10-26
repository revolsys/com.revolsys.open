package com.revolsys.swing;

import java.awt.Dimension;
import java.awt.Toolkit;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.text.JTextComponent;

import com.revolsys.swing.field.NumberTextField;

public class SwingUtil {

  @SuppressWarnings("unchecked")
  public static <V> V getValue(JComponent component) {
    if (component instanceof NumberTextField) {
      NumberTextField numberField = (NumberTextField)component;
      return (V)numberField.getFieldValue();
    } else if (component instanceof JTextComponent) {
      JTextComponent textComponent = (JTextComponent)component;
      return (V)textComponent.getText();
    } else if (component instanceof JComboBox) {
      JComboBox comboBox = (JComboBox)component;
      return (V)comboBox.getSelectedItem();
    } else if (component instanceof JList) {
      JList list = (JList)component;
      return (V)list.getSelectedValue();
    } else if (component instanceof JCheckBox) {
      JCheckBox checkBox = (JCheckBox)component;
      return (V)(Object)checkBox.isSelected();
    } else {
      return null;
    }
  }

  public static void setSizeAndMaximize(JFrame frame, int minusX, int minusY) {
    Toolkit toolkit = Toolkit.getDefaultToolkit();
    Dimension screenSize = toolkit.getScreenSize();
    double screenWidth = screenSize.getWidth();
    double screenHeight = screenSize.getHeight();
    Dimension size = new Dimension((int)(screenWidth-minusX), (int)(screenHeight-minusY));
    frame.setSize(size);
    frame.setPreferredSize(size);
    frame.setExtendedState( frame.getExtendedState()|JFrame.MAXIMIZED_BOTH );
  }
}
