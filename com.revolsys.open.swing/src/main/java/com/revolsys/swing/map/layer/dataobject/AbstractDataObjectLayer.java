package com.revolsys.swing.map.layer.dataobject;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
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
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
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
import com.revolsys.gis.data.model.ArrayDataObject;
import com.revolsys.gis.data.model.Attribute;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectFactory;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.gis.data.model.DataObjectState;
import com.revolsys.gis.data.model.filter.DataObjectGeometryDistanceFilter;
import com.revolsys.gis.data.model.filter.DataObjectGeometryIntersectsFilter;
import com.revolsys.gis.data.model.property.DirectionalAttributes;
import com.revolsys.gis.data.model.types.DataType;
import com.revolsys.gis.data.model.types.DataTypes;
import com.revolsys.gis.data.query.Query;
import com.revolsys.gis.jts.LineStringUtil;
import com.revolsys.gis.model.coordinates.Coordinates;
import com.revolsys.gis.model.coordinates.CoordinatesUtil;
import com.revolsys.gis.model.data.equals.EqualsRegistry;
import com.revolsys.io.map.MapSerializerUtil;
import com.revolsys.spring.ByteArrayResource;
import com.revolsys.swing.DockingFramesUtil;
import com.revolsys.swing.SwingUtil;
import com.revolsys.swing.action.enablecheck.AndEnableCheck;
import com.revolsys.swing.action.enablecheck.EnableCheck;
import com.revolsys.swing.component.BaseDialog;
import com.revolsys.swing.component.BasePanel;
import com.revolsys.swing.component.TabbedValuePanel;
import com.revolsys.swing.dnd.ClipboardUtil;
import com.revolsys.swing.dnd.transferable.DataObjectReaderTransferable;
import com.revolsys.swing.map.MapPanel;
import com.revolsys.swing.map.form.DataObjectLayerForm;
import com.revolsys.swing.map.form.SnapLayersPanel;
import com.revolsys.swing.map.layer.AbstractLayer;
import com.revolsys.swing.map.layer.Layer;
import com.revolsys.swing.map.layer.LayerGroup;
import com.revolsys.swing.map.layer.LayerRenderer;
import com.revolsys.swing.map.layer.Project;
import com.revolsys.swing.map.layer.dataobject.component.MergeRecordsDialog;
import com.revolsys.swing.map.layer.dataobject.renderer.AbstractDataObjectLayerRenderer;
import com.revolsys.swing.map.layer.dataobject.renderer.GeometryStyleRenderer;
import com.revolsys.swing.map.layer.dataobject.style.GeometryStyle;
import com.revolsys.swing.map.layer.dataobject.style.panel.DataObjectLayerStylePanel;
import com.revolsys.swing.map.layer.dataobject.table.DataObjectLayerTable;
import com.revolsys.swing.map.layer.dataobject.table.DataObjectLayerTablePanel;
import com.revolsys.swing.map.layer.dataobject.table.model.DataObjectLayerTableModel;
import com.revolsys.swing.map.layer.dataobject.table.model.DataObjectMetaDataTableModel;
import com.revolsys.swing.map.overlay.AbstractOverlay;
import com.revolsys.swing.map.overlay.AddGeometryCompleteAction;
import com.revolsys.swing.map.overlay.CloseLocation;
import com.revolsys.swing.map.overlay.EditGeometryOverlay;
import com.revolsys.swing.menu.MenuFactory;
import com.revolsys.swing.parallel.Invoke;
import com.revolsys.swing.table.BaseJxTable;
import com.revolsys.swing.tree.TreeItemPropertyEnableCheck;
import com.revolsys.swing.tree.TreeItemRunnable;
import com.revolsys.swing.tree.model.ObjectTreeModel;
import com.revolsys.swing.undo.SetObjectProperty;
import com.revolsys.util.CompareUtil;
import com.revolsys.util.ExceptionUtil;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;

