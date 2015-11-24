package com.revolsys.swing;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Insets;
import java.awt.KeyboardFocusManager;
import java.awt.LayoutManager;
import java.awt.MenuContainer;
import java.awt.MenuItem;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.SplashScreen;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.datatransfer.DataFlavor;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
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
import javax.swing.JLayeredPane;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.text.JTextComponent;

import com.revolsys.awt.WebColors;
import com.revolsys.datatype.DataType;
import com.revolsys.datatype.DataTypes;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.identifier.Identifier;
import com.revolsys.io.FileUtil;
import com.revolsys.record.code.CodeTable;
import com.revolsys.record.schema.FieldDefinition;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.swing.action.RunnableAction;
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
import com.revolsys.swing.menu.MenuFactory;
import com.revolsys.swing.parallel.Invoke;
import com.revolsys.util.CaseConverter;
import com.revolsys.util.OS;
import com.revolsys.util.PreferencesUtil;
import com.revolsys.util.Property;
import com.revolsys.util.Strings;

public interface SwingUtil {
  static final Font BOLD_FONT = new Font(Font.SANS_SERIF, Font.BOLD, 11);

  static final Font FONT = new Font(Font.SANS_SERIF, Font.PLAIN, 11);

  static void addAction(final JComponent component, final KeyStroke keyStroke,
    final String actionKey, final Runnable runnable) {
    final InputMap inputMap = component.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    inputMap.put(keyStroke, actionKey);

    final ActionMap actionMap = component.getActionMap();
    final RunnableAction action = new RunnableAction(actionKey, runnable);
    actionMap.put(actionKey, action);
    if (component instanceof JComboBox) {
      final JComboBox comboBox = (JComboBox)component;
      final JComponent editorComponent = (JComponent)comboBox.getEditor().getEditorComponent();
      addAction(editorComponent, keyStroke, actionKey, runnable);
    }
  }

  static JComponent addField(final Container panel, final String fieldName,
    final Object fieldValue) {
    return addField(panel, fieldName, fieldName, fieldValue);
  }

  static JComponent addField(final Container panel, final String fieldName, final String label,
    final Object fieldValue) {
    addLabel(panel, label);
    final JComponent field = SwingUtil.newField(fieldValue.getClass(), fieldName, fieldValue);
    panel.add(field);
    return field;
  }

  static JLabel addLabel(final Container container, final String text) {
    final String labelText = CaseConverter.toCapitalizedWords(text) + " ";
    final JLabel label = newLabel(labelText);
    container.add(label);
    return label;
  }

  static void addLabelledReadOnlyTextField(final JPanel container, final String fieldName,
    final Object value) {
    final String string = DataTypes.toString(value);
    final int length = Math.max(1, string.length());
    addLabelledReadOnlyTextField(container, fieldName, value, length);
  }

  static void addLabelledReadOnlyTextField(final JPanel container, final String fieldName,
    final Object value, final int length) {
    addLabel(container, fieldName);
    addReadOnlyTextField(container, fieldName, value, length);
  }

  static void addLayer(final JLayeredPane layeredPane, final JComponent component,
    final Integer layerIndex) {
    layeredPane.add(component, layerIndex);
  }

  static JComponent addObjectField(final Container container, final Object object,
    final String fieldName) {
    return addObjectField(container, object, fieldName, fieldName);
  }

  static JComponent addObjectField(final Container panel, final Object object,
    final String fieldName, final String label) {
    final Object fieldValue = Property.get(object, fieldName);
    return addField(panel, fieldName, label, fieldValue);
  }

  static void addReadOnlyTextField(final JPanel container, final String fieldName,
    final Object value, final int length) {
    final TextField field = new TextField(fieldName, value, length);
    field.setEditable(false);
    container.add(field);
  }

  static Rectangle applyInsets(final Rectangle bounds, final Insets insets) {
    final int x = bounds.x + insets.left;
    final int y = bounds.y + insets.top;
    final int width = bounds.width - insets.left - insets.right;
    final int height = bounds.height - insets.top - insets.bottom;
    return new Rectangle(x, y, width, height);
  }

  static void autoAdjustPosition(final Window window) {
    window.pack();

    final Rectangle bounds = getScreenBounds();
    final int width = Math.min(window.getWidth(), bounds.width - 100);
    final int height = Math.min(window.getHeight(), bounds.height - 100);
    window.setSize(width, height);

    setLocationCentre(bounds, window);
  }

