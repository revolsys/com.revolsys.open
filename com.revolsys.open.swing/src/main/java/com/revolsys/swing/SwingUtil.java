package com.revolsys.swing;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.KeyboardFocusManager;
import java.awt.LayoutManager;
import java.awt.MenuContainer;
import java.awt.MenuItem;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.datatransfer.DataFlavor;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map.Entry;

import javax.swing.ActionMap;
import javax.swing.ComboBoxEditor;
import javax.swing.InputMap;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.text.JTextComponent;

import org.springframework.util.StringUtils;

import com.revolsys.awt.WebColors;
import com.revolsys.beans.MethodInvoker;
import com.revolsys.converter.string.StringConverterRegistry;
import com.revolsys.gis.data.model.Attribute;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.gis.data.model.codes.CodeTable;
import com.revolsys.gis.data.model.types.DataType;
import com.revolsys.gis.data.model.types.DataTypes;
import com.revolsys.io.FileUtil;
import com.revolsys.swing.action.InvokeMethodAction;
import com.revolsys.swing.border.TitledBorder;
import com.revolsys.swing.field.CheckBox;
import com.revolsys.swing.field.CodeTableComboBoxModel;
import com.revolsys.swing.field.ColorChooserField;
import com.revolsys.swing.field.ComboBox;
import com.revolsys.swing.field.DateField;
import com.revolsys.swing.field.Field;
import com.revolsys.swing.field.NumberTextField;
import com.revolsys.swing.field.ObjectLabelField;
import com.revolsys.swing.field.TextArea;
import com.revolsys.swing.field.TextField;
import com.revolsys.swing.menu.PopupMenu;
import com.revolsys.swing.parallel.Invoke;
import com.revolsys.util.CaseConverter;
import com.revolsys.util.CollectionUtil;
import com.revolsys.util.ExceptionUtil;
import com.revolsys.util.OS;
import com.revolsys.util.PreferencesUtil;
import com.revolsys.util.Property;
import com.vividsolutions.jts.geom.Geometry;

public class SwingUtil {
  public static final Font FONT = new Font(Font.SANS_SERIF, Font.PLAIN, 11);

  public static final Font BOLD_FONT = new Font(Font.SANS_SERIF, Font.BOLD, 11);

