package com.revolsys.swing.map.layer.dataobject;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Window;
import java.awt.datatransfer.DataFlavor;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.undo.UndoableEdit;

import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.util.StringUtils;

import bibliothek.gui.dock.common.DefaultSingleCDockable;
import bibliothek.gui.dock.common.SingleCDockable;
import bibliothek.gui.dock.common.event.CDockableStateListener;
import bibliothek.gui.dock.common.intern.CDockable;
import bibliothek.gui.dock.common.mode.ExtendedMode;

import com.revolsys.beans.InvokeMethodCallable;
import com.revolsys.converter.string.StringConverterRegistry;
import com.revolsys.filter.Filter;
import com.revolsys.gis.algorithm.index.DataObjectQuadTree;
import com.revolsys.gis.cs.BoundingBox;
import com.revolsys.gis.cs.CoordinateSystem;
import com.revolsys.gis.cs.GeometryFactory;
import com.revolsys.gis.data.io.AbstractDataObjectReaderFactory;
import com.revolsys.gis.data.io.DataObjectReader;
import com.revolsys.gis.data.io.DataObjectStore;
import com.revolsys.gis.data.io.ListDataObjectReader;
import com.revolsys.gis.data.model.Attribute;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectFactory;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.gis.data.model.DataObjectState;
import com.revolsys.gis.data.model.filter.DataObjectGeometryDistanceFilter;
import com.revolsys.gis.data.model.filter.DataObjectGeometryIntersectsFilter;
import com.revolsys.gis.data.model.types.DataType;
import com.revolsys.gis.data.model.types.DataTypes;
import com.revolsys.gis.data.query.Query;
import com.revolsys.gis.model.data.equals.EqualsRegistry;
import com.revolsys.io.map.MapSerializerUtil;
import com.revolsys.spring.ByteArrayResource;
import com.revolsys.swing.DockingFramesUtil;
import com.revolsys.swing.SwingUtil;
import com.revolsys.swing.action.enablecheck.AndEnableCheck;
import com.revolsys.swing.action.enablecheck.EnableCheck;
import com.revolsys.swing.component.BaseDialog;
import com.revolsys.swing.component.TabbedValuePanel;
import com.revolsys.swing.component.ValueField;
import com.revolsys.swing.dnd.ClipboardUtil;
import com.revolsys.swing.dnd.transferable.DataObjectReaderTransferable;
import com.revolsys.swing.map.MapPanel;
import com.revolsys.swing.map.form.DataObjectLayerForm;
import com.revolsys.swing.map.layer.AbstractLayer;
import com.revolsys.swing.map.layer.Layer;
import com.revolsys.swing.map.layer.LayerGroup;
import com.revolsys.swing.map.layer.LayerRenderer;
import com.revolsys.swing.map.layer.Project;
import com.revolsys.swing.map.layer.dataobject.component.MergeObjectsDialog;
import com.revolsys.swing.map.layer.dataobject.renderer.AbstractDataObjectLayerRenderer;
import com.revolsys.swing.map.layer.dataobject.renderer.GeometryStyleRenderer;
import com.revolsys.swing.map.overlay.AbstractOverlay;
import com.revolsys.swing.map.overlay.AddGeometryCompleteAction;
import com.revolsys.swing.map.overlay.EditGeometryOverlay;
import com.revolsys.swing.map.table.DataObjectLayerTableModel;
import com.revolsys.swing.map.table.DataObjectLayerTablePanel;
import com.revolsys.swing.map.table.DataObjectMetaDataTableModel;
import com.revolsys.swing.menu.MenuFactory;
import com.revolsys.swing.parallel.Invoke;
import com.revolsys.swing.table.BaseJxTable;
import com.revolsys.swing.tree.TreeItemPropertyEnableCheck;
import com.revolsys.swing.tree.TreeItemRunnable;
import com.revolsys.swing.tree.model.ObjectTreeModel;
import com.revolsys.swing.undo.SetObjectProperty;
import com.vividsolutions.jts.geom.Geometry;

