package com.revolsys.swing.map.form;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dialog.ModalityType;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.undo.UndoableEdit;

import org.springframework.util.StringUtils;

import com.revolsys.converter.string.StringConverterRegistry;
import com.revolsys.gis.data.io.DataObjectStore;
import com.revolsys.gis.data.model.Attribute;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.gis.data.model.DataObjectState;
import com.revolsys.gis.data.model.codes.CodeTable;
import com.revolsys.gis.data.model.property.DirectionalAttributes;
import com.revolsys.gis.data.model.types.DataType;
import com.revolsys.gis.data.model.types.DataTypes;
import com.revolsys.gis.model.data.equals.EqualsRegistry;
import com.revolsys.swing.SwingUtil;
import com.revolsys.swing.SwingWorkerManager;
import com.revolsys.swing.action.InvokeMethodAction;
import com.revolsys.swing.action.enablecheck.EnableCheck;
import com.revolsys.swing.action.enablecheck.ObjectPropertyEnableCheck;
import com.revolsys.swing.builder.DataObjectMetaDataUiBuilderRegistry;
import com.revolsys.swing.dnd.transferhandler.DataObjectLayerFormTransferHandler;
import com.revolsys.swing.field.ComboBox;
import com.revolsys.swing.field.Field;
import com.revolsys.swing.field.NumberTextField;
import com.revolsys.swing.field.ObjectLabelField;
import com.revolsys.swing.layout.GroupLayoutUtil;
import com.revolsys.swing.map.layer.dataobject.DataObjectLayer;
import com.revolsys.swing.map.layer.dataobject.LayerDataObject;
import com.revolsys.swing.map.table.DataObjectLayerAttributesTableModel;
import com.revolsys.swing.map.util.LayerUtil;
import com.revolsys.swing.menu.MenuFactory;
import com.revolsys.swing.table.BaseJxTable;
import com.revolsys.swing.table.dataobject.AbstractDataObjectTableModel;
import com.revolsys.swing.table.dataobject.DataObjectTableCellEditor;
import com.revolsys.swing.table.dataobject.DataObjectTableCellRenderer;
import com.revolsys.swing.table.dataobject.ExcludeGeometryRowFilter;
import com.revolsys.swing.toolbar.ToolBar;
import com.revolsys.swing.tree.ObjectTree;
import com.revolsys.swing.tree.model.ObjectTreeModel;
import com.revolsys.swing.undo.UndoManager;
import com.revolsys.util.CollectionUtil;

