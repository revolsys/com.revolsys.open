package com.revolsys.swing;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.Window;
import java.util.Date;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.text.JTextComponent;

import org.jdesktop.swingx.JXDatePicker;
import org.jdesktop.swingx.autocomplete.AutoCompleteDecorator;
import org.springframework.util.StringUtils;

import com.revolsys.converter.string.StringConverterRegistry;
import com.revolsys.gis.data.model.Attribute;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.gis.data.model.codes.CodeTable;
import com.revolsys.gis.data.model.types.DataType;
import com.revolsys.gis.data.model.types.DataTypes;
import com.revolsys.swing.field.CodeTableComboBoxModel;
import com.revolsys.swing.field.CodeTableObjectToStringConverter;
import com.revolsys.swing.field.DateTextField;
import com.revolsys.swing.field.NumberTextField;

public class SwingUtil {

  public static void setMaximumWidth(JComponent component, int width) {
    Dimension preferredSize = component.getPreferredSize();
    Dimension size = new Dimension(width, preferredSize.height);
    component.setMaximumSize(size);
  }

  public static JLabel addLabel(Container container, String text) {
    JLabel label = new JLabel(text);
    label.setFont(label.getFont().deriveFont(Font.BOLD));
    container.add(label);
    return label;
  }

  public static void setFieldValue(final JComponent field,
    final String fieldName, final Object value) {
    if (field instanceof NumberTextField) {
      final NumberTextField numberField = (NumberTextField)field;
      numberField.setFieldValue((Number)value);
    } else if (field instanceof DateTextField) {
      final DateTextField dateField = (DateTextField)field;
      dateField.setFieldValue((Date)value);
    } else if (field instanceof JXDatePicker) {
      final JXDatePicker dateField = (JXDatePicker)field;
      dateField.setDate((Date)value);
    } else if (field instanceof JLabel) {
      final JLabel label = (JLabel)field;
      String string;
      if (value == null) {
        string = "";
      } else {
        string = StringConverterRegistry.toString(value);
      }
      label.setText(string);
    } else if (field instanceof JTextField) {
      final JTextField textField = (JTextField)field;
      String string;
      if (value == null) {
        string = "";
      } else {
        string = StringConverterRegistry.toString(value);
      }
      textField.setText(string);
    } else if (field instanceof JTextArea) {
      final JTextArea textField = (JTextArea)field;
      String string;
      if (value == null) {
        string = "";
      } else {
        string = StringConverterRegistry.toString(value);
      }
      textField.setText(string);
    } else if (field instanceof JComboBox) {
      final JComboBox comboField = (JComboBox)field;
      comboField.setSelectedItem(value);
    }
    Container parent = field.getParent();
    if (parent != null) {
      parent.getLayout().layoutContainer(parent);
      field.revalidate();
    }
  }

  @SuppressWarnings("unchecked")
  public static <T extends JComponent> T createField(
    DataObjectMetaData metaData, final String fieldName, boolean enabled) {
    JComponent field;
    final Attribute attribute = metaData.getAttribute(fieldName);
    if (attribute == null) {
      throw new IllegalArgumentException("Cannot find field " + fieldName);
    } else {
      final boolean required = attribute.isRequired();
      final int length = attribute.getLength();
      final CodeTable codeTable = metaData.getCodeTableByColumn(fieldName);
      final DataType type = attribute.getType();
      int size = length;
      if (size == 0) {
        size = 10;
      } else if (size > 50) {
        size = 50;
      }
      if (!enabled) {
        field = new JTextField(1);
        field.setEnabled(false);
      } else if (codeTable != null) {
        final JComboBox comboBox = CodeTableComboBoxModel.create(codeTable,
          !required);
        comboBox.setSelectedIndex(0);
        final CodeTableObjectToStringConverter stringConverter = new CodeTableObjectToStringConverter(
          codeTable);
        AutoCompleteDecorator.decorate(comboBox, stringConverter);
        field = comboBox;
      } else if (Number.class.isAssignableFrom(type.getJavaClass())) {
        final int scale = attribute.getScale();
        field = new NumberTextField(type, length, scale);
      } else if (type.equals(DataTypes.DATE)) {
        final JXDatePicker captureDateField = new JXDatePicker();
        captureDateField.setFormats("yyyy-MM-dd", "yyyy/MM/dd","yyyy-MMM-dd", "yyyy/MMM/dd");
        field = captureDateField;
      } else {
        JTextField textField = new JTextField(size);
        field = textField;
      }
    }
    return (T)field;
  }

  @SuppressWarnings("unchecked")
  public static <V> V getValue(JComponent component) {
    if (component instanceof JXDatePicker) {
      JXDatePicker dateField = (JXDatePicker)component;
      return (V)dateField.getDate();
    } else if (component instanceof NumberTextField) {
      NumberTextField numberField = (NumberTextField)component;
      return (V)numberField.getFieldValue();
    } else if (component instanceof JTextComponent) {
      JTextComponent textComponent = (JTextComponent)component;
      String text = textComponent.getText();
      if (StringUtils.hasText(text)) {
        return (V)text;
      } else {
        return null;
      }
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
    Dimension size = new Dimension((int)(screenWidth - minusX),
      (int)(screenHeight - minusY));
    frame.setSize(size);
    frame.setPreferredSize(size);
    frame.setExtendedState(frame.getExtendedState() | JFrame.MAXIMIZED_BOTH);
  }

  public static void setSize(Window window, int minusX, int minusY) {
    Toolkit toolkit = Toolkit.getDefaultToolkit();
    Dimension screenSize = toolkit.getScreenSize();
    double screenWidth = screenSize.getWidth();
    double screenHeight = screenSize.getHeight();
    Dimension size = new Dimension((int)(screenWidth - minusX),
      (int)(screenHeight - minusY));
    window.setBounds(minusX / 2, minusY / 2, size.width, size.height);
    window.setPreferredSize(size);
  }
}
