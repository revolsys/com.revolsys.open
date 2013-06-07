package com.revolsys.swing.map.form;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.ComboBoxEditor;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.TransferHandler;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.text.JTextComponent;

import org.jdesktop.swingx.JXDatePicker;
import org.springframework.util.StringUtils;

import com.revolsys.converter.string.StringConverterRegistry;
import com.revolsys.gis.data.io.DataObjectStore;
import com.revolsys.gis.data.model.Attribute;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.gis.data.model.codes.CodeTable;
import com.revolsys.gis.data.model.property.DirectionalAttributes;
import com.revolsys.gis.data.model.types.DataType;
import com.revolsys.gis.data.model.types.DataTypes;
import com.revolsys.gis.model.data.equals.EqualsRegistry;
import com.revolsys.swing.SwingUtil;
import com.revolsys.swing.action.enablecheck.EnableCheck;
import com.revolsys.swing.action.enablecheck.ObjectPropertyEnableCheck;
import com.revolsys.swing.builder.DataObjectMetaDataUiBuilderRegistry;
import com.revolsys.swing.field.NumberTextField;
import com.revolsys.swing.field.ObjectLabelField;
import com.revolsys.swing.field.ValidatingField;
import com.revolsys.swing.layout.GroupLayoutUtil;
import com.revolsys.swing.map.util.LayerUtil;
import com.revolsys.swing.table.BaseJxTable;
import com.revolsys.swing.table.dataobject.AbstractDataObjectTableModel;
import com.revolsys.swing.table.dataobject.DataObjectMapTableModel;
import com.revolsys.swing.table.dataobject.DataObjectTableCellEditor;
import com.revolsys.swing.table.dataobject.DataObjectTableCellRenderer;
import com.revolsys.swing.table.dataobject.ExcludeGeometryRowFilter;
import com.revolsys.swing.toolbar.ToolBar;
import com.revolsys.util.CaseConverter;
import com.revolsys.util.CollectionUtil;
import com.vividsolutions.jts.geom.Geometry;

