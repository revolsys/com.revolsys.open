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
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
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
import com.revolsys.data.codes.CodeTable;
import com.revolsys.data.equals.EqualsRegistry;
import com.revolsys.data.identifier.SingleIdentifier;
import com.revolsys.data.io.RecordStore;
import com.revolsys.data.record.Record;
import com.revolsys.data.record.RecordState;
import com.revolsys.data.record.property.DirectionalAttributes;
import com.revolsys.data.record.schema.Attribute;
import com.revolsys.data.record.schema.RecordDefinition;
import com.revolsys.data.types.DataType;
import com.revolsys.data.types.DataTypes;
import com.revolsys.swing.SwingUtil;
import com.revolsys.swing.action.InvokeMethodAction;
import com.revolsys.swing.action.enablecheck.EnableCheck;
import com.revolsys.swing.action.enablecheck.ObjectPropertyEnableCheck;
import com.revolsys.swing.dnd.transferhandler.RecordLayerFormTransferHandler;
import com.revolsys.swing.field.ComboBox;
import com.revolsys.swing.field.Field;
import com.revolsys.swing.field.NumberTextField;
import com.revolsys.swing.field.ObjectLabelField;
import com.revolsys.swing.layout.GroupLayoutUtil;
import com.revolsys.swing.listener.WeakFocusListener;
import com.revolsys.swing.map.ProjectFrame;
import com.revolsys.swing.map.layer.record.AbstractRecordLayer;
import com.revolsys.swing.map.layer.record.LayerRecord;
import com.revolsys.swing.map.layer.record.table.model.RecordLayerAttributesTableModel;
import com.revolsys.swing.map.layer.record.table.model.RecordLayerTableModel;
import com.revolsys.swing.map.layer.record.table.predicate.FormAllFieldsErrorPredicate;
import com.revolsys.swing.map.layer.record.table.predicate.FormAllFieldsModifiedPredicate;
import com.revolsys.swing.menu.MenuFactory;
import com.revolsys.swing.parallel.Invoke;
import com.revolsys.swing.table.BaseJxTable;
import com.revolsys.swing.table.record.editor.RecordTableCellEditor;
import com.revolsys.swing.table.record.model.AbstractSingleRecordTableModel;
import com.revolsys.swing.toolbar.ToolBar;
import com.revolsys.swing.tree.ObjectTree;
import com.revolsys.swing.tree.model.ObjectTreeModel;
import com.revolsys.swing.undo.ReverseRecordAttributesUndo;
import com.revolsys.swing.undo.ReverseRecordGeometryUndo;
import com.revolsys.swing.undo.ReverseRecordUndo;
import com.revolsys.swing.undo.UndoManager;
import com.revolsys.util.CollectionUtil;
import com.revolsys.util.Property;

