package com.revolsys.swing.map.form;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dialog.ModalityType;
import java.awt.FlowLayout;
import java.awt.GridLayout;
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
import javax.swing.undo.UndoableEdit;

import org.jdesktop.swingx.VerticalLayout;
import org.slf4j.LoggerFactory;

import com.revolsys.awt.WebColors;
import com.revolsys.beans.PropertyChangeSupportProxy;
import com.revolsys.collection.map.Maps;
import com.revolsys.converter.string.StringConverterRegistry;
import com.revolsys.datatype.DataType;
import com.revolsys.datatype.DataTypes;
import com.revolsys.equals.Equals;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.identifier.Identifier;
import com.revolsys.record.Record;
import com.revolsys.record.RecordState;
import com.revolsys.record.code.CodeTable;
import com.revolsys.record.property.DirectionalFields;
import com.revolsys.record.schema.FieldDefinition;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.record.schema.RecordStore;
import com.revolsys.swing.Panels;
import com.revolsys.swing.SwingUtil;
import com.revolsys.swing.action.RunnableAction;
import com.revolsys.swing.action.enablecheck.EnableCheck;
import com.revolsys.swing.action.enablecheck.ObjectPropertyEnableCheck;
import com.revolsys.swing.dnd.transferhandler.RecordLayerFormTransferHandler;
import com.revolsys.swing.field.ComboBox;
import com.revolsys.swing.field.Field;
import com.revolsys.swing.field.NumberTextField;
import com.revolsys.swing.field.ObjectLabelField;
import com.revolsys.swing.layout.GroupLayouts;
import com.revolsys.swing.listener.WeakFocusListener;
import com.revolsys.swing.map.ProjectFrame;
import com.revolsys.swing.map.layer.record.AbstractRecordLayer;
import com.revolsys.swing.map.layer.record.LayerRecord;
import com.revolsys.swing.map.layer.record.component.RecordLayerFields;
import com.revolsys.swing.map.layer.record.table.model.LayerRecordTableModel;
import com.revolsys.swing.map.layer.record.table.model.RecordLayerTableModel;
import com.revolsys.swing.menu.MenuFactory;
import com.revolsys.swing.parallel.Invoke;
import com.revolsys.swing.table.TablePanel;
import com.revolsys.swing.table.record.editor.RecordTableCellEditor;
import com.revolsys.swing.toolbar.ToolBar;
import com.revolsys.swing.undo.ReverseRecordFieldsUndo;
import com.revolsys.swing.undo.ReverseRecordGeometryUndo;
import com.revolsys.swing.undo.ReverseRecordUndo;
import com.revolsys.swing.undo.UndoManager;
import com.revolsys.util.Property;
import com.revolsys.util.Strings;

