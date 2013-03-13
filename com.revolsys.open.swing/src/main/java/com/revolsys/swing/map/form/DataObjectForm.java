package com.revolsys.swing.map.form;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.LayoutManager;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import org.jdesktop.swingx.JXDatePicker;
import org.jdesktop.swingx.JXTable;
import org.springframework.util.StringUtils;

import com.revolsys.converter.string.StringConverterRegistry;
import com.revolsys.gis.data.io.DataObjectStore;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.gis.data.model.codes.CodeTable;
import com.revolsys.gis.data.model.types.DataType;
import com.revolsys.gis.data.model.types.DataTypes;
import com.revolsys.gis.model.data.equals.EqualsRegistry;
import com.revolsys.swing.SwingUtil;
import com.revolsys.swing.builder.DataObjectMetaDataUiBuilderRegistry;
import com.revolsys.swing.field.DateTextField;
import com.revolsys.swing.field.NumberTextField;
import com.revolsys.swing.field.ValidatingField;
import com.revolsys.swing.table.dataobject.AbstractDataObjectTableModel;
import com.revolsys.swing.table.dataobject.DataObjectMapTableModel;
import com.revolsys.swing.table.dataobject.DataObjectTableCellEditor;
import com.revolsys.swing.table.dataobject.DataObjectTableCellRenderer;
import com.revolsys.swing.table.dataobject.ExcludeGeometryRowFilter;
import com.revolsys.util.CaseConverter;
import com.revolsys.util.CollectionUtil;
import com.vividsolutions.jts.geom.Geometry;