public class LayerRecordForm extends JPanel implements PropertyChangeListener,
CellEditorListener, FocusListener, PropertyChangeSupportProxy, WindowListener {

  public static final String FLIP_FIELDS_ICON = "flip_fields";

  public static final String FLIP_FIELDS_NAME = "Flip Fields Orientation";

  public static final String FLIP_LINE_ORIENTATION_ICON = "flip_line_orientation";

  public static final String FLIP_LINE_ORIENTATION_NAME = "Flip Line Orientation (Visually Flips Fields)";

  public static final String FLIP_RECORD_ICON = "flip_orientation";

  public static final String FLIP_RECORD_NAME = "Flip Record Orientation";

  private static final long serialVersionUID = 1L;

  private JButton addOkButton;

  private RecordLayerAttributesTableModel allAttributes;

  private RecordStore recordStore;

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

  private AbstractRecordLayer layer;

  private RecordDefinition recordDefinition;

  private LayerRecord record;

  private PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(
    this);

  private Set<String> readOnlyFieldNames = new HashSet<String>();

  private Set<String> requiredFieldNames = new HashSet<String>();

  private final Map<Integer, Set<String>> tabInvalidFieldMap = new TreeMap<Integer, Set<String>>();

  private JTabbedPane tabs = new JTabbedPane();

  private ToolBar toolBar;

  private UndoManager undoManager = new RecordLayerFormUndoManager(this);

  private String focussedFieldName;

  public LayerRecordForm(final AbstractRecordLayer layer) {
    ProjectFrame.addSaveActions(this, layer.getProject());
    setLayout(new BorderLayout());
    setName(layer.getName());
    this.layer = layer;
    final RecordDefinition recordDefinition = layer.getRecordDefinition();
    setRecordDefinition(recordDefinition);
    addToolBar(layer);

    final ActionMap map = getActionMap();
    map.put("copy", TransferHandler.getCopyAction());
    map.put("paste", TransferHandler.getPasteAction());

    final RecordLayerFormTransferHandler transferHandler = new RecordLayerFormTransferHandler(
      this);
    setTransferHandler(transferHandler);
    setFont(SwingUtil.FONT);

    addTabAllFields();
    final boolean editable = layer.isEditable();
    setEditable(editable);
    getAllAttributes().setEditable(isEditable());
    if (recordDefinition.getGeometryAttributeName() != null) {
      addTabGeometry();
    }
    Property.addListener(layer, this);
    this.undoManager.setLimit(100);
    this.undoManager.addKeyMap(this);
  }

  public LayerRecordForm(final AbstractRecordLayer layer,
    final LayerRecord object) {
    this(layer);
    setRecord(object);
  }

  public void actionAddCancel() {
    final AbstractRecordLayer layer = getLayer();
    final LayerRecord object = getRecord();
    layer.deleteRecords(object);
    this.record = null;
    closeWindow();
  }

  public void actionAddOk() {
    final AbstractRecordLayer layer = getLayer();
    final LayerRecord record = getRecord();
    layer.saveChanges(record);
    layer.setSelectedRecords(record);
    layer.showRecordsTable(RecordLayerTableModel.MODE_SELECTED);
    closeWindow();
  }

  public void actionZoomToObject() {
    getLayer().zoomToObject(getRecord());
  }

  protected ObjectLabelField addCodeTableLabelField(final String fieldName) {
    final RecordStore recordStore = getDataStore();
    final CodeTable codeTable = recordStore.getCodeTableByColumn(fieldName);
    final ObjectLabelField field = new ObjectLabelField(fieldName, codeTable);
    field.setFont(SwingUtil.FONT);
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
    Property.addListener(field, fieldName, this);
    field.setUndoManager(this.undoManager);
    if (field instanceof ComboBox) {
      final ComboBox comboBox = (ComboBox)field;
      comboBox.getEditor()
      .getEditorComponent()
      .addFocusListener(new WeakFocusListener(this));
    } else {
      ((JComponent)field).addFocusListener(new WeakFocusListener(this));
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
    this.allAttributes = new RecordLayerAttributesTableModel(this);
    final BaseJxTable table = AbstractSingleRecordTableModel.createTable(this.allAttributes);
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
    final String geometryAttributeName = this.recordDefinition.getGeometryAttributeName();
    if (this.geometryCoordinatesPanel == null && geometryAttributeName != null) {
      this.geometryCoordinatesPanel = new GeometryCoordinatesPanel(this,
        geometryAttributeName);
      addField(geometryAttributeName, this.geometryCoordinatesPanel);
      final JPanel panel = new JPanel(new GridLayout(1, 1));

      SwingUtil.setTitledBorder(this.geometryCoordinatesPanel, "Coordinates");
      panel.add(this.geometryCoordinatesPanel);

      addTab("Geometry", panel);
    }
  }

  public ToolBar addToolBar(final AbstractRecordLayer layer) {
    this.toolBar = new ToolBar();
    add(this.toolBar, BorderLayout.NORTH);
    final RecordDefinition recordDefinition = getRecordDefinition();
    final Attribute geometryAttribute = recordDefinition.getGeometryAttribute();
    final boolean hasGeometry = geometryAttribute != null;
    final EnableCheck editable = new ObjectPropertyEnableCheck(this, "editable");

    if (layer != null) {
      final MenuFactory menuFactory = ObjectTreeModel.findMenu(layer);
      if (menuFactory != null) {
        this.toolBar.addButtonTitleIcon("menu", "Layer Menu", "menu",
          ObjectTree.class, "showMenu", menuFactory, layer, this, 10, 10);
      }
    }
    final EnableCheck deletableEnableCheck = new ObjectPropertyEnableCheck(
      this, "deletable");
    this.toolBar.addButton("record", "Delete Record", "table_row_delete",
      deletableEnableCheck, this, "deleteRecord");

    // Cut, Copy Paste

    this.toolBar.addButton("dnd", "Copy Record", "page_copy",
      (EnableCheck)null, this, "dataTransferCopy");

    this.toolBar.addButton("dnd", "Copy Geometry", "geometry_copy",
      (EnableCheck)null, this, "copyGeometry");

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
        if (DirectionalAttributes.getProperty(recordDefinition)
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
    final AbstractRecordLayer layer = getLayer();
    if (layer == null) {
      return false;
    } else {
      final LayerRecord record = getRecord();
      return layer.canPasteRecordGeometry(record);
    }
  }

  public void clearTabColor(final int index) {
    if (index > -1) {
      if (SwingUtilities.isEventDispatchThread()) {
        this.tabs.setTabComponentAt(index, null);
      } else {
        Invoke.later(this, "setTabColor", index);
      }
    }
  }

  public void closeWindow() {
    final Window window = SwingUtilities.windowForComponent(this);
    SwingUtil.setVisible(window, false);
  }

  public void copyGeometry() {
    final LayerRecord record = getRecord();
    final AbstractRecordLayer layer = getLayer();
    if (layer != null) {
      if (record != null) {
        layer.copyRecordGeometry(record);
      }
    }
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
    final LayerRecord object = getRecord();
    if (object != null) {
      getLayer().deleteRecords(object);
    }
  }

  public void destroy() {
    this.addOkButton = null;
    this.allAttributes = null;
    this.recordStore = null;
    this.fieldInValidMessage.clear();
    for (final Field field : this.fields.values()) {
      Property.removeAllListeners(field);
    }
    this.fields.clear();
    this.fieldTabIndex.clear();
    this.fieldToNameMap.clear();
    this.fieldValidMap.clear();
    this.geometryCoordinatesPanel = null;
    this.recordDefinition = null;
    this.record = null;
    this.propertyChangeSupport = null;
    this.readOnlyFieldNames.clear();
    this.tabInvalidFieldMap.clear();
    this.tabs = null;
    this.toolBar = null;
    this.undoManager = null;
    final Container parent = getParent();
    if (parent != null) {
      parent.remove(this);
    }

    final AbstractRecordLayer layer = getLayer();
    if (layer != null) {
      this.layer = null;
      if (this.allAttributes != null) {
        Property.removeListener(layer, this.allAttributes);
        this.allAttributes = null;
      }
      Property.removeListener(layer, this);
    }
    final Window window = SwingUtil.getWindowAncestor(this);
    if (window != null) {
      window.removeWindowListener(this);
    }
    removeAll();
  }

  @Override
  public void editingCanceled(final ChangeEvent e) {
  }

  @Override
  public void editingStopped(final ChangeEvent e) {
    final RecordTableCellEditor editor = (RecordTableCellEditor)e.getSource();
    final String name = editor.getAttributeName();
    final Object value = editor.getCellEditorValue();
    setFieldValue(name, value, true);
  }

  public void flipFields() {
    addUndo(new ReverseRecordAttributesUndo(this.record));
  }

  public void flipLineOrientation() {
    addUndo(new ReverseRecordGeometryUndo(this.record));
  }

  public void flipRecordOrientation() {
    addUndo(new ReverseRecordUndo(this.record));
  }

  @Override
  public void focusGained(final FocusEvent e) {
    Component component = e.getComponent();
    while (component != null) {
      if (component instanceof Field) {
        final Field field = (Field)component;
        this.focussedFieldName = field.getFieldName();
        return;
      } else {
        component = component.getParent();
      }
    }
  }

  @Override
  public void focusLost(final FocusEvent e) {
    Component component = e.getComponent();
    while (component != null) {
      if (component instanceof Field) {
        final Field field = (Field)component;
        this.lastFocussedFieldName = field.getFieldName();
        return;
      } else {
        component = component.getParent();
      }
    }
  }

  public RecordLayerAttributesTableModel getAllAttributes() {
    return this.allAttributes;
  }

  public String getCodeValue(final String fieldName, final Object value) {
    final CodeTable codeTable = this.recordDefinition.getCodeTableByColumn(fieldName);
    String string;
    if (value == null) {
      return "-";
    } else if (codeTable == null) {
      string = StringConverterRegistry.toString(value);
    } else {
      final List<Object> values = codeTable.getValues(SingleIdentifier.create(value));
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

  public RecordStore getDataStore() {
    if (this.recordStore == null) {
      if (this.recordDefinition == null) {
        return null;
      } else {
        return this.recordDefinition.getDataStore();
      }
    } else {
      return this.recordStore;
    }
  }

  public Color getErrorForegroundColor() {
    return WebColors.Red;
  }

  @SuppressWarnings("unchecked")
  protected <T extends Field> T getField(final String fieldName) {
    synchronized (this.fields) {
      Field field = this.fields.get(fieldName);
      if (field == null) {
        final boolean enabled = !this.readOnlyFieldNames.contains(fieldName);
        try {
          field = SwingUtil.createField(this.recordDefinition, fieldName, enabled);
          addField(fieldName, field);
        } catch (final IllegalArgumentException e) {
        }
      }
      if (field != null && !isEditable()) {
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

  public Set<String> getFieldsToValidate() {
    return this.fieldsToValidate.get();
  }

  protected Map<String, Integer> getFieldTabIndex() {
    return this.fieldTabIndex;
  }

  @SuppressWarnings("unchecked")
  public <T> T getFieldValue(final String name) {
    final Object value = this.fieldValues.get(name);
    final CodeTable codeTable = this.recordDefinition.getCodeTableByColumn(name);
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
    return getRecordDefinition().getGeometryAttributeName();
  }

  public GeometryCoordinatesPanel getGeometryCoordinatesPanel() {
    return this.geometryCoordinatesPanel;
  }

  protected JLabel getLabel(final String fieldName) {
    final AbstractRecordLayer layer = getLayer();
    String title = layer.getFieldTitle(fieldName);
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

  public AbstractRecordLayer getLayer() {
    return this.layer;
  }

  public RecordDefinition getRecordDefinition() {
    return this.recordDefinition;
  }

  public <T> T getOriginalValue(final String fieldName) {
    final LayerRecord object = getRecord();
    return object.getOriginalValue(fieldName);
  }

  @Override
  public PropertyChangeSupport getPropertyChangeSupport() {
    return this.propertyChangeSupport;
  }

  public Set<String> getReadOnlyFieldNames() {
    return this.readOnlyFieldNames;
  }

  public LayerRecord getRecord() {
    return this.record;
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
    return (T)this.record.getValue(name);
  }

  public Map<String, Object> getValues() {
    final Map<String, Object> values = new LinkedHashMap<String, Object>();
    if (this.record != null) {
      values.putAll(this.record);
    }
    return values;
  }

  public boolean hasOriginalValue(final String name) {
    return getRecordDefinition().hasAttribute(name);
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
    final LayerRecord object = getRecord();
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
    final LayerRecord object = getRecord();
    if (object == null) {
      return false;
    } else {
      return object.isDeleted() || object.isModified();
    }
  }

  public boolean isReadOnly(final String fieldName) {
    return getReadOnlyFieldNames().contains(fieldName);
  }

  public boolean isSame(final Object object) {
    final LayerRecord record = getRecord();
    if (record != null) {
      if (object instanceof Record) {
        final Record otherRecord = (Record)object;
        if (record.isSame(otherRecord)) {
          return true;
        }
      }
    }
    return false;
  }

  protected boolean isTabValid(final int tabIndex) {
    return this.tabInvalidFieldMap.get(tabIndex) == null;
  }

  public void pasteGeometry() {
    final LayerRecord record = getRecord();
    final AbstractRecordLayer layer = getLayer();
    if (layer != null) {
      if (record != null) {
        layer.pasteRecordGeometry(record);
      }
    }
  }

  public void pasteValues(final Map<String, Object> map) {
    final AbstractRecordLayer layer = getLayer();
    if (layer != null) {
      final Map<String, Object> newValues = new LinkedHashMap<String, Object>(
          map);
      final Collection<String> ignorePasteFields = layer.getProperty("ignorePasteFields");
      if (ignorePasteFields != null) {
        newValues.keySet().removeAll(ignorePasteFields);
      }
      newValues.keySet().removeAll(getReadOnlyFieldNames());

      final Map<String, Object> values = getValues();
      values.putAll(newValues);
      setValues(values);
    }
  }

  protected void postValidate() {

  }

  @Override
  public void propertyChange(final PropertyChangeEvent event) {
    final AbstractRecordLayer layer = getLayer();
    if (layer != null) {
      final LayerRecord record = getRecord();
      if (record != null) {
        final Object source = event.getSource();
        final String propertyName = event.getPropertyName();
        if (source == layer) {
          if (propertyName.equals("recordsChanged")) {
            setRecord(record);
          }
        } else if (source instanceof Field) {
          final Field field = (Field)source;
          final String fieldName = field.getFieldName();
          final Object fieldValue = field.getFieldValue();
          final Object objectValue = this.record.getValue(fieldName);
          if (!EqualsRegistry.equal(objectValue, fieldValue)) {
            boolean equal = false;
            if (fieldValue instanceof String) {
              final String string = (String)fieldValue;
              if (!StringUtils.hasText(string) && objectValue == null) {
                equal = true;
              }
            }
            if (!equal && layer.isEditable()) {
              this.record.setValueByPath(fieldName, fieldValue);
            }
          }
        } else {
          if (isSame(source)) {
            if (record.isDeleted()) {
              final Window window = SwingUtilities.getWindowAncestor(this);
              SwingUtil.setVisible(window, false);
            }
            final Object value = event.getNewValue();
            final RecordDefinition recordDefinition = getRecordDefinition();
            if ("errorsUpdated".equals(propertyName)) {
              updateErrors();
            } else if (recordDefinition.hasAttribute(propertyName)) {
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
  }

  public void revertChanges() {
    final LayerRecord object = getRecord();
    if (object != null) {
      object.revertChanges();
      setValues(object);
    }
  }

  public void revertEmptyFields() {
    final LayerRecord record = getRecord();
    if (record != null) {
      record.revertEmptyFields();
    }
  }

  public void setAddOkButtonEnabled(final boolean enabled) {
    if (this.addOkButton != null) {
      this.addOkButton.setEnabled(enabled);
    }
  }

  protected void setDataStore(final RecordStore recordStore) {
    this.recordStore = recordStore;
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
        field.setFieldInvalid(message, WebColors.Red, WebColors.Pink);

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
        if (this.record.isModified(fieldName)) {
          final Object originalValue = this.record.getOriginalValue(fieldName);
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

  public void setFieldValue(final String fieldName, Object value,
    final boolean validate) {
    boolean changed = false;
    final Object oldValue = getFieldValue(fieldName);
    final RecordDefinition recordDefinition = getRecordDefinition();
    if (recordDefinition != null) {
      try {
        final Class<?> attributeClass = recordDefinition.getAttributeClass(fieldName);
        value = StringConverterRegistry.toObject(attributeClass, value);
      } catch (final Throwable e) {
      }
    }
    this.fieldValues.put(fieldName, value);
    final JComponent field = (JComponent)getField(fieldName);
    if (oldValue == null & value != null
        || !EqualsRegistry.equal(value, oldValue)) {
      changed = true;
    }
    final Object objectValue = this.record.getValue(fieldName);
    if (!EqualsRegistry.equal(value, objectValue)) {
      this.record.setValueByPath(fieldName, value);
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

  public void setRecordDefinition(final RecordDefinition recordDefinition) {
    this.recordDefinition = recordDefinition;
    setDataStore(recordDefinition.getDataStore());
    final String idAttributeName = recordDefinition.getIdAttributeName();
    if (StringUtils.hasText(idAttributeName)) {
      this.readOnlyFieldNames.add(idAttributeName);
    }
    for (final Attribute attribute : recordDefinition.getAttributes()) {
      if (attribute.isRequired()) {
        final String name = attribute.getName();
        addRequiredFieldNames(name);
      }

    }
  }

  public void setReadOnlyFieldNames(final Collection<String> readOnlyFieldNames) {
    this.readOnlyFieldNames = new HashSet<String>(readOnlyFieldNames);
    updateReadOnlyFields();
  }

  public void setReadOnlyFieldNames(final String... readOnlyFieldNames) {
    setReadOnlyFieldNames(Arrays.asList(readOnlyFieldNames));
  }

  public void setRecord(final LayerRecord object) {
    final boolean undo = this.undoManager.setEventsEnabled(false);
    final boolean validate = setFieldValidationEnabled(false);
    try {
      this.record = object;
      this.allAttributes.setObject(object);
      setValues(object);
      this.undoManager.discardAllEdits();
    } finally {
      setFieldValidationEnabled(validate);
      this.undoManager.setEventsEnabled(undo);
    }
  }

  public void setRequiredFieldNames(final Collection<String> requiredFieldNames) {
    this.requiredFieldNames = new HashSet<String>(requiredFieldNames);
  }

  public void setTabColor(final int index, final Color foregroundColor) {
    if (index > -1) {
      if (foregroundColor == null) {
        this.tabs.setTabComponentAt(index, null);
      } else {
        if (SwingUtilities.isEventDispatchThread()) {
          final JLabel label = new JLabel(this.tabs.getTitleAt(index));
          label.setOpaque(false);
          label.setForeground(foregroundColor);
          this.tabs.setTabComponentAt(index, label);
        } else {
          Invoke.later(this, "setTabColor", index, foregroundColor);
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

  public LayerRecord showAddDialog() {
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
    dialog.addWindowListener(this);
    dialog.setVisible(true);
    final LayerRecord object = getRecord();
    dialog.dispose();
    return object;
  }

  protected void updateErrors() {
  }

  public void updateFocussedField() {
    final Field field = this.fields.get(this.focussedFieldName);
    if (field != null) {
      field.updateFieldValue();
    }
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
      this.allAttributes.setReadOnlyFieldNames(this.readOnlyFieldNames);
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

  protected boolean validateFieldInternal(final String fieldName) {
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
        if (this.record.getState() == RecordState.New) {
          final String idAttributeName = getRecordDefinition().getIdAttributeName();
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

  @Override
  public void windowActivated(final WindowEvent e) {
  }

  @Override
  public void windowClosed(final WindowEvent e) {
    destroy();
    final Window window = (Window)e.getSource();
    window.removeWindowListener(this);
  }

  @Override
  public void windowClosing(final WindowEvent e) {
    updateFocussedField();
  }

  @Override
  public void windowDeactivated(final WindowEvent e) {
    updateFocussedField();
  }

  @Override
  public void windowDeiconified(final WindowEvent e) {
  }

  @Override
  public void windowIconified(final WindowEvent e) {
  }

  @Override
  public void windowOpened(final WindowEvent e) {
  }
}