public class RecordLayerForm extends JPanel implements PropertyChangeListener, CellEditorListener,
  FocusListener, PropertyChangeSupportProxy, WindowListener {

  public static final String FLIP_FIELDS_ICON = "flip_fields";

  public static final String FLIP_FIELDS_NAME = "Flip Fields Orientation";

  public static final String FLIP_LINE_ORIENTATION_ICON = "flip_line_orientation";

  public static final String FLIP_LINE_ORIENTATION_NAME = "Flip Line Orientation (Visually Flips Fields)";

  public static final String FLIP_RECORD_ICON = "flip_orientation";

  public static final String FLIP_RECORD_NAME = "Flip Record Orientation";

  private static final long serialVersionUID = 1L;

  private JButton addOkButton = RunnableAction.newButton("OK", this::actionAddOk);

  private LayerRecord addRecord;

  private LayerRecordTableModel fieldsTableModel;

  private boolean allowAddWithErrors = false;

  private boolean cancelled = false;

  private boolean editable = true;

  private final Map<String, List<String>> fieldErrors = new HashMap<>();

  private final Map<String, String> fieldInValidMessage = new HashMap<String, String>();

  private final Map<String, Field> fields = new LinkedHashMap<String, Field>();

  private final ThreadLocal<Set<String>> fieldsToValidate = new ThreadLocal<Set<String>>();

  private final Map<String, Integer> fieldTabIndex = new HashMap<String, Integer>();

  private final Map<Field, String> fieldToNameMap = new HashMap<Field, String>();

  private final ThreadLocal<Boolean> fieldValidationDisabled = new ThreadLocal<Boolean>();

  private final Map<String, Object> fieldValues = new HashMap<>();

  private final Map<String, List<String>> fieldWarnings = new HashMap<>();

  private String focussedFieldName;

  private GeometryCoordinatesPanel geometryCoordinatesPanel;

  private final Set<String> invalidFieldNames = new HashSet<>();

  private String lastFocussedFieldName;

  private AbstractRecordLayer layer;

  private PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

  private Set<String> readOnlyFieldNames = new HashSet<String>();

  private LayerRecord record;

  private RecordDefinition recordDefinition;

  private RecordStore recordStore;

  private Set<String> requiredFieldNames = new HashSet<String>();

  private final Map<Integer, Set<String>> tabInvalidFieldMap = new TreeMap<Integer, Set<String>>();

  private JTabbedPane tabs = new JTabbedPane();

  private ToolBar toolBar;

  private UndoManager undoManager = new RecordLayerFormUndoManager(this);

  public RecordLayerForm(final AbstractRecordLayer layer) {
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

    final RecordLayerFormTransferHandler transferHandler = new RecordLayerFormTransferHandler(this);
    setTransferHandler(transferHandler);
    setFont(SwingUtil.FONT);

    addTabFields();
    final boolean editable = layer.isEditable();
    setEditable(editable);
    getFieldsTableModel().setEditable(isEditable());
    if (recordDefinition.getGeometryFieldName() != null) {
      addTabGeometry();
    }
    Property.addListener(layer, this);
    this.undoManager.setLimit(100);
    this.undoManager.addKeyMap(this);
  }

  public RecordLayerForm(final AbstractRecordLayer layer, final LayerRecord record) {
    this(layer);
    setRecord(record);
  }

  protected void actionAddCancel() {
    final AbstractRecordLayer layer = getLayer();
    final LayerRecord record = getRecord();
    setRecord(null);
    layer.deleteRecords(record);
    layer.saveChanges(record);
    this.cancelled = true;
    closeWindow();
  }

  protected void actionAddOk() {
    final AbstractRecordLayer layer = getLayer();
    final LayerRecord record = getRecord();
    layer.saveChanges(record);
    layer.setSelectedRecords(record);
    layer.showRecordsTable(RecordLayerTableModel.MODE_SELECTED_RECORDS);
    closeWindow();
  }

  protected ObjectLabelField addCodeTableLabelField(final String fieldName) {
    final RecordStore recordStore = getRecordStore();
    final CodeTable codeTable = recordStore.getCodeTableByFieldName(fieldName);
    final ObjectLabelField field = new ObjectLabelField(fieldName, codeTable);
    field.setFont(SwingUtil.FONT);
    addField(fieldName, field);
    return field;
  }

  protected void addDoubleField(final String fieldName, final int length, final int scale,
    final Double minimumValie, final Double maximumValue) {
    final DataType dataType = DataTypes.DOUBLE;
    final NumberTextField field = new NumberTextField(fieldName, dataType, length, scale,
      minimumValie, maximumValue);
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
      comboBox.getEditor().getEditorComponent().addFocusListener(new WeakFocusListener(this));
    } else {
      ((JComponent)field).addFocusListener(new WeakFocusListener(this));
    }
    this.fields.put(fieldName, field);
    this.fieldToNameMap.put(field, fieldName);
    return field;
  }

  protected boolean addFieldError(final String fieldName, final String message) {
    Maps.addToList(this.fieldErrors, fieldName, message);
    return false;
  }

  public void addFields(final Collection<? extends Field> fields) {
    for (final Field field : fields) {
      addField(field);
    }
  }

  protected boolean addFieldWarning(final String fieldName, final String message) {
    Maps.addToList(this.fieldWarnings, fieldName, message);
    return false;
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
  public <T> T addLabelledField(final Container container, final String fieldName) {
    final Field field = getField(fieldName);
    if (field == null) {
      LoggerFactory.getLogger(getClass())
        .error("Cannot find field " + this.recordDefinition.getPath() + " " + fieldName);
    } else {
      addLabelledField(container, field);
    }
    return (T)field;
  }

  protected void addNumberField(final String fieldName, final DataType dataType, final int length,
    final Number minimumValue, final Number maximumValue) {
    final NumberTextField field = new NumberTextField(fieldName, dataType, length, 0, minimumValue,
      maximumValue);
    addField(fieldName, field);
  }

  protected void addPanel(final JPanel container, final String title,
    final List<String> fieldNames) {
    final JPanel panel = newPanel(container, title);

    for (final String fieldName : fieldNames) {
      addLabelledField(panel, fieldName);
    }

    GroupLayouts.makeColumns(panel, 2, true);
  }

  public void addReadOnlyFieldNames(final Collection<String> readOnlyFieldNames) {
    this.readOnlyFieldNames.addAll(readOnlyFieldNames);
    for (final Entry<String, Field> entry : this.fields.entrySet()) {
      final String name = entry.getKey();
      final Field field = entry.getValue();
      if (this.readOnlyFieldNames.contains(name)) {
        field.setEditable(false);
      } else {
        field.setEditable(true);
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

  protected JPanel addScrollPaneTab(final int index, final String title) {
    final JPanel panel = new JPanel(new VerticalLayout());
    panel.setOpaque(false);
    addScrollPaneTab(index, title, panel);
    return panel;
  }

  public JScrollPane addScrollPaneTab(final int index, final String name,
    final Component component) {
    boolean init = false;
    final Container parent = this.tabs.getParent();
    if (parent != this) {
      add(this.tabs, BorderLayout.CENTER);
      init = true;
    }
    final JScrollPane scrollPane = new JScrollPane(component);
    scrollPane.getViewport().setOpaque(false);
    scrollPane.setOpaque(false);
    scrollPane.setBorder(null);
    this.tabs.insertTab(name, null, scrollPane, null, index);
    if (init) {
      this.tabs.setSelectedIndex(0);
    }
    final JLabel label = new JLabel(name);
    this.tabs.setTabComponentAt(index, label);
    return scrollPane;
  }

  protected JPanel addScrollPaneTab(final String title) {
    final JPanel panel = new JPanel(new VerticalLayout());
    panel.setOpaque(false);
    addScrollPaneTab(title, panel);
    return panel;
  }

  public JScrollPane addScrollPaneTab(final String name, final Component component) {
    return addScrollPaneTab(this.tabs.getTabCount(), name, component);
  }

  public void addTab(final int index, final String name, final Component component) {
    boolean init = false;
    final Container parent = this.tabs.getParent();
    if (parent != this) {
      add(this.tabs, BorderLayout.CENTER);
      init = true;
    }
    this.tabs.insertTab(name, null, component, null, index);
    if (init) {
      this.tabs.setSelectedIndex(0);
    }
    final JLabel label = new JLabel(name);
    this.tabs.setTabComponentAt(index, label);
  }

  public void addTab(final String name, final Component component) {
    addTab(this.tabs.getTabCount(), name, component);
  }

  protected void addTabFields() {
    final TablePanel tablePanel = LayerRecordTableModel.newTablePanel(this);
    this.fieldsTableModel = tablePanel.getTableModel();
    addTab("Fields", tablePanel);
  }

  protected void addTabGeometry() {
    final String geometryFieldName = this.recordDefinition.getGeometryFieldName();
    if (this.geometryCoordinatesPanel == null && geometryFieldName != null) {
      this.geometryCoordinatesPanel = new GeometryCoordinatesPanel(this, geometryFieldName);
      addField(geometryFieldName, this.geometryCoordinatesPanel);
      final JPanel panel = Panels.titledTransparent(new GridLayout(1, 1), "Vertices");
      panel.add(this.geometryCoordinatesPanel);

      addScrollPaneTab("Geometry", panel);
    }
  }

  public ToolBar addToolBar(final AbstractRecordLayer layer) {
    this.toolBar = new ToolBar();
    add(this.toolBar, BorderLayout.NORTH);
    final RecordDefinition recordDefinition = getRecordDefinition();
    final FieldDefinition geometryField = recordDefinition.getGeometryField();
    final boolean hasGeometry = geometryField != null;
    final EnableCheck editable = new ObjectPropertyEnableCheck(this, "editable");

    if (layer != null) {
      final MenuFactory menuFactory = MenuFactory.findMenu(layer);
      if (menuFactory != null) {
        this.toolBar.addButtonTitleIcon("menu", "Layer Menu", "menu",
          () -> menuFactory.show(layer, this, 10, 10));
      }
    }
    final EnableCheck deletableEnableCheck = new ObjectPropertyEnableCheck(this, "deletable");
    this.toolBar.addButton("record", "Delete Record", "table_row_delete", deletableEnableCheck,
      this::deleteRecord);

    // Cut, Copy Paste

    this.toolBar.addButton("dnd", "Copy Record", "page_copy", (EnableCheck)null,
      this::dataTransferCopy);

    if (hasGeometry) {
      this.toolBar.addButton("dnd", "Copy Geometry", "geometry_copy", (EnableCheck)null,
        this::copyGeometry);
    }

    this.toolBar.addButton("dnd", "Paste Record", "paste_plain", editable, this::dataTransferPaste);

    if (hasGeometry) {
      this.toolBar.addButton("dnd", "Paste Geometry", "geometry_paste", editable,
        this::pasteGeometry);
    }

    final EnableCheck canUndo = new ObjectPropertyEnableCheck(this.undoManager, "canUndo");
    final EnableCheck canRedo = new ObjectPropertyEnableCheck(this.undoManager, "canRedo");

    final EnableCheck modifiedOrDeleted = new ObjectPropertyEnableCheck(this, "modifiedOrDeleted");

    this.toolBar.addButton("changes", "Revert Record", "arrow_revert", modifiedOrDeleted,
      this::revertChanges);

    this.toolBar.addButton("changes", "Revert Empty Fields", "field_empty_revert",
      modifiedOrDeleted, this::revertEmptyFields);

    this.toolBar.addButton("changes", "Undo", "arrow_undo", canUndo, this.undoManager::undo);
    this.toolBar.addButton("changes", "Redo", "arrow_redo", canRedo, this.undoManager::redo);

    // Zoom

    if (hasGeometry) {
      this.toolBar.addButtonTitleIcon("zoom", "Zoom to Record", "magnifier", () -> {
        final LayerRecord record = getRecord();
        layer.zoomToRecord(record);
      });
    }

    // Geometry manipulation
    if (hasGeometry) {
      final DataType geometryDataType = geometryField.getType();
      if (geometryDataType == DataTypes.LINE_STRING
        || geometryDataType == DataTypes.MULTI_LINE_STRING) {
        if (DirectionalFields.getProperty(recordDefinition).hasDirectionalFields()) {
          this.toolBar.addButton("geometry", FLIP_RECORD_NAME, FLIP_RECORD_ICON, editable,
            this::flipRecordOrientation);
          this.toolBar.addButton("geometry", FLIP_LINE_ORIENTATION_NAME, FLIP_LINE_ORIENTATION_ICON,
            editable, this::flipLineOrientation);
          this.toolBar.addButton("geometry", FLIP_FIELDS_NAME, FLIP_FIELDS_ICON, editable,
            this::flipFields);
        } else {
          this.toolBar.addButton("geometry", "Flip Line Orientation", "flip_line", editable,
            this::flipLineOrientation);
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
    setTabColor(index, null);
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

  public void dataTransferCopy() {
    invokeAction("copy");
  }

  public void dataTransferPaste() {
    invokeAction("paste");
  }

  public void deleteRecord() {
    final LayerRecord record = getRecord();
    if (record != null) {
      getLayer().deleteRecords(record);
    }
  }

  public void destroy() {
    this.addOkButton = null;
    this.fieldsTableModel = null;
    this.recordStore = null;
    this.fieldInValidMessage.clear();
    for (final Field field : this.fields.values()) {
      Property.removeAllListeners(field);
    }
    this.fields.clear();
    this.fieldTabIndex.clear();
    this.fieldToNameMap.clear();
    this.invalidFieldNames.clear();
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
      if (this.fieldsTableModel != null) {
        Property.removeListener(layer, this.fieldsTableModel);
        this.fieldsTableModel = null;
      }
      Property.removeListener(layer, this);
    }
    final Window window = SwingUtil.getWindowAncestor(this);
    if (window != null) {
      window.removeWindowListener(this);
    }
    removeAll();
  }

  protected void doSetFieldInvalid(final String fieldName, String message) {
    final String oldValue = this.fieldInValidMessage.get(fieldName);
    if (message == null) {
      message = "Invalid value";
    }
    if (!Equals.equal(message, oldValue)) {
      this.fieldInValidMessage.put(fieldName, message);
      final Field field = getField(fieldName);
      field.setFieldInvalid(message, WebColors.Red, WebColors.Pink);

      this.invalidFieldNames.add(fieldName);
      final int tabIndex = getTabIndex(fieldName);
      Maps.addToSet(this.tabInvalidFieldMap, tabIndex, fieldName);
      updateTabValid(tabIndex);
      updateInvalidFields(true);
    }
  }

  protected void doSetFieldValid(final String fieldName) {
    this.fieldErrors.remove(fieldName);
    this.fieldWarnings.remove(fieldName);
    final boolean valid = isFieldValid(fieldName);
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
    if (!valid) {
      this.invalidFieldNames.remove(fieldName);
      this.fieldInValidMessage.remove(fieldName);
      final int tabIndex = getTabIndex(fieldName);
      Maps.removeFromSet(this.tabInvalidFieldMap, tabIndex, fieldName);
      updateTabValid(tabIndex);
      updateInvalidFields(true);
    }
  }

  protected void doUpdateErrors() {
    for (final Field field : getFields()) {
      final String fieldName = field.getFieldName();
      final List<String> errors = this.fieldErrors.get(fieldName);
      final List<String> warnings = this.fieldWarnings.get(fieldName);
      if (Property.hasValue(errors)) {
        String message = Strings.toString("<br />", errors);
        if (Property.hasValue(warnings)) {
          message += "<br />" + warnings;
        }
        field.setFieldInvalid("<html>" + message + "</html>", WebColors.Red, WebColors.Pink);
      } else {
        field.setFieldValid();
        if (Property.hasValue(warnings)) {
          field.setFieldToolTip("<html>" + Strings.toString("<br />", warnings) + "</html>");
          field.setFieldBackgroundColor(WebColors.Yellow);
        }
      }
    }
  }

  protected boolean doValidateFieldInternal(final String fieldName) {
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
        if (this.record.getState() == RecordState.NEW) {
          final String idFieldName = getRecordDefinition().getIdFieldName();
          if (fieldName.equals(idFieldName)) {
            run = false;
          }
        }
        if (run) {
          final Object value = getFieldValue(fieldName);
          if (!Property.hasValue(value)) {
            valid = addFieldError(fieldName, "Required");
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

  protected boolean doValidateFields(final Collection<String> fieldNames) {
    boolean valid = true;
    for (final String fieldName : fieldNames) {
      setFieldValid(fieldName);
      valid &= doValidateFieldInternal(fieldName);
    }
    return valid;
  }

  @Override
  public void editingCanceled(final ChangeEvent e) {
  }

  @Override
  public void editingStopped(final ChangeEvent e) {
    final RecordTableCellEditor editor = (RecordTableCellEditor)e.getSource();
    final String name = editor.getFieldName();
    final Object value = editor.getCellEditorValue();
    setFieldValue(name, value, true);
  }

  public void flipFields() {
    addUndo(new ReverseRecordFieldsUndo(this.record));
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

  public LayerRecord getAddRecord() {
    return this.addRecord;
  }

  public String getCodeValue(final String fieldName, final Object value) {
    final CodeTable codeTable = this.recordDefinition.getCodeTableByFieldName(fieldName);
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
        string = Strings.toString(values);
      }
    }
    if (!Property.hasValue(string)) {
      string = "-";
    }
    return string;
  }

  public Color getErrorForegroundColor() {
    return WebColors.Red;
  }

  @SuppressWarnings("unchecked")
  protected <T extends Field> T getField(final String fieldName) {
    synchronized (this.fields) {
      Field field = this.fields.get(fieldName);
      if (field == null) {
        final boolean editable = !this.readOnlyFieldNames.contains(fieldName);
        try {
          field = RecordLayerFields.newFormField(this.layer, fieldName, editable);
          addField(fieldName, field);
        } catch (final IllegalArgumentException e) {
        }
      }
      if (field != null && !isEditable()) {
        field.setEditable(false);
      }
      return (T)field;
    }
  }

  public Map<String, List<String>> getFieldErrors() {
    return this.fieldErrors;
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

  public LayerRecordTableModel getFieldsTableModel() {
    return this.fieldsTableModel;
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
    final CodeTable codeTable = this.recordDefinition.getCodeTableByFieldName(name);
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
      final Identifier id = codeTable.getIdentifier(value);
      return (T)id;
    }
  }

  public Map<String, List<String>> getFieldWarnings() {
    return this.fieldWarnings;
  }

  public GeometryCoordinatesPanel getGeometryCoordinatesPanel() {
    return this.geometryCoordinatesPanel;
  }

  public String getGeometryFieldName() {
    return getRecordDefinition().getGeometryFieldName();
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

  public <T> T getOriginalValue(final String fieldName) {
    final LayerRecord record = getRecord();
    return record.getOriginalValue(fieldName);
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

  public RecordDefinition getRecordDefinition() {
    return this.recordDefinition;
  }

  public RecordStore getRecordStore() {
    if (this.recordStore == null) {
      if (this.recordDefinition == null) {
        return null;
      } else {
        return this.recordDefinition.getRecordStore();
      }
    } else {
      return this.recordStore;
    }
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
    final Map<String, Object> values = new LinkedHashMap<>();
    if (this.record != null) {
      values.putAll(this.record);
    }
    return values;
  }

  public boolean hasErrors() {
    return !this.fieldErrors.isEmpty();
  }

  public boolean hasFieldValue(final String fieldName) {
    final Field field = getField(fieldName);
    if (field == null) {
      return false;
    } else {
      final Object value = field.getFieldValue();
      return Property.hasValue(value);
    }
  }

  public boolean hasOriginalValue(final String name) {
    return getRecordDefinition().hasField(name);
  }

  public boolean hasWarnings() {
    return !this.fieldWarnings.isEmpty();
  }

  protected void invokeAction(final String actionName) {
    final Action action = getActionMap().get(actionName);
    if (action != null) {
      final ActionEvent event = new ActionEvent(this, ActionEvent.ACTION_PERFORMED, null);
      action.actionPerformed(event);
    }
  }

  public boolean isAllowAddWithErrors() {
    return this.allowAddWithErrors;
  }

  public boolean isDeletable() {
    final LayerRecord record = getRecord();
    if (record == null) {
      return false;
    } else {
      return record.isDeletable();
    }
  }

  public boolean isEditable() {
    return this.editable;
  }

  public boolean isEditable(final String fieldName) {
    if (isEditable()) {
      if (!this.readOnlyFieldNames.contains(fieldName)) {
        return true;
      }
    }
    return false;
  }

  public boolean isFieldsValid() {
    return this.invalidFieldNames.isEmpty();
  }

  public boolean isFieldValid(final String fieldName) {
    return !this.invalidFieldNames.contains(fieldName);
  }

  protected boolean isFieldValidationEnabled() {
    final boolean enabled = this.fieldValidationDisabled.get() != Boolean.FALSE;
    return enabled;
  }

  public boolean isModifiedOrDeleted() {
    final LayerRecord record = getRecord();
    if (record == null) {
      return false;
    } else {
      return record.isDeleted() || record.isModified();
    }
  }

  public boolean isNewRecord(final LayerRecord record) {
    return record.getState() == RecordState.NEW;
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

  protected JPanel newPanel(final JPanel container, final String title) {
    final JPanel panel = Panels.titledTransparentBorderLayout(title);
    container.add(panel);
    return panel;
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
      final Map<String, Object> newValues = new LinkedHashMap<String, Object>(map);
      final Collection<String> ignorePasteFields = layer.getProperty("ignorePasteFields");
      final Set<String> keySet = newValues.keySet();
      if (ignorePasteFields != null) {
        keySet.removeAll(ignorePasteFields);
      }
      keySet.removeAll(getReadOnlyFieldNames());

      final Map<String, Object> values = getValues();
      values.putAll(newValues);
      setValues(values);
    }
  }

  protected void postValidate() {

  }

  @Override
  public void propertyChange(final PropertyChangeEvent event) {
    final String propertyName = event.getPropertyName();
    final AbstractRecordLayer layer = getLayer();
    if (layer != null) {
      final LayerRecord record = getRecord();
      if (record != null) {
        final RecordState state = record.getState();
        if (!state.equals(RecordState.DELETED)) {
          final Object source = event.getSource();

          if (this.geometryCoordinatesPanel != null
            && source == this.geometryCoordinatesPanel.getTable().getModel()) {
            if (propertyName.equals("geometry")) {
              record.setGeometryValue((Geometry)event.getNewValue());
            }
          } else if (source == layer) {
            if ("recordDeleted".equals(propertyName)) {
              if (record.isDeleted() || isSame(event.getNewValue())) {
                final Window window = SwingUtilities.getWindowAncestor(this);
                SwingUtil.setVisible(window, false);
              }
            } else if ("recordsChanged".equals(propertyName)) {
              if (record.isDeleted()) {
                final Window window = SwingUtilities.getWindowAncestor(this);
                SwingUtil.setVisible(window, false);
              }
              setRecord(record);
            }
          } else if (source instanceof Field) {
            final Field field = (Field)source;
            final String fieldName = field.getFieldName();
            final Object fieldValue = field.getFieldValue();
            final Object recordValue = this.record.getValue(fieldName);
            if (!Equals.equal(recordValue, fieldValue)) {
              boolean equal = false;
              if (fieldValue instanceof String) {
                final String string = (String)fieldValue;
                if (!Property.hasValue(string) && recordValue == null) {
                  equal = true;
                }
              }
              if (!equal && layer.isEditable()
                && (state == RecordState.NEW && layer.isCanAddRecords()
                  || layer.isCanEditRecords())) {
                record.setValueByPath(fieldName, fieldValue);
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
              if ("qaMessagesUpdated".equals(propertyName)) {
                updateErrors();
              } else if (recordDefinition.hasField(propertyName)) {
                setFieldValue(propertyName, value, isFieldValidationEnabled());
              }
              final boolean modifiedOrDeleted = isModifiedOrDeleted();
              if (this.propertyChangeSupport != null) {
                this.propertyChangeSupport.firePropertyChange("modifiedOrDeleted",
                  !modifiedOrDeleted, modifiedOrDeleted);
                final boolean deletable = isDeletable();
                this.propertyChangeSupport.firePropertyChange("deletable", !deletable, deletable);
              }
              repaint();
            }
          }
        }
      }
    }
  }

  public void revertChanges() {
    final LayerRecord record = getRecord();
    if (record != null) {
      record.revertChanges();
      setValues(record);
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

  public void setAddRecord(final LayerRecord addRecord) {
    this.addRecord = addRecord;
  }

  public void setAllowAddWithErrors(final boolean allowAddWithErrors) {
    this.allowAddWithErrors = allowAddWithErrors;
  }

  public void setEditable(final boolean editable) {
    this.editable = editable;
    for (final String fieldName : getFieldNames()) {
      if (!getReadOnlyFieldNames().contains(fieldName)) {
        final Field field = getField(fieldName);
        field.setEditable(editable);
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

  public final void setFieldInvalid(final String fieldName, final String message) {
    Invoke.later(() -> doSetFieldInvalid(fieldName, message));
  }

  public void setFieldInvalidToolTip(final String fieldName, final JComponent field) {
    final String message = this.fieldInValidMessage.get(fieldName);
    if (Property.hasValue(message)) {
      field.setToolTipText(message);
    }
  }

  public final void setFieldValid(final String fieldName) {
    Invoke.later(() -> doSetFieldValid(fieldName));
  }

  protected boolean setFieldValidationEnabled(final boolean fieldValidationEnabled) {
    final boolean oldValue = isFieldValidationEnabled();
    if (fieldValidationEnabled) {
      this.fieldsToValidate.remove();
    } else if (oldValue) {
      this.fieldsToValidate.set(new TreeSet<String>());
    }
    this.fieldValidationDisabled.set(fieldValidationEnabled);
    return oldValue;
  }

  public void setFieldValue(final String fieldName, Object value, final boolean validate) {
    final Object oldValue = getFieldValue(fieldName);
    final RecordDefinition recordDefinition = getRecordDefinition();
    if (recordDefinition != null) {
      try {
        final Class<?> fieldClass = recordDefinition.getFieldClass(fieldName);
        value = StringConverterRegistry.toObject(fieldClass, value);
      } catch (final Throwable e) {
      }
    }
    this.fieldValues.put(fieldName, value);
    final JComponent field = (JComponent)getField(fieldName);

    boolean changed = Property.isChanged(oldValue, value);
    if (!changed) {
      final Object recordValue = this.record.getValue(fieldName);

      if (Property.isChanged(oldValue, recordValue)) {
        this.record.setValueByPath(fieldName, value);
        changed = true;
      }
    }
    SwingUtil.setFieldValue(field, value);
    if (changed) {
      if (validate) {
        validateFields(fieldName);
      } else {
        final Set<String> fieldsToValidate = this.fieldsToValidate.get();
        if (fieldsToValidate != null) {
          fieldsToValidate.add(fieldName);
        }
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

  public void setRecord(final LayerRecord record) {
    final boolean undo = this.undoManager.setEventsEnabled(false);
    final boolean validate = setFieldValidationEnabled(false);
    try {
      final boolean same = record != null && record.isSame(getRecord());
      this.record = record;
      this.fieldsTableModel.setRecord(record);
      if (!same) {
        setValues(record);
        this.undoManager.discardAllEdits();
      }
    } finally {
      setFieldValidationEnabled(validate);
      this.undoManager.setEventsEnabled(undo);
    }
  }

  public void setRecordDefinition(final RecordDefinition recordDefinition) {
    this.recordDefinition = recordDefinition;
    setRecordStore(recordDefinition.getRecordStore());
    final String idFieldName = recordDefinition.getIdFieldName();
    if (Property.hasValue(idFieldName)) {
      this.readOnlyFieldNames.add(idFieldName);
    }
    for (final FieldDefinition field : recordDefinition.getFields()) {
      if (field.isRequired()) {
        final String name = field.getName();
        addRequiredFieldNames(name);
      }

    }
  }

  protected void setRecordStore(final RecordStore recordStore) {
    this.recordStore = recordStore;
  }

  public void setRequiredFieldNames(final Collection<String> requiredFieldNames) {
    this.requiredFieldNames = new HashSet<String>(requiredFieldNames);
  }

  public final void setTabColor(final int index, final Color foregroundColor) {
    if (index > -1) {
      Invoke.later(() -> {
        if (foregroundColor == null) {
          this.tabs.setTabComponentAt(index, null);
        } else {
          if (this.tabs != null) {
            final JLabel label = new JLabel(this.tabs.getTitleAt(index));
            label.setOpaque(false);
            label.setForeground(foregroundColor);
            this.tabs.setTabComponentAt(index, label);
          }
        }
      });
    }
  }

  public final void setValues(final Map<String, Object> values) {
    if (values != null) {
      Invoke.later(() -> {
        final Set<String> fieldNames = values.keySet();
        final boolean validationEnabled = setFieldValidationEnabled(false);
        try {
          this.fieldValues.putAll(values);
          for (final String fieldName : fieldNames) {
            final Object value = values.get(fieldName);
            final JComponent field = (JComponent)getField(fieldName);
            if (field != null) {
              SwingUtil.setFieldValue(field, value);
            }
          }
        } finally {
          setFieldValidationEnabled(validationEnabled);
        }
        validateFields(fieldNames);
      });
    }

  }

  public boolean showAddDialog() {
    final String title = "Add NEW " + getName();
    final Window window = SwingUtil.getActiveWindow();
    final JDialog dialog = new JDialog(window, title, ModalityType.APPLICATION_MODAL);
    dialog.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
    dialog.setLayout(new BorderLayout());

    dialog.add(this, BorderLayout.CENTER);

    final JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    dialog.add(buttons, BorderLayout.SOUTH);
    final JButton addCancelButton = RunnableAction.newButton("Cancel", () -> actionAddCancel());
    buttons.add(addCancelButton);
    buttons.add(this.addOkButton);

    dialog.pack();
    dialog.setLocation(50, 50);
    dialog.addWindowListener(this);
    dialog.setVisible(true);
    dialog.dispose();
    return !this.cancelled;
  }

  protected final void updateErrors() {
    Invoke.later(() -> doUpdateErrors());
  }

  public void updateFocussedField() {
    final Field field = this.fields.get(this.focussedFieldName);
    if (field != null) {
      field.updateFieldValue();
    }
  }

  protected void updateInvalidFields(final boolean fieldsValid) {
    if (isAllowAddWithErrors()) {
      setAddOkButtonEnabled(true);
    } else {
      final boolean enabled = fieldsValid && isFieldsValid();
      setAddOkButtonEnabled(enabled);
    }
  }

  protected void updateReadOnlyFields() {
    for (final Entry<String, Field> entry : this.fields.entrySet()) {
      final String name = entry.getKey();
      final Field field = entry.getValue();
      if (this.readOnlyFieldNames.contains(name)) {
        field.setEditable(false);
      } else {
        field.setEditable(true);
      }
    }
    if (this.fieldsTableModel != null) {
      this.fieldsTableModel.setReadOnlyFieldNames(this.readOnlyFieldNames);
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

  public final void validateFields() {
    final Set<String> fieldNames = getFieldNames();
    validateFields(fieldNames);
  }

  protected final void validateFields(final Collection<String> fieldNames) {
    if (isFieldValidationEnabled()) {
      final boolean valid = doValidateFields(fieldNames);
      postValidate();
      updateInvalidFields(valid);
    }
  }

  public void validateFields(final String... fieldNames) {
    validateFields(Arrays.asList(fieldNames));
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
