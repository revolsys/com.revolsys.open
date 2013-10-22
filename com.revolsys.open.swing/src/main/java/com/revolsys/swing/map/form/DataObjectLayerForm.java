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
import java.beans.PropertyChangeSupport;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
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
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler;
import javax.swing.WindowConstants;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.undo.UndoableEdit;

import org.jdesktop.swingx.VerticalLayout;
import org.springframework.util.StringUtils;

import com.revolsys.awt.WebColors;
import com.revolsys.beans.PropertyChangeSupportProxy;
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
import com.revolsys.swing.action.InvokeMethodAction;
import com.revolsys.swing.action.enablecheck.EnableCheck;
import com.revolsys.swing.action.enablecheck.ObjectPropertyEnableCheck;
import com.revolsys.swing.dnd.transferhandler.DataObjectLayerFormTransferHandler;
import com.revolsys.swing.field.ComboBox;
import com.revolsys.swing.field.Field;
import com.revolsys.swing.field.NumberTextField;
import com.revolsys.swing.field.ObjectLabelField;
import com.revolsys.swing.layout.GroupLayoutUtil;
import com.revolsys.swing.map.layer.dataobject.AbstractDataObjectLayer;
import com.revolsys.swing.map.layer.dataobject.LayerDataObject;
import com.revolsys.swing.map.layer.dataobject.table.model.DataObjectLayerAttributesTableModel;
import com.revolsys.swing.map.layer.dataobject.table.predicate.FormAllFieldsErrorPredicate;
import com.revolsys.swing.map.layer.dataobject.table.predicate.FormAllFieldsModifiedPredicate;
import com.revolsys.swing.menu.MenuFactory;
import com.revolsys.swing.parallel.Invoke;
import com.revolsys.swing.table.BaseJxTable;
import com.revolsys.swing.table.dataobject.editor.DataObjectTableCellEditor;
import com.revolsys.swing.table.dataobject.model.AbstractSingleDataObjectTableModel;
import com.revolsys.swing.toolbar.ToolBar;
import com.revolsys.swing.tree.ObjectTree;
import com.revolsys.swing.tree.model.ObjectTreeModel;
import com.revolsys.swing.undo.ReverseDataObjectAttributesUndo;
import com.revolsys.swing.undo.ReverseDataObjectGeometryUndo;
import com.revolsys.swing.undo.ReverseDataObjectUndo;
import com.revolsys.swing.undo.UndoManager;
import com.revolsys.util.CollectionUtil;