public class DataObjectForm extends JPanel implements FocusListener,
  CellEditorListener {

  private static final long serialVersionUID = 1L;

  private DataObjectMapTableModel allAttributes;

  private DataObjectStore dataStore;

  private final Map<String, Object> disabledFieldValues = new HashMap<String, Object>();

  private final Map<String, String> fieldInValidMessage = new HashMap<String, String>();

  private final Map<String, JComponent> fields = new LinkedHashMap<String, JComponent>();

  private boolean fieldsValid;

  private final Map<Component, String> fieldToNameMap = new HashMap<Component, String>();

  private boolean fieldValidationEnabled = true;

  private final Map<String, Boolean> fieldValidMap = new HashMap<String, Boolean>();

  private GeometryCoordinatesPanel geometryCoordinatesPanel;

  private DataObjectMetaData metaData;

  private DataObject object;

  private final Map<String, String> originalToolTip = new HashMap<String, String>();

  private Object previousValue;

  private Set<String> readOnlyFieldNames = new HashSet<String>();

  private Set<String> requiredFieldNames = new HashSet<String>();

  private final Color selectedTextColor = new JTextField().getSelectedTextColor();

  private final JTabbedPane tabs = new JTabbedPane();

  private final Map<String, Boolean> tabValid = new HashMap<String, Boolean>();

  private final Color textBackgroundColor = new JTextField().getBackground();

  private final Color textColor = new JTextField().getForeground();

  private ToolBar toolBar;

  private final DataObjectMetaDataUiBuilderRegistry uiBuilderRegistry = new DataObjectMetaDataUiBuilderRegistry();

  private Map<String, Object> originalValues = Collections.emptyMap();

  private boolean editable = true;

  public DataObjectForm(final DataObject object) {
    this(object.getMetaData());
    setObject(object);
  }

  public DataObjectForm(final DataObjectMetaData metaData) {
    super(new BorderLayout());
    setMetaData(metaData);
    addToolBar();

    final ActionMap map = getActionMap();
    map.put("copy", TransferHandler.getCopyAction());
    map.put("paste", TransferHandler.getPasteAction());

    final DataObjectFormTransferHandler transferHandler = new DataObjectFormTransferHandler(
      this);
    setTransferHandler(transferHandler);
    setFont(SwingUtil.FONT);
  }

  public void actionZoomToObject() {
    LayerUtil.zoomToObject(getObject());
  }

  protected ObjectLabelField addCodeTableLabelField(final String fieldName) {
    final DataObjectStore dataStore = getDataStore();
    final CodeTable codeTable = dataStore.getCodeTableByColumn(fieldName);
    final ObjectLabelField field = new ObjectLabelField(fieldName, codeTable);
    addField(fieldName, field);
    return field;
  }

  protected void addDoubleField(final String fieldName, final int length,
    final int scale, final Double minimumValie, final Double maximumValue) {
    final DataType dataType = DataTypes.DOUBLE;
    final NumberTextField field = new NumberTextField(dataType, length, scale,
      minimumValie, maximumValue);
    addField(fieldName, field);
  }

  public void addField(final Container container, JComponent field) {
    if (field instanceof JTextArea) {
      final JTextArea textArea = (JTextArea)field;
      field = new JScrollPane(textArea);
    }
    container.add(field);
  }

  public JComponent addField(final String fieldName, final JComponent field) {
    if (field instanceof JTextField) {
      final JTextField textField = (JTextField)field;
      final int preferedWidth = textField.getPreferredSize().width;
      textField.setMinimumSize(new Dimension(preferedWidth, 0));
      textField.setMaximumSize(new Dimension(preferedWidth, Integer.MAX_VALUE));
    }
    originalToolTip.put(fieldName, field.getToolTipText());
    field.addFocusListener(this);
    fields.put(fieldName, field);
    fieldToNameMap.put(field, fieldName);
    return field;
  }

  public void addLabelledField(final Container container, final String fieldName) {
    final JLabel label = getLabel(fieldName);
    container.add(label);
    final JComponent field = getField(fieldName);
    addField(container, field);
  }

  protected void addNumberField(final String fieldName,
    final DataType dataType, final int length, final Number minimumValue,
    final Number maximumValue) {
    final NumberTextField field = new NumberTextField(dataType, length, 0,
      minimumValue, maximumValue);
    addField(fieldName, field);
  }

  protected void addPanel(final JPanel container, final String title,
    final List<String> fieldNames) {
    final JPanel panel = new JPanel();
    container.add(panel);

    panel.setBorder(BorderFactory.createTitledBorder(title));

    for (final String fieldName : fieldNames) {
      addLabelledField(panel, fieldName);
    }

    GroupLayoutUtil.makeColumns(panel, 2);
  }

  public void addReadOnlyFieldNames(final Collection<String> readOnlyFieldNames) {
    this.readOnlyFieldNames.addAll(readOnlyFieldNames);
    for (final Entry<String, JComponent> entry : fields.entrySet()) {
      final String name = entry.getKey();
      final JComponent field = entry.getValue();
      if (this.readOnlyFieldNames.contains(name)) {
        field.setEnabled(false);
      } else {
        field.setEnabled(true);
      }
    }
  }

  public void addReadOnlyFieldNames(final String... readOnlyFieldNames) {
    addReadOnlyFieldNames(Arrays.asList(readOnlyFieldNames));
  }

  public void addRequiredFieldNames(final Collection<String> requiredFieldNames) {
    this.requiredFieldNames.addAll(requiredFieldNames);
  }

  public void addRequiredFieldNames(final String... requiredFieldNames) {
    addRequiredFieldNames(Arrays.asList(requiredFieldNames));
  }

  public void addTab(final int index, final String name,
    final Component component) {
    boolean init = false;
    final Container parent = tabs.getParent();
    if (parent != this) {
      add(tabs, BorderLayout.CENTER);
      init = true;
    }
    tabs.insertTab(name, null, component, null, index);
    if (init) {
      tabs.setSelectedIndex(0);
    }
  }

  public void addTab(final String name, final Component component) {
    addTab(tabs.getComponentCount(), name, component);
  }

  protected void addTabAllAttributes() {
    allAttributes = new DataObjectMapTableModel(getMetaData(), getValues(),
      true);
    allAttributes.setReadOnlyFieldNames(getReadOnlyFieldNames());
    final BaseJxTable table = AbstractDataObjectTableModel.create(allAttributes);
    table.setRowFilter(new ExcludeGeometryRowFilter());
    final TableColumnModel columnModel = table.getColumnModel();
    final DataObjectMetaData metaData = getMetaData();
    final DataObjectTableCellRenderer cellRenderer = new DataObjectTableCellRenderer() {
      @Override
      public Component getTableCellRendererComponent(final JTable table,
        Object value, final boolean isSelected, final boolean hasFocus,
        final int row, final int column) {
        final boolean even = row % 2 == 0;

        final String fieldName = (String)table.getValueAt(row, 1);
        final boolean isIdField = fieldName.equals(metaData.getIdAttributeName());
        if (isIdField) {
          if (value == null) {
            value = "NEW";
          }
        }
        final JComponent component = (JComponent)super.getTableCellRendererComponent(
          table, value, isSelected, hasFocus, row, column);
        component.setToolTipText("");
        if (isIdField) {
          setRowColor(table, component, row);
        } else if (isFieldValid(fieldName)) {
          if (hasOriginalValue(fieldName)) {
            final Object fieldValue = getFieldValue(fieldName);
            final Object originalValue = getOriginalValue(fieldName);
            if (!EqualsRegistry.equal(originalValue, fieldValue)) {
              CodeTable codeTable = null;
              if (!isIdField) {
                codeTable = metaData.getCodeTableByColumn(fieldName);
              }
              String text;
              if (value == null) {
                text = "-";
              } else if (codeTable == null) {
                text = StringConverterRegistry.toString(originalValue);
              } else {
                text = codeTable.getValue(originalValue);
                if (text == null) {
                  text = "-";
                }
              }
              component.setToolTipText(text);
              if (isSelected) {
                component.setForeground(Color.GREEN);
              } else {
                component.setForeground(new Color(33, 99, 00));
                if (even) {
                  component.setBackground(new Color(33, 99, 0, 31));
                } else {
                  component.setBackground(new Color(33, 99, 0, 95));
                }
              }
            }
          }
        } else {
          component.setForeground(Color.RED);
          if (!isSelected) {
            if (even) {
              component.setBackground(new Color(255, 175, 175, 127));
            } else {
              component.setBackground(Color.PINK);
            }
          }
          setFieldInvalidToolTip(fieldName, component);
        }
        return component;
      }
    };
    cellRenderer.setUiBuilderRegistry(uiBuilderRegistry);
    for (int i = 0; i < columnModel.getColumnCount(); i++) {
      final TableColumn column = columnModel.getColumn(i);
      column.setCellRenderer(cellRenderer);
      if (i == 2) {
        final TableCellEditor cellEditor = column.getCellEditor();
        if (cellEditor instanceof DataObjectTableCellEditor) {
          final DataObjectTableCellEditor dataObjectCellEditor = (DataObjectTableCellEditor)cellEditor;
          dataObjectCellEditor.setUiBuilderRegistry(uiBuilderRegistry);
        }
        cellEditor.addCellEditorListener(this);
      }
    }
    final JScrollPane scrollPane = new JScrollPane(table);
    int maxHeight = Integer.MAX_VALUE;
    for (final GraphicsDevice device : GraphicsEnvironment.getLocalGraphicsEnvironment()
      .getScreenDevices()) {
      final GraphicsConfiguration graphicsConfiguration = device.getDefaultConfiguration();
      final Rectangle bounds = graphicsConfiguration.getBounds();
      maxHeight = Math.min(bounds.height, maxHeight);
    }
    maxHeight -= 300;
    final int preferredHeight = allAttributes.getRowCount() * 25;
    scrollPane.setMinimumSize(new Dimension(0, preferredHeight));
    scrollPane.setPreferredSize(new Dimension(800, Math.min(maxHeight,
      allAttributes.getRowCount() * 25)));
    scrollPane.setMaximumSize(new Dimension(Integer.MAX_VALUE, maxHeight));
    addTab("All Attributes", scrollPane);
  }

  protected void addTabGeometry() {
    final String geometryAttributeName = metaData.getGeometryAttributeName();
    if (geometryCoordinatesPanel == null && geometryAttributeName != null) {
      geometryCoordinatesPanel = new GeometryCoordinatesPanel(
        geometryAttributeName);
      addField(geometryAttributeName, geometryCoordinatesPanel);
      final JPanel panel = new JPanel(new GridLayout(1, 1));

      geometryCoordinatesPanel.setBorder(BorderFactory.createTitledBorder("Coordinates"));
      panel.add(geometryCoordinatesPanel);

      addTab("Geometry", panel);
    }
  }

  public ToolBar addToolBar() {
    toolBar = new ToolBar();
    add(toolBar, BorderLayout.NORTH);
    final DataObjectMetaData metaData = getMetaData();
    final Attribute geometryAttribute = metaData.getGeometryAttribute();
    final boolean hasGeometry = geometryAttribute != null;
    final EnableCheck editable = new ObjectPropertyEnableCheck(this, "editable");

    // Cut, Copy Paste
    // TODO enable checks

    toolBar.addButton("dnd", "Copy", "page_copy", editable, this,
      "dataTransferCopy");
    toolBar.addButton("dnd", "Paste", "paste_plain", editable, this,
      "dataTransferPaste");

    // Zoom

    if (hasGeometry) {
      toolBar.addButtonTitleIcon("zoom", "Zoom to Object", "magnifier", this,
        "actionZoomToObject");
    }

    // Geometry manipulation

    if (hasGeometry) {
      final DataType geometryDataType = geometryAttribute.getType();
      if (geometryDataType == DataTypes.LINE_STRING
        || geometryDataType == DataTypes.MULTI_LINE_STRING) {
        toolBar.addButton("geometry", "Reverse Geometry", "line_reverse",
          editable, this, "reverseGeometry");
        if (DirectionalAttributes.getProperty(metaData)
          .hasDirectionalAttributes()) {
          toolBar.addButton("geometry", "Reverse Attributes",
            "attributes_reverse", editable, this, "reverseAttributes");
          toolBar.addButton("geometry", "Reverse Geometry & Attributes",
            "attributes_line_reverse", editable, this,
            "reverseAttributesAndGeometry");
        }
      }
    }
    return toolBar;
  }

  public void dataTransferCopy() {
    invokeAction("copy");
  }

  public void dataTransferCut() {

  }

  public void dataTransferPaste() {
    invokeAction("paste");
  }

  @Override
  public void editingCanceled(final ChangeEvent e) {
  }

  @Override
  public void editingStopped(final ChangeEvent e) {
    final DataObjectTableCellEditor editor = (DataObjectTableCellEditor)e.getSource();
    final String name = editor.getAttributeName();
    final Object value = editor.getCellEditorValue();
    setFieldValue(name, value);
  }

  @Override
  public void focusGained(final FocusEvent e) {
    final Object source = e.getSource();
    if (source instanceof Component) {
      final Component component = (Component)source;
      final String fieldName = getFieldName(component);
      if (fieldName != null) {
        previousValue = getFieldValue(fieldName);

      }
    }
  }

  @Override
  public void focusLost(final FocusEvent e) {
    final Object source = e.getSource();
    if (source instanceof Component) {
      final Component component = (Component)source;
      final String fieldName = getFieldName(component);
      if (fieldName != null) {
        final Object value = getFieldValue(fieldName);
        if (!EqualsRegistry.equal(value, previousValue)) {
          object.setValue(fieldName, value);
          validateFields();
          if (allAttributes != null) {
            allAttributes.setValues(getValues());
          }

        }
      }
    }
    previousValue = null;
  }

  public DataObjectMapTableModel getAllAttributes() {
    return allAttributes;
  }

  public String getCodeValue(final String fieldName, final Object value) {
    final CodeTable codeTable = metaData.getCodeTableByColumn(fieldName);
    String string;
    if (value == null) {
      return "-";
    } else if (codeTable == null) {
      string = StringConverterRegistry.toString(value);
    } else {
      final List<Object> values = codeTable.getValues(value);
      if (values == null || values.isEmpty()) {
        string = "-";
      } else {
        string = CollectionUtil.toString(values);
      }
    }
    if (!StringUtils.hasText(string)) {
      string = "-";
    }
    return string;
  }

  public DataObjectStore getDataStore() {
    if (dataStore == null) {
      if (metaData == null) {
        return null;
      } else {
        return metaData.getDataObjectStore();
      }
    } else {
      return dataStore;
    }
  }

  @SuppressWarnings("unchecked")
  protected <T extends JComponent> T getField(final String fieldName) {
    synchronized (fields) {
      JComponent field = fields.get(fieldName);
      if (field == null) {
        final boolean enabled = !readOnlyFieldNames.contains(fieldName);
        field = SwingUtil.createField(metaData, fieldName, enabled);
        if (field instanceof JComboBox) {
          final JComboBox comboBox = (JComboBox)field;
          final ComboBoxEditor editor = comboBox.getEditor();
          final Component editorComponent = editor.getEditorComponent();
          editorComponent.addFocusListener(this);
        }
        addField(fieldName, field);
      }
      if (!isEditable()) {
        field.setEnabled(false);
      }
      return (T)field;
    }
  }

  public String getFieldName(Component field) {
    String fieldName = null;
    do {
      fieldName = fieldToNameMap.get(field);
      field = field.getParent();
    } while (fieldName == null && field != null);
    return fieldName;
  }

  public Collection<JComponent> getFields() {
    return fields.values();
  }

  @SuppressWarnings("unchecked")
  public <T> T getFieldValue(final String name) {
    final JComponent field = getField(name);
    if (field.isEnabled()) {
      Object value;
      if (field instanceof JXDatePicker) {
        final JXDatePicker dateField = (JXDatePicker)field;
        value = dateField.getDate();
      } else {
        value = SwingUtil.getValue(field);
      }
      final CodeTable codeTable = metaData.getCodeTableByColumn(name);
      if (codeTable == null) {
        if (value != null && name.endsWith("_IND")) {
          if ("Y".equals(value) || Boolean.TRUE.equals(value)) {
            return (T)"Y";
          } else {
            return (T)"N";
          }
        } else {
          return (T)value;
        }
      } else {
        final Object id = codeTable.getId(value);
        return (T)id;
      }
    } else {
      return (T)disabledFieldValues.get(name);
    }
  }

  public GeometryCoordinatesPanel getGeometryCoordinatesPanel() {
    return geometryCoordinatesPanel;
  }

  protected JLabel getLabel(final String fieldName) {
    String title = CaseConverter.toCapitalizedWords(fieldName);
    title = title.replaceAll(" Code$", "");
    title = title.replaceAll(" Ind$", "");
    final JLabel label = new JLabel(title);
    label.setFont(SwingUtil.BOLD_FONT);
    label.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
    return label;
  }

  public DataObjectMetaData getMetaData() {
    return metaData;
  }

  public DataObject getObject() {
    return object;
  }

  @SuppressWarnings("unchecked")
  public <T> T getOriginalValue(final String fieldName) {
    return (T)originalValues.get(fieldName);
  }

  public Set<String> getReadOnlyFieldNames() {
    return readOnlyFieldNames;
  }

  public Set<String> getRequiredFieldNames() {
    return requiredFieldNames;
  }

  protected int getTabIndex(final String fieldName) {
    final JComponent field = getField(fieldName);
    Component panel = field;
    Component component = field.getParent();
    while (component != tabs && component != null) {
      panel = component;
      component = component.getParent();
    }
    final int index = tabs.indexOfComponent(panel);
    return index;
  }

  public JTabbedPane getTabs() {
    return tabs;
  }

  public ToolBar getToolBar() {
    return toolBar;
  }

  public DataObjectMetaDataUiBuilderRegistry getUiBuilderRegistry() {
    return uiBuilderRegistry;
  }

  @SuppressWarnings("unchecked")
  public <T> T getValue(final String name) {
    return (T)object.getValue(name);
  }

  public Map<String, Object> getValues() {
    final Map<String, Object> values = new LinkedHashMap<String, Object>();
    if (object != null) {
      values.putAll(object);
    }
    return values;
  }

  public boolean hasOriginalValue(final String name) {
    return getMetaData().hasAttribute(name);
  }

  protected void invokeAction(final String actionName) {
    final Action action = getActionMap().get(actionName);
    if (action != null) {
      final ActionEvent event = new ActionEvent(this,
        ActionEvent.ACTION_PERFORMED, null);
      action.actionPerformed(event);
    }
  }

  public boolean isEditable() {
    return editable;
  }

  public boolean isFieldsValid() {
    return fieldsValid;
  }

  public boolean isFieldValid(final String fieldName) {
    final Boolean valid = fieldValidMap.get(fieldName);
    return valid == Boolean.TRUE;
  }

  public boolean isFieldValidationEnabled() {
    return fieldValidationEnabled;
  }

  public void pasteValues(final Map<String, Object> map) {
    final Map<String, Object> values = getValues();
    values.putAll(map);
    setValues(values);
  }

  protected void postValidate() {
    if (allAttributes != null) {
      allAttributes.setValues(getValues());
    }
  }

  public void reverseAttributes() {
    final DirectionalAttributes property = DirectionalAttributes.getProperty(metaData);
    property.reverseAttributes(object);
  }

  public void reverseAttributesAndGeometry() {
    final DirectionalAttributes property = DirectionalAttributes.getProperty(metaData);
    property.reverseAttributesAndGeometry(object);
  }

  public void reverseGeometry() {
    final DirectionalAttributes property = DirectionalAttributes.getProperty(metaData);
    property.reverseGeometry(object);
  }

  protected void setDataStore(final DataObjectStore dataStore) {
    this.dataStore = dataStore;
  }

  public void setEditable(final boolean editable) {
    this.editable = editable;
  }

  public void setFieldFocussed(final String fieldName) {
    final int tabIndex = getTabIndex(fieldName);
    if (tabIndex >= 0) {
      tabs.setSelectedIndex(tabIndex);
    }
    final JComponent field = getField(fieldName);
    field.requestFocusInWindow();
  }

  protected void setFieldInvalid(final String fieldName, final String message) {
    if (isFieldValid(fieldName)) {
      fieldsValid = false;
      final JComponent field = getField(fieldName);
      fieldInValidMessage.put(fieldName, message);
      setFieldInvalidToolTip(fieldName, field);
      field.setForeground(Color.RED);
      if (field instanceof JTextComponent) {
        final JTextComponent textField = (JTextComponent)field;
        textField.setSelectedTextColor(Color.RED);
        textField.setBackground(Color.PINK);
      } else if (field instanceof JComboBox) {
        final JComboBox comboBox = (JComboBox)field;
        final ComboBoxEditor editor = comboBox.getEditor();
        final Component component = editor.getEditorComponent();
        component.setForeground(Color.RED);
        component.setBackground(Color.PINK);
      }
      fieldValidMap.put(fieldName, false);
    }
    final boolean valid = false;
    setTabValid(fieldName, valid);
  }

  protected void setFieldInvalidToolTip(final String fieldName,
    final JComponent field) {
    final String message = fieldInValidMessage.get(fieldName);
    if (StringUtils.hasText(message)) {
      field.setToolTipText(message);
    }
  }

  protected void setFieldValid(final String fieldName) {
    final JComponent field = getField(fieldName);
    field.setForeground(Color.BLACK);
    final String toolTip = originalToolTip.get(fieldName);
    field.setToolTipText(toolTip);
    if (field instanceof JTextField) {
      final JTextField textField = (JTextField)field;
      textField.setSelectedTextColor(selectedTextColor);
      textField.setBackground(textBackgroundColor);
    } else if (field instanceof JComboBox) {
      final JComboBox comboBox = (JComboBox)field;
      final ComboBoxEditor editor = comboBox.getEditor();
      final Component component = editor.getEditorComponent();
      component.setForeground(textColor);
      component.setBackground(textBackgroundColor);
    }

    fieldValidMap.put(fieldName, true);
    fieldInValidMessage.remove(field);
  }

  protected boolean setFieldValidationEnabled(
    final boolean fieldValidationEnabled) {
    final boolean oldValue = this.fieldValidationEnabled;
    this.fieldValidationEnabled = fieldValidationEnabled;
    return oldValue;
  }

  public void setFieldValue(final String fieldName, final Object value) {
    final Object oldValue = getFieldValue(fieldName);
    if (oldValue == null & value != null
      || !EqualsRegistry.equal(value, oldValue)) {
      final JComponent field = getField(fieldName);
      SwingUtil.setFieldValue(field, value);
      if (field.isEnabled()) {
        object.setValue(fieldName, value);
      } else {
        if (field instanceof JTextComponent) {
          final JTextComponent textField = (JTextComponent)field;
          String string;
          disabledFieldValues.put(fieldName, value);
          if (value == null) {
            string = "";
          } else {
            string = getCodeValue(fieldName, value);
          }
          textField.setText(string);
        }
      }
      validateFields();
    }
  }

  public void setMetaData(final DataObjectMetaData metaData) {
    this.metaData = metaData;
    setDataStore(metaData.getDataObjectStore());
    final String idAttributeName = metaData.getIdAttributeName();
    if (StringUtils.hasText(idAttributeName)) {
      this.readOnlyFieldNames.add(idAttributeName);
    }
    for (final Attribute attribute : metaData.getAttributes()) {
      if (attribute.isRequired()) {
        final String name = attribute.getName();
        addRequiredFieldNames(name);
      }

    }
  }

  public void setObject(final DataObject object) {
    this.object = object;
    this.originalValues = new HashMap<String, Object>(object);
    setValues(object);
  }

  public void setReadOnlyFieldNames(final Collection<String> readOnlyFieldNames) {
    this.readOnlyFieldNames = new HashSet<String>(readOnlyFieldNames);
    updateReadOnlyFields();
  }

  public void setReadOnlyFieldNames(final String... readOnlyFieldNames) {
    setReadOnlyFieldNames(Arrays.asList(readOnlyFieldNames));
  }

  public void setRequiredFieldNames(final Collection<String> requiredFieldNames) {
    this.requiredFieldNames = new HashSet<String>(requiredFieldNames);
  }

  protected void setTabsValid() {
    for (int i = 0; i < tabs.getTabCount(); i++) {
      final String tabName = tabs.getTitleAt(i);
      tabValid.put(tabName, true);
    }
  }

  protected void setTabValid(final int index, final boolean valid) {
    if (valid == Boolean.TRUE) {
      tabs.setForegroundAt(index, null);
      tabs.setBackgroundAt(index, null);
    } else {
      tabs.setForegroundAt(index, Color.RED);
      tabs.setBackgroundAt(index, Color.PINK);
    }
  }

  protected void setTabValid(final String fieldName, final boolean valid) {
    final int index = getTabIndex(fieldName);
    if (index != -1) {
      final String title = tabs.getTitleAt(index);
      tabValid.put(title, valid);
      setTabValid(index, valid);
    }
  }

  public void setValues(final Map<String, Object> values) {
    final boolean validationEnabled = setFieldValidationEnabled(false);
    try {
      if (values != null) {
        for (final String fieldName : metaData.getAttributeNames()) {
          final Object value = values.get(fieldName);
          setFieldValue(fieldName, value);
        }
      }
    } finally {
      setFieldValidationEnabled(validationEnabled);
    }
    final String geometryAttributeName = metaData.getGeometryAttributeName();
    if (StringUtils.hasText(geometryAttributeName)) {
      final Geometry geometry = (Geometry)values.get(geometryAttributeName);
      if (geometryCoordinatesPanel != null) {
        geometryCoordinatesPanel.setGeometry(geometry);
      }
    }
    validateFields();
  }

  protected void updateReadOnlyFields() {
    for (final Entry<String, JComponent> entry : fields.entrySet()) {
      final String name = entry.getKey();
      final JComponent field = entry.getValue();
      if (readOnlyFieldNames.contains(name)) {
        field.setEnabled(false);
      } else {
        field.setEnabled(true);
      }
    }
    if (allAttributes != null) {
      allAttributes.setReadOnlyFieldNames(readOnlyFieldNames);
    }
  }

  protected void updateTabsValid() {
    for (int i = 0; i < tabs.getTabCount(); i++) {
      final String tabName = tabs.getTitleAt(i);
      final Boolean valid = tabValid.get(tabName);
      if (valid == Boolean.TRUE) {
        tabs.setForegroundAt(i, null);
        tabs.setBackgroundAt(i, null);
      } else {
        tabs.setForegroundAt(i, Color.RED);
        tabs.setBackgroundAt(i, Color.PINK);
      }
    }
  }

  @SuppressWarnings("rawtypes")
  protected void validateFields() {
    if (isFieldValidationEnabled()) {
      setFieldValidationEnabled(false);
      try {
        setTabsValid();
        fieldsValid = true;
        for (final String fieldName : fields.keySet()) {
          setFieldValid(fieldName);
        }
        for (final Entry<String, JComponent> entry : fields.entrySet()) {
          final String fieldName = entry.getKey();
          final JComponent field = entry.getValue();
          if (field instanceof ValidatingField) {
            final ValidatingField validatingField = (ValidatingField)field;
            if (!validatingField.isFieldValid()) {
              final String message = validatingField.getFieldValidationMessage();
              setFieldInvalid(fieldName, message);
            }
          }
        }
        for (final String fieldName : getRequiredFieldNames()) {
          final Object value = getFieldValue(fieldName);
          if (value == null) {
            setFieldInvalid(fieldName, "Required");
          } else if (value instanceof String) {
            final String string = (String)value;
            if (!StringUtils.hasText(string)) {
              setFieldInvalid(fieldName, "Required");
            }
          }
        }
        updateTabsValid();
        postValidate();
      } finally {
        setFieldValidationEnabled(true);
      }
    }
  }

}
