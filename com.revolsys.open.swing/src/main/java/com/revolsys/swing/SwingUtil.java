package com.revolsys.swing;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.MenuContainer;
import java.awt.MenuItem;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.datatransfer.DataFlavor;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.Date;

import javax.swing.ComboBoxEditor;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.text.JTextComponent;

import org.jdesktop.swingx.JXDatePicker;
import org.jdesktop.swingx.JXTextArea;
import org.jdesktop.swingx.JXTextField;
import org.jdesktop.swingx.autocomplete.AutoCompleteDecorator;
import org.springframework.util.StringUtils;

import com.revolsys.converter.string.StringConverterRegistry;
import com.revolsys.gis.data.model.Attribute;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.gis.data.model.codes.CodeTable;
import com.revolsys.gis.data.model.types.DataType;
import com.revolsys.gis.data.model.types.DataTypes;
import com.revolsys.io.FileUtil;
import com.revolsys.swing.component.JLabelWithObject;
import com.revolsys.swing.field.CodeTableComboBoxModel;
import com.revolsys.swing.field.CodeTableObjectToStringConverter;
import com.revolsys.swing.field.DateTextField;
import com.revolsys.swing.field.NumberTextField;
import com.revolsys.swing.menu.PopupMenu;
import com.revolsys.util.PreferencesUtil;
import com.vividsolutions.jts.geom.Geometry;

public class SwingUtil {

  public static JLabel addLabel(final Container container, final String text) {
    final JLabel label = new JLabel(text);
    label.setFont(label.getFont().deriveFont(Font.BOLD));
    container.add(label);
    return label;
  }

  public static JFileChooser createFileChooser(Class<?> preferencesClass,
    String preferenceName) {
    final JFileChooser fileChooser = new JFileChooser();
    String currentDirectoryName = PreferencesUtil.getString(preferencesClass,
      preferenceName);
    if (StringUtils.hasText(currentDirectoryName)) {
      File directory = new File(currentDirectoryName);
      if (directory.exists() && directory.canRead()) {
        fileChooser.setCurrentDirectory(directory);
      }
    }
    return fileChooser;
  }

  public static DataFlavor createDataFlavor(String mimeType) {
    try {
      return new DataFlavor(mimeType);
    } catch (ClassNotFoundException e) {
      throw new IllegalArgumentException("Cannot create data flavor for "
        + mimeType, e);
    }
  }

  public static void saveFileChooserDirectory(Class<?> preferencesClass,
    String preferenceName, JFileChooser fileChooser) {
    File currentDirectory = fileChooser.getCurrentDirectory();
    String path = FileUtil.getCanonicalPath(currentDirectory);
    PreferencesUtil.setString(preferencesClass, preferenceName, path);
  }

  @SuppressWarnings("unchecked")
  public static <T extends JComponent> T createField(
    final DataObjectMetaData metaData, final String fieldName,
    final boolean editable) {
    JComponent field;
    final Attribute attribute = metaData.getAttribute(fieldName);
    if (attribute == null) {
      throw new IllegalArgumentException("Cannot find field " + fieldName);
    } else {
      final boolean required = attribute.isRequired();
      final int length = attribute.getLength();
      final CodeTable codeTable = metaData.getCodeTableByColumn(fieldName);
      final DataType type = attribute.getType();
      int columns = length;
      if (columns == 0) {
        columns = 10;
      } else if (columns > 50) {
        columns = 50;
      }
      if (!editable) {
        JXTextField textField = createTextField(columns);
        textField.setEditable(false);
        field = textField;
      } else if (codeTable != null) {
        field = createComboBox(codeTable, required);
      } else if (Number.class.isAssignableFrom(type.getJavaClass())) {
        final int scale = attribute.getScale();
        NumberTextField numberTextField = new NumberTextField(type, length,
          scale);
        field = numberTextField;
      } else if (type.equals(DataTypes.DATE)) {
        field = createDateField();
      } else if (Geometry.class.isAssignableFrom(type.getJavaClass())) {
        field = new JLabelWithObject();
      } else {
        field = createTextField(columns);
      }
      if (field instanceof JTextField) {
        JTextField textField = (JTextField)field;
        int preferedWidth = textField.getPreferredSize().width;
        textField.setMinimumSize(new Dimension(preferedWidth, 0));
        textField.setMaximumSize(new Dimension(preferedWidth, Integer.MAX_VALUE));

      }
    }
    return (T)field;
  }

  public static JComboBox createComboBox(final CodeTable codeTable,
    final boolean required) {
    final JComboBox comboBox = CodeTableComboBoxModel.create(codeTable,
      !required);
    if (comboBox.getModel().getSize() > 0) {
      comboBox.setSelectedIndex(0);
    }
    final CodeTableObjectToStringConverter stringConverter = new CodeTableObjectToStringConverter(
      codeTable);
    AutoCompleteDecorator.decorate(comboBox, stringConverter);
    ComboBoxEditor editor = comboBox.getEditor();
    Component editorComponent = editor.getEditorComponent();
    if (editorComponent instanceof JTextComponent) {
      JTextComponent textComponent = (JTextComponent)editorComponent;
      PopupMenu.createPopupMenu(textComponent);
    }
    return comboBox;
  }

