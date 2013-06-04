package com.revolsys.swing;

import java.awt.Color;
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
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.Map.Entry;

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
import javax.swing.JTabbedPane;
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
import com.revolsys.swing.field.CodeTableComboBoxModel;
import com.revolsys.swing.field.CodeTableObjectToStringConverter;
import com.revolsys.swing.field.ColorChooserField;
import com.revolsys.swing.field.Field;
import com.revolsys.swing.field.NumberTextField;
import com.revolsys.swing.field.ObjectLabelField;
import com.revolsys.swing.field.TextField;
import com.revolsys.swing.menu.PopupMenu;
import com.revolsys.util.CaseConverter;
import com.revolsys.util.CollectionUtil;
import com.revolsys.util.JavaBeanUtil;
import com.revolsys.util.PreferencesUtil;
import com.vividsolutions.jts.geom.Geometry;

public class SwingUtil {
  public static final Font FONT = new Font(Font.SANS_SERIF, Font.PLAIN, 11);

  public static final Font BOLD_FONT = new Font(Font.SANS_SERIF, Font.BOLD, 11);

  public static JComponent addField(final Container container,
    final Object object, final String fieldName) {
    return addField(container, object, fieldName, fieldName);
  }

  public static JComponent addField(final Container panel, final Object object,
    final String fieldName, final String label) {
    addLabel(panel, label);
    final Object fieldValue = JavaBeanUtil.getValue(object, fieldName);
    final JComponent field = SwingUtil.createField(fieldValue.getClass(),
      fieldName, fieldValue);
    panel.add(field);
    return field;
  }

  public static JLabel addLabel(final Container container, final String text) {
    final JLabel label = new JLabel(CaseConverter.toCapitalizedWords(text));
    label.setFont(BOLD_FONT);
    container.add(label);
    return label;
  }

  public static JComboBox createComboBox(final CodeTable codeTable,
    final boolean required) {
    final JComboBox comboBox = CodeTableComboBoxModel.create(codeTable,
      !required);
    if (comboBox.getModel().getSize() > 0) {
      comboBox.setSelectedIndex(0);
    }
    String longestValue = "";
    for (final Entry<Object, List<Object>> codes : codeTable.getCodes()
      .entrySet()) {
      final List<Object> values = codes.getValue();
      if (values != null && !values.isEmpty()) {
        final String text = CollectionUtil.toString(values);
        if (text.length() > longestValue.length()) {
          longestValue = text;
        }
      }
    }
    comboBox.setPrototypeDisplayValue(longestValue);

    final CodeTableObjectToStringConverter stringConverter = new CodeTableObjectToStringConverter(
      codeTable);
    AutoCompleteDecorator.decorate(comboBox, stringConverter);
    final ComboBoxEditor editor = comboBox.getEditor();
    final Component editorComponent = editor.getEditorComponent();
    if (editorComponent instanceof JTextComponent) {
      final JTextField textComponent = (JTextField)editorComponent;
      textComponent.setColumns((int)(longestValue.length() * 0.8));
      PopupMenu.createPopupMenu(textComponent);
    }
    return comboBox;
  }

  public static DataFlavor createDataFlavor(final String mimeType) {
    try {
      return new DataFlavor(mimeType);
    } catch (final ClassNotFoundException e) {
      throw new IllegalArgumentException("Cannot create data flavor for "
        + mimeType, e);
    }
  }

  public static JXDatePicker createDateField() {
    final JXDatePicker dateField = new JXDatePicker();
    dateField.setFormats("yyyy-MM-dd", "yyyy/MM/dd", "yyyy-MMM-dd",
      "yyyy/MMM/dd");
    PopupMenu.createPopupMenu(dateField.getEditor());
    return dateField;
  }