  public static void addAction(final JComponent component,
    final KeyStroke keyStroke, final String actionKey, final Object object,
    final String methodName, final Object... parameters) {
    final InputMap inputMap = component.getInputMap(JTable.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    inputMap.put(keyStroke, actionKey);

    final ActionMap actionMap = component.getActionMap();
    final InvokeMethodAction action = new InvokeMethodAction(actionKey, object,
      methodName, parameters);
    actionMap.put(actionKey, action);
    if (component instanceof JComboBox) {
      final JComboBox comboBox = (JComboBox)component;
      final JComponent editorComponent = (JComponent)comboBox.getEditor()
        .getEditorComponent();
      addAction(editorComponent, keyStroke, actionKey, object, methodName,
        parameters);
    }
  }

  public static JComponent addField(final Container container,
    final Object object, final String fieldName) {
    return addField(container, object, fieldName, fieldName);
  }

  public static JComponent addField(final Container panel, final Object object,
    final String fieldName, final String label) {
    addLabel(panel, label);
    final Object fieldValue = Property.get(object, fieldName);
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

  public static void addReadOnlyTextField(final JPanel container,
    final String fieldName, final Object value, final int length) {
    addLabel(container, fieldName);
    final TextField crsField = new TextField(fieldName, value, length);
    crsField.setEditable(false);
    container.add(crsField);
  }

  public static void autoAdjustPosition(final Window window) {
    window.pack();
    int x = window.getX();
    int y = window.getY();
    // TODO consider centering
    if (x == 0) {
      x = 10;
    }
    if (y == 0) {
      y = 100;
    }
    window.setLocation(x, y);
    int width = window.getWidth();
    int height = window.getHeight();
    final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    if (width > 100) {
      if (x + width > screenSize.width) {
        width = screenSize.width - x - 10;
      }
    }
    if (height > 100) {
      if (y + height > screenSize.height - 100) {
        height = screenSize.height - y - 100;
      }
    }
    window.setSize(width, height);
  }

  public static ComboBox createComboBox(final CodeTable codeTable,
    final boolean required) {
    return createComboBox("fieldValue", codeTable, required);
  }

  public static ComboBox createComboBox(final String fieldName,
    final CodeTable codeTable, final boolean required) {

    final ComboBox comboBox = CodeTableComboBoxModel.create(fieldName,
      codeTable, !required);
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

    final ComboBoxEditor editor = comboBox.getEditor();
    final Component editorComponent = editor.getEditorComponent();
    if (editorComponent instanceof JTextComponent) {
      final JTextField textComponent = (JTextField)editorComponent;
      textComponent.setColumns((int)(longestValue.length() * 0.8));
      final PopupMenu menu = PopupMenu.getPopupMenu(textComponent);
      menu.addToComponent(comboBox);
    } else {
      PopupMenu.getPopupMenuFactory(comboBox);
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

  public static DateField createDateField(final String fieldName) {
    final DateField dateField = new DateField(fieldName);
    dateField.setFormats("yyyy-MM-dd", "yyyy/MM/dd", "yyyy-MMM-dd",
      "yyyy/MMM/dd");
    PopupMenu.getPopupMenuFactory(dateField.getEditor());
    return dateField;
  }

  @SuppressWarnings("unchecked")
  public static <T extends JComponent> T createField(final Class<?> fieldClass,
    final String fieldName, final Object fieldValue) {
    JComponent field;
    if (Number.class.isAssignableFrom(fieldClass)) {
      final NumberTextField numberTextField = new NumberTextField(fieldName,
        DataTypes.DOUBLE, 10, 2);
      if (fieldValue instanceof Number) {
        final Number number = (Number)fieldValue;
        numberTextField.setFieldValue(number);
      }
      field = numberTextField;
    } else if (Date.class.isAssignableFrom(fieldClass)) {
      final DateField dateField = createDateField(fieldName);
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
    } else if (Boolean.class.isAssignableFrom(fieldClass)) {
      field = new CheckBox(fieldName, fieldValue);
    } else {
      final TextField textField = new TextField(fieldName, fieldValue);
      textField.setColumns(50);
      PopupMenu.getPopupMenuFactory(textField);
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
  public static <T extends Field> T createField(
    final DataObjectMetaData metaData, final String fieldName,
    final boolean editable) {
    Field field;
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
        final TextField textField = createTextField(fieldName, columns);
        textField.setEditable(false);
        field = textField;
      } else if (codeTable != null) {
        final JComponent component = codeTable.getSwingEditor();
        if (component == null) {
          field = createComboBox(fieldName, codeTable, required);
        } else {
          field = (Field)component;
        }
      } else if (Number.class.isAssignableFrom(type.getJavaClass())) {
        final int scale = attribute.getScale();
        final Number minValue = attribute.getMinValue();
        final Number maxValue = attribute.getMaxValue();
        final NumberTextField numberTextField = new NumberTextField(fieldName,
          type, length, scale, minValue, maxValue);
        field = numberTextField;
      } else if (type.equals(DataTypes.DATE)) {
        field = createDateField(fieldName);
      } else if (Geometry.class.isAssignableFrom(type.getJavaClass())) {
        field = new ObjectLabelField(fieldName);
      } else {
        field = createTextField(fieldName, columns);
      }
      if (field instanceof JTextField) {
        final JTextField textField = (JTextField)field;
        final int preferedWidth = textField.getPreferredSize().width;
        textField.setMinimumSize(new Dimension(preferedWidth, 0));
        textField.setMaximumSize(new Dimension(preferedWidth, Integer.MAX_VALUE));

      }
    }

    ((JComponent)field).setFont(FONT);
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

  public static TextArea createTextArea(final int rows, final int columns) {
    final TextArea textField = new TextArea(rows, columns);
    return textField;
  }

  public static TextArea createTextArea(final String fieldName, final int rows,
    final int columns) {
    final TextArea textField = new TextArea(fieldName, rows, columns);
    return textField;
  }

  public static TextField createTextField(final int columns) {
    final TextField textField = new TextField(columns);
    return textField;
  }

  public static TextField createTextField(final String fieldName,
    final int columns) {
    final TextField textField = new TextField(fieldName, columns);
    return textField;
  }

  public static void dndCopy(final Component component) {
    final JTextComponent textComponent = getTextComponent(component);
    if (textComponent != null) {
      textComponent.copy();
    }
  }

  public static void dndCut(final Component component) {
    final JTextComponent textComponent = getTextComponent(component);
    if (textComponent != null) {
      textComponent.cut();
    }
  }

  public static void dndPaste(final Component component) {
    final JTextComponent textComponent = getTextComponent(component);
    if (textComponent != null) {
      textComponent.paste();
    }
  }

  public static Window getActiveWindow() {
    final KeyboardFocusManager keyboardFocusManager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
    final Window activeWindow = keyboardFocusManager.getActiveWindow();
    if (activeWindow == null) {
      final Window[] windows = Window.getOwnerlessWindows();
      for (final Window window : windows) {
        if (window.isVisible()) {
          return window;
        }
      }
    }
    return activeWindow;
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

  public static JTextComponent getTextComponent(final Component component) {
    if (component instanceof JTextComponent) {
      return (JTextComponent)component;
    } else if (component instanceof JComboBox) {
      final JComboBox comboBox = (JComboBox)component;
      final ComboBoxEditor editor = comboBox.getEditor();
      final Component editorComponent = editor.getEditorComponent();
      return getTextComponent(editorComponent);
    } else {
      return null;
    }
  }

  @SuppressWarnings({
    "unchecked"
  })
  public static <V> V getValue(final JComponent component) {
    if (component instanceof Field) {
      final Field field = (Field)component;
      return (V)field.getFieldValue();
    } else if (component instanceof JTextComponent) {
      final JTextComponent textComponent = (JTextComponent)component;
      final String text = textComponent.getText();
      if (StringUtils.hasText(text)) {
        return (V)text;
      } else {
        return null;
      }
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

  public static Window getWindowAncestor(final Component component) {
    if (component == null) {
      return null;
    } else {
      return SwingUtilities.getWindowAncestor(component);
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

  public static boolean isScrollReversed() {
    if (OS.isMac()) {
      final String[] cmdAttribs = new String[] {
        "/usr/bin/defaults",
        "read",
        System.getProperty("user.home")
          + "/Library/Preferences/.GlobalPreferences.plist",
        "com.apple.swipescrolldirection"
      };
      Process process = null;
      InputStream in = null;
      OutputStream out = null;
      InputStream err = null;
      BufferedReader inr = null;
      final List<String> lines = new ArrayList<String>();
      try {

        process = Runtime.getRuntime().exec(cmdAttribs);
        in = process.getInputStream();
        out = process.getOutputStream();
        err = process.getErrorStream();
        inr = new BufferedReader(new InputStreamReader(in));
        String line = inr.readLine();
        while (line != null) {
          line = line.toLowerCase().trim();
          lines.add(line);
          line = inr.readLine();
        }

        process.waitFor();
        if (process.exitValue() == 0) {
          if (lines.size() == 1) {
            final String result = lines.get(0);
            return "1".equals(result);
          }
        }
      } catch (final Throwable e) {
      } finally {
        FileUtil.closeSilent(in);
        FileUtil.closeSilent(out);
        FileUtil.closeSilent(err);
        FileUtil.closeSilent(inr);
        if (process != null) {
          process.destroy();
        }
      }
      return true;
    }
    return false;
  }

  public static void saveFileChooserDirectory(final Class<?> preferencesClass,
    final String preferenceName, final JFileChooser fileChooser) {
    final File currentDirectory = fileChooser.getCurrentDirectory();
    final String path = FileUtil.getCanonicalPath(currentDirectory);
    PreferencesUtil.setString(preferencesClass, preferenceName, path);
  }

  public static void setFieldValue(final JComponent field, final Object value) {
    if (SwingUtilities.isEventDispatchThread()) {
      if (field instanceof Field) {
        final Field fieldObject = (Field)field;
        fieldObject.setFieldValue(value);
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
      }
      final Container parent = field.getParent();
      if (parent != null) {
        final LayoutManager layout = parent.getLayout();
        if (layout != null) {
          layout.layoutContainer(parent);
        }
        field.revalidate();
      }
    } else {
      try {
        final Method method = SwingUtil.class.getMethod("setFieldValue",
          JComponent.class, Object.class);
        final MethodInvoker runnable = new MethodInvoker(method,
          SwingUtil.class, field, value);
        Invoke.later(runnable);
      } catch (final Throwable t) {
        ExceptionUtil.throwUncheckedException(t);
      }
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
    frame.setLocation(minusX / 2, minusY / 2);
    frame.setSize(size);
    frame.setPreferredSize(size);
    frame.setExtendedState(frame.getExtendedState() | Frame.MAXIMIZED_BOTH);
    frame.setState(frame.getExtendedState() | Frame.MAXIMIZED_BOTH);
  }

  public static void setTitledBorder(final JComponent component,
    final String title) {
    if (component != null) {
      final TitledBorder border = new TitledBorder(title);
      component.setBorder(border);
      component.setBackground(WebColors.White);
    }
  }

}