  public static JComponent createDateField() {
    final JXDatePicker dateField = new JXDatePicker();
    dateField.setFormats("yyyy-MM-dd", "yyyy/MM/dd", "yyyy-MMM-dd",
      "yyyy/MMM/dd");
    PopupMenu.createPopupMenu(dateField.getEditor());
    return dateField;
  }

  public static JXTextField createTextField(int columns) {
    JXTextField textField = new JXTextField();
    textField.setColumns(columns);
    PopupMenu.createPopupMenu(textField);
    return textField;
  }

  public static JXTextArea createTextArea(int rows, int columns) {
    JXTextArea textField = new JXTextArea();
    textField.setRows(rows);
    textField.setColumns(columns);
    PopupMenu.createPopupMenu(textField);
    return textField;
  }

  @SuppressWarnings("unchecked")
  public static <V> V getValue(final JComponent component) {
    if (component instanceof JLabelWithObject) {
      JLabelWithObject label = (JLabelWithObject)component;
      return (V)label.getObject();
    } else if (component instanceof JXDatePicker) {
      final JXDatePicker dateField = (JXDatePicker)component;
      return (V)dateField.getDate();
    } else if (component instanceof NumberTextField) {
      final NumberTextField numberField = (NumberTextField)component;
      return (V)numberField.getFieldValue();
    } else if (component instanceof JTextComponent) {
      final JTextComponent textComponent = (JTextComponent)component;
      final String text = textComponent.getText();
      if (StringUtils.hasText(text)) {
        return (V)text;
      } else {
        return null;
      }
    } else if (component instanceof JComboBox) {
      final JComboBox comboBox = (JComboBox)component;
      return (V)comboBox.getSelectedItem();
    } else if (component instanceof JList) {
      final JList list = (JList)component;
      return (V)list.getSelectedValue();
    } else if (component instanceof JCheckBox) {
      final JCheckBox checkBox = (JCheckBox)component;
      return (V)(Object)checkBox.isSelected();
    } else {
      return null;
    }
  }

  public static int getX(final Component component) {
    final int x = component.getX();
    final Component parent = component.getParent();
    if (parent == null) {
      return x;
    } else {
      return x + getX(parent);
    }
  }

  public static int getY(final Component component) {
    final int y = component.getY();
    final Component parent = component.getParent();
    if (parent == null) {
      return y;
    } else {
      return y + getY(parent);
    }
  }

  public static void setFieldValue(final JComponent field,
    final String fieldName, final Object value) {
    if (field instanceof JLabelWithObject) {
      JLabelWithObject label = (JLabelWithObject)field;
      label.setObject(value);
    } else if (field instanceof NumberTextField) {
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
    final Container parent = field.getParent();
    if (parent != null) {
      parent.getLayout().layoutContainer(parent);
      field.revalidate();
    }
  }

  public static void setMaximumWidth(final JComponent component, final int width) {
    final Dimension preferredSize = component.getPreferredSize();
    final Dimension size = new Dimension(width, preferredSize.height);
    component.setMaximumSize(size);
  }

  public static void setSize(final Window window, final int minusX,
    final int minusY) {
    final Toolkit toolkit = Toolkit.getDefaultToolkit();
    final Dimension screenSize = toolkit.getScreenSize();
    final double screenWidth = screenSize.getWidth();
    final double screenHeight = screenSize.getHeight();
    final Dimension size = new Dimension((int)(screenWidth - minusX),
      (int)(screenHeight - minusY));
    window.setBounds(minusX / 2, minusY / 2, size.width, size.height);
    window.setPreferredSize(size);
  }

  public static void setSizeAndMaximize(final JFrame frame, final int minusX,
    final int minusY) {
    final Toolkit toolkit = Toolkit.getDefaultToolkit();
    final Dimension screenSize = toolkit.getScreenSize();
    final double screenWidth = screenSize.getWidth();
    final double screenHeight = screenSize.getHeight();
    final Dimension size = new Dimension((int)(screenWidth - minusX),
      (int)(screenHeight - minusY));
    frame.setSize(size);
    frame.setPreferredSize(size);
    frame.setExtendedState(frame.getExtendedState() | Frame.MAXIMIZED_BOTH);
  }

  public static Component getInvoker(final JMenuItem menuItem) {
    MenuContainer menuContainer = menuItem.getParent();
    while (menuContainer != null && !(menuContainer instanceof JPopupMenu)) {
      if (menuContainer instanceof MenuItem) {
        menuContainer = ((MenuItem)menuContainer).getParent();
      } else {
        menuContainer = null;
      }
    }
    if (menuContainer != null) {
      final JPopupMenu menu = (JPopupMenu)menuContainer;
      final Component invoker = menu.getInvoker();
      return invoker;
    } else {
      return null;
    }

  }

  public static boolean isLeftButtonAndNoModifiers(final MouseEvent event) {
    int modifiers = event.getModifiers();
    return SwingUtilities.isLeftMouseButton(event) && MouseEvent.BUTTON1_MASK == modifiers;
  }
}