public class DataObjectLayerForm extends JPanel implements
  PropertyChangeListener, CellEditorListener, FocusListener,
  PropertyChangeSupportProxy {

  public static final String FLIP_FIELDS_ICON = "flip_fields";

  public static final String FLIP_FIELDS_NAME = "Flip Fields Orientation";

  public static final String FLIP_LINE_ORIENTATION_ICON = "flip_line_orientation";

  public static final String FLIP_LINE_ORIENTATION_NAME = "Flip Line Orientation (Visually Flips Fields)";

  public static final String FLIP_RECORD_ICON = "flip_orientation";

  public static final String FLIP_RECORD_NAME = "Flip Record Orientation";

  private static final long serialVersionUID = 1L;

  private JButton addOkButton;

  private DataObjectLayerAttributesTableModel allAttributes;

  private DataObjectStore dataStore;

  private boolean editable = true;

  private final Map<String, String> fieldInValidMessage = new HashMap<String, String>();

  private final Map<String, Field> fields = new LinkedHashMap<String, Field>();

  private final ThreadLocal<Set<String>> fieldsToValidate = new ThreadLocal<Set<String>>();

  private boolean fieldsValid;

  private final Map<String, Integer> fieldTabIndex = new HashMap<String, Integer>();

  private final Map<Field, String> fieldToNameMap = new HashMap<Field, String>();

  private final ThreadLocal<Boolean> fieldValidationDisabled = new ThreadLocal<Boolean>();

  private final Map<String, Boolean> fieldValidMap = new HashMap<String, Boolean>();

  private final Map<String, Object> fieldValues = new HashMap<String, Object>();

  private GeometryCoordinatesPanel geometryCoordinatesPanel;

  private String lastFocussedFieldName;

  private AbstractDataObjectLayer layer;

  private DataObjectMetaData metaData;

  private LayerDataObject object;

  private final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(
    this);

  private Set<String> readOnlyFieldNames = new HashSet<String>();

  private Set<String> requiredFieldNames = new HashSet<String>();

  private final Map<Integer, Set<String>> tabInvalidFieldMap = new TreeMap<Integer, Set<String>>();

  private final JTabbedPane tabs = new JTabbedPane();

  private ToolBar toolBar;

  private final UndoManager undoManager = new UndoManager() {
    private static final long serialVersionUID = 1L;

    @Override
    public void redo() {
      final boolean validationEnabled = setFieldValidationEnabled(false);
      try {
        super.redo();
      } finally {
        if (validationEnabled) {
          validateFields(DataObjectLayerForm.this.fieldsToValidate.get());
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
          validateFields(DataObjectLayerForm.this.fieldsToValidate.get());
        }
        setFieldValidationEnabled(validationEnabled);
      }
    }

  };

  public DataObjectLayerForm(final AbstractDataObjectLayer layer) {
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
    getAllAttributes().setEditable(isEditable());
    if (metaData.getGeometryAttributeName() != null) {
      addTabGeometry();
    }
    layer.addPropertyChangeListener(this);
    this.undoManager.setLimit(100);
    this.undoManager.addKeyMap(this);
  }

  public DataObjectLayerForm(final AbstractDataObjectLayer layer,
    final LayerDataObject object) {
    this(layer);
    setObject(object);
  }

  public void actionAddCancel() {
    final AbstractDataObjectLayer layer = getLayer();
    final LayerDataObject object = getObject();
    layer.deleteRecords(object);
    this.object = null;
    closeWindow();
  }

  public void actionAddOk() {
    final AbstractDataObjectLayer layer = getLayer();
    final LayerDataObject object = getObject();
    layer.saveChanges(object);
    layer.addSelectedRecords(object);
    closeWindow();
  }

  public void actionZoomToObject() {
    getLayer().zoomToObject(getObject());
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
    field.setUndoManager(this.undoManager);
    if (field instanceof ComboBox) {
      final ComboBox comboBox = (ComboBox)field;
      comboBox.getEditor().getEditorComponent().addFocusListener(this);
    } else {
      ((JComponent)field).addFocusListener(this);
    }
    this.fields.put(fieldName, field);
    this.fieldToNameMap.put(field, fieldName);
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

    GroupLayoutUtil.makeColumns(panel, 2, true);
  }

  public void addReadOnlyFieldNames(final Collection<String> readOnlyFieldNames) {
    this.readOnlyFieldNames.addAll(readOnlyFieldNames);
    for (final Entry<String, Field> entry : this.fields.entrySet()) {
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

  protected JPanel addTab(final int index, final String title) {
    final JPanel panel = new JPanel(new VerticalLayout());
    panel.setOpaque(false);
    addTab(index, title, panel);
    return panel;
  }

  public JScrollPane addTab(final int index, final String name,
    final Component component) {
    boolean init = false;
    final Container parent = this.tabs.getParent();
    if (parent != this) {
      add(this.tabs, BorderLayout.CENTER);
      init = true;
    }
    final JScrollPane scrollPane = new JScrollPane(component);
    this.tabs.insertTab(name, null, scrollPane, null, index);
    if (init) {
      this.tabs.setSelectedIndex(0);
    }
    final JLabel label = new JLabel(name);
    this.tabs.setTabComponentAt(index, label);
    return scrollPane;
  }

  protected JPanel addTab(final String title) {
    final JPanel panel = new JPanel(new VerticalLayout());
    panel.setOpaque(false);
    addTab(title, panel);
    return panel;
  }

  public JScrollPane addTab(final String name, final Component component) {
    return addTab(this.tabs.getTabCount(), name, component);
  }

  protected void addTabAllFields() {
    this.allAttributes = new DataObjectLayerAttributesTableModel(this);
    final BaseJxTable table = AbstractSingleDataObjectTableModel.createTable(this.allAttributes);
    final TableColumnModel columnModel = table.getColumnModel();
    FormAllFieldsModifiedPredicate.add(this, table);
    FormAllFieldsErrorPredicate.add(this, table);

    for (int i = 0; i < columnModel.getColumnCount(); i++) {
      final TableColumn column = columnModel.getColumn(i);
      if (i == 2) {
        final TableCellEditor cellEditor = column.getCellEditor();
        cellEditor.addCellEditorListener(this);
      }
    }

    final JScrollPane scrollPane = addTab("All Fields", table);
    int maxHeight = 500;
    for (final GraphicsDevice device : GraphicsEnvironment.getLocalGraphicsEnvironment()
      .getScreenDevices()) {
      final GraphicsConfiguration graphicsConfiguration = device.getDefaultConfiguration();
      final Rectangle bounds = graphicsConfiguration.getBounds();

      maxHeight = Math.min(bounds.height, maxHeight);
    }
    final int preferredHeight = Math.min(maxHeight,
      (this.allAttributes.getRowCount() + 1) * 20);
    scrollPane.setMinimumSize(new Dimension(100, preferredHeight));
    scrollPane.setMaximumSize(new Dimension(Integer.MAX_VALUE, maxHeight));
    scrollPane.setPreferredSize(new Dimension(800, preferredHeight));
  }

  protected void addTabGeometry() {
    final String geometryAttributeName = this.metaData.getGeometryAttributeName();
    if (this.geometryCoordinatesPanel == null && geometryAttributeName != null) {
      this.geometryCoordinatesPanel = new GeometryCoordinatesPanel(this,
        geometryAttributeName);
      addField(geometryAttributeName, this.geometryCoordinatesPanel);
      final JPanel panel = new JPanel(new GridLayout(1, 1));

      SwingUtil.setTitledBorder(geometryCoordinatesPanel, "Coordinates");
      panel.add(this.geometryCoordinatesPanel);

      addTab("Geometry", panel);
    }
  }

  public ToolBar addToolBar() {
    this.toolBar = new ToolBar();
    add(this.toolBar, BorderLayout.NORTH);
    final DataObjectMetaData metaData = getMetaData();
    final Attribute geometryAttribute = metaData.getGeometryAttribute();
    final boolean hasGeometry = geometryAttribute != null;
    final EnableCheck editable = new ObjectPropertyEnableCheck(this, "editable");

    final MenuFactory menuFactory = ObjectTreeModel.findMenu(this.layer);
    if (menuFactory != null) {
      this.toolBar.addButtonTitleIcon("menu", "Layer Menu", "menu",
        ObjectTree.class, "showMenu", menuFactory, this.layer, this, 10, 10);
    }

    final EnableCheck deletableEnableCheck = new ObjectPropertyEnableCheck(
      this, "deletable");
    this.toolBar.addButton("record", "Delete Record", "table_row_delete",
      deletableEnableCheck, this, "deleteRecord");

    // Cut, Copy Paste

    this.toolBar.addButton("dnd", "Copy Record", "page_copy",
      (EnableCheck)null, this, "dataTransferCopy");

    this.toolBar.addButton("dnd", "Paste Record", "paste_plain", editable,
      this, "dataTransferPaste");

    if (hasGeometry) {
      this.toolBar.addButton("dnd", "Paste Geometry", "geometry_paste",
        editable, this, "pasteGeometry");

    }
    final EnableCheck canUndo = new ObjectPropertyEnableCheck(this.undoManager,
      "canUndo");
    final EnableCheck canRedo = new ObjectPropertyEnableCheck(this.undoManager,
      "canRedo");

    final EnableCheck modifiedOrDeleted = new ObjectPropertyEnableCheck(this,
      "modifiedOrDeleted");

    this.toolBar.addButton("changes", "Revert Record", "arrow_revert",
      modifiedOrDeleted, this, "revertChanges");

    this.toolBar.addButton("changes", "Revert Empty Fields",
      "field_empty_revert", modifiedOrDeleted, this, "revertEmptyFields");

    this.toolBar.addButton("changes", "Undo", "arrow_undo", canUndo,
      this.undoManager, "undo");
    this.toolBar.addButton("changes", "Redo", "arrow_redo", canRedo,
      this.undoManager, "redo");

    // Zoom

    if (hasGeometry) {
      this.toolBar.addButtonTitleIcon("zoom", "Zoom to Object", "magnifier",
        this, "actionZoomToObject");
    }

    // Geometry manipulation
    if (hasGeometry) {
      final DataType geometryDataType = geometryAttribute.getType();
      if (geometryDataType == DataTypes.LINE_STRING
        || geometryDataType == DataTypes.MULTI_LINE_STRING) {
        if (DirectionalAttributes.getProperty(metaData)
          .hasDirectionalAttributes()) {
          this.toolBar.addButton("geometry", FLIP_RECORD_NAME,
            FLIP_RECORD_ICON, editable, this, "flipRecordOrientation");
          this.toolBar.addButton("geometry", FLIP_LINE_ORIENTATION_NAME,
            FLIP_LINE_ORIENTATION_ICON, editable, this, "flipLineOrientation");
          this.toolBar.addButton("geometry", FLIP_FIELDS_NAME,
            FLIP_FIELDS_ICON, editable, this, "flipFields");
        } else {
          this.toolBar.addButton("geometry", "Flip Line Orientation",
            "flip_line", editable, this, "flipLineOrientation");
        }
      }
    }
    return this.toolBar;
  }

  public void addUndo(final UndoableEdit edit) {
    final boolean validationEnabled = setFieldValidationEnabled(false);
    try {
      this.undoManager.addEdit(edit);
    } finally {
      if (validationEnabled) {
        validateFields(this.fieldsToValidate.get());
      }
      setFieldValidationEnabled(validationEnabled);
    }
  }

  public boolean canPasteRecordGeometry() {
    final LayerDataObject record = getObject();
    return this.layer.canPasteRecordGeometry(record);
  }

  public void closeWindow() {
    final Window window = SwingUtilities.windowForComponent(this);
    SwingUtil.setVisible(window, false);
  }

  protected JPanel createPanel(final JPanel container, final String title) {
    final JPanel panel = new JPanel();
    panel.setOpaque(false);
    container.add(panel);
    SwingUtil.setTitledBorder(panel, title);
    return panel;
  }

  public void dataTransferCopy() {
    invokeAction("copy");
  }

  public void dataTransferPaste() {
    invokeAction("paste");
  }

  public void deleteRecord() {
    final LayerDataObject object = getObject();
    if (object != null) {
      getLayer().deleteRecords(object);
    }
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

  public void flipFields() {
    addUndo(new ReverseDataObjectAttributesUndo(this.object));
  }

  public void flipLineOrientation() {
    addUndo(new ReverseDataObjectGeometryUndo(this.object));
  }

  public void flipRecordOrientation() {
    addUndo(new ReverseDataObjectUndo(this.object));
  }

  @Override
  public void focusGained(final FocusEvent e) {
  }

  @Override
  public void focusLost(final FocusEvent e) {
    Component component = e.getComponent();
    while (component != null) {
      if (component instanceof Field) {
        this.lastFocussedFieldName = ((Field)component).getFieldName();
        return;
      } else {
        component = component.getParent();
      }
    }
  }

  public DataObjectLayerAttributesTableModel getAllAttributes() {
    return this.allAttributes;
  }

  public String getCodeValue(final String fieldName, final Object value) {
    final CodeTable codeTable = this.metaData.getCodeTableByColumn(fieldName);
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
    if (this.dataStore == null) {
      if (this.metaData == null) {
        return null;
      } else {
        return this.metaData.getDataObjectStore();
      }
    } else {
      return this.dataStore;
    }
  }

  @SuppressWarnings("unchecked")
  protected <T extends Field> T getField(final String fieldName) {
    synchronized (this.fields) {
      Field field = this.fields.get(fieldName);
      if (field == null) {
        final boolean enabled = !this.readOnlyFieldNames.contains(fieldName);
        try {
          field = SwingUtil.createField(this.metaData, fieldName, enabled);
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
      fieldName = this.fieldToNameMap.get(field);
      field = field.getParent();
    } while (fieldName == null && field != null);
    return fieldName;
  }

  public Set<String> getFieldNames() {
    return this.fields.keySet();
  }

  public Collection<Field> getFields() {
    return this.fields.values();
  }

  protected Map<String, Integer> getFieldTabIndex() {
    return this.fieldTabIndex;
  }

  public String getFieldTitle(final String fieldName) {
    final DataObjectMetaData metaData = getMetaData();
    return metaData.getAttributeTitle(fieldName);
  }

  @SuppressWarnings("unchecked")
  public <T> T getFieldValue(final String name) {
    final Object value = this.fieldValues.get(name);
    final CodeTable codeTable = this.metaData.getCodeTableByColumn(name);
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
    return this.geometryCoordinatesPanel;
  }

  protected JLabel getLabel(final String fieldName) {
    String title = this.metaData.getAttributeTitle(fieldName);
    title = title.replaceAll(" Code$", "");
    title = title.replaceAll(" Ind$", "");
    final JLabel label = new JLabel(title);
    label.setFont(SwingUtil.BOLD_FONT);
    label.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
    return label;
  }

  public String getLastFocussedFieldName() {
    return this.lastFocussedFieldName;
  }

  public AbstractDataObjectLayer getLayer() {
    return this.layer;
  }

  public DataObjectMetaData getMetaData() {
    return this.metaData;
  }

  public LayerDataObject getObject() {
    return this.object;
  }

  public <T> T getOriginalValue(final String fieldName) {
    final LayerDataObject object = getObject();
    return object.getOriginalValue(fieldName);
  }

  @Override
  public PropertyChangeSupport getPropertyChangeSupport() {
    return this.propertyChangeSupport;
  }

  public Set<String> getReadOnlyFieldNames() {
    return this.readOnlyFieldNames;
  }

  public Set<String> getRequiredFieldNames() {
    return this.requiredFieldNames;
  }

  protected int getTabIndex(final String fieldName) {
    Integer index = this.fieldTabIndex.get(fieldName);
    if (index == null) {
      final JComponent field = (JComponent)getField(fieldName);
      if (field == null) {
        return -1;
      } else {
        Component panel = field;
        Component component = field.getParent();
        while (component != this.tabs && component != null) {
          panel = component;
          component = component.getParent();
        }
        index = this.tabs.indexOfComponent(panel);
        this.fieldTabIndex.put(fieldName, index);
      }
    }
    return index;

  }

  public JTabbedPane getTabs() {
    return this.tabs;
  }

  public ToolBar getToolBar() {
    return this.toolBar;
  }

  public UndoManager getUndoManager() {
    return this.undoManager;
  }

  @SuppressWarnings("unchecked")
  public <T> T getValue(final String name) {
    return (T)this.object.getValue(name);
  }

  public Map<String, Object> getValues() {
    final Map<String, Object> values = new LinkedHashMap<String, Object>();
    if (this.object != null) {
      values.putAll(this.object);
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

  public boolean isDeletable() {
    final LayerDataObject object = getObject();
    if (object == null) {
      return false;
    } else {
      return object.isDeletable();
    }
  }

  public boolean isEditable() {
    return this.editable;
  }

  public boolean isEditable(final String attributeName) {
    if (isEditable()) {
      if (!this.readOnlyFieldNames.contains(attributeName)) {
        return true;
      }
    }
    return false;
  }

  public boolean isFieldsValid() {
    return this.fieldsValid;
  }

  public boolean isFieldValid(final String fieldName) {
    final Boolean valid = this.fieldValidMap.get(fieldName);
    return valid != Boolean.FALSE;
  }

  protected boolean isFieldValidationEnabled() {
    final boolean enabled = this.fieldValidationDisabled.get() != Boolean.FALSE;
    return enabled;
  }

  public boolean isModifiedOrDeleted() {
    final LayerDataObject object = getObject();
    if (object == null) {
      return false;
    } else {
      return object.isDeleted() || object.isModified();
    }
  }

  public boolean isReadOnly(final String fieldName) {
    return getReadOnlyFieldNames().contains(fieldName);
  }

  protected boolean isTabValid(final int tabIndex) {
    return this.tabInvalidFieldMap.get(tabIndex) == null;
  }

  public void pasteGeometry() {
    final LayerDataObject record = getObject();
    if (record != null) {
      this.layer.pasteRecordGeometry(record);
    }
  }

  public void pasteValues(final Map<String, Object> map) {
    final Map<String, Object> newValues = new LinkedHashMap<String, Object>(map);
    final Collection<String> ignorePasteFields = this.layer.getProperty("ignorePasteFields");
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
    if (this.object != null) {
      final Object source = event.getSource();
      if (source instanceof Field) {
        final Field field = (Field)source;
        final String fieldName = field.getFieldName();
        final Object fieldValue = field.getFieldValue();
        final Object objectValue = this.object.getValue(fieldName);
        if (!EqualsRegistry.equal(objectValue, fieldValue)) {
          boolean equal = false;
          if (fieldValue instanceof String) {
            final String string = (String)fieldValue;
            if (!StringUtils.hasText(string) && objectValue == null) {
              equal = true;
            }
          }
          if (!equal && layer.isEditable()) {
            this.object.setValueByPath(fieldName, fieldValue);
          }
        }
      } else {
        final LayerDataObject object = getObject();
        if (source == object) {
          if (object.isDeleted()) {
            final Window window = SwingUtilities.getWindowAncestor(this);
            SwingUtil.setVisible(window, false);

          }
          final String propertyName = event.getPropertyName();
          final Object value = event.getNewValue();
          final DataObjectMetaData metaData = getMetaData();
          if ("errorsUpdated".equals(propertyName)) {
            updateErrors();
          } else if (metaData.hasAttribute(propertyName)) {
            setFieldValue(propertyName, value, isFieldValidationEnabled());
          }
          final boolean modifiedOrDeleted = isModifiedOrDeleted();
          this.propertyChangeSupport.firePropertyChange("modifiedOrDeleted",
            !modifiedOrDeleted, modifiedOrDeleted);
          final boolean deletable = isDeletable();
          this.propertyChangeSupport.firePropertyChange("deletable",
            !deletable, deletable);
          repaint();
        }
      }
    }
  }

  @Override
  public void removeNotify() {
    try {
      super.removeNotify();
    } finally {
      if (this.layer != null) {
        if (this.allAttributes != null) {
          this.layer.removePropertyChangeListener(this.allAttributes);
          this.allAttributes = null;
        }
        this.layer.removePropertyChangeListener(this);
        this.layer = null;
      }
    }
  }

  public void revertChanges() {
    final LayerDataObject object = getObject();
    if (object != null) {
      object.revertChanges();
    }
  }

  public void revertEmptyFields() {
    final LayerDataObject record = getObject();
    if (record != null) {
      record.revertEmptyFields();
    }
  }

  public void setAddOkButtonEnabled(final boolean enabled) {
    if (this.addOkButton != null) {
      this.addOkButton.setEnabled(enabled);
    }
  }

  protected void setDataStore(final DataObjectStore dataStore) {
    this.dataStore = dataStore;
  }

  public void setEditable(final boolean editable) {
    this.editable = editable;
    for (final String fieldName : getFieldNames()) {
      if (!getReadOnlyFieldNames().contains(fieldName)) {
        final Field field = getField(fieldName);
        field.setEnabled(editable);
      }
    }
  }

  public void setFieldFocussed(final String fieldName) {
    final int tabIndex = getTabIndex(fieldName);
    if (tabIndex >= 0) {
      this.tabs.setSelectedIndex(tabIndex);
    }
    final JComponent field = (JComponent)getField(fieldName);
    if (field != null) {
      field.requestFocusInWindow();
    }
  }

  public void setFieldInvalid(final String fieldName, String message) {
    if (message == null) {
      message = "Invalid value";
    }
    if (!EqualsRegistry.equal(message, this.fieldInValidMessage.equals(message))) {
      if (SwingUtilities.isEventDispatchThread()) {
        this.fieldInValidMessage.put(fieldName, message);
        this.fieldsValid = false;
        final Field field = getField(fieldName);
        field.setFieldInvalid(message, WebColors.Red);

        this.fieldValidMap.put(fieldName, false);
        final int tabIndex = getTabIndex(fieldName);
        CollectionUtil.addToSet(this.tabInvalidFieldMap, tabIndex, fieldName);
        updateTabValid(tabIndex);
      } else {
        Invoke.later(this, "setFieldInvalid", fieldName, message);
      }
    }
  }

  public void setFieldInvalidToolTip(final String fieldName,
    final JComponent field) {
    final String message = this.fieldInValidMessage.get(fieldName);
    if (StringUtils.hasText(message)) {
      field.setToolTipText(message);
    }
  }

  public boolean setFieldValid(final String fieldName) {
    if (!isFieldValid(fieldName)) {
      if (SwingUtilities.isEventDispatchThread()) {
        final Field field = getField(fieldName);
        field.setFieldValid();
        if (this.object.isModified(fieldName)) {
          final Object originalValue = this.object.getOriginalValue(fieldName);
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
        this.fieldValidMap.put(fieldName, true);
        this.fieldInValidMessage.remove(fieldName);
        final int tabIndex = getTabIndex(fieldName);
        CollectionUtil.removeFromSet(this.tabInvalidFieldMap, tabIndex,
          fieldName);
        updateTabValid(tabIndex);
        return true;
      } else {
        Invoke.later(this, "setFieldValid", fieldName);
        return false;
      }
    }
    return false;
  }

  protected boolean setFieldValidationEnabled(
    final boolean fieldValidationEnabled) {
    final boolean oldValue = isFieldValidationEnabled();
    if (fieldValidationEnabled) {
      this.fieldsToValidate.remove();
    } else if (oldValue) {
      this.fieldsToValidate.set(new TreeSet<String>());
    }
    this.fieldValidationDisabled.set(fieldValidationEnabled);
    return oldValue;
  }

  public void setFieldValue(final String fieldName, final Object value,
    final boolean validate) {
    boolean changed = false;
    final Object oldValue = getFieldValue(fieldName);
    this.fieldValues.put(fieldName, value);
    final JComponent field = (JComponent)getField(fieldName);
    if (oldValue == null & value != null
      || !EqualsRegistry.equal(value, oldValue)) {
      changed = true;
    }
    final Object objectValue = this.object.getValue(fieldName);
    if (!EqualsRegistry.equal(value, objectValue)) {
      this.object.setValueByPath(fieldName, value);
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
    final boolean undo = this.undoManager.setEventsEnabled(false);
    final boolean validate = setFieldValidationEnabled(false);
    try {
      this.object = object;
      this.allAttributes.setObject(object);
      setValues(object);
      this.undoManager.discardAllEdits();
    } finally {
      setFieldValidationEnabled(validate);
      this.undoManager.setEventsEnabled(undo);
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

  public void setTabColor(final int index) {
    if (index > -1) {
      if (SwingUtilities.isEventDispatchThread()) {
        this.tabs.setBackgroundAt(index, null);
      } else {
        Invoke.later(this, "setTabColor", index);
      }
    }
  }

  public void setTabColor(final int index, final Color color) {
    if (index > -1) {
      if (SwingUtilities.isEventDispatchThread()) {
        this.tabs.setBackgroundAt(index, color);
      } else {
        if (color == null) {
          Invoke.later(this, "setTabColor", index);
        } else {
          Invoke.later(this, "setTabColor", index, color);
        }
      }
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
            final JComponent field = (JComponent)getField(fieldName);
            SwingUtil.setFieldValue(field, value);
          }
        } finally {
          setFieldValidationEnabled(validationEnabled);
        }
        validateFields(fieldNames);
      } else {
        Invoke.later(this, "setValues", values);
      }
    }

  }

  public LayerDataObject showAddDialog() {
    final String title = "Add New " + getName();
    final Window window = SwingUtil.getActiveWindow();
    final JDialog dialog = new JDialog(window, title,
      ModalityType.APPLICATION_MODAL);
    dialog.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
    dialog.setLayout(new BorderLayout());

    dialog.add(this, BorderLayout.CENTER);

    final JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    dialog.add(buttons, BorderLayout.SOUTH);
    final JButton addCancelButton = InvokeMethodAction.createButton("Cancel",
      this, "actionAddCancel");
    buttons.add(addCancelButton);
    this.addOkButton = InvokeMethodAction.createButton("OK", this,
      "actionAddOk");
    buttons.add(this.addOkButton);

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
    for (final Entry<String, Field> entry : this.fields.entrySet()) {
      final String name = entry.getKey();
      final Field field = entry.getValue();
      if (this.readOnlyFieldNames.contains(name)) {
        field.setEnabled(false);
      } else {
        field.setEnabled(true);
      }
    }
    if (this.allAttributes != null) {
      this.allAttributes.setReadOnlyAttributeNames(this.readOnlyFieldNames);
    }
  }

  public boolean updateTabValid(final int tabIndex) {
    final boolean tabValid = isTabValid(tabIndex);
    if (tabValid) {
      setTabColor(tabIndex, null);
    } else {
      setTabColor(tabIndex, WebColors.Red);
    }
    return tabValid;
  }

  public boolean validateField(final String fieldName) {
    if (SwingUtilities.isEventDispatchThread()) {
      Invoke.background("Validate Field " + fieldName, this, "validateField",
        fieldName);
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
        if (this.object.getState() == DataObjectState.New) {
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
      final int tabIndex = getTabIndex(fieldName);
      updateTabValid(tabIndex);
    }
    return valid;
  }

  public void validateFields() {
    final Set<String> fieldNames = getFieldNames();
    validateFields(fieldNames);
  }

  protected boolean validateFields(final Collection<String> fieldNames) {
    boolean valid = true;
    if (isFieldValidationEnabled()) {
      for (final String fieldName : fieldNames) {
        valid &= validateFieldInternal(fieldName);
      }
    }
    return valid;
  }
}