public abstract class AbstractDataObjectLayer extends AbstractLayer implements
  DataObjectLayer, DataObjectFactory, AddGeometryCompleteAction {

  public static final String FORM_FACTORY_EXPRESSION = "formFactoryExpression";

  static {
    final MenuFactory menu = ObjectTreeModel.getMenu(AbstractDataObjectLayer.class);
    menu.addGroup(0, "table");
    menu.addGroup(2, "edit");
    menu.addGroup(3, "dnd");

    menu.addMenuItem("table", TreeItemRunnable.createAction("View Records",
      "table_go", "showRecordsTable"));

    final EnableCheck hasSelectedRecords = new TreeItemPropertyEnableCheck(
      "hasSelectedRecords");
    final EnableCheck hasGeometry = new TreeItemPropertyEnableCheck(
      "hasGeometry");
    menu.addMenuItem("zoom", TreeItemRunnable.createAction("Zoom to Selected",
      "magnifier_zoom_selected", new AndEnableCheck(hasGeometry,
        hasSelectedRecords), "zoomToSelected"));

    final EnableCheck editable = new TreeItemPropertyEnableCheck("editable");
    final EnableCheck readonly = new TreeItemPropertyEnableCheck("readOnly",
      false);
    final EnableCheck hasChanges = new TreeItemPropertyEnableCheck("hasChanges");
    final EnableCheck canAdd = new TreeItemPropertyEnableCheck("canAddRecords");
    final EnableCheck canDelete = new TreeItemPropertyEnableCheck(
      "canDeleteRecords");
    final EnableCheck canMergeRecords = new TreeItemPropertyEnableCheck(
      "canMergeRecords");
    final EnableCheck canPaste = new TreeItemPropertyEnableCheck("canPaste");

    menu.addCheckboxMenuItem("edit", TreeItemRunnable.createAction("Editable",
      "pencil", readonly, "toggleEditable"), editable);

    menu.addMenuItem("edit", TreeItemRunnable.createAction("Save Changes",
      "table_save", hasChanges, "saveChanges"));

    menu.addMenuItem("edit", TreeItemRunnable.createAction("Cancel Changes",
      "table_cancel", hasChanges, "cancelChanges"));

    menu.addMenuItem("edit", TreeItemRunnable.createAction("Add New Record",
      "table_row_insert", canAdd, "addNewRecord"));

    menu.addMenuItem("edit", TreeItemRunnable.createAction(
      "Delete Selected Records", "table_row_delete", new AndEnableCheck(
        hasSelectedRecords, canDelete), "deleteSelectedRecords"));

    menu.addMenuItem("edit", TreeItemRunnable.createAction(
      "Merged Selected Records", "shape_group", canMergeRecords,
      "mergeSelectedRecords"));

    menu.addMenuItem("dnd", TreeItemRunnable.createAction(
      "Copy Selected Records", "page_copy", hasSelectedRecords,
      "copySelectedRecords"));

    menu.addMenuItem("dnd", TreeItemRunnable.createAction("Paste New Records",
      "paste_plain", new AndEnableCheck(canAdd, canPaste), "pasteRecords"));

    menu.addMenuItem("layer", 0, TreeItemRunnable.createAction("Layer Style",
      "palette", hasGeometry, "showProperties", "Style"));
  }

  private BoundingBox boundingBox = new BoundingBox();

  private boolean canAddRecords = true;

  private boolean canDeleteRecords = true;

  private boolean canEditRecords = true;

  private final Set<LayerDataObject> deletedRecords = new LinkedHashSet<LayerDataObject>();

  private final Object editSync = new Object();

  private final Map<DataObject, Window> forms = new HashMap<DataObject, Window>();

  private DataObjectMetaData metaData;

  private final Set<LayerDataObject> modifiedRecords = new LinkedHashSet<LayerDataObject>();

  private final Set<LayerDataObject> newRecords = new LinkedHashSet<LayerDataObject>();

  protected Query query;

  private final Set<LayerDataObject> selectedRecords = new LinkedHashSet<LayerDataObject>();

  private DataObjectQuadTree selectedRecordsIndex;

  private List<String> columnNames;

  private List<String> columnNameOrder = Collections.emptyList();

  private final ThreadLocal<Boolean> eventsEnabled = new ThreadLocal<Boolean>();

  public AbstractDataObjectLayer() {
    this("");
  }

  public AbstractDataObjectLayer(final DataObjectMetaData metaData) {
    this(metaData.getTypeName());
    setMetaData(metaData);
  }

  public AbstractDataObjectLayer(final String name) {
    this(name, GeometryFactory.getFactory(4326));
    setReadOnly(false);
    setSelectSupported(true);
    setQuerySupported(true);
    setRenderer(new GeometryStyleRenderer(this));
  }

  public AbstractDataObjectLayer(final String name,
    final GeometryFactory geometryFactory) {
    super(name);
    setGeometryFactory(geometryFactory);
  }

  @Override
  public LayerDataObject addComplete(final AbstractOverlay overlay,
    final Geometry geometry) {
    final DataObjectMetaData metaData = getMetaData();
    final String geometryAttributeName = metaData.getGeometryAttributeName();
    final Map<String, Object> parameters = new HashMap<String, Object>();
    parameters.put(geometryAttributeName, geometry);
    return showAddForm(parameters);
  }

  protected void addModifiedObject(final LayerDataObject object) {
    synchronized (this.modifiedRecords) {
      this.modifiedRecords.add(object);
    }
  }

  @Override
  public void addNewRecord() {
    final DataObjectMetaData metaData = getMetaData();
    final Attribute geometryAttribute = metaData.getGeometryAttribute();
    if (geometryAttribute == null) {
      showAddForm(null);
    } else {
      final MapPanel map = MapPanel.get(this);
      if (map != null) {
        final EditGeometryOverlay addGeometryOverlay = map.getMapOverlay(EditGeometryOverlay.class);
        synchronized (addGeometryOverlay) {
          // TODO what if there is another feature being edited?
          addGeometryOverlay.addObject(this, this);
          // TODO cancel action
        }
      }
    }
  }

  protected void addSelectedRecord(final LayerDataObject object) {
    if (isLayerObject(object)) {
      clearSelectedRecordsIndex();
      this.selectedRecords.add(object);
    }
  }

  @Override
  public void addSelectedRecords(final BoundingBox boundingBox) {
    if (isSelectable()) {
      final List<LayerDataObject> objects = query(boundingBox);
      for (final Iterator<LayerDataObject> iterator = objects.iterator(); iterator.hasNext();) {
        final LayerDataObject layerDataObject = iterator.next();
        if (!isVisible(layerDataObject)
          || this.deletedRecords.contains(layerDataObject)) {
          iterator.remove();
        }
      }
      addSelectedRecords(objects);
      if (!this.selectedRecords.isEmpty()) {
        showRecordsTable(DataObjectLayerTableModel.MODE_SELECTED);
      }
    }
  }

  @Override
  public void addSelectedRecords(
    final Collection<? extends LayerDataObject> objects) {
    for (final LayerDataObject object : objects) {
      addSelectedRecord(object);
    }
    fireSelected();
  }

  @Override
  public void addSelectedRecords(final LayerDataObject... objects) {
    addSelectedRecords(Arrays.asList(objects));
  }

  public void cancelChanges() {
    synchronized (this.editSync) {
      internalCancelChanges();
      refresh();
    }
  }

  protected void clearChanges() {
    clearSelectedRecords();
    removeForm(this.newRecords);
    removeForm(this.modifiedRecords);
    removeForm(this.deletedRecords);
    this.newRecords.clear();
    this.modifiedRecords.clear();
    this.deletedRecords.clear();
    fireRecordsChanged();
  }

  @Override
  public void clearSelectedRecords() {
    this.selectedRecords.clear();
    ;
    fireSelected();
  }

  protected void clearSelectedRecordsIndex() {
    this.selectedRecordsIndex = null;
  }

  @Override
  @SuppressWarnings("unchecked")
  public <V extends LayerDataObject> V copyObject(final V object) {
    final LayerDataObject copy = createRecord(object);
    return (V)copy;
  }

  public void copySelectedRecords() {
    final List<LayerDataObject> selectedRecords = getSelectedRecords();
    if (!selectedRecords.isEmpty()) {
      final DataObjectMetaData metaData = getMetaData();
      final DataObjectReader reader = new ListDataObjectReader(metaData,
        selectedRecords);
      final DataObjectReaderTransferable transferable = new DataObjectReaderTransferable(
        reader);
      ClipboardUtil.setContents(transferable);
    }
  }

  @Override
  public LayerDataObject createDataObject(final DataObjectMetaData metaData) {
    if (metaData.equals(getMetaData())) {
      return new LayerDataObject(this);
    } else {
      throw new IllegalArgumentException("Cannot create objects for "
        + metaData);
    }
  }

  protected DataObjectLayerForm createDefaultForm(final LayerDataObject object) {
    return new DataObjectLayerForm(this, object);
  }

  public DataObjectLayerForm createForm(final LayerDataObject object) {
    final String formFactoryExpression = getProperty(FORM_FACTORY_EXPRESSION);
    if (StringUtils.hasText(formFactoryExpression)) {
      try {
        final SpelExpressionParser parser = new SpelExpressionParser();
        final Expression expression = parser.parseExpression(formFactoryExpression);
        final EvaluationContext context = new StandardEvaluationContext(this);
        context.setVariable("object", object);
        return expression.getValue(context, DataObjectLayerForm.class);
      } catch (final Throwable e) {
        LoggerFactory.getLogger(getClass()).error(
          "Unable to create form for " + this, e);
        return null;
      }
    } else {
      return createDefaultForm(object);
    }
  }

  @Override
  public TabbedValuePanel createPropertiesPanel() {
    final TabbedValuePanel propertiesPanel = super.createPropertiesPanel();

    final DataObjectMetaData metaData = getMetaData();
    final BaseJxTable fieldTable = DataObjectMetaDataTableModel.createTable(metaData);

    final JPanel fieldPanel = new JPanel(new BorderLayout());
    final JScrollPane fieldScroll = new JScrollPane(fieldTable);
    fieldPanel.add(fieldScroll, BorderLayout.CENTER);
    propertiesPanel.addTab("Fields", fieldPanel);

    final LayerRenderer<? extends Layer> renderer = getRenderer();
    if (renderer != null) {
      final ValueField stylePanel = renderer.createStylePanel();
      if (stylePanel != null) {
        propertiesPanel.addTab(stylePanel);
      }
    }
    return propertiesPanel;
  }

  @Override
  public UndoableEdit createPropertyEdit(final LayerDataObject object,
    final String propertyName, final Object oldValue, final Object newValue) {
    return new SetObjectProperty(object, propertyName, oldValue, newValue);
  }

  @Override
  public LayerDataObject createRecord() {
    if (!isReadOnly() && isEditable() && isCanAddRecords()) {
      final LayerDataObject object = createDataObject(getMetaData());
      this.newRecords.add(object);
      return object;
    } else {
      return null;
    }
  }

  public LayerDataObject createRecord(final Map<String, Object> values) {
    final LayerDataObject record = createRecord();
    if (record == null) {
      return null;
    } else {
      record.setState(DataObjectState.Initalizing);
      try {
        if (values != null && !values.isEmpty()) {
          record.setValues(values);
          record.setIdValue(null);
        }
      } finally {
        record.setState(DataObjectState.New);
      }
      return record;
    }
  }

  @Override
  public Component createTablePanel() {
    final JTable table = DataObjectLayerTableModel.createTable(this);
    if (table == null) {
      return null;
    } else {
      return new DataObjectLayerTablePanel(this, table);
    }
  }

  @Override
  public void delete() {
    clearChanges();
    super.delete();
    if (this.forms != null) {
      for (final Window window : this.forms.values()) {
        if (window != null) {
          window.dispose();
        }
      }
    }
    System.gc();
  }

  protected void deleteObject(final LayerDataObject object) {
    final boolean trackDeletions = true;
    deleteObject(object, trackDeletions);
  }

  protected void deleteObject(final LayerDataObject object,
    final boolean trackDeletions) {
    if (isLayerObject(object)) {
      clearSelectedRecordsIndex();
      if (!this.newRecords.remove(object)) {
        this.modifiedRecords.remove(object);
        if (trackDeletions) {
          this.deletedRecords.add(object);
        }
        this.selectedRecords.remove(object);
      }
      object.setState(DataObjectState.Deleted);
    }
  }

  @Override
  public void deleteRecords(final Collection<? extends LayerDataObject> objects) {
    if (isCanDeleteRecords()) {
      synchronized (this.editSync) {
        unselectRecords(objects);
        for (final LayerDataObject object : objects) {
          deleteObject(object);
        }
      }
      fireRecordsChanged();
    }
  }

  @Override
  public void deleteRecords(final LayerDataObject... objects) {
    deleteRecords(Arrays.asList(objects));
  }

  public void deleteSelectedRecords() {
    final List<LayerDataObject> selectedRecords = getSelectedRecords();
    deleteRecords(selectedRecords);
  }

  protected List<LayerDataObject> doQuery(final BoundingBox boundingBox) {
    return Collections.emptyList();
  }

  protected List<LayerDataObject> doQuery(final Geometry geometry,
    final double maxDistance) {
    return Collections.emptyList();
  }

  @Override
  protected boolean doSaveChanges() {
    return true;
  }

  protected void filter(final List<LayerDataObject> results,
    final Set<LayerDataObject> records, final Filter<DataObject> filter) {
    for (final LayerDataObject record : records) {
      if (filter.accept(record)) {
        if (!results.contains(record)) {
          results.add(record);
        }
      } else {
        results.remove(record);
      }
    }
  }

  protected void filterUpdates(final List<LayerDataObject> results,
    final Filter<DataObject> filter) {
    results.remove(this.deletedRecords);
    filter(results, this.modifiedRecords, filter);
    filter(results, this.newRecords, filter);
  }

  protected void fireRecordsChanged() {
    firePropertyChange("recordsChanged", false, true);
  }

  protected void fireSelected() {
    final int selectionCount = this.selectedRecords.size();
    final boolean selected = selectionCount > 0;
    firePropertyChange("hasSelectedRecords", !selected, selected);
    firePropertyChange("selectionCount", -1, selectionCount);
  }

  @Override
  public BoundingBox getBoundingBox() {
    return this.boundingBox;
  }

  @Override
  public int getChangeCount() {
    int changeCount = 0;
    changeCount += this.newRecords.size();
    changeCount += this.modifiedRecords.size();
    changeCount += this.deletedRecords.size();
    return changeCount;
  }

  @Override
  public List<LayerDataObject> getChanges() {
    synchronized (this.editSync) {
      final List<LayerDataObject> objects = new ArrayList<LayerDataObject>();
      objects.addAll(this.newRecords);
      objects.addAll(this.modifiedRecords);
      objects.addAll(this.deletedRecords);
      return objects;
    }
  }

  @Override
  public List<String> getColumnNames() {
    synchronized (this) {
      if (this.columnNames == null) {
        final Set<String> columnNames = new LinkedHashSet<String>(
          this.columnNameOrder);
        final DataObjectMetaData metaData = getMetaData();
        final List<String> attributeNames = metaData.getAttributeNames();
        columnNames.addAll(attributeNames);
        this.columnNames = new ArrayList<String>(columnNames);
        updateColumnNames();
      }
    }
    return this.columnNames;
  }

  public CoordinateSystem getCoordinateSystem() {
    return getGeometryFactory().getCoordinateSystem();
  }

  @Override
  public DataObjectStore getDataStore() {
    return getMetaData().getDataObjectStore();
  }

  public Set<LayerDataObject> getDeletedRecords() {
    return new LinkedHashSet<LayerDataObject>(this.deletedRecords);
  }

  public String getGeometryAttributeName() {
    if (this.metaData == null) {
      return "";
    } else {
      return getMetaData().getGeometryAttributeName();
    }
  }

  @Override
  public DataType getGeometryType() {
    final DataObjectMetaData metaData = getMetaData();
    if (metaData == null) {
      return null;
    } else {
      final Attribute geometryAttribute = metaData.getGeometryAttribute();
      if (geometryAttribute == null) {
        return null;
      } else {
        return geometryAttribute.getType();
      }
    }
  }

  @Override
  public List<LayerDataObject> getMergeableSelectedRecords() {
    final List<LayerDataObject> selectedRecords = getSelectedRecords();
    for (final Iterator<LayerDataObject> iterator = selectedRecords.iterator(); iterator.hasNext();) {
      final LayerDataObject mergedDataObject = iterator.next();
      if (mergedDataObject.isDeleted()) {
        iterator.remove();
      }
    }
    return selectedRecords;
  }

  @Override
  public DataObjectMetaData getMetaData() {
    return this.metaData;
  }

  public Set<LayerDataObject> getModifiedRecords() {
    return new LinkedHashSet<LayerDataObject>(this.modifiedRecords);
  }

  @Override
  public int getNewObjectCount() {
    if (this.newRecords == null) {
      return 0;
    } else {
      return this.newRecords.size();
    }
  }

  @Override
  public List<LayerDataObject> getNewRecords() {
    return new ArrayList<LayerDataObject>(this.newRecords);
  }

  @Override
  public Query getQuery() {
    if (this.query == null) {
      return null;
    } else {
      return this.query.clone();
    }
  }

  @Override
  public LayerDataObject getRecord(final int row) {
    throw new UnsupportedOperationException();
  }

  @Override
  public LayerDataObject getRecordById(final Object id) {
    return null;
  }

  @Override
  public List<LayerDataObject> getRecords() {
    throw new UnsupportedOperationException();
  }

  @Override
  public int getRowCount() {
    final DataObjectMetaData metaData = getMetaData();
    final Query query = new Query(metaData);
    return getRowCount(query);
  }

  @Override
  public int getRowCount(final Query query) {
    LoggerFactory.getLogger(getClass()).error("Get row count not implemented");
    return 0;
  }

  @Override
  public BoundingBox getSelectedBoundingBox() {
    BoundingBox boundingBox = super.getSelectedBoundingBox();
    for (final DataObject object : getSelectedRecords()) {
      final Geometry geometry = object.getGeometryValue();
      boundingBox = boundingBox.expandToInclude(geometry);
    }
    return boundingBox;
  }

  @Override
  public List<LayerDataObject> getSelectedRecords() {
    return new ArrayList<LayerDataObject>(this.selectedRecords);
  }

  @Override
  @SuppressWarnings({
    "rawtypes", "unchecked"
  })
  public List<LayerDataObject> getSelectedRecords(final BoundingBox boundingBox) {
    final DataObjectQuadTree index = getSelectedRecordsIndex();
    return (List)index.queryIntersects(boundingBox);
  }

  protected DataObjectQuadTree getSelectedRecordsIndex() {
    if (this.selectedRecordsIndex == null) {
      final List<LayerDataObject> selectedRecords = getSelectedRecords();
      final DataObjectQuadTree index = new DataObjectQuadTree(
        getProject().getGeometryFactory(), selectedRecords);
      this.selectedRecordsIndex = index;
    }
    return this.selectedRecordsIndex;
  }

  @Override
  public int getSelectionCount() {
    return this.selectedRecords.size();
  }

  protected boolean hasPermission(final String permission) {
    if (this.metaData == null) {
      return true;
    } else {
      final Collection<String> permissions = this.metaData.getProperty("permissions");
      if (permissions == null) {
        return true;
      } else {
        final boolean hasPermission = permissions.contains(permission);
        return hasPermission;
      }
    }
  }

  protected void internalCancelChanges() {
    clearChanges();
  }

  @Override
  public boolean isCanAddRecords() {
    return !super.isReadOnly() && isEditable() && this.canAddRecords
      && hasPermission("INSERT");
  }

  @Override
  public boolean isCanDeleteRecords() {
    return !super.isReadOnly() && isEditable() && this.canDeleteRecords
      && hasPermission("DELETE");
  }

  @Override
  public boolean isCanEditRecords() {
    return !super.isReadOnly() && isEditable() && this.canEditRecords
      && hasPermission("UPDATE");
  }

  public boolean isCanMergeRecords() {
    if (isCanAddRecords()) {
      if (isCanDeleteRecords()) {
        if (isCanDeleteRecords()) {
          final DataType geometryType = getGeometryType();
          if (DataTypes.LINE_STRING.equals(geometryType)) {
            if (getMergeableSelectedRecords().size() > 1) {
              return true;
            }
          } // TODO allow merging other type
        }
      }
    }
    return false;
  }

  public boolean isCanPaste() {
    return ClipboardUtil.isDataFlavorAvailable(DataObjectReaderTransferable.DATA_OBJECT_READER_FLAVOR)
      || ClipboardUtil.isDataFlavorAvailable(DataFlavor.stringFlavor);
  }

  @Override
  public boolean isDeleted(final LayerDataObject object) {
    return this.deletedRecords != null && this.deletedRecords.contains(object);
  }

  @Override
  public boolean isEventsEnabled() {
    return this.eventsEnabled.get() != Boolean.FALSE;
  }

  @Override
  public boolean isHasChanges() {
    if (isEditable()) {
      synchronized (this.editSync) {
        if (!this.newRecords.isEmpty()) {
          return true;
        } else if (!this.modifiedRecords.isEmpty()) {
          return true;
        } else if (!this.deletedRecords.isEmpty()) {
          return true;
        } else {
          return false;
        }
      }
    } else {
      return false;
    }
  }

  @Override
  public boolean isHasGeometry() {
    return getGeometryAttributeName() != null;
  }

  public boolean isHasSelectedRecords() {
    return getSelectionCount() > 0;
  }

  @Override
  public boolean isHidden(final LayerDataObject object) {
    if (isCanDeleteRecords() && isDeleted(object)) {
      return true;
    } else if (isSelected(object)) {
      return true;
    } else {
      return false;
    }
  }

  public boolean isLayerObject(final DataObject object) {
    if (object.getMetaData() == getMetaData()) {
      return true;
    } else {
      return false;
    }
  }

  @Override
  public boolean isModified(final LayerDataObject object) {
    return this.modifiedRecords.contains(object);
  }

  @Override
  public boolean isNew(final LayerDataObject object) {
    return this.newRecords.contains(object);
  }

  @Override
  public boolean isReadOnly() {
    if (super.isReadOnly()) {
      return true;
    } else {
      if (this.canAddRecords && hasPermission("INSERT")) {
        return false;
      } else if (this.canDeleteRecords && hasPermission("DELETE")) {
        return false;
      } else if (this.canEditRecords && hasPermission("UPDATE")) {
        return false;
      } else {
        return true;
      }
    }
  }

  @Override
  public boolean isSelected(final LayerDataObject object) {
    if (object == null) {
      return false;
    } else {
      return this.selectedRecords.contains(object);
    }
  }

  @Override
  public boolean isVisible(final LayerDataObject object) {
    if (isVisible()) {
      final AbstractDataObjectLayerRenderer renderer = getRenderer();
      if (renderer == null || renderer.isVisible(object)) {
        return true;
      }
    }
    return false;
  }

  public void mergeSelectedRecords() {
    if (isCanMergeRecords()) {
      Invoke.later(MergeObjectsDialog.class, "showDialog",
        this);
    }
  }

  public void pasteRecords() {
    DataObjectReader reader = ClipboardUtil.getContents(DataObjectReaderTransferable.DATA_OBJECT_READER_FLAVOR);
    if (reader == null) {
      final String string = ClipboardUtil.getContents(DataFlavor.stringFlavor);
      final Resource resource = new ByteArrayResource("t.csv", string);
      reader = AbstractDataObjectReaderFactory.dataObjectReader(resource);
    }
    final List<LayerDataObject> newRecords = new ArrayList<LayerDataObject>();
    final List<DataObject> regectedRecords = new ArrayList<DataObject>();
    if (reader != null) {
      final DataObjectMetaData metaData = getMetaData();
      final Attribute geometryAttribute = metaData.getGeometryAttribute();
      DataType geometryDataType = null;
      Class<?> layerGeometryClass = null;
      final GeometryFactory geometryFactory = getGeometryFactory();
      if (geometryAttribute != null) {
        geometryDataType = geometryAttribute.getType();
        layerGeometryClass = geometryDataType.getJavaClass();
      }
      final List<String> attributeNames = metaData.getAttributeNames();
      final Collection<String> ignorePasteFields = getProperty("ignorePasteFields");
      if (ignorePasteFields != null) {
        attributeNames.removeAll(ignorePasteFields);
      }
      for (final DataObject sourceRecord : reader) {
        final Map<String, Object> newValues = new LinkedHashMap<String, Object>(
          sourceRecord);
        newValues.keySet().retainAll(attributeNames);
        if (geometryDataType != null) {
          Geometry sourceGeometry = sourceRecord.getGeometryValue();
          if (sourceGeometry == null) {
            final Object value = sourceRecord.get(geometryAttribute.getName());
            sourceGeometry = StringConverterRegistry.toObject(Geometry.class,
              value);
          }
          final Geometry geometry = geometryFactory.createGeometry(
            layerGeometryClass, sourceGeometry);
          if (geometry == null) {
            newValues.clear();
          } else {
            final String geometryAttributeName = geometryAttribute.getName();
            newValues.put(geometryAttributeName, geometry);
          }
        }
        LayerDataObject newRecord = null;
        if (newValues.isEmpty()) {
          regectedRecords.add(sourceRecord);
        } else {
          newRecord = createRecord(newValues);
        }
        if (newRecord == null) {
          regectedRecords.add(sourceRecord);
        } else {
          newRecords.add(newRecord);
        }
      }
    }
  }

  @Override
  public void propertyChange(final PropertyChangeEvent event) {
    super.propertyChange(event);
    final Object source = event.getSource();
    final String propertyName = event.getPropertyName();
    if (!"errorsUpdated".equals(propertyName)) {
      if (source instanceof LayerDataObject) {
        final LayerDataObject dataObject = (LayerDataObject)source;
        if (dataObject.getLayer() == this) {
          if (EqualsRegistry.equal(propertyName, getGeometryAttributeName())) {
            final Geometry oldGeometry = (Geometry)event.getOldValue();
            updateSpatialIndex(dataObject, oldGeometry);
          }
          clearSelectedRecordsIndex();
          final DataObjectState state = dataObject.getState();
          if (state == DataObjectState.Modified) {
            addModifiedObject(dataObject);
          } else if (state == DataObjectState.Persisted) {
            removeModifiedObject(dataObject);
          }
        }
      }
    }
  }

  @Override
  public final List<LayerDataObject> query(final BoundingBox boundingBox) {
    final List<LayerDataObject> results = doQuery(boundingBox);
    final Filter<DataObject> filter = new DataObjectGeometryIntersectsFilter(
      boundingBox);
    filterUpdates(results, filter);
    return results;
  }

  @Override
  public List<LayerDataObject> query(final Geometry geometry,
    final double maxDistance) {
    final List<LayerDataObject> results = doQuery(geometry, maxDistance);
    final Filter<DataObject> filter = new DataObjectGeometryDistanceFilter(
      geometry, maxDistance);
    filterUpdates(results, filter);
    return results;
  }

  @Override
  public List<LayerDataObject> query(final Query query) {
    throw new UnsupportedOperationException("Query not currently supported");
  }

  protected void removeDeletedObject(final LayerDataObject object) {
    synchronized (this.deletedRecords) {
      this.deletedRecords.remove(object);
      unselectRecords(object);
    }
  }

  protected void removeForm(final LayerDataObject object) {
    final Window form = this.forms.remove(object);
    if (form != null) {
      form.dispose();
      form.removeNotify();

    }
  }

  public void removeForm(final Set<LayerDataObject> records) {
    for (final LayerDataObject object : records) {
      removeForm(object);
    }
  }

  protected void removeModifiedObject(final LayerDataObject object) {
    synchronized (this.modifiedRecords) {
      this.modifiedRecords.remove(object);
    }
  }

  protected void removeNewObject(final LayerDataObject object) {
    synchronized (this.newRecords) {
      this.newRecords.remove(object);
    }
  }

  protected void removeSelectedRecord(final LayerDataObject object) {
    this.selectedRecords.remove(object);
    clearSelectedRecordsIndex();
  }

  @Override
  public void removeSelectedRecords(final BoundingBox boundingBox) {
    if (isSelectable()) {
      final List<LayerDataObject> objects = query(boundingBox);
      for (final Iterator<LayerDataObject> iterator = objects.iterator(); iterator.hasNext();) {
        final LayerDataObject layerDataObject = iterator.next();
        if (!isVisible(layerDataObject)
          || this.deletedRecords.contains(layerDataObject)) {
          iterator.remove();
        }
      }
      removeSelectedRecords(objects);
      if (!this.selectedRecords.isEmpty()) {
        showRecordsTable(DataObjectLayerTableModel.MODE_SELECTED);
      }
    }
  }

  public void removeSelectedRecords(
    final Collection<? extends LayerDataObject> objects) {
    for (final LayerDataObject object : objects) {
      removeSelectedRecord(object);
    }
    fireSelected();
  }

  @Override
  public void revertChanges(final LayerDataObject object) {
    synchronized (this.modifiedRecords) {
      if (isLayerObject(object)) {
        removeModifiedObject(object);
        this.deletedRecords.remove(object);
      }
    }
  }

  @Override
  public boolean saveChanges() {
    synchronized (this.editSync) {
      final boolean saved = doSaveChanges();
      if (saved) {
        clearChanges();
      }
      refresh();
      return saved;
    }
  }

  @Override
  public boolean saveChanges(final LayerDataObject object) {
    return false;
  }

  public void setBoundingBox(final BoundingBox boundingBox) {
    this.boundingBox = boundingBox;
  }

  public void setCanAddRecords(final boolean canAddRecords) {
    this.canAddRecords = canAddRecords;
    firePropertyChange("canAddRecords", !isCanAddRecords(), isCanAddRecords());
  }

  public void setCanDeleteRecords(final boolean canDeleteRecords) {
    this.canDeleteRecords = canDeleteRecords;
    firePropertyChange("canDeleteRecords", !isCanDeleteRecords(),
      isCanDeleteRecords());
  }

  public void setCanEditRecords(final boolean canEditRecords) {
    this.canEditRecords = canEditRecords;
    firePropertyChange("canEditRecords", !isCanEditRecords(),
      isCanEditRecords());
  }

  public void setColumnNameOrder(final Collection<String> columnNameOrder) {
    this.columnNameOrder = new ArrayList<String>(columnNameOrder);
  }

  public void setColumnNames(final Collection<String> columnNames) {
    this.columnNames = new ArrayList<String>(columnNames);
    updateColumnNames();
  }

  @Override
  public void setEditable(final boolean editable) {
    if (SwingUtilities.isEventDispatchThread()) {
      Invoke.background("Set editable", this, "setEditable", editable);
    } else {
      synchronized (this.editSync) {
        if (editable == false) {
          firePropertyChange("preEditable", false, true);
          if (isHasChanges()) {
            final Integer result = InvokeMethodCallable.invokeAndWait(
              JOptionPane.class,
              "showConfirmDialog",
              JOptionPane.getRootFrame(),
              "The layer has unsaved changes. Click Yes to save changes. Click No to discard changes. Click Cancel to continue editing.",
              "Save Changes", JOptionPane.YES_NO_CANCEL_OPTION);

            if (result == JOptionPane.YES_OPTION) {
              if (!saveChanges()) {
                return;
              }
            } else if (result == JOptionPane.NO_OPTION) {
              cancelChanges();
            } else {
              // Don't allow state change if cancelled
              return;
            }

          }
        }
        super.setEditable(editable);
        setCanAddRecords(this.canAddRecords);
        setCanDeleteRecords(this.canDeleteRecords);
        setCanEditRecords(this.canEditRecords);
      }
    }
  }

  @Override
  public boolean setEventsEnabled(final boolean enabled) {
    final boolean oldValue = isEventsEnabled();
    this.eventsEnabled.set(enabled);
    return oldValue;
  }

  @Override
  protected void setGeometryFactory(final GeometryFactory geometryFactory) {
    super.setGeometryFactory(geometryFactory);
    if (geometryFactory != null && this.boundingBox.isNull()) {
      this.boundingBox = geometryFactory.getCoordinateSystem()
        .getAreaBoundingBox();
    }
  }

  protected void setMetaData(final DataObjectMetaData metaData) {
    this.metaData = metaData;
    if (metaData != null) {

      setGeometryFactory(metaData.getGeometryFactory());
      if (metaData.getGeometryAttributeIndex() == -1) {
        setSelectSupported(false);
        setRenderer(null);
      }
      updateColumnNames();
    }
  }

  @Override
  public void setProperty(final String name, final Object value) {
    if ("style".equals(name)) {
      if (value instanceof Map) {
        @SuppressWarnings("unchecked")
        final Map<String, Object> style = (Map<String, Object>)value;
        final LayerRenderer<DataObjectLayer> renderer = AbstractDataObjectLayerRenderer.getRenderer(
          this, style);
        if (renderer != null) {
          setRenderer(renderer);
        }
      }
    } else {
      super.setProperty(name, value);
    }
  }

  @Override
  public void setQuery(final Query query) {
    final Query oldValue = this.query;
    this.query = query;
    firePropertyChange("query", oldValue, query);
  }

  @Override
  public void setSelectedRecords(final BoundingBox boundingBox) {
    if (isSelectable()) {
      final List<LayerDataObject> objects = query(boundingBox);
      for (final Iterator<LayerDataObject> iterator = objects.iterator(); iterator.hasNext();) {
        final LayerDataObject layerDataObject = iterator.next();
        if (!isVisible(layerDataObject)
          || this.deletedRecords.contains(layerDataObject)) {
          iterator.remove();
        }
      }
      setSelectedRecords(objects);
      if (!this.selectedRecords.isEmpty()) {
        showRecordsTable(DataObjectLayerTableModel.MODE_SELECTED);
      }
    }
  }

  @Override
  public void setSelectedRecords(
    final Collection<LayerDataObject> selectedRecords) {
    synchronized (selectedRecords) {
      clearSelectedRecordsIndex();
      this.selectedRecords.clear();
      this.selectedRecords.addAll(selectedRecords);
    }
    fireSelected();
  }

  @Override
  public void setSelectedRecords(final LayerDataObject... selectedRecords) {
    setSelectedRecords(Arrays.asList(selectedRecords));
  }

  @Override
  public void setSelectedRecordsById(final Object id) {
    final DataObjectMetaData metaData = getMetaData();
    final String idAttributeName = metaData.getIdAttributeName();
    if (idAttributeName == null) {
      clearSelectedRecords();
    } else {
      final Query query = Query.equal(metaData, idAttributeName, id);
      final List<LayerDataObject> objects = query(query);
      setSelectedRecords(objects);
    }
  }

  @Override
  public int setSelectedWithinDistance(final boolean selected,
    final Geometry geometry, final int distance) {
    clearSelectedRecordsIndex();
    final List<LayerDataObject> objects = query(geometry, distance);
    for (final Iterator<LayerDataObject> iterator = objects.iterator(); iterator.hasNext();) {
      final LayerDataObject layerDataObject = iterator.next();
      if (!isVisible(layerDataObject)) {
        iterator.remove();
      }
    }
    if (selected) {
      this.selectedRecords.addAll(objects);
    } else {
      this.selectedRecords.removeAll(objects);
    }
    return objects.size();
  }

  @Override
  public LayerDataObject showAddForm(final Map<String, Object> parameters) {
    if (isCanAddRecords()) {
      final LayerDataObject newObject = createRecord(parameters);
      final DataObjectLayerForm form = createForm(newObject);
      if (form == null) {
        return null;
      } else {
        final LayerDataObject object = form.showAddDialog();
        return object;
      }
    } else {
      final Window window = SwingUtil.getActiveWindow();
      JOptionPane.showMessageDialog(window,
        "Adding records is not enabled for the " + getPath()
          + " layer. If possible make the layer editable", "Cannot Add Record",
        JOptionPane.ERROR_MESSAGE);
      return null;
    }

  }

  @SuppressWarnings("unchecked")
  @Override
  public <V extends JComponent> V showForm(final LayerDataObject object) {
    if (object == null) {
      return null;
    } else {
      synchronized (this.forms) {
        Window window = this.forms.get(object);
        if (window == null) {
          final Object id = object.getIdValue();
          final Component form = createForm(object);
          if (form == null) {
            return null;
          } else {
            String title;
            if (object.getState() == DataObjectState.New) {
              title = "Add NEW " + getName();
            } else if (isCanEditRecords()) {
              title = "Edit " + getName() + " #" + id;
            } else {
              title = "View " + getName() + " #" + id;
              if (form instanceof DataObjectLayerForm) {
                final DataObjectLayerForm dataObjectForm = (DataObjectLayerForm)form;
                dataObjectForm.setEditable(false);
              }
            }
            final Window parent = SwingUtil.getActiveWindow();
            window = new BaseDialog(parent, title);
            window.add(form);
            window.pack();
            window.setLocation(50, 50);
            // TODO smart location
            window.setVisible(true);
            this.forms.put(object, window);
            window.addWindowListener(new WindowAdapter() {
              @Override
              public void windowClosing(final WindowEvent e) {
                removeForm(object);
              }
            });
            window.requestFocus();
            return (V)form;
          }
        } else {
          window.requestFocus();
          final Component component = window.getComponent(0);
          if (component instanceof JScrollPane) {
            final JScrollPane scrollPane = (JScrollPane)component;
            return (V)scrollPane.getComponent(0);
          }
          return null;
        }
      }
    }

  }

  @Override
  public void showRecordsTable() {
    showRecordsTable(DataObjectLayerTableModel.MODE_ALL);
  }

  @Override
  public void showRecordsTable(String attributeFilterMode) {
    if (SwingUtilities.isEventDispatchThread()) {
      final Object tableView = getProperty("TableView");
      DefaultSingleCDockable dockable = null;
      if (tableView instanceof DefaultSingleCDockable) {
        dockable = (DefaultSingleCDockable)tableView;
      }
      final Component component;
      if (dockable == null) {
        final LayerGroup project = getProject();

        component = createTablePanel();

        if (component != null) {
          final String id = getClass().getName() + "." + getId();
          dockable = DockingFramesUtil.addDockable(project,
            MapPanel.MAP_TABLE_WORKING_AREA, id, getName(), component);

          if (dockable != null) {
            dockable.setCloseable(true);
            setProperty("TableView", dockable);
            dockable.addCDockableStateListener(new CDockableStateListener() {
              @Override
              public void extendedModeChanged(final CDockable dockable,
                final ExtendedMode mode) {
              }

              @Override
              public void visibilityChanged(final CDockable dockable) {
                final boolean visible = dockable.isVisible();
                if (!visible) {
                  dockable.getControl()
                    .getOwner()
                    .remove((SingleCDockable)dockable);
                  setProperty("TableView", null);
                }
              }
            });
            dockable.toFront();
          }
        }
      } else {
        component = dockable.getContentPane().getComponent(0);
        dockable.toFront();
      }

      if (component instanceof DataObjectLayerTablePanel) {
        final DataObjectLayerTablePanel tablePanel = (DataObjectLayerTablePanel)component;
        tablePanel.setAttributeFilterMode(attributeFilterMode);
      }
    } else {
      if (!StringUtils.hasText(attributeFilterMode)) {
        attributeFilterMode = DataObjectLayerTableModel.MODE_ALL;
      }
      Invoke.later(this, "showRecordsTable",
        attributeFilterMode);
    }
  }

  public void toggleEditable() {
    final boolean editable = isEditable();
    setEditable(!editable);
  }

  @Override
  public Map<String, Object> toMap() {
    final Map<String, Object> map = super.toMap();
    if (!super.isReadOnly()) {
      MapSerializerUtil.add(map, "canAddRecords", this.canAddRecords, true);
      MapSerializerUtil.add(map, "canDeleteRecords", this.canDeleteRecords,
        true);
      MapSerializerUtil.add(map, "canEditRecords", this.canEditRecords, true);
    }
    MapSerializerUtil.add(map, "columnNameOrder", this.columnNameOrder);
    map.remove("TableView");
    return map;
  }

  @Override
  public void unselectRecords(
    final Collection<? extends LayerDataObject> objects) {
    clearSelectedRecordsIndex();
    this.selectedRecords.removeAll(objects);
    fireSelected();
  }

  @Override
  public void unselectRecords(final LayerDataObject... objects) {
    unselectRecords(Arrays.asList(objects));
  }

  protected void updateColumnNames() {
    if (this.columnNames != null && this.metaData != null) {
      final List<String> attributeNames = this.metaData.getAttributeNames();
      this.columnNames.retainAll(attributeNames);
    }
  }

  protected void updateSpatialIndex(final LayerDataObject object,
    final Geometry oldGeometry) {
  }

  @Override
  public void zoomTo(final Geometry geometry) {
    if (geometry != null) {
      final Project project = getProject();
      final GeometryFactory geometryFactory = project.getGeometryFactory();
      final BoundingBox boundingBox = BoundingBox.getBoundingBox(
        geometryFactory, geometry)
        .expandPercent(0.1)
        .clipToCoordinateSystem();

      project.setViewBoundingBox(boundingBox);
    }
  }

  @Override
  public void zoomToObject(final DataObject object) {
    final Geometry geometry = object.getGeometryValue();

    zoomTo(geometry);
  }

  public void zoomToSelected() {
    final Project project = getProject();
    final GeometryFactory geometryFactory = project.getGeometryFactory();
    final BoundingBox boundingBox = getSelectedBoundingBox().convert(
      geometryFactory).expandPercent(0.1);
    project.setViewBoundingBox(boundingBox);
  }
}