public abstract class AbstractDataObjectLayer extends AbstractLayer implements
  DataObjectFactory, AddGeometryCompleteAction {

  public static final String FORM_FACTORY_EXPRESSION = "formFactoryExpression";

  public static void addVisibleLayers(
    final List<AbstractDataObjectLayer> layers, final LayerGroup group) {
    if (group.isExists() && group.isVisible()) {
      for (final Layer layer : group) {
        if (layer instanceof LayerGroup) {
          final LayerGroup layerGroup = (LayerGroup)layer;
          addVisibleLayers(layers, layerGroup);
        } else if (layer instanceof AbstractDataObjectLayer) {
          if (layer.isExists() && layer.isVisible()) {
            final AbstractDataObjectLayer dataObjectLayer = (AbstractDataObjectLayer)layer;
            layers.add(dataObjectLayer);
          }
        }
      }
    }
  }

  public static List<AbstractDataObjectLayer> getVisibleLayers(
    final LayerGroup group) {
    final List<AbstractDataObjectLayer> layers = new ArrayList<AbstractDataObjectLayer>();
    addVisibleLayers(layers, group);
    return layers;
  }

  private DataObjectQuadTree index = new DataObjectQuadTree();

  private boolean snapToAllLayers = false;

  private Set<String> userReadOnlyFieldNames = new LinkedHashSet<String>();

  private final List<LayerDataObject> highlightedRecords = new ArrayList<LayerDataObject>();

  static {
    final MenuFactory menu = ObjectTreeModel.getMenu(AbstractDataObjectLayer.class);
    menu.addGroup(0, "table");
    menu.addGroup(2, "edit");
    menu.addGroup(3, "dnd");

    final EnableCheck exists = new TreeItemPropertyEnableCheck("exists");

    menu.addMenuItem("table", TreeItemRunnable.createAction("View Records",
      "table_go", exists, "showRecordsTable"));

    final EnableCheck hasSelectedRecords = new TreeItemPropertyEnableCheck(
      "hasSelectedRecords");
    final EnableCheck hasGeometry = new TreeItemPropertyEnableCheck(
      "hasGeometry");
    menu.addMenuItem("zoom", TreeItemRunnable.createAction("Zoom to Selected",
      "magnifier_zoom_selected", new AndEnableCheck(exists, hasGeometry,
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
      "palette", new AndEnableCheck(exists, hasGeometry), "showProperties",
      "Style"));
  }

  private BoundingBox boundingBox = new BoundingBox();

  private boolean canAddRecords = true;

  private boolean canDeleteRecords = true;

  private boolean canEditRecords = true;

  private final List<LayerDataObject> deletedRecords = new ArrayList<LayerDataObject>();

  private Object editSync;

  private final Map<DataObject, Component> forms = new HashMap<DataObject, Component>();

  private final Map<DataObject, Window> formWindows = new HashMap<DataObject, Window>();

  private DataObjectMetaData metaData;

  private final List<LayerDataObject> modifiedRecords = new ArrayList<LayerDataObject>();

  private final List<LayerDataObject> newRecords = new ArrayList<LayerDataObject>();

  private Query query;

  private final List<LayerDataObject> selectedRecords = new ArrayList<LayerDataObject>();

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

  public AbstractDataObjectLayer(final Map<String, ? extends Object> properties) {
    setReadOnly(false);
    setSelectSupported(true);
    setQuerySupported(true);
    setRenderer(new GeometryStyleRenderer(this));
    if (!properties.containsKey("style")) {
      final GeometryStyleRenderer renderer = getRenderer();
      renderer.setStyle(GeometryStyle.createStyle());
    }
    setProperties(properties);
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
  public void addComplete(final AbstractOverlay overlay, final Geometry geometry) {
    if (geometry != null) {
      final DataObjectMetaData metaData = getMetaData();
      final String geometryAttributeName = metaData.getGeometryAttributeName();
      final Map<String, Object> parameters = new HashMap<String, Object>();
      parameters.put(geometryAttributeName, geometry);
      showAddForm(parameters);
    }
  }

  protected void addHighlightedRecord(final LayerDataObject record) {
    if (isLayerRecord(record)) {
      synchronized (highlightedRecords) {
        if (!this.highlightedRecords.contains(record)) {
          this.highlightedRecords.add(record);
        }
      }
    }
  }

  public void addHighlightedRecords(
    final Collection<? extends LayerDataObject> records) {
    synchronized (this.highlightedRecords) {
      for (final LayerDataObject record : records) {
        addHighlightedRecord(record);
      }
    }
    fireHighlighted();
  }

  protected void addModifiedRecord(final LayerDataObject record) {
    synchronized (this.modifiedRecords) {
      if (!this.modifiedRecords.contains(record)) {
        this.modifiedRecords.add(record);
      }
    }
  }

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
          clearSelectedRecords();
          addGeometryOverlay.addRecord(this, this);
        }
      }
    }
  }

  protected void addSelectedRecord(final LayerDataObject record) {
    if (isLayerRecord(record)) {
      synchronized (this.selectedRecords) {
        if (!this.selectedRecords.contains(record)) {
          this.selectedRecords.add(record);
        }
      }
    }
  }

  public void addSelectedRecords(final BoundingBox boundingBox) {
    if (isSelectable()) {
      final List<LayerDataObject> records = query(boundingBox);
      for (final Iterator<LayerDataObject> iterator = records.iterator(); iterator.hasNext();) {
        final LayerDataObject layerDataObject = iterator.next();
        if (!isVisible(layerDataObject) || internalIsDeleted(layerDataObject)) {
          iterator.remove();
        }
      }
      addSelectedRecords(records);
      if (!this.selectedRecords.isEmpty()) {
        showRecordsTable(DataObjectLayerTableModel.MODE_SELECTED);
      }
    }
  }

  public void addSelectedRecords(
    final Collection<? extends LayerDataObject> records) {
    for (final LayerDataObject record : records) {
      addSelectedRecord(record);
    }
    clearSelectedRecordsIndex();
    fireSelected();
  }

  public void addSelectedRecords(final LayerDataObject... records) {
    addSelectedRecords(Arrays.asList(records));
  }

  public void cancelChanges() {
    synchronized (this.getEditSync()) {
      final boolean eventsEnabled = setEventsEnabled(false);
      boolean cancelled = true;
      try {
        cancelled &= internalCancelChanges(getNewRecords());
        cancelled &= internalCancelChanges(getDeletedRecords());
        cancelled &= internalCancelChanges(getModifiedRecords());
      } finally {
        setEventsEnabled(eventsEnabled);
        fireRecordsChanged();
      }
      if (!cancelled) {
        JOptionPane.showMessageDialog(MapPanel.get(this),
          "<html><p>There was an error cancelling changes for one or more records.</p>"
            + "<p>" + getPath() + "</p>"
            + "<p>Check the logging panel for details.</html>",
          "Error Cancelling Changes", JOptionPane.ERROR_MESSAGE);
      }

    }
  }

  public boolean canPasteRecordGeometry(final LayerDataObject record) {
    final Geometry geometry = getPasteRecordGeometry(record, false);

    return geometry != null;
  }

  public void clearSelectedRecords() {
    synchronized (this.selectedRecords) {
      selectedRecords.clear();
      clearSelectedRecordsIndex();
    }
    synchronized (this.highlightedRecords) {
      this.highlightedRecords.clear();
    }
    fireSelected();
  }

  protected void clearSelectedRecordsIndex() {
    this.selectedRecordsIndex = null;
  }

  @SuppressWarnings("unchecked")
  public <V extends LayerDataObject> V copyRecord(final V record) {
    final LayerDataObject copy = createRecord(record);
    return (V)copy;
  }

  public void copyRecordsToClipboard(final List<LayerDataObject> records) {
    if (!records.isEmpty()) {
      final DataObjectMetaData metaData = getMetaData();
      final List<DataObject> copies = new ArrayList<DataObject>();
      for (final LayerDataObject record : records) {
        copies.add(new ArrayDataObject(record));
      }
      final DataObjectReader reader = new ListDataObjectReader(metaData, copies);
      final DataObjectReaderTransferable transferable = new DataObjectReaderTransferable(
        reader);
      ClipboardUtil.setContents(transferable);
    }
  }

  public void copySelectedRecords() {
    final List<LayerDataObject> selectedRecords = getSelectedRecords();
    copyRecordsToClipboard(selectedRecords);
  }

  @Override
  public LayerDataObject createDataObject(final DataObjectMetaData metaData) {
    if (metaData.equals(getMetaData())) {
      return new LayerDataObject(this);
    } else {
      throw new IllegalArgumentException("Cannot create records for "
        + metaData);
    }
  }

  protected DataObjectLayerForm createDefaultForm(final LayerDataObject record) {
    return new DataObjectLayerForm(this, record);
  }

  public DataObjectLayerForm createForm(final LayerDataObject record) {
    final String formFactoryExpression = getProperty(FORM_FACTORY_EXPRESSION);
    if (StringUtils.hasText(formFactoryExpression)) {
      try {
        final SpelExpressionParser parser = new SpelExpressionParser();
        final Expression expression = parser.parseExpression(formFactoryExpression);
        final EvaluationContext context = new StandardEvaluationContext(this);
        context.setVariable("object", record);
        return expression.getValue(context, DataObjectLayerForm.class);
      } catch (final Throwable e) {
        LoggerFactory.getLogger(getClass()).error(
          "Unable to create form for " + this, e);
        return null;
      }
    } else {
      return createDefaultForm(record);
    }
  }

  @Override
  public TabbedValuePanel createPropertiesPanel() {
    final TabbedValuePanel propertiesPanel = super.createPropertiesPanel();
    createPropertiesPanelFields(propertiesPanel);
    createPropertiesPanelStyle(propertiesPanel);
    createPropertiesPanelSnapping(propertiesPanel);
    return propertiesPanel;
  }

  protected void createPropertiesPanelFields(
    final TabbedValuePanel propertiesPanel) {
    final DataObjectMetaData metaData = getMetaData();
    final BaseJxTable fieldTable = DataObjectMetaDataTableModel.createTable(metaData);

    final BasePanel fieldPanel = new BasePanel(new BorderLayout());
    fieldPanel.setPreferredSize(new Dimension(500, 400));
    final JScrollPane fieldScroll = new JScrollPane(fieldTable);
    fieldPanel.add(fieldScroll, BorderLayout.CENTER);
    propertiesPanel.addTab("Fields", fieldPanel);
  }

  protected void createPropertiesPanelSnapping(
    final TabbedValuePanel propertiesPanel) {
    final SnapLayersPanel panel = new SnapLayersPanel(this);
    propertiesPanel.addTab("Snapping", panel);
  }

  protected void createPropertiesPanelStyle(
    final TabbedValuePanel propertiesPanel) {
    if (getRenderer() != null) {
      final DataObjectLayerStylePanel stylePanel = new DataObjectLayerStylePanel(
        this);
      propertiesPanel.addTab("Style", stylePanel);
    }
  }

  public UndoableEdit createPropertyEdit(final LayerDataObject record,
    final String propertyName, final Object oldValue, final Object newValue) {
    return new SetObjectProperty(record, propertyName, oldValue, newValue);
  }

  public LayerDataObject createRecord() {
    if (!isReadOnly() && isEditable() && isCanAddRecords()) {
      final LayerDataObject record = createDataObject(getMetaData());
      synchronized (this.newRecords) {
        this.newRecords.add(record);
      }
      return record;
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

  public Component createTablePanel() {
    final DataObjectLayerTable table = DataObjectLayerTableModel.createTable(this);
    if (table == null) {
      return null;
    } else {
      return new DataObjectLayerTablePanel(this, table);
    }
  }

  @Override
  public void delete() {
    super.delete();
    setIndex(null);
    if (this.forms != null) {
      for (final Window window : this.formWindows.values()) {
        if (window != null) {
          window.dispose();
        }
      }
      for (final Component form : this.forms.values()) {
        if (form != null) {
          if (form instanceof DataObjectLayerForm) {
            final DataObjectLayerForm recordForm = (DataObjectLayerForm)form;
            recordForm.destroy();
          }
        }
      }
    }
    deletedRecords.clear();
    forms.clear();
    formWindows.clear();
    highlightedRecords.clear();
    modifiedRecords.clear();
    newRecords.clear();
    selectedRecords.clear();
    clearSelectedRecordsIndex();
  }

  protected void deleteRecord(final LayerDataObject record) {
    final boolean trackDeletions = true;
    deleteRecord(record, trackDeletions);
  }

  protected void deleteRecord(final LayerDataObject record,
    final boolean trackDeletions) {
    if (isLayerRecord(record)) {
      unSelectRecords(record);
      clearSelectedRecordsIndex();
      synchronized (this.newRecords) {
        if (!this.newRecords.remove(record)) {
          synchronized (this.modifiedRecords) {
            this.modifiedRecords.remove(record);
          }
          if (trackDeletions) {
            synchronized (this.deletedRecords) {
              if (!deletedRecords.contains(record)) {
                this.deletedRecords.add(record);
              }
            }
          }
          unSelectRecords(record);
        }
      }
      record.setState(DataObjectState.Deleted);
    }
  }

  public void deleteRecords(final Collection<? extends LayerDataObject> records) {
    if (isCanDeleteRecords()) {
      synchronized (this.getEditSync()) {
        unSelectRecords(records);
        for (final LayerDataObject record : records) {
          deleteRecord(record);
        }
      }
      fireRecordsChanged();
    }
  }

  public void deleteRecords(final LayerDataObject... records) {
    deleteRecords(Arrays.asList(records));
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

  protected List<LayerDataObject> doQueryBackground(
    final BoundingBox boundingBox) {
    return doQuery(boundingBox);
  }

  @Override
  protected boolean doSaveChanges() {
    if (isExists()) {
      boolean saved = true;
      try {
        saved &= doSaveChanges(getDeletedRecords());
        saved &= doSaveChanges(getModifiedRecords());
        saved &= doSaveChanges(getNewRecords());
      } finally {
        fireRecordsChanged();
      }
      return saved;
    } else {
      return false;
    }
  }

  private boolean doSaveChanges(final Collection<LayerDataObject> records) {
    boolean saved = true;
    for (final LayerDataObject record : new ArrayList<LayerDataObject>(records)) {
      if (!internalSaveChanges(record)) {
        saved = false;
      }
    }
    return saved;
  }

  protected boolean doSaveChanges(final LayerDataObject record) {
    return false;
  }

  protected void filter(final List<LayerDataObject> results,
    final Collection<LayerDataObject> records, final Filter<DataObject> filter) {
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
    synchronized (this.deletedRecords) {
      results.remove(this.deletedRecords);
    }
    synchronized (this.modifiedRecords) {
      filter(results, this.modifiedRecords, filter);
    }
    synchronized (this.newRecords) {
      filter(results, this.newRecords, filter);
    }
  }

  protected void fireHighlighted() {
    final int highlightedCount = getHighlightedCount();
    final boolean highlighted = highlightedCount > 0;
    firePropertyChange("hasHighlightedRecords", !highlighted, highlighted);
    firePropertyChange("highlightedCount", -1, highlightedCount);
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

  public int getChangeCount() {
    int changeCount = 0;
    synchronized (this.newRecords) {
      changeCount += this.newRecords.size();
    }
    synchronized (this.modifiedRecords) {
      changeCount += this.modifiedRecords.size();
    }
    synchronized (this.deletedRecords) {
      changeCount += this.deletedRecords.size();
    }
    return changeCount;
  }

  public List<LayerDataObject> getChanges() {
    synchronized (this.getEditSync()) {
      final List<LayerDataObject> records = new ArrayList<LayerDataObject>();
      synchronized (this.newRecords) {
        records.addAll(this.newRecords);
      }
      synchronized (this.modifiedRecords) {
        records.addAll(this.modifiedRecords);
      }
      synchronized (this.deletedRecords) {
        records.addAll(this.deletedRecords);
      }
      return records;
    }
  }

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

  public DataObjectStore getDataStore() {
    return getMetaData().getDataObjectStore();
  }

  public Collection<LayerDataObject> getDeletedRecords() {
    synchronized (this.deletedRecords) {
      return new ArrayList<LayerDataObject>(this.deletedRecords);
    }
  }

  public synchronized Object getEditSync() {
    if (editSync == null) {
      editSync = new Object();
    }
    return editSync;
  }

  public String getGeometryAttributeName() {
    if (this.metaData == null) {
      return "";
    } else {
      return getMetaData().getGeometryAttributeName();
    }
  }

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

  public int getHighlightedCount() {
    return this.highlightedRecords.size();
  }

  public Collection<LayerDataObject> getHighlightedRecords() {
    synchronized (this.highlightedRecords) {
      return new ArrayList<LayerDataObject>(highlightedRecords);
    }
  }

  public String getIdAttributeName() {
    return getMetaData().getIdAttributeName();
  }

  public DataObjectQuadTree getIndex() {
    return index;
  }

  public List<LayerDataObject> getMergeableSelectedRecords() {
    final List<LayerDataObject> selectedRecords = getSelectedRecords();
    for (final ListIterator<LayerDataObject> iterator = selectedRecords.listIterator(); iterator.hasNext();) {
      final LayerDataObject record = iterator.next();
      if (record.isDeleted()) {
        iterator.remove();
      }
    }
    return selectedRecords;
  }

  public DataObjectMetaData getMetaData() {
    return this.metaData;
  }

  public Collection<LayerDataObject> getModifiedRecords() {
    return new ArrayList<LayerDataObject>(this.modifiedRecords);
  }

  public int getNewObjectCount() {
    return this.newRecords.size();
  }

  public List<LayerDataObject> getNewRecords() {
    synchronized (this.newRecords) {
      return new ArrayList<LayerDataObject>(this.newRecords);
    }
  }

  protected Geometry getPasteRecordGeometry(final LayerDataObject record,
    final boolean alert) {
    try {
      if (record == null) {
        return null;
      } else {
        DataObjectReader reader = ClipboardUtil.getContents(DataObjectReaderTransferable.DATA_OBJECT_READER_FLAVOR);
        if (reader == null) {
          final String string = ClipboardUtil.getContents(DataFlavor.stringFlavor);
          if (StringUtils.hasText(string)) {
            final Resource resource = new ByteArrayResource("t.csv", string);
            reader = AbstractDataObjectReaderFactory.dataObjectReader(resource);
          } else {
            return null;
          }
        }
        if (reader != null) {
          final MapPanel parentComponent = MapPanel.get(getProject());
          final DataObjectMetaData metaData = getMetaData();
          final Attribute geometryAttribute = metaData.getGeometryAttribute();
          if (geometryAttribute != null) {
            DataType geometryDataType = null;
            Class<?> layerGeometryClass = null;
            final GeometryFactory geometryFactory = getGeometryFactory();
            geometryDataType = geometryAttribute.getType();
            layerGeometryClass = geometryDataType.getJavaClass();

            Geometry geometry = null;
            for (final DataObject sourceRecord : reader) {
              if (geometry == null) {
                final Geometry sourceGeometry = sourceRecord.getGeometryValue();
                if (sourceGeometry == null) {
                  if (alert) {
                    JOptionPane.showMessageDialog(parentComponent,
                      "Clipboard does not contain a record with a geometry.",
                      "Paste Geometry", JOptionPane.ERROR_MESSAGE);
                  }
                  return null;
                }
                geometry = geometryFactory.createGeometry(layerGeometryClass,
                  sourceGeometry);
                if (geometry == null) {
                  if (alert) {
                    JOptionPane.showMessageDialog(
                      parentComponent,
                      "Clipboard should contain a record with a "
                        + geometryDataType + " not a "
                        + sourceGeometry.getGeometryType() + ".",
                      "Paste Geometry", JOptionPane.ERROR_MESSAGE);
                  }
                  return null;
                }
              } else {
                if (alert) {
                  JOptionPane.showMessageDialog(
                    parentComponent,
                    "Clipboard contains more than one record. Copy a single record.",
                    "Paste Geometry", JOptionPane.ERROR_MESSAGE);
                }
                return null;
              }
            }
            if (geometry == null) {
              if (alert) {
                JOptionPane.showMessageDialog(parentComponent,
                  "Clipboard does not contain a record with a geometry.",
                  "Paste Geometry", JOptionPane.ERROR_MESSAGE);
              }
            } else if (geometry.isEmpty()) {
              if (alert) {
                JOptionPane.showMessageDialog(parentComponent,
                  "Clipboard contains an empty geometry.", "Paste Geometry",
                  JOptionPane.ERROR_MESSAGE);
              }
              return null;
            } else {
              return geometry;
            }
          }
        }
        return null;
      }
    } catch (final Throwable t) {
      return null;
    }
  }

  public Query getQuery() {
    if (this.query == null) {
      return null;
    } else {
      return this.query.clone();
    }
  }

  public LayerDataObject getRecord(final int row) {
    throw new UnsupportedOperationException();
  }

  public LayerDataObject getRecordById(final Object id) {
    return null;
  }

  public List<LayerDataObject> getRecords() {
    throw new UnsupportedOperationException();
  }

  public int getRowCount() {
    final DataObjectMetaData metaData = getMetaData();
    final Query query = new Query(metaData);
    return getRowCount(query);
  }

  public int getRowCount(final Query query) {
    LoggerFactory.getLogger(getClass()).error("Get row count not implemented");
    return 0;
  }

  @Override
  public BoundingBox getSelectedBoundingBox() {
    BoundingBox boundingBox = super.getSelectedBoundingBox();
    for (final DataObject record : getSelectedRecords()) {
      final Geometry geometry = record.getGeometryValue();
      boundingBox = boundingBox.expandToInclude(geometry);
    }
    return boundingBox;
  }

  public List<LayerDataObject> getSelectedRecords() {
    synchronized (this.selectedRecords) {
      return new ArrayList<LayerDataObject>(this.selectedRecords);
    }
  }

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

  public int getSelectionCount() {
    return this.selectedRecords.size();
  }

  public Collection<String> getSnapLayerPaths() {
    return getProperty("snapLayers", Collections.<String> emptyList());
  }

  public Collection<String> getUserReadOnlyFieldNames() {
    return Collections.unmodifiableSet(userReadOnlyFieldNames);
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

  /**
   * Cancel changes for one of the lists of changes {@link #deletedRecords},
   *  {@link #newRecords}, {@link #modifiedRecords}.
   * @param records
   */
  private boolean internalCancelChanges(
    final Collection<LayerDataObject> records) {
    boolean cancelled = true;
    for (final LayerDataObject record : new ArrayList<LayerDataObject>(records)) {
      final boolean selected = isSelected(record);
      removeForm(record);
      index.remove(record);
      try {
        final LayerDataObject originalRecord = internalCancelChanges(record);
        if (originalRecord == null) {
          unSelectRecords(record);
        } else {
          index.insert(originalRecord);
          if (selected) {
            if (originalRecord != record) {
              unSelectRecords(record);
              addSelectedRecords(record);
            }
          }
        }
      } catch (final Throwable e) {
        ExceptionUtil.log(getClass(), "Unable to cancel changes.\n" + record, e);
        cancelled = false;
      } finally {
        records.remove(record);
      }
    }
    return cancelled;
  }

  /**
   * Revert the values of the record to the last values loaded from the database
   * @param record
   */
  protected LayerDataObject internalCancelChanges(final LayerDataObject record) {
    if (record != null) {
      final boolean isNew = record.getState() == DataObjectState.New;
      record.cancelChanges();
      if (!isNew) {
        return record;
      }
    }
    return null;
  }

  protected boolean internalIsDeleted(final LayerDataObject layerDataObject) {
    synchronized (this.deletedRecords) {
      return this.deletedRecords.contains(layerDataObject);
    }
  }

  /**
   * Revert the values of the record to the last values loaded from the database
   * @param record
   */
  protected LayerDataObject internalPostSaveChanges(final LayerDataObject record) {
    if (record != null) {

      return record;
    }
    return null;
  }

  protected boolean internalSaveChanges(final LayerDataObject record) {
    try {
      final DataObjectState originalState = record.getState();
      final boolean saved = doSaveChanges(record);
      if (saved) {
        postSaveChanges(originalState, record);
      }
      return saved;
    } catch (final Throwable e) {
      ExceptionUtil.log(getClass(), "Unable to save changes for record:\n"
        + record, e);
      return false;
    }
  }

  public boolean isCanAddRecords() {
    return !super.isReadOnly() && isEditable() && this.canAddRecords
      && hasPermission("INSERT");
  }

  public boolean isCanDeleteRecords() {
    return !super.isReadOnly() && isEditable() && this.canDeleteRecords
      && hasPermission("DELETE");
  }

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

  public boolean isDeleted(final LayerDataObject record) {
    synchronized (this.deletedRecords) {
      return this.deletedRecords != null && internalIsDeleted(record);
    }
  }

  @Override
  public boolean isEventsEnabled() {
    return eventsEnabled != null && this.eventsEnabled.get() != Boolean.FALSE;
  }

  public boolean isFieldUserReadOnly(final String fieldName) {
    return getUserReadOnlyFieldNames().contains(fieldName);
  }

  @Override
  public boolean isHasChanges() {
    if (isEditable()) {
      synchronized (this.getEditSync()) {
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

  public boolean isHidden(final LayerDataObject record) {
    if (isCanDeleteRecords() && isDeleted(record)) {
      return true;
    } else if (isSelected(record)) {
      return true;
    } else {
      return false;
    }
  }

  public boolean isHighlighted(final LayerDataObject record) {
    synchronized (this.highlightedRecords) {
      return highlightedRecords.contains(record);
    }
  }

  public boolean isLayerRecord(final DataObject record) {
    if (record.getMetaData() == getMetaData()) {
      return true;
    } else {
      return false;
    }
  }

  public boolean isModified(final LayerDataObject record) {
    synchronized (this.modifiedRecords) {
      return this.modifiedRecords.contains(record);
    }
  }

  public boolean isNew(final LayerDataObject record) {
    synchronized (this.newRecords) {
      return this.newRecords.contains(record);
    }
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

  public boolean isSelected(final LayerDataObject record) {
    if (record == null) {
      return false;
    } else {
      synchronized (this.selectedRecords) {
        return this.selectedRecords.contains(record);
      }
    }
  }

  public boolean isSnapToAllLayers() {
    return snapToAllLayers;
  }

  public boolean isVisible(final LayerDataObject record) {
    if (isExists() && isVisible()) {
      final AbstractDataObjectLayerRenderer renderer = getRenderer();
      if (renderer == null || renderer.isVisible(record)) {
        return true;
      }
    }
    return false;
  }

  @SuppressWarnings("unchecked")
  public <V extends DataObject> V mergeRecord(final Coordinates point,
    final DataObject record1, final DataObject record2) {
    if (record1 == record2) {
      return (V)record1;
    } else {
      final String sourceIdAttributeName = getIdAttributeName();
      final Object id1 = record1.getValue(sourceIdAttributeName);
      final Object id2 = record2.getValue(sourceIdAttributeName);
      int compare = 0;
      if (id1 == null) {
        if (id2 != null) {
          compare = 1;
        }
      } else if (id2 == null) {
        compare = -1;
      } else {
        compare = CompareUtil.compare(id1, id2);
      }
      if (compare == 0) {
        final Geometry geometry1 = record1.getGeometryValue();
        final Geometry geometry2 = record2.getGeometryValue();
        final double length1 = geometry1.getLength();
        final double length2 = geometry2.getLength();
        if (length1 > length2) {
          compare = -1;
        } else {
          compare = 1;
        }
      }
      if (compare > 0) {
        return (V)mergeRecord(point, record2, record1);
      } else {
        final DirectionalAttributes property = DirectionalAttributes.getProperty(getMetaData());
        final Map<String, Object> newValues = property.getMergedMap(point,
          record1, record2);

        if (record2 instanceof LayerDataObject) {
          deleteRecords((LayerDataObject)record2);
        }
        for (final Entry<String, Object> entry : newValues.entrySet()) {
          final String name = entry.getKey();
          final Object value = entry.getValue();
          record1.setValue(name, value);
        }
        return (V)record1;
      }
    }
  }

  public void mergeSelectedRecords() {
    if (isCanMergeRecords()) {
      Invoke.later(MergeRecordsDialog.class, "showDialog", this);
    }
  }

  public void pasteRecordGeometry(final LayerDataObject record) {
    final Geometry geometry = getPasteRecordGeometry(record, true);
    if (geometry != null) {
      record.setGeometryValue(geometry);
    }
  }

  public void pasteRecords() {
    DataObjectReader reader = ClipboardUtil.getContents(DataObjectReaderTransferable.DATA_OBJECT_READER_FLAVOR);
    if (reader == null) {
      final String string = ClipboardUtil.getContents(DataFlavor.stringFlavor);
      if (StringUtils.hasText(string)) {
        final Resource resource = new ByteArrayResource("t.csv", string);
        reader = AbstractDataObjectReaderFactory.dataObjectReader(resource);
      }
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
      Collection<String> ignorePasteFields = getProperty("ignorePasteFields");
      if (ignorePasteFields == null) {
        ignorePasteFields = Collections.emptySet();
      }
      for (final DataObject sourceRecord : reader) {
        final Map<String, Object> newValues = new LinkedHashMap<String, Object>(
          sourceRecord);

        Geometry sourceGeometry = sourceRecord.getGeometryValue();
        for (final Iterator<String> iterator = newValues.keySet().iterator(); iterator.hasNext();) {
          final String attributeName = iterator.next();
          final Attribute attribute = metaData.getAttribute(attributeName);
          if (attribute == null) {
            iterator.remove();
          } else if (ignorePasteFields != null) {
            if (ignorePasteFields.contains(attribute.getName())) {
              iterator.remove();
            }
          }
        }
        if (geometryDataType != null) {
          if (sourceGeometry == null) {
            final Object value = sourceRecord.getValue(geometryAttribute.getName());
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
    addSelectedRecords(newRecords);
  }

  protected void postSaveChanges(final DataObjectState originalState,
    final LayerDataObject record) {
    postSaveDeletedRecord(record);
    postSaveModifiedRecord(record);
    postSaveNewRecord(record);
  }

  protected boolean postSaveDeletedRecord(final LayerDataObject record) {
    boolean deleted;
    synchronized (this.deletedRecords) {
      deleted = this.deletedRecords.remove(record);
    }
    if (deleted) {
      unSelectRecords(record);
      index.remove(record);
      return true;
    } else {
      return false;
    }
  }

  protected boolean postSaveModifiedRecord(final LayerDataObject record) {
    synchronized (this.modifiedRecords) {
      return this.modifiedRecords.remove(record);
    }
  }

  protected boolean postSaveNewRecord(final LayerDataObject record) {
    synchronized (this.newRecords) {
      if (this.newRecords.remove(record)) {
        index.insert(record);
        if (isSelected(record)) {
          unSelectRecords(record);
          addSelectedRecords(record);
        }
        return true;
      }
    }
    return false;
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
        }
      }
    }
  }

  public final List<LayerDataObject> query(final BoundingBox boundingBox) {
    final List<LayerDataObject> results = doQuery(boundingBox);
    final Filter<DataObject> filter = new DataObjectGeometryIntersectsFilter(
      boundingBox);
    filterUpdates(results, filter);
    return results;
  }

  public List<LayerDataObject> query(final Geometry geometry,
    final double maxDistance) {
    final List<LayerDataObject> results = doQuery(geometry, maxDistance);
    final Filter<DataObject> filter = new DataObjectGeometryDistanceFilter(
      geometry, maxDistance);
    filterUpdates(results, filter);
    return results;
  }

  public List<LayerDataObject> query(final Query query) {
    throw new UnsupportedOperationException("Query not currently supported");
  }

  public final List<LayerDataObject> queryBackground(
    final BoundingBox boundingBox) {
    final List<LayerDataObject> results = doQueryBackground(boundingBox);
    final Filter<DataObject> filter = new DataObjectGeometryIntersectsFilter(
      boundingBox);
    filterUpdates(results, filter);
    return results;
  }

  protected void removeForm(final LayerDataObject record) {
    if (record != null) {
      if (SwingUtilities.isEventDispatchThread()) {
        final Window window = this.formWindows.remove(record);
        if (window != null) {
          window.dispose();
        }
        final Component form = this.forms.remove(record);
        if (form != null) {
          if (form instanceof DataObjectLayerForm) {
            final DataObjectLayerForm recordForm = (DataObjectLayerForm)form;
            recordForm.destroy();
          }
        }
      } else {
        Invoke.later(this, "removeForm", record);
      }
    }
  }

  public void removeForms(final Collection<LayerDataObject> records) {
    if (records != null && !records.isEmpty()) {
      if (SwingUtilities.isEventDispatchThread()) {
        for (final LayerDataObject record : records) {
          removeForm(record);
        }
      } else {
        Invoke.later(this, "removeForms", records);
      }
    }
  }

  protected void removeSelectedRecord(final LayerDataObject record) {
    synchronized (this.selectedRecords) {
      this.selectedRecords.remove(record);
    }
  }

  public void revertChanges(final LayerDataObject record) {
    synchronized (this.modifiedRecords) {
      if (isLayerRecord(record)) {
        postSaveModifiedRecord(record);
        synchronized (this.deletedRecords) {
          this.deletedRecords.remove(record);
        }
      }
    }
  }

  @Override
  public boolean saveChanges() {
    synchronized (this.getEditSync()) {
      boolean saved = true;
      if (isHasChanges()) {
        final boolean eventsEnabled = setEventsEnabled(false);
        try {
          saved &= doSaveChanges();
        } catch (final Throwable e) {
          ExceptionUtil.log(getClass(), "Unable to save changes for layer: "
            + this, e);
          saved = false;
        } finally {
          setEventsEnabled(eventsEnabled);
          fireRecordsChanged();
        }
      }
      if (!saved) {
        JOptionPane.showMessageDialog(MapPanel.get(this),
          "<html><p>There was an error saving changes for one or more records.</p>"
            + "<p>" + getPath() + "</p>"
            + "<p>Check the logging panel for details.</html>",
          "Error Saving Changes", JOptionPane.ERROR_MESSAGE);
      }
      return saved;
    }
  }

  public final boolean saveChanges(final LayerDataObject record) {
    final boolean saved = internalSaveChanges(record);
    fireRecordsChanged();
    if (!saved) {
      JOptionPane.showMessageDialog(MapPanel.get(this),
        "<html><p>There was an error saving changes to the record.</p>" + "<p>"
          + getPath() + "</p>"
          + "<p>Check the logging panel for details.</html>",
        "Error Saving Changes", JOptionPane.ERROR_MESSAGE);
    }
    return saved;
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
      synchronized (this.getEditSync()) {
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
    if (geometryFactory != null && this.boundingBox.isEmpty()) {
      this.boundingBox = geometryFactory.getCoordinateSystem()
        .getAreaBoundingBox();
    }
  }

  public void setHighlightedRecords(
    final Collection<LayerDataObject> highlightedRecords) {
    synchronized (this.highlightedRecords) {
      this.highlightedRecords.clear();
      this.highlightedRecords.addAll(highlightedRecords);

    }
    fireHighlighted();
  }

  public void setIndex(final DataObjectQuadTree index) {
    if (index == null) {
      this.index = new DataObjectQuadTree();
    } else {
      this.index = index;
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
      if (query == null) {
        setQuery(null);
      }
    }
  }

  @Override
  public void setProperty(final String name, final Object value) {
    if ("style".equals(name)) {
      if (value instanceof Map) {
        @SuppressWarnings("unchecked")
        final Map<String, Object> style = (Map<String, Object>)value;
        final LayerRenderer<AbstractDataObjectLayer> renderer = AbstractDataObjectLayerRenderer.getRenderer(
          this, style);
        if (renderer != null) {
          setRenderer(renderer);
        }
      }
    } else {
      super.setProperty(name, value);
    }
  }

  public void setQuery(final Query query) {
    final Query oldValue = this.query;
    if (query == null) {
      final DataObjectMetaData metaData = getMetaData();
      if (metaData == null) {
        this.query = null;
      } else {
        this.query = new Query(metaData);
      }
    } else {
      this.query = query;
    }
    firePropertyChange("query", oldValue, this.query);
  }

  public void setSelectedRecords(final BoundingBox boundingBox) {
    if (isSelectable()) {
      final List<LayerDataObject> records = query(boundingBox);
      for (final Iterator<LayerDataObject> iterator = records.iterator(); iterator.hasNext();) {
        final LayerDataObject layerDataObject = iterator.next();
        if (!isVisible(layerDataObject) || internalIsDeleted(layerDataObject)) {
          iterator.remove();
        }
      }
      setSelectedRecords(records);
      if (!this.selectedRecords.isEmpty()) {
        showRecordsTable(DataObjectLayerTableModel.MODE_SELECTED);
      }
    }
  }

  public void setSelectedRecords(
    final Collection<LayerDataObject> selectedRecords) {
    synchronized (this.selectedRecords) {
      clearSelectedRecordsIndex();
      this.selectedRecords.clear();
      this.selectedRecords.addAll(selectedRecords);
    }
    synchronized (this.highlightedRecords) {
      highlightedRecords.retainAll(selectedRecords);
    }
    fireSelected();
  }

  public void setSelectedRecords(final LayerDataObject... selectedRecords) {
    setSelectedRecords(Arrays.asList(selectedRecords));
  }

  public void setSelectedRecordsById(final Object id) {
    final DataObjectMetaData metaData = getMetaData();
    if (metaData != null) {
      final String idAttributeName = metaData.getIdAttributeName();
      if (idAttributeName == null) {
        clearSelectedRecords();
      } else {
        final Query query = Query.equal(metaData, idAttributeName, id);
        final List<LayerDataObject> records = query(query);
        setSelectedRecords(records);
      }
    }
  }

  public int setSelectedWithinDistance(final boolean selected,
    final Geometry geometry, final int distance) {
    clearSelectedRecordsIndex();
    final List<LayerDataObject> records = query(geometry, distance);
    for (final Iterator<LayerDataObject> iterator = records.iterator(); iterator.hasNext();) {
      final LayerDataObject layerDataObject = iterator.next();
      if (!isVisible(layerDataObject)) {
        iterator.remove();
      }
    }
    if (selected) {
      addSelectedRecords(records);
    } else {
      unSelectRecords(records);
    }
    return records.size();
  }

  public void setSnapLayerPaths(final Collection<String> snapLayerPaths) {
    if (snapLayerPaths == null || snapLayerPaths.isEmpty()) {
      removeProperty("snapLayers");
    } else {
      setProperty("snapLayers", new TreeSet<String>(snapLayerPaths));
    }
  }

  public void setSnapToAllLayers(final boolean snapToAllLayers) {
    this.snapToAllLayers = snapToAllLayers;
  }

  public void setUserReadOnlyFieldNames(
    final Collection<String> userReadOnlyFieldNames) {
    this.userReadOnlyFieldNames = new LinkedHashSet<String>(
      userReadOnlyFieldNames);
  }

  public LayerDataObject showAddForm(final Map<String, Object> parameters) {
    if (isCanAddRecords()) {
      final LayerDataObject newRecord = createRecord(parameters);
      // newRecord.setGeometryValue(getGeometryFactory().createEmptyGeometry());
      final DataObjectLayerForm form = createForm(newRecord);
      if (form == null) {
        return null;
      } else {
        final LayerDataObject addedRecord = form.showAddDialog();
        return addedRecord;
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
  public <V extends JComponent> V showForm(final LayerDataObject record) {
    if (record == null) {
      return null;
    } else {
      if (SwingUtilities.isEventDispatchThread()) {
        Window window = this.formWindows.get(record);
        if (window == null) {
          final Component form = createForm(record);
          final Object id = record.getIdValue();
          if (form == null) {
            return null;
          } else {
            String title;
            if (record.getState() == DataObjectState.New) {
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
            SwingUtil.autoAdjustPosition(window);
            this.forms.put(record, form);
            this.formWindows.put(record, window);
            window.addWindowListener(new WindowAdapter() {

              @Override
              public void windowClosing(final WindowEvent e) {
                removeForm(record);
              }
            });
            SwingUtil.setVisible(window, true);

            window.requestFocus();
            return (V)form;
          }
        } else {
          SwingUtil.setVisible(window, true);

          window.requestFocus();
          final Component component = window.getComponent(0);
          if (component instanceof JScrollPane) {
            final JScrollPane scrollPane = (JScrollPane)component;
            return (V)scrollPane.getComponent(0);
          }
          return null;
        }
      } else {
        Invoke.later(this, "showForm", record);
        return null;
      }
    }
  }

  public void showRecordsTable() {
    showRecordsTable(DataObjectLayerTableModel.MODE_ALL);
  }

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
      Invoke.later(this, "showRecordsTable", attributeFilterMode);
    }
  }

  public List<LayerDataObject> splitRecord(final LayerDataObject record,
    final CloseLocation mouseLocation) {

    final Geometry geometry = mouseLocation.getGeometry();
    if (geometry instanceof LineString) {
      final LineString line = (LineString)geometry;
      final int[] vertexIndex = mouseLocation.getVertexIndex();
      final Point point = mouseLocation.getPoint();
      final Point convertedPoint = getGeometryFactory().copy(point);
      final Coordinates coordinates = CoordinatesUtil.get(convertedPoint);
      final LineString line1;
      final LineString line2;

      final int numPoints = line.getNumPoints();
      if (vertexIndex == null) {
        final int pointIndex = mouseLocation.getSegmentIndex()[0];
        line1 = LineStringUtil.subLineString(line, null, 0, pointIndex + 1,
          coordinates);
        line2 = LineStringUtil.subLineString(line, coordinates, pointIndex + 1,
          numPoints - pointIndex - 1, null);
      } else {
        final int pointIndex = vertexIndex[0];
        if (numPoints - pointIndex < 2) {
          return Collections.singletonList(record);
        } else {
          line1 = LineStringUtil.subLineString(line, pointIndex + 1);
          line2 = LineStringUtil.subLineString(line, null, pointIndex,
            numPoints - pointIndex, null);
        }

      }
      if (line1 == null || line2 == null) {
        return Collections.singletonList(record);
      }
      unSelectRecords(record);

      final DirectionalAttributes property = DirectionalAttributes.getProperty(record);

      final LayerDataObject record2 = copyRecord(record);
      record.setGeometryValue(line1);
      record2.setGeometryValue(line2);

      property.setSplitAttributes(line, coordinates, record);
      property.setSplitAttributes(line, coordinates, record2);

      updateSpatialIndex(record2, line);

      index.insert(record2);
      addSelectedRecords(record, record2);
      return Arrays.asList(record, record2);
    }
    return Arrays.asList(record);
  }

  @Override
  public Map<String, Object> toMap() {
    final Map<String, Object> map = super.toMap();
    if (!super.isReadOnly()) {
      MapSerializerUtil.add(map, "canAddRecords", this.canAddRecords);
      MapSerializerUtil.add(map, "canDeleteRecords", this.canDeleteRecords);
      MapSerializerUtil.add(map, "canEditRecords", this.canEditRecords);
      MapSerializerUtil.add(map, "snapToAllLayers", this.snapToAllLayers);
    }
    MapSerializerUtil.add(map, "columnNameOrder", this.columnNameOrder);
    map.remove("TableView");
    return map;
  }

  public void unHighlightRecords(
    final Collection<? extends LayerDataObject> records) {
    synchronized (this.highlightedRecords) {
      this.highlightedRecords.removeAll(records);
    }
    fireHighlighted();
  }

  public void unSelectRecords(final BoundingBox boundingBox) {
    if (isSelectable()) {
      final List<LayerDataObject> records = query(boundingBox);
      for (final Iterator<LayerDataObject> iterator = records.iterator(); iterator.hasNext();) {
        final LayerDataObject layerDataObject = iterator.next();
        if (!isVisible(layerDataObject) || internalIsDeleted(layerDataObject)) {
          iterator.remove();
        }
      }
      unSelectRecords(records);
      if (!this.selectedRecords.isEmpty()) {
        showRecordsTable(DataObjectLayerTableModel.MODE_SELECTED);
      }
    }
  }

  public void unSelectRecords(
    final Collection<? extends LayerDataObject> records) {
    for (final LayerDataObject record : records) {
      removeSelectedRecord(record);
    }
    clearSelectedRecordsIndex();
    fireSelected();
    unHighlightRecords(records);
  }

  public void unSelectRecords(final LayerDataObject... records) {
    unSelectRecords(Arrays.asList(records));
  }

  protected void updateColumnNames() {
    if (this.columnNames != null && this.metaData != null) {
      final List<String> attributeNames = this.metaData.getAttributeNames();
      this.columnNames.retainAll(attributeNames);
    }
  }

  protected void updateRecordState(final LayerDataObject record) {
    final DataObjectState state = record.getState();
    if (state == DataObjectState.Modified) {
      addModifiedRecord(record);
    } else if (state == DataObjectState.Persisted) {
      postSaveModifiedRecord(record);
    }
  }

  protected void updateSpatialIndex(final LayerDataObject record,
    final Geometry oldGeometry) {
    index.remove(BoundingBox.getBoundingBox(oldGeometry), record);
  }

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

  public void zoomToObject(final DataObject record) {
    final Geometry geometry = record.getGeometryValue();

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