public class DataObjectForm extends JPanel implements FocusListener,
  CellEditorListener {
  private static final long serialVersionUID = 1L;

  private final Map<String, JComponent> fields = new LinkedHashMap<String, JComponent>();

  private final Map<Component, String> fieldToNameMap = new HashMap<Component, String>();

  private boolean fieldsValid;

  private final Map<String, Boolean> fieldValidMap = new HashMap<String, Boolean>();

  private final Map<String, String> fieldInValidMessage = new HashMap<String, String>();

  private DataObjectMetaData metaData;

  private Map<String, Object> values = new HashMap<String, Object>();

  private final Map<String, String> originalToolTip = new HashMap<String, String>();

  private List<String> readOnlyFieldNames = new ArrayList<String>();

  private List<String> requiredFieldNames = new ArrayList<String>();

  private final Map<String, Object> disabledFieldValues = new HashMap<String, Object>();

  private final Color textColor = new JTextField().getForeground();

  private final Color textBackgroundColor = new JTextField().getBackground();

  private final Color selectedTextColor = new JTextField().getSelectedTextColor();

  private Object previousValue;

  private boolean fieldValidationEnabled = true;

  private DataObjectMapTableModel allAttributes;

  private final DataObjectMetaDataUiBuilderRegistry uiBuilderRegistry = new DataObjectMetaDataUiBuilderRegistry();

  private final JTabbedPane tabs = new JTabbedPane();

  private JComponent allAttributesPanel;

  private final Map<String, Boolean> tabValid = new HashMap<String, Boolean>();

  public DataObjectMapTableModel getAllAttributes() {
    return allAttributes;
  }

  public DataObjectStore getDataStore() {
    if (metaData == null) {
      return null;
    } else {
      return metaData.getDataObjectStore();
    }
  }

  private GeometryCoordinatesPanel geometryCoordinatesPanel;

  protected void addGeometryTab() {
    if (geometryCoordinatesPanel == null) {
      geometryCoordinatesPanel = new GeometryCoordinatesPanel();

      final JPanel panel = new JPanel(new GridLayout(1, 1));

      geometryCoordinatesPanel.setBorder(BorderFactory.createTitledBorder("Coordinates"));
      panel.add(geometryCoordinatesPanel);

      addTab("Geometry", panel);
    }
  }

  public DataObjectForm() {
    this(new BorderLayout());
  }

  public DataObjectForm(final LayoutManager layout) {
    super(layout);
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

  public void addField(final String fieldName, final JComponent field) {
    originalToolTip.put(fieldName, field.getToolTipText());
    field.addFocusListener(this);
    fields.put(fieldName, field);
    fieldToNameMap.put(field, fieldName);
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

  public void addTab(final String name, final Component component) {
    Container parent = tabs.getParent();
    if (parent != this && allAttributesPanel != null) {
      remove(allAttributesPanel);
      tabs.addTab("All Attributes", allAttributesPanel);
      add(tabs, BorderLayout.CENTER);
    }
    if (name.equals("Geometry")) {
      tabs.addTab(name, component);
    } else {
      tabs.insertTab(name, null, component, null, tabs.getTabCount() - 1);
    }
  }

  protected JComponent createAllAttributesPanel() {
    allAttributes = new DataObjectMapTableModel(getMetaData(), getValues(),
      true);
    allAttributes.setReadOnlyFieldNames(getReadOnlyFieldNames());
    final JXTable table = AbstractDataObjectTableModel.create(allAttributes);
    table.setRowFilter(new ExcludeGeometryRowFilter());
    final TableColumnModel columnModel = table.getColumnModel();
    final DataObjectTableCellRenderer cellRenderer = new DataObjectTableCellRenderer() {
      @Override
      public Component getTableCellRendererComponent(final JTable table,
        final Object value, final boolean isSelected, final boolean hasFocus,
        final int row, final int column) {
        final boolean even = row % 2 == 0;

        final String fieldName = (String)table.getValueAt(row, 1);
        final JComponent component = (JComponent)super.getTableCellRendererComponent(
          table, value, isSelected, hasFocus, row, column);
        component.setToolTipText("");
        if (isFieldValid(fieldName)) {
          if (hasOriginalValue(fieldName)) {
            final Object fieldValue = getFieldValue(fieldName);
            final Object originalValue = getOriginalValue(fieldName);
            if (!EqualsRegistry.equal(originalValue, fieldValue)) {
              DataObjectMetaData metaData = getMetaData();
              CodeTable codeTable = null;
              if (!fieldName.equals(metaData.getIdAttributeName())) {
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
    scrollPane.setPreferredSize(new Dimension(800, 600));
    this.allAttributesPanel = scrollPane;
    return scrollPane;
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
    validateFields();
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
          validateFields();
        }
      }
    }
    previousValue = null;
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

  public Map<String, Object> getFieldValues() {
    final Map<String, Object> values = new LinkedHashMap<String, Object>();
    for (final String name : fields.keySet()) {
      final Object value = getFieldValue(name);
      values.put(name, value);
    }
    return values;
  }

  protected JLabel getLabel(final String fieldName) {
    String title = CaseConverter.toCapitalizedWords(fieldName);
    title = title.replaceAll(" Code$", "");
    title = title.replaceAll(" Ind$", "");
    final JLabel label = new JLabel(title);
    label.setFont(label.getFont().deriveFont(Font.BOLD));
    return label;
  }

  public DataObjectMetaData getMetaData() {
    return metaData;
  }

  @SuppressWarnings("unchecked")
  public <T> T getObjectValue(final String name) {
    return (T)values.get(name);
  }

  public boolean hasOriginalValue(final String name) {
    return getMetaData().hasAttribute(name);
  }

  @SuppressWarnings("unchecked")
  public <T> T getOriginalValue(final String fieldName) {
    return (T)values.get(fieldName);
  }

  public List<String> getReadOnlyFieldNames() {
    return readOnlyFieldNames;
  }

  public List<String> getRequiredFieldNames() {
    return requiredFieldNames;
  }

  public JTabbedPane getTabs() {
    return tabs;
  }

  public DataObjectMetaDataUiBuilderRegistry getUiBuilderRegistry() {
    return uiBuilderRegistry;
  }

  public Map<String, Object> getValues() {
    return values;
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

  protected void setFieldInvalid(final String fieldName, final String message) {
    if (isFieldValid(fieldName)) {
      fieldsValid = false;
      final JComponent field = getField(fieldName);
      fieldInValidMessage.put(fieldName, message);
      setFieldInvalidToolTip(fieldName, field);
      field.setForeground(Color.RED);
      if (field instanceof JTextField) {
        final JTextField textField = (JTextField)field;
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
    final JComponent field = getField(fieldName);
    Component panel = field;
    Component component = field.getParent();
    while (component != tabs && component != null) {
      panel = component;
      component = component.getParent();
    }
    final int index = tabs.indexOfComponent(panel);
    if (index != -1) {
      final String title = tabs.getTitleAt(index);
      tabValid.put(title, false);
    }
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

  protected void setFieldValidationEnabled(final boolean fieldValidationEnabled) {
    this.fieldValidationEnabled = fieldValidationEnabled;
  }

  public void setFieldValue(final String fieldName, final Object value) {
    final Object oldValue = getFieldValue(fieldName);
    if (oldValue == null & value != null
      || !EqualsRegistry.equal(value, oldValue)) {
      final JComponent field = getField(fieldName);
      if (field instanceof NumberTextField) {
        final NumberTextField numberField = (NumberTextField)field;
        numberField.setFieldValue((Number)value);
      } else if (field instanceof DateTextField) {
        final DateTextField dateField = (DateTextField)field;
        dateField.setFieldValue((Date)value);
      } else if (field instanceof JXDatePicker) {
        final JXDatePicker dateField = (JXDatePicker)field;
        dateField.setDate((Date)value);
      } else if (field instanceof JTextField) {
        final JTextField textField = (JTextField)field;
        String string;
        if (textField.isEnabled()) {
          if (value == null) {
            string = "";
          } else {
            string = StringConverterRegistry.toString(value);
          }
        } else {
          disabledFieldValues.put(fieldName, value);
          if (value == null) {
            string = "";
          } else {
            string = getCodeValue(fieldName, value);
          }
          textField.setColumns(Math.max(string.length(), 1));
        }
        textField.setText(string);
      } else if (field instanceof JTextArea) {
        final JTextArea textField = (JTextArea)field;
        String string;
        if (textField.isEnabled()) {
          if (value == null) {
            string = "";
          } else {
            string = StringConverterRegistry.toString(value);
          }
        } else {
          string = getCodeValue(fieldName, value);
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
      validateFields();
    }
    if (allAttributes != null) {
      allAttributes.setValues(getFieldValues());
    }
  }

  public void setMetaData(final DataObjectMetaData metaData) {
    this.metaData = metaData;
    String idAttributeName = metaData.getIdAttributeName();
    if (StringUtils.hasText(idAttributeName)) {
      this.readOnlyFieldNames.add(idAttributeName);
    }
  }

  public void setReadOnlyFieldNames(final List<String> readOnlyFieldNames) {
    this.readOnlyFieldNames = readOnlyFieldNames;
  }

  public void setRequiredFieldNames(final List<String> requiredFieldNames) {
    this.requiredFieldNames = requiredFieldNames;
  }

  protected void setTabsValid() {
    for (int i = 0; i < tabs.getTabCount(); i++) {
      final String tabName = tabs.getTitleAt(i);
      tabValid.put(tabName, true);
    }
  }

  public void setValues(final Map<String, Object> object) {
    fieldValidationEnabled = false;
    try {
      this.values = object;
      for (final String fieldName : metaData.getAttributeNames()) {
        final Object value = object.get(fieldName);
        setFieldValue(fieldName, value);
      }
    } finally {
      fieldValidationEnabled = true;
    }
    String geometryAttributeName = metaData.getGeometryAttributeName();
    if (geometryCoordinatesPanel != null
      && StringUtils.hasText(geometryAttributeName)) {
      final Geometry geometry = (Geometry)object.get(geometryAttributeName);
      geometryCoordinatesPanel.setGeometry(geometry);
    }
    validateFields();
  }

  protected Map<String, Object> updateObject() {
    for (final String fieldName : fields.keySet()) {
      Object value = getFieldValue(fieldName);
      if (value instanceof String) {
        final String string = (String)value;
        value = string.trim();
      }
      values.put(fieldName, value);
    }
    return values;
  }

  protected void validateFields() {
    if (isFieldValidationEnabled()) {
      setTabsValid();
      fieldsValid = true;
      for (final String fieldName : fields.keySet()) {
        setFieldValid(fieldName);
      }
      if (values != null) {
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
      }
    }
    updateTabsValid();
  }

  protected void updateTabsValid() {
    for (int i = 0; i < tabs.getTabCount(); i++) {
      final String tabName = tabs.getTitleAt(i);
      final Boolean valid = tabValid.put(tabName, true);
      if (valid == Boolean.TRUE) {
        tabs.setForegroundAt(i, null);
        tabs.setBackgroundAt(i, null);
      } else {
        tabs.setForegroundAt(i, Color.RED);
        tabs.setBackgroundAt(i, Color.PINK);
      }
    }
  }

}