  @SuppressWarnings("unchecked")
  public static <T extends JComponent> T createField(final Class<?> fieldClass,
    final String fieldName, final Object fieldValue) {
    JComponent field;
    if (Number.class.isAssignableFrom(fieldClass)) {
      final NumberTextField numberTextField = new NumberTextField(
        DataTypes.DOUBLE, 10, 2);
      if (fieldValue instanceof Number) {
        final Number number = (Number)fieldValue;
        numberTextField.setFieldValue(number);
      }
      field = numberTextField;
    } else if (Date.class.isAssignableFrom(fieldClass)) {
      final JXDatePicker dateField = createDateField();
      if (fieldValue instanceof Date) {
        final Date date = (Date)fieldValue;
        dateField.setDate(date);
      }
      field = dateField;
    } else if (Geometry.class.isAssignableFrom(fieldClass)) {
      final ObjectLabelField objectField = new ObjectLabelField(fieldName);
      objectField.setFieldValue(fieldValue);
      field = objectField;
    } else if (Color.class.isAssignableFrom(fieldClass)) {
      field = new ColorChooserField(fieldName, (Color)fieldValue);
    } else {
      final TextField textField = new TextField(fieldName, fieldValue);
      textField.setColumns(50);
      PopupMenu.createPopupMenu(textField);
      field = textField;
    }
    if (field instanceof JTextField) {
      final JTextField textField = (JTextField)field;
      final int preferedWidth = textField.getPreferredSize().width;
      textField.setMinimumSize(new Dimension(preferedWidth, 0));
      textField.setMaximumSize(new Dimension(preferedWidth, Integer.MAX_VALUE));
      textField.setText(StringConverterRegistry.toString(fieldValue));
    }
    field.setFont(FONT);
    return (T)field;
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
        final JXTextField textField = createTextField(columns);
        textField.setEditable(false);
        field = textField;
      } else if (codeTable != null) {
        field = createComboBox(codeTable, required);
      } else if (Number.class.isAssignableFrom(type.getJavaClass())) {
        final int scale = attribute.getScale();
        final NumberTextField numberTextField = new NumberTextField(type,
          length, scale);
        field = numberTextField;
      } else if (type.equals(DataTypes.DATE)) {
        field = createDateField();
      } else if (Geometry.class.isAssignableFrom(type.getJavaClass())) {
        field = new ObjectLabelField(fieldName);
      } else {
        field = createTextField(columns);
      }
      if (field instanceof JTextField) {
        final JTextField textField = (JTextField)field;
        final int preferedWidth = textField.getPreferredSize().width;
        textField.setMinimumSize(new Dimension(preferedWidth, 0));
        textField.setMaximumSize(new Dimension(preferedWidth, Integer.MAX_VALUE));

      }
    }
    field.setFont(FONT);
    return (T)field;
  }

  public static JFileChooser createFileChooser(final Class<?> preferencesClass,
    final String preferenceName) {
    final JFileChooser fileChooser = new JFileChooser();
    final String currentDirectoryName = PreferencesUtil.getString(
      preferencesClass, preferenceName);
    if (StringUtils.hasText(currentDirectoryName)) {
      final File directory = new File(currentDirectoryName);
      if (directory.exists() && directory.canRead()) {
        fileChooser.setCurrentDirectory(directory);
      }
    }
    return fileChooser;
  }

  public static JXTextArea createTextArea(final int rows, final int columns) {
    final JXTextArea textField = new JXTextArea();
    textField.setRows(rows);
    textField.setColumns(columns);
    PopupMenu.createPopupMenu(textField);
    return textField;
  }

  public static JXTextField createTextField(final int columns) {
    final JXTextField textField = new JXTextField();
    textField.setColumns(columns);
    PopupMenu.createPopupMenu(textField);
    return textField;
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

  public static int getTabIndex(final JTabbedPane tabs, final String title) {
    for (int i = 0; i < tabs.getTabCount(); i++) {
      if (tabs.getTitleAt(i).equals(title)) {
        return i;
      }

    }
    return -1;
  }

  @SuppressWarnings({
    "unchecked", "rawtypes"
  })
  public static <V> V getValue(final JComponent component) {
    if (component instanceof Field) {
      final Field field = (Field)component;
      return (V)field.getFieldValue();
    } else if (component instanceof JXDatePicker) {
      final JXDatePicker dateField = (JXDatePicker)component;
      return (V)dateField.getDate();
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

  public static boolean isLeftButtonAndNoModifiers(final MouseEvent event) {
    final int modifiers = event.getModifiers();
    return SwingUtilities.isLeftMouseButton(event)
      && InputEvent.BUTTON1_MASK == modifiers;
  }

  public static void saveFileChooserDirectory(final Class<?> preferencesClass,
    final String preferenceName, final JFileChooser fileChooser) {
    final File currentDirectory = fileChooser.getCurrentDirectory();
    final String path = FileUtil.getCanonicalPath(currentDirectory);
    PreferencesUtil.setString(preferencesClass, preferenceName, path);
  }

  @SuppressWarnings({
    "unchecked", "rawtypes"
  })
  public static void setFieldValue(final JComponent field, final Object value) {
    if (field instanceof Field) {
      final Field fieldObject = (Field)field;
      fieldObject.setFieldValue(value);
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
}