  static void autoAdjustSize(final Window window) {
    window.pack();

    final Rectangle bounds = getScreenBounds();
    final int width = Math.min(window.getWidth(), bounds.width - 20 - window.getX());
    final int height = Math.min(window.getHeight(), bounds.height - 20 - window.getY());
    window.setSize(width, height);
  }

  static void dndCopy(final Component component) {
    final JTextComponent textComponent = getTextComponent(component);
    if (textComponent != null) {
      textComponent.copy();
    }
  }

  static void dndCut(final Component component) {
    final JTextComponent textComponent = getTextComponent(component);
    if (textComponent != null) {
      textComponent.cut();
    }
  }

  static void dndPaste(final Component component) {
    final JTextComponent textComponent = getTextComponent(component);
    if (textComponent != null) {
      textComponent.paste();
    }
  }

  static Window getActiveWindow() {
    final KeyboardFocusManager keyboardFocusManager = KeyboardFocusManager
      .getCurrentKeyboardFocusManager();
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

  static Component getInvoker(final JMenuItem menuItem) {
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

  static Rectangle getScreenBounds() {
    return getScreenBounds((Component)null);
  }

  static Rectangle getScreenBounds(Component component) {
    if (component == null) {
      component = SwingUtil.getActiveWindow();
    }
    final Point mousePosition;
    if (component == null) {
      mousePosition = null;
    } else {
      mousePosition = component.getMousePosition();
      if (mousePosition != null) {
        SwingUtilities.convertPointToScreen(mousePosition, component);
      }
    }
    return getScreenBounds(mousePosition);
  }

  static Rectangle getScreenBounds(final int x, final int y) {
    return getScreenBounds(new Point(x, y));
  }

  /**
   * Get the screen rectangle in which the mouse is in. If the mouse is outside all bounds return the current screen.
   *
   * @param point
   * @return
   */
  static Rectangle getScreenBounds(final Point point) {
    Rectangle firstBounds = null;
    final GraphicsEnvironment graphicsEnvironment = GraphicsEnvironment
      .getLocalGraphicsEnvironment();
    for (final GraphicsDevice device : graphicsEnvironment.getScreenDevices()) {
      for (final GraphicsConfiguration config : device.getConfigurations()) {
        final Rectangle bounds = config.getBounds();

        if (point != null && bounds.contains(point)) {
          final Insets insets = Toolkit.getDefaultToolkit().getScreenInsets(config);
          return applyInsets(bounds, insets);
        } else if (firstBounds == null) {
          firstBounds = bounds;
        }
      }
    }
    final GraphicsDevice defaultScreenDevice = graphicsEnvironment.getDefaultScreenDevice();
    for (final GraphicsConfiguration config : defaultScreenDevice.getConfigurations()) {
      final Rectangle bounds = config.getBounds();
      final Insets insets = Toolkit.getDefaultToolkit().getScreenInsets(config);
      return applyInsets(bounds, insets);
    }
    return firstBounds;
  }

  static int getTabIndex(final JTabbedPane tabs, final String title) {
    for (int i = 0; i < tabs.getTabCount(); i++) {
      if (tabs.getTitleAt(i).equals(title)) {
        return i;
      }

    }
    return -1;
  }

  static JTextComponent getTextComponent(final Component component) {
    if (component instanceof JTextComponent) {
      return (JTextComponent)component;
    } else if (component instanceof JComboBox) {
      final JComboBox<?> comboBox = (JComboBox<?>)component;
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
  static <V> V getValue(final JComponent component) {
    if (component instanceof Field) {
      final Field field = (Field)component;
      return (V)field.getFieldValue();
    } else if (component instanceof JTextComponent) {
      final JTextComponent textComponent = (JTextComponent)component;
      final String text = textComponent.getText();
      if (Property.hasValue(text)) {
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

  static Window getWindowAncestor(final Component component) {
    if (component == null) {
      return null;
    } else {
      return SwingUtilities.getWindowAncestor(component);
    }
  }

  static int getX(final Component component) {
    final int x = component.getX();
    final Component parent = component.getParent();
    if (parent == null) {
      return x;
    } else {
      return x + getX(parent);
    }
  }

  static int getY(final Component component) {
    final int y = component.getY();
    final Component parent = component.getParent();
    if (parent == null) {
      return y;
    } else {
      return y + getY(parent);
    }
  }

  static boolean isAltDown(final InputEvent event) {
    final int modifiersEx = event.getModifiersEx();
    final int flag = modifiersEx & InputEvent.ALT_DOWN_MASK;
    return flag != 0;
  }

  static boolean isControlDown(final InputEvent event) {
    final int modifiersEx = event.getModifiersEx();
    final int flag = modifiersEx & InputEvent.CTRL_DOWN_MASK;
    return flag != 0;
  }

  static boolean isControlOrMetaDown(final InputEvent event) {
    final int modifiersEx = event.getModifiersEx();
    final int flag = modifiersEx & (InputEvent.CTRL_DOWN_MASK | InputEvent.META_DOWN_MASK);
    return flag != 0;
  }

  static boolean isEventDispatchThread() {
    try {
      return SwingUtilities.isEventDispatchThread();
    } catch (final NullPointerException e) {
      return false;
    }
  }

  /**
   * Check to see if the event is for the left mouse button and the Alt key is pressed.
   * Also allows the right mouse button with the control key down. This is so it can
   * work via Citrix Receiver.
   *
   * @param event
   * @return
   */
  static boolean isLeftButtonAndAltDown(final MouseEvent event) {
    if (event.getButton() == MouseEvent.BUTTON1) {
      return isAltDown(event);
    } else if (event.getButton() == MouseEvent.BUTTON3) {
      return isControlDown(event);
    } else {
      return false;
    }
  }

  static boolean isLeftButtonAndNoModifiers(final MouseEvent event) {
    final int modifiers = event.getModifiers();
    return SwingUtilities.isLeftMouseButton(event) && InputEvent.BUTTON1_MASK == modifiers;
  }

  static boolean isLeftButtonOnly(final MouseEvent event) {
    final int modifiers = event.getModifiersEx();
    if ((modifiers & InputEvent.BUTTON1_DOWN_MASK) == InputEvent.BUTTON1_DOWN_MASK) {
      for (int button = 11; button <= 30; button++) {
        final int mask = 1 << button;
        if ((modifiers & mask) == mask) {
          return false;
        }
      }
      return true;
    }
    return false;
  }

  static boolean isMetaDown(final InputEvent event) {
    final int modifiersEx = event.getModifiersEx();
    final int flag = modifiersEx & InputEvent.META_DOWN_MASK;
    return flag != 0;
  }

  static boolean isMiddleDown(final InputEvent event) {
    final int modifiersEx = event.getModifiersEx();
    final int flag = modifiersEx & InputEvent.BUTTON2_DOWN_MASK;
    return flag != 0;
  }

  static boolean isModifierKeyDown(final InputEvent event) {
    final int modifiersEx = event.getModifiersEx();
    final int flag = modifiersEx & (InputEvent.SHIFT_DOWN_MASK | InputEvent.ALT_DOWN_MASK
      | InputEvent.ALT_GRAPH_DOWN_MASK | InputEvent.CTRL_DOWN_MASK | InputEvent.META_DOWN_MASK);
    return flag != 0;
  }

  static boolean isRightDown(final InputEvent event) {
    final int modifiersEx = event.getModifiersEx();
    final int flag = modifiersEx & InputEvent.BUTTON3_DOWN_MASK;
    return flag != 0;
  }

  static boolean isScrollReversed() {
    if (OS.isMac()) {
      final String[] cmdAttribs = new String[] {
        "/usr/bin/defaults", "read",
        System.getProperty("user.home") + "/Library/Preferences/.GlobalPreferences.plist",
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
        inr = new BufferedReader(FileUtil.newUtf8Reader(in));
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

  static boolean isShiftDown(final InputEvent event) {
    final int modifiersEx = event.getModifiersEx();
    final int flag = modifiersEx & InputEvent.SHIFT_DOWN_MASK;
    return flag != 0;
  }

  static ComboBox<Identifier> newComboBox(final CodeTable codeTable, final boolean required,
    final int maxLength) {
    return newComboBox("fieldValue", codeTable, required, maxLength);
  }

  static ComboBox<Identifier> newComboBox(final String fieldName, final CodeTable codeTable,
    final boolean required, final int maxLength) {
    if (codeTable == null) {
      return null;
    } else {
      final ComboBox<Identifier> comboBox = CodeTableComboBoxModel.newComboBox(fieldName, codeTable,
        !required);
      if (comboBox.getModel().getSize() > 0) {
        comboBox.setSelectedIndex(0);
      }
      int longestLength = -1;
      for (final Entry<Identifier, List<Object>> codes : codeTable.getCodes().entrySet()) {
        final List<Object> values = codes.getValue();
        if (values != null && !values.isEmpty()) {
          final String text = Strings.toString(values);
          final int length = text.length();
          if (length > longestLength) {
            longestLength = length;
          }
        }
      }
      if (longestLength == -1) {
        longestLength = 10;
      }
      if (maxLength > 0 && longestLength > maxLength) {
        longestLength = maxLength;
      }
      final StringBuilder value = new StringBuilder();
      for (int i = 0; i < longestLength; i++) {
        value.append("W");
      }
      comboBox.setPrototypeDisplayValue(Identifier.newIdentifier(value));

      final ComboBoxEditor editor = comboBox.getEditor();
      final Component editorComponent = editor.getEditorComponent();
      if (editorComponent instanceof JTextComponent) {
        final JTextField textComponent = (JTextField)editorComponent;
        textComponent.setColumns((int)(longestLength * 0.8));
        final MenuFactory menu = MenuFactory.getPopupMenuFactory(textComponent);
        MenuFactory.addToComponent(comboBox, menu);
      } else {
        MenuFactory.getPopupMenuFactory(comboBox);
      }
      return comboBox;
    }
  }

  static DataFlavor newDataFlavor(final String mimeType) {
    try {
      return new DataFlavor(mimeType);
    } catch (final ClassNotFoundException e) {
      throw new IllegalArgumentException("Cannot create data flavor for " + mimeType, e);
    }
  }

  static DateField newDateField(final String fieldName) {
    final DateField dateField = new DateField(fieldName);
    dateField.setFormats("yyyy-MM-dd", "yyyy/MM/dd", "yyyy-MMM-dd", "yyyy/MMM/dd");
    MenuFactory.getPopupMenuFactory(dateField.getEditor());
    return dateField;
  }

  @SuppressWarnings("unchecked")
  static <T extends JComponent> T newField(final Class<?> fieldClass, final String fieldName,
    final Object fieldValue) {
    JComponent field;
    if (Number.class.isAssignableFrom(fieldClass)) {
      final NumberTextField numberTextField = new NumberTextField(fieldName, DataTypes.DOUBLE, 10,
        2);
      if (fieldValue instanceof Number) {
        final Number number = (Number)fieldValue;
        numberTextField.setFieldValue(number);
      }
      field = numberTextField;
    } else if (Date.class.isAssignableFrom(fieldClass)) {
      final DateField dateField = newDateField(fieldName);
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
      MenuFactory.getPopupMenuFactory(textField);
      field = textField;
    }
    if (field instanceof JTextField) {
      final JTextField textField = (JTextField)field;
      final int preferedWidth = textField.getPreferredSize().width;
      textField.setMinimumSize(new Dimension(preferedWidth, 0));
      textField.setMaximumSize(new Dimension(preferedWidth, Integer.MAX_VALUE));
      textField.setText(DataTypes.toString(fieldValue));
    }
    field.setFont(FONT);

    return (T)field;
  }

  static <T extends JComponent> T newField(final DataType dataType, final String fieldName,
    final Object fieldValue) {
    return newField(dataType.getJavaClass(), fieldName, fieldValue);
  }

  @SuppressWarnings("unchecked")
  static <T extends Field> T newField(final RecordDefinition recordDefinition,
    final String fieldName, final boolean editable) {
    Field field;
    final FieldDefinition fieldDefinition = recordDefinition.getField(fieldName);
    if (fieldDefinition == null) {
      throw new IllegalArgumentException("Cannot find field " + fieldName);
    } else {
      final boolean required = fieldDefinition.isRequired();
      final int length = fieldDefinition.getLength();
      CodeTable codeTable;
      if (recordDefinition.getIdFieldNames().contains(fieldName)) {
        codeTable = null;
      } else {
        codeTable = recordDefinition.getCodeTableByFieldName(fieldName);
      }

      final DataType type = fieldDefinition.getType();
      int columns = length;
      if (columns <= 0) {
        columns = 10;
      } else if (columns > 50) {
        columns = 50;
      }
      final Class<?> javaClass = type.getJavaClass();
      if (codeTable != null) {
        if (editable) {
          final JComponent component = codeTable.getSwingEditor();
          if (component == null) {
            field = newComboBox(fieldName, codeTable, required, -1);
          } else {
            field = ((Field)component).clone();
          }
        } else {
          field = new ObjectLabelField(fieldName, columns, codeTable);
        }
      } else if (!editable) {
        final TextField textField = newTextField(fieldName, columns);
        textField.setEditable(false);
        field = textField;
      } else if (Number.class.isAssignableFrom(javaClass)) {
        final int scale = fieldDefinition.getScale();
        final Number minValue = fieldDefinition.getMinValue();
        final Number maxValue = fieldDefinition.getMaxValue();
        final NumberTextField numberTextField = new NumberTextField(fieldName, type, length, scale,
          minValue, maxValue);
        field = numberTextField;
      } else if (Date.class.isAssignableFrom(javaClass)) {
        field = newDateField(fieldName);
      } else if (Geometry.class.isAssignableFrom(javaClass)) {
        field = new ObjectLabelField(fieldName);
      } else {
        field = newTextField(fieldName, columns);
      }
    }
    if (field instanceof JTextField) {
      final JTextField textField = (JTextField)field;
      final int preferedWidth = textField.getPreferredSize().width;
      textField.setMinimumSize(new Dimension(preferedWidth, 0));
      textField.setMaximumSize(new Dimension(preferedWidth, Integer.MAX_VALUE));
    }

    ((JComponent)field).setFont(FONT);
    return (T)field;
  }

  static JFileChooser newFileChooser(final Class<?> preferencesClass, final String preferenceName) {
    final JFileChooser fileChooser = new JFileChooser();
    fileChooser.setDialogTitle("Open File");
    final String currentDirectoryName = PreferencesUtil.getString(preferencesClass, preferenceName);
    if (Property.hasValue(currentDirectoryName)) {
      final File directory = new File(currentDirectoryName);
      if (directory.exists() && directory.canRead()) {
        fileChooser.setCurrentDirectory(directory);
      }
    }
    return fileChooser;
  }

  static JFileChooser newFileChooser(final String title, final String preferencesGroup,
    final String preferenceName) {
    final JFileChooser fileChooser = new JFileChooser();
    fileChooser.setDialogTitle(title);
    final String currentDirectoryName = PreferencesUtil.getUserString(preferencesGroup,
      preferenceName);
    if (Property.hasValue(currentDirectoryName)) {
      final File directory = new File(currentDirectoryName);
      if (directory.exists() && directory.canRead()) {
        fileChooser.setCurrentDirectory(directory);
      }
    }
    return fileChooser;
  }

  static JLabel newLabel(final String text) {
    final JLabel label = new JLabel(text);
    label.setFont(BOLD_FONT);
    return label;
  }

  static TextArea newTextArea(final int rows, final int columns) {
    final TextArea textField = new TextArea(rows, columns);
    return textField;
  }

  static TextArea newTextArea(final String fieldName, final int rows, final int columns) {
    final TextArea textField = new TextArea(fieldName, rows, columns);
    return textField;
  }

  static TextField newTextField(final int columns) {
    final TextField textField = new TextField(columns);
    return textField;
  }

  static TextField newTextField(final String fieldName, final int columns) {
    final TextField textField = new TextField(fieldName, columns);
    return textField;
  }

  static void saveFileChooserDirectory(final Class<?> preferencesClass, final String preferenceName,
    final JFileChooser fileChooser) {
    final File currentDirectory = fileChooser.getCurrentDirectory();
    final String path = FileUtil.getCanonicalPath(currentDirectory);
    PreferencesUtil.setString(preferencesClass, preferenceName, path);
  }

  static void setDescendantsEnabled(final Component component, final boolean enabled) {
    if (component != null) {
      component.setEnabled(enabled);
      if (component instanceof Container) {
        final Container container = (Container)component;
        for (final Component child : container.getComponents()) {
          setDescendantsEnabled(child, enabled);
        }
      }
    }
  }

  static void setFieldValue(final JComponent field, final Object value) {
    Invoke.later(() -> {
      if (field instanceof Field) {
        final Field fieldObject = (Field)field;
        fieldObject.setFieldValue(value);
      } else if (field instanceof JLabel) {
        final JLabel label = (JLabel)field;
        String string;
        if (value == null) {
          string = "";
        } else {
          string = DataTypes.toString(value);
        }
        label.setText(string);
      } else if (field instanceof JTextField) {
        final JTextField textField = (JTextField)field;
        String string;
        if (value == null) {
          string = "";
        } else {
          string = DataTypes.toString(value);
        }
        textField.setText(string);
      } else if (field instanceof JTextArea) {
        final JTextArea textField = (JTextArea)field;
        String string;
        if (value == null) {
          string = "";
        } else {
          string = DataTypes.toString(value);
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
    });
  }

  static void setLocationCentre(final Rectangle bounds, final Window window) {
    final int width = window.getWidth();
    final int height = window.getHeight();

    final int x = bounds.x + (bounds.width - width) / 2;
    final int y = bounds.y + (bounds.height - height) / 2;

    window.setLocation(x, y);
  }

  static void setLocationCentre(final Window window) {
    final Rectangle bounds = getScreenBounds();
    setLocationCentre(bounds, window);
  }

  /**
   * Set the location of the window in relation to the top left of the screen the current window is on
   *
   * @param window
   * @param x
   * @param y
   */
  static void setLocationOffset(final Window window, int x, int y) {
    final Rectangle bounds = getScreenBounds();

    x += bounds.x;
    y += bounds.y;

    window.setLocation(x, y);

  }

  static void setMaximumWidth(final JComponent component, final int width) {
    final Dimension preferredSize = component.getPreferredSize();
    final Dimension size = new Dimension(width, preferredSize.height);
    component.setMaximumSize(size);
  }

  static void setSize(final Window window, final int minusX, final int minusY) {
    final Toolkit toolkit = Toolkit.getDefaultToolkit();
    final Dimension screenSize = toolkit.getScreenSize();
    final double screenWidth = screenSize.getWidth();
    final double screenHeight = screenSize.getHeight();
    final Dimension size = new Dimension((int)(screenWidth - minusX), (int)(screenHeight - minusY));
    window.setBounds(minusX / 2, minusY / 2, size.width, size.height);
    window.setPreferredSize(size);
  }

  static void setSizeAndMaximize(final JFrame frame, final int minusX, final int minusY) {
    final Rectangle bounds = getScreenBounds((Point)null);
    final Dimension size = new Dimension(bounds.width - minusX, bounds.height - minusY);
    frame.setPreferredSize(size);
    frame.pack();
    frame.setExtendedState(Frame.MAXIMIZED_BOTH);
    frame.setState(Frame.MAXIMIZED_BOTH);
    setLocationCentre(bounds, frame);
  }

  static void setSplashTitle(final String title) {
    final SplashScreen splash = SplashScreen.getSplashScreen();
    if (splash != null) {
      final Graphics2D graphics = splash.createGraphics();
      if (graphics != null) {
        graphics.setColor(WebColors.Black);
        graphics.setFont(new Font("sans-serif", Font.BOLD, 14));
        graphics.drawString("Starting application: ", 100, 100);
        graphics.setFont(new Font("sans-serif", Font.PLAIN, 12));
        graphics.drawString(title, 100, 130);
        splash.update();
      }
    }
  }

  static void setTitledBorder(final JComponent component, final String title) {
    if (component != null) {
      final TitledBorder border = new TitledBorder(title);
      component.setBorder(border);
      component.setBackground(WebColors.White);
    }
  }

  static void setVisible(final Component component, final boolean visible) {
    if (component != null) {
      Invoke.later(() -> {
        component.setVisible(visible);
      });
    }
  }

  static void showErrorDialog(final Window window, final String title, final String message,
    final Throwable e) {
    final String exceptionMessage = e.getMessage().replaceAll("\n", "<br />");
    final String errorMessage = "<html><body><p style=\"margin-bottom: 10px\"><strong>" + message
      + "</strong></p><pre>" + exceptionMessage + "</pre></body></p>";

    final JScrollPane scrollPane = new JScrollPane(new JLabel(errorMessage));
    final Dimension preferredSize = scrollPane.getPreferredSize();
    final Rectangle bounds = SwingUtil.getScreenBounds(window);
    final int width = Math.min(bounds.width - 200, preferredSize.width + 20);
    final int height = Math.min(bounds.height - 100, preferredSize.height + 20);

    scrollPane.setPreferredSize(new Dimension(width, height));

    JOptionPane.showMessageDialog(window, scrollPane, title, JOptionPane.ERROR_MESSAGE);
  }
}