public class DataObjectLayerForm extends JPanel implements
  PropertyChangeListener, CellEditorListener, FocusListener {

  private static final long serialVersionUID = 1L;

  @SuppressWarnings("serial")
  private final UndoManager undoManager = new UndoManager() {

    @Override
    public void redo() {
      final boolean validationEnabled = setFieldValidationEnabled(false);
      try {
        super.redo();
      } finally {
        if (validationEnabled) {
          validateFields(fieldsToValidate.get());
        }
        setFieldValidationEnabled(validationEnabled);
      }
    }

    @Override
    public void undo() {
      final boolean validationEnabled = setFieldValidationEnabled(false);
      try {
        super.undo();
      } finally {
        if (validationEnabled) {
          validateFields(fieldsToValidate.get());
        }
        setFieldValidationEnabled(validationEnabled);
      }
    }

  };

  private DataObjectLayerAttributesTableModel allAttributes;

  private String lastFocussedFieldName;

  private DataObjectStore dataStore;

  private final Map<String, String> fieldInValidMessage = new HashMap<String, String>();

  private final Map<String, Field> fields = new LinkedHashMap<String, Field>();

  private boolean fieldsValid;

  private final Map<Field, String> fieldToNameMap = new HashMap<Field, String>();

  private final ThreadLocal<Boolean> fieldValidationDisabled = new ThreadLocal<Boolean>();

  private final Map<String, Boolean> fieldValidMap = new HashMap<String, Boolean>();

  private final ThreadLocal<Set<String>> fieldsToValidate = new ThreadLocal<Set<String>>();

  private GeometryCoordinatesPanel geometryCoordinatesPanel;

  private DataObjectMetaData metaData;

  private LayerDataObject object;

  private Set<String> readOnlyFieldNames = new HashSet<String>();

  private Set<String> requiredFieldNames = new HashSet<String>();

  private final JTabbedPane tabs = new JTabbedPane();

  private ToolBar toolBar;

  private final DataObjectMetaDataUiBuilderRegistry uiBuilderRegistry = new DataObjectMetaDataUiBuilderRegistry();

  private boolean editable = true;

  private DataObjectLayer layer;

  private JButton addOkButton;

  private final Map<String, Integer> fieldTabIndex = new HashMap<String, Integer>();

  private final Map<String, Object> fieldValues = new HashMap<String, Object>();

  public DataObjectLayerForm(final DataObjectLayer layer) {
    setLayout(new BorderLayout());
    setName(layer.getName());
    this.layer = layer;
    final DataObjectMetaData metaData = layer.getMetaData();
    setMetaData(metaData);
    addToolBar();

    final ActionMap map = getActionMap();
    map.put("copy", TransferHandler.getCopyAction());
    map.put("paste", TransferHandler.getPasteAction());

    final DataObjectLayerFormTransferHandler transferHandler = new DataObjectLayerFormTransferHandler(
      this);
    setTransferHandler(transferHandler);
    setFont(SwingUtil.FONT);

    addTabAllFields();
    final boolean editable = layer.isEditable();
    setEditable(editable);
    getAllAttributes().setEditable(editable);
    if (metaData.getGeometryAttributeName() != null) {
      addTabGeometry();
    }
    layer.addPropertyChangeListener(this);
    undoManager.setLimit(100);
    undoManager.addKeyMap(this);
  }

  public DataObjectLayerForm(final DataObjectLayer layer,
    final LayerDataObject object) {
    this(layer);
    setObject(object);
  }

  public void actionAddCancel() {
    final DataObjectLayer layer = getLayer();
    final LayerDataObject object = getObject();
    layer.deleteRecords(object);
    this.object = null;
    closeWindow();
  }

  public void actionAddOk() {
    final DataObjectLayer layer = getLayer();
    final LayerDataObject object = getObject();
    layer.saveChanges(object);
    layer.addSelectedRecords(object);
    closeWindow();
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
    final NumberTextField field = new NumberTextField(fieldName, dataType,
      length, scale, minimumValie, maximumValue);
    addField(fieldName, field);
  }

  public void addField(final Container container, final Field field) {
    final JComponent component = (JComponent)field;
    addField(container, component);
  }

  public void addField(final Container container, JComponent field) {
    if (field instanceof JTextArea) {
      final JTextArea textArea = (JTextArea)field;
      field = new JScrollPane(textArea);
    }
    container.add(field);
  }

  @SuppressWarnings("unchecked")
  public <T> T addField(final Container container, final String fieldName) {
    final Field field = getField(fieldName);
    addField(container, field);
    return (T)field;
  }

  public void addField(final Field field) {
    final String fieldName = field.getFieldName();
    addField(fieldName, field);
  }

  public Field addField(final String fieldName, final Field field) {
    field.addPropertyChangeListener(fieldName, this);
    field.setUndoManager(undoManager);
    if (field instanceof ComboBox) {
      final ComboBox comboBox = (ComboBox)field;
      comboBox.getEditor().getEditorComponent().addFocusListener(this);
    } else {
      ((JComponent)field).addFocusListener(this);
    }
    fields.put(fieldName, field);
    fieldToNameMap.put(field, fieldName);
    return field;
  }

  public void addFields(final Collection<? extends Field> fields) {
    for (final Field field : fields) {
      addField(field);
    }
  }

  protected void addLabel(final Container container, final String fieldName) {
    final JLabel label = getLabel(fieldName);
    container.add(label);
  }

  public void addLabelledField(final Container container, final Field field) {
    final String fieldName = field.getFieldName();
    addLabel(container, fieldName);
    addField(container, field);
  }

  @SuppressWarnings("unchecked")
  public <T> T addLabelledField(final Container container,
    final String fieldName) {
    final Field field = getField(fieldName);
    addLabelledField(container, field);
    return (T)field;
  }

  protected void addNumberField(final String fieldName,
    final DataType dataType, final int length, final Number minimumValue,
    final Number maximumValue) {
    final NumberTextField field = new NumberTextField(fieldName, dataType,
      length, 0, minimumValue, maximumValue);
    addField(fieldName, field);
  }

  protected void addPanel(final JPanel container, final String title,
    final List<String> fieldNames) {
    final JPanel panel = createPanel(container, title);

    for (final String fieldName : fieldNames) {
      addLabelledField(panel, fieldName);
    }

    GroupLayoutUtil.makeColumns(panel, 2);
  }

  public void addReadOnlyFieldNames(final Collection<String> readOnlyFieldNames) {
    this.readOnlyFieldNames.addAll(readOnlyFieldNames);
    for (final Entry<String, Field> entry : fields.entrySet()) {
      final String name = entry.getKey();
      final Field field = entry.getValue();
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

  public JScrollPane addTab(final int index, final String name,
    final Component component) {
    boolean init = false;
    final Container parent = tabs.getParent();
    if (parent != this) {
      add(tabs, BorderLayout.CENTER);
      init = true;
    }
    final JScrollPane scrollPane = new JScrollPane(component);
    tabs.insertTab(name, null, scrollPane, null, index);
    if (init) {
      tabs.setSelectedIndex(0);
    }
    return scrollPane;
  }

  public JScrollPane addTab(final String name, final Component component) {
    return addTab(tabs.getComponentCount(), name, component);
  }

  protected void addTabAllFields() {
    allAttributes = new DataObjectLayerAttributesTableModel(this);
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

        final String fieldName = allAttributes.getAttributeName(row);
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
            boolean equal = EqualsRegistry.equal(originalValue, fieldValue);
            if (!equal) {
              if (originalValue == null) {
                if (fieldValue instanceof String) {
                  final String string = (String)fieldValue;
                  if (!StringUtils.hasText(string)) {
                    equal = true;
                  }
                }
              }
            }
            if (!equal) {
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

    final JScrollPane scrollPane = addTab("All Fields", table);
    int maxHeight = Integer.MAX_VALUE;
    for (final GraphicsDevice device : GraphicsEnvironment.getLocalGraphicsEnvironment()
      .getScreenDevices()) {
      final GraphicsConfiguration graphicsConfiguration = device.getDefaultConfiguration();
      final Rectangle bounds = graphicsConfiguration.getBounds();
      maxHeight = Math.min(bounds.height, maxHeight);
    }
    maxHeight -= 300;
    final int preferredHeight = Math.min(maxHeight,
      (allAttributes.getRowCount() + 1) * 20);
    scrollPane.setMinimumSize(new Dimension(100, preferredHeight));
    scrollPane.setMaximumSize(new Dimension(Integer.MAX_VALUE, maxHeight));
    scrollPane.setPreferredSize(new Dimension(800, preferredHeight));
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

    final MenuFactory menuFactory = ObjectTreeModel.findMenu(layer);
    if (menuFactory != null) {
      toolBar.addButtonTitleIcon("menu", "Layer Menu", "menu",
        ObjectTree.class, "showMenu", menuFactory, layer, this, 10, 10);
    }
    // Cut, Copy Paste
    // TODO copy enable checks

    toolBar.addButton("dnd", "Copy", "page_copy", (EnableCheck)null, this,
      "dataTransferCopy");
    toolBar.addButton("dnd", "Paste", "paste_plain", editable, this,
      "dataTransferPaste");

    final EnableCheck canUndo = new ObjectPropertyEnableCheck(undoManager,
      "canUndo");
    final EnableCheck canRedo = new ObjectPropertyEnableCheck(undoManager,
      "canRedo");

    toolBar.addButton("undo", "Undo", "arrow_undo", canUndo, undoManager,
      "undo");
    toolBar.addButton("undo", "Redo", "arrow_redo", canRedo, undoManager,
      "redo");

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
          toolBar.addButton("geometry", "Reverse Fields", "attributes_reverse",
            editable, this, "reverseAttributes");
          toolBar.addButton("geometry", "Reverse Geometry & Fields",
            "attributes_line_reverse", editable, this,
            "reverseAttributesAndGeometry");
        }
      }
    }
    return toolBar;
  }

  public void addUndo(final UndoableEdit edit) {
    final boolean validationEnabled = setFieldValidationEnabled(false);
    try {
      undoManager.addEdit(edit);
    } finally {
      if (validationEnabled) {
        validateFields(fieldsToValidate.get());
      }
      setFieldValidationEnabled(validationEnabled);
    }
  }

  public void closeWindow() {
    final Window window = SwingUtilities.windowForComponent(this);
    if (window != null) {
      window.setVisible(false);
    }
  }

  protected JPanel createPanel(final JPanel container, final String title) {
    final JPanel panel = new JPanel();
    container.add(panel);

    panel.setBorder(BorderFactory.createTitledBorder(title));
    return panel;
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
    setFieldValue(name, value, true);
  }

  @Override
  public void focusGained(final FocusEvent e) {
  }

  @Override
  public void focusLost(final FocusEvent e) {
    Component component = e.getComponent();
    while (component != null) {
      if (component instanceof Field) {
        lastFocussedFieldName = ((Field)component).getFieldName();
        return;
      } else {
        component = component.getParent();
      }
    }
  }

  public DataObjectLayerAttributesTableModel getAllAttributes() {
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
  protected <T extends Field> T getField(final String fieldName) {
    synchronized (fields) {
      Field field = fields.get(fieldName);
      if (field == null) {
        final boolean enabled = !readOnlyFieldNames.contains(fieldName);
        try {
          field = SwingUtil.createField(metaData, fieldName, enabled);
          addField(fieldName, field);
        } catch (final IllegalArgumentException e) {
        }
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

  public Set<String> getFieldNames() {
    return fields.keySet();
  }

  public Collection<Field> getFields() {
    return fields.values();
  }

  public String getFieldTitle(final String fieldName) {
    final DataObjectMetaData metaData = getMetaData();
    return metaData.getAttributeTitle(fieldName);
  }

  @SuppressWarnings("unchecked")
  public <T> T getFieldValue(final String name) {
    final Object value = fieldValues.get(name);
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
  }

  public String getGeometryAttributeName() {
    return getMetaData().getGeometryAttributeName();
  }

  public GeometryCoordinatesPanel getGeometryCoordinatesPanel() {
    return geometryCoordinatesPanel;
  }

  protected JLabel getLabel(final String fieldName) {
    String title = metaData.getAttributeTitle(fieldName);
    title = title.replaceAll(" Code$", "");
    title = title.replaceAll(" Ind$", "");
    final JLabel label = new JLabel(title);
    label.setFont(SwingUtil.BOLD_FONT);
    label.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
    return label;
  }

  public String getLastFocussedFieldName() {
    return lastFocussedFieldName;
  }

  public DataObjectLayer getLayer() {
    return layer;
  }

  public DataObjectMetaData getMetaData() {
    return metaData;
  }

  public LayerDataObject getObject() {
    return object;
  }

  public <T> T getOriginalValue(final String fieldName) {
    final LayerDataObject object = getObject();
    return object.getOriginalValue(fieldName);
  }

  public Set<String> getReadOnlyFieldNames() {
    return readOnlyFieldNames;
  }

  public Set<String> getRequiredFieldNames() {
    return requiredFieldNames;
  }

  protected int getTabIndex(final String fieldName) {
    Integer index = fieldTabIndex.get(fieldName);
    if (index == null) {
      final JComponent field = getField(fieldName);
      if (field == null) {
        return -1;
      } else {
        Component panel = field;
        Component component = field.getParent();
        while (component != tabs && component != null) {
          panel = component;
          component = component.getParent();
        }
        index = tabs.indexOfComponent(panel);
        fieldTabIndex.put(fieldName, index);
      }
    }
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

  public UndoManager getUndoManager() {
    return undoManager;
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

  public boolean isEditable(final String attributeName) {
    if (isEditable()) {
      if (!readOnlyFieldNames.contains(attributeName)) {
        return true;
      }
    }
    return false;
  }

  public boolean isFieldsValid() {
    return fieldsValid;
  }

  public boolean isFieldValid(final String fieldName) {
    final Boolean valid = fieldValidMap.get(fieldName);
    return valid != Boolean.FALSE;
  }

  protected boolean isFieldValidationEnabled() {
    final boolean enabled = fieldValidationDisabled.get() != Boolean.FALSE;
    return enabled;
  }

  protected boolean isTabValid(final int index) {
    if (index < 0) {
      return false;
    } else {
      for (final Entry<String, Integer> entry : fieldTabIndex.entrySet()) {
        final Integer tabIndex = entry.getValue();
        if (tabIndex == index) {
          final String name = entry.getKey();
          if (!isFieldValid(name)) {
            return false;
          }
        }
      }
      return true;
    }
  }

  public void pasteValues(final Map<String, Object> map) {
    final Map<String, Object> newValues = new LinkedHashMap<String, Object>(map);
    final Collection<String> ignorePasteFields = layer.getProperty("ignorePasteFields");
    if (ignorePasteFields != null) {
      newValues.keySet().removeAll(ignorePasteFields);
    }
    newValues.keySet().removeAll(getReadOnlyFieldNames());

    final Map<String, Object> values = getValues();
    values.putAll(newValues);
    setValues(values);
  }

  protected void postValidate() {

  }

  @Override
  public void propertyChange(final PropertyChangeEvent event) {
    final Object source = event.getSource();
    if (source instanceof Field) {
      final Field field = (Field)source;
      final String fieldName = field.getFieldName();
      final Object fieldValue = field.getFieldValue();
      final Object objectValue = object.getValue(fieldName);
      if (!EqualsRegistry.equal(objectValue, fieldValue)) {
        boolean equal = false;
        if (fieldValue instanceof String) {
          final String string = (String)fieldValue;
          if (!StringUtils.hasText(string) && objectValue == null) {
            equal = true;
          }
        }
        if (!equal) {
          object.setValueByPath(fieldName, fieldValue);
        }
      }
    } else if (source == getObject()) {
      final String propertyName = event.getPropertyName();
      final Object value = event.getNewValue();
      final DataObjectMetaData metaData = getMetaData();
      if ("errorsUpdated".equals(propertyName)) {
        updateErrors();
      } else if (metaData.hasAttribute(propertyName)) {
        setFieldValue(propertyName, value, isFieldValidationEnabled());
      }
    }
  }

  @Override
  public void removeNotify() {
    try {
      super.removeNotify();
    } finally {
      if (layer != null) {
        if (allAttributes != null) {
          layer.removePropertyChangeListener(allAttributes);
          allAttributes = null;
        }
        layer.removePropertyChangeListener(this);
        layer = null;
      }
    }
  }

  public void reverseAttributes() {
    final boolean enbled = setFieldValidationEnabled(false);
    try {
      final DirectionalAttributes property = DirectionalAttributes.getProperty(metaData);
      property.reverseAttributes(object);
    } finally {
      setFieldValidationEnabled(enbled);
      validateFields();
    }
  }

  public void reverseAttributesAndGeometry() {
    final boolean enbled = setFieldValidationEnabled(false);
    try {
      final DirectionalAttributes property = DirectionalAttributes.getProperty(metaData);
      property.reverseAttributesAndGeometry(object);
    } finally {
      setFieldValidationEnabled(enbled);
      validateFields();
    }
  }

  public void reverseGeometry() {
    final boolean enbled = setFieldValidationEnabled(false);
    try {
      final DirectionalAttributes property = DirectionalAttributes.getProperty(metaData);
      property.reverseGeometry(object);
    } finally {
      setFieldValidationEnabled(enbled);
      final String geometryAttributeName = getGeometryAttributeName();
      validateField(geometryAttributeName);
    }
  }

  public void setAddOkButtonEnabled(final boolean enabled) {
    if (addOkButton != null) {
      addOkButton.setEnabled(enabled);
    }
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

  public void setFieldInvalid(final String fieldName, String message) {
    if (message == null) {
      message = "Invalid value";
    }
    fieldInValidMessage.put(fieldName, message);
    if (SwingUtilities.isEventDispatchThread()) {
      fieldsValid = false;
      final Field field = getField(fieldName);
      field.setFieldInvalid(message);

      fieldValidMap.put(fieldName, false);
      updateTabsValid(fieldName);
    } else {
      SwingUtil.invokeLater(this, "setFieldInvalid", fieldName, message);
    }
  }

  protected void setFieldInvalidToolTip(final String fieldName,
    final JComponent field) {
    final String message = fieldInValidMessage.get(fieldName);
    if (StringUtils.hasText(message)) {
      field.setToolTipText(message);
    }
  }

  public boolean setFieldValid(final String fieldName) {
    if (SwingUtilities.isEventDispatchThread()) {
      final Field field = getField(fieldName);
      field.setFieldValid();
      if (object.isModified(fieldName)) {
        final Object originalValue = object.getOriginalValue(fieldName);
        String originalString;
        if (originalValue == null) {
          originalString = "-";
        } else {
          originalString = StringConverterRegistry.toString(originalValue);
        }
        field.setFieldToolTip(originalString);
        field.setFieldBackgroundColor(new Color(0, 255, 0, 31));
      } else {
        field.setFieldToolTip("");
      }
      fieldValidMap.put(fieldName, true);
      fieldInValidMessage.remove(fieldName);
      updateTabsValid(fieldName);
      return true;
    } else {
      SwingUtil.invokeLater(this, "setFieldValid", fieldName);
      return false;
    }
  }

  protected boolean setFieldValidationEnabled(
    final boolean fieldValidationEnabled) {
    final boolean oldValue = isFieldValidationEnabled();
    if (fieldValidationEnabled) {
      fieldsToValidate.remove();
    } else if (oldValue) {
      fieldsToValidate.set(new TreeSet<String>());
    }
    this.fieldValidationDisabled.set(fieldValidationEnabled);
    return oldValue;
  }

  public void setFieldValue(final String fieldName, final Object value,
    final boolean validate) {

    boolean changed = false;
    final Object oldValue = getFieldValue(fieldName);
    fieldValues.put(fieldName, value);
    final JComponent field = getField(fieldName);
    if (oldValue == null & value != null
      || !EqualsRegistry.equal(value, oldValue)) {
      changed = true;
    }
    final Object objectValue = object.getValue(fieldName);
    if (!EqualsRegistry.equal(value, objectValue)) {
      object.setValueByPath(fieldName, value);
      changed = true;
    }
    SwingUtil.setFieldValue(field, value);
    if (changed) {
      if (validate) {
        validateField(fieldName);
      } else {
        final Set<String> fieldsToValidate = this.fieldsToValidate.get();
        if (fieldsToValidate != null) {
          fieldsToValidate.add(fieldName);
        }
      }
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

  public void setObject(final LayerDataObject object) {
    final boolean undo = undoManager.setEventsEnabled(false);
    final boolean validate = setFieldValidationEnabled(false);
    try {
      this.object = object;
      allAttributes.setObject(object);
      setValues(object);
      undoManager.discardAllEdits();
    } finally {
      setFieldValidationEnabled(validate);
      undoManager.setEventsEnabled(undo);
    }
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

  protected void setTabValid(final int index, final boolean valid) {
    if (valid) {
      tabs.setForegroundAt(index, null);
      tabs.setBackgroundAt(index, null);
    } else {
      tabs.setForegroundAt(index, Color.RED);
      tabs.setBackgroundAt(index, Color.PINK);
    }
  }

  public void setValues(final Map<String, Object> values) {
    if (values != null) {
      if (SwingUtilities.isEventDispatchThread()) {
        final Set<String> fieldNames = values.keySet();
        final boolean validationEnabled = setFieldValidationEnabled(false);
        try {
          this.fieldValues.putAll(values);
          for (final String fieldName : fieldNames) {
            final Object value = values.get(fieldName);
            final JComponent field = getField(fieldName);
            SwingUtil.setFieldValue(field, value);
          }
        } finally {
          setFieldValidationEnabled(validationEnabled);
        }
        validateFields(fieldNames);
      } else {
        SwingUtil.invokeLater(this, "setValues", values);
      }
    }

  }

  public LayerDataObject showAddDialog() {
    final String title = "Add New " + getName();
    final Window window = SwingUtil.getActiveWindow();
    final JDialog dialog = new JDialog(window, title,
      ModalityType.APPLICATION_MODAL);
    dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
    dialog.setLayout(new BorderLayout());

    dialog.add(this, BorderLayout.CENTER);

    final JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    dialog.add(buttons, BorderLayout.SOUTH);
    final JButton addCancelButton = InvokeMethodAction.createButton("Cancel",
      this, "actionAddCancel");
    buttons.add(addCancelButton);
    addOkButton = InvokeMethodAction.createButton("OK", this, "actionAddOk");
    buttons.add(addOkButton);

    dialog.pack();
    dialog.setLocation(50, 50);
    dialog.setVisible(true);
    final LayerDataObject object = getObject();
    dialog.dispose();
    return object;
  }

  protected void updateErrors() {
  }

  protected void updateReadOnlyFields() {
    for (final Entry<String, Field> entry : fields.entrySet()) {
      final String name = entry.getKey();
      final Field field = entry.getValue();
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

  public void updateTabsValid(final String fieldName) {
    final int index = getTabIndex(fieldName);
    if (index >= 0) {
      final boolean valid = isTabValid(index);
      setTabValid(index, valid);
    }
  }

  public boolean validateField(final String fieldName) {
    if (SwingUtilities.isEventDispatchThread()) {
      SwingWorkerManager.execute("Validate Field " + fieldName, this,
        "validateField", fieldName);
      return false;
    } else {
      return validateFieldInternal(fieldName);
    }
  }

  private boolean validateFieldInternal(final String fieldName) {
    final boolean oldValid = isFieldValid(fieldName);
    final Field field = getField(fieldName);
    boolean valid = true;
    if (!field.isFieldValid()) {
      final String message = field.getFieldValidationMessage();
      setFieldInvalid(fieldName, message);
      valid = false;
    }

    if (valid) {
      final Set<String> requiredFieldNames = getRequiredFieldNames();
      if (requiredFieldNames.contains(fieldName)) {
        boolean run = true;
        if (object.getState() == DataObjectState.New) {
          final String idAttributeName = getMetaData().getIdAttributeName();
          if (fieldName.equals(idAttributeName)) {
            run = false;
          }
        }
        if (run) {
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

    if (oldValid != valid) {
      SwingUtil.invokeLater(this, "updateTabsValid", fieldName);
    }
    return valid;
  }

  public void validateFields() {
    final Set<String> fieldNames = getFieldNames();
    validateFields(fieldNames);
  }

  protected boolean validateFields(final Collection<String> fieldNames) {
    boolean valid = true;
    for (final String fieldName : fieldNames) {
      valid &= validateFieldInternal(fieldName);
    }
    return valid;
  }
}
