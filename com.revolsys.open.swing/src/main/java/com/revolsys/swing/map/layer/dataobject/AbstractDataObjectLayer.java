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
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
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
import com.revolsys.swing.component.TabbedValuePanel;
import com.revolsys.swing.dnd.ClipboardUtil;
import com.revolsys.swing.dnd.transferable.DataObjectReaderTransferable;
import com.revolsys.swing.field.CheckBox;
import com.revolsys.swing.layout.GroupLayoutUtil;
import com.revolsys.swing.map.MapPanel;
import com.revolsys.swing.map.form.DataObjectLayerForm;
import com.revolsys.swing.map.layer.AbstractLayer;
import com.revolsys.swing.map.layer.Layer;
import com.revolsys.swing.map.layer.LayerGroup;
import com.revolsys.swing.map.layer.LayerRenderer;
import com.revolsys.swing.map.layer.Project;
import com.revolsys.swing.map.layer.dataobject.component.MergeRecordsDialog;
import com.revolsys.swing.map.layer.dataobject.renderer.AbstractDataObjectLayerRenderer;
import com.revolsys.swing.map.layer.dataobject.renderer.GeometryStyleRenderer;
import com.revolsys.swing.map.layer.dataobject.style.panel.DataObjectLayerStylePanel;
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
import com.revolsys.swing.table.dataobject.row.DataObjectRowTable;
import com.revolsys.swing.tree.TreeItemPropertyEnableCheck;
import com.revolsys.swing.tree.TreeItemRunnable;
import com.revolsys.swing.tree.model.ObjectTreeModel;
import com.revolsys.swing.undo.SetObjectProperty;
import com.revolsys.util.CompareUtil;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;

public abstract class AbstractDataObjectLayer extends AbstractLayer implements
  DataObjectFactory, AddGeometryCompleteAction {

  public static final String FORM_FACTORY_EXPRESSION = "formFactoryExpression";

  public static void addVisibleLayers(
    final List<AbstractDataObjectLayer> layers, final LayerGroup group) {
    if (group.isVisible()) {
      for (final Layer layer : group) {
        if (layer instanceof LayerGroup) {
          final LayerGroup layerGroup = (LayerGroup)layer;
          addVisibleLayers(layers, layerGroup);
        } else if (layer instanceof AbstractDataObjectLayer) {
          if (layer.isVisible()) {
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

  private LayerDataObject highlightedObject;

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

  private final Set<LayerDataObject> deletedRecords = new LinkedHashSet<LayerDataObject>();

  private final Object editSync = new Object();

  private final Map<DataObject, Window> forms = new HashMap<DataObject, Window>();

  private DataObjectMetaData metaData;

  private final Set<LayerDataObject> modifiedRecords = new LinkedHashSet<LayerDataObject>();

  private final Set<LayerDataObject> newRecords = new LinkedHashSet<LayerDataObject>();

  private Query query;

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
    if (geometry == null) {
      return null;
    } else {
      final DataObjectMetaData metaData = getMetaData();
      final String geometryAttributeName = metaData.getGeometryAttributeName();
      final Map<String, Object> parameters = new HashMap<String, Object>();
      parameters.put(geometryAttributeName, geometry);
      return showAddForm(parameters);
    }
  }

  protected void addModifiedObject(final LayerDataObject object) {
    synchronized (this.modifiedRecords) {
      this.modifiedRecords.add(object);
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

  public void addSelectedRecords(
    final Collection<? extends LayerDataObject> objects) {
    for (final LayerDataObject object : objects) {
      addSelectedRecord(object);
    }
    fireSelected();
  }

  public void addSelectedRecords(final LayerDataObject... objects) {
    addSelectedRecords(Arrays.asList(objects));
  }

  public void cancelChanges() {
    synchronized (this.editSync) {
      internalCancelChanges();
      refresh();
    }
  }

  public boolean canPasteRecordGeometry(final LayerDataObject record) {
    final Geometry geometry = getPasteRecordGeometry(record, false);

    return geometry != null;
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

  public void clearSelectedRecords() {
    this.selectedRecords.clear();
    fireSelected();
  }

  protected void clearSelectedRecordsIndex() {
    this.selectedRecordsIndex = null;
  }

  @SuppressWarnings("unchecked")
  public <V extends LayerDataObject> V copyRecord(final V object) {
    final LayerDataObject copy = createRecord(object);
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
    createPropertiesPanelFields(propertiesPanel);
    createPropertiesPanelStyle(propertiesPanel);
    createPropertiesPanelSnapping(propertiesPanel);
    return propertiesPanel;
  }

  protected void createPropertiesPanelFields(
    final TabbedValuePanel propertiesPanel) {
    final DataObjectMetaData metaData = getMetaData();
    final BaseJxTable fieldTable = DataObjectMetaDataTableModel.createTable(metaData);

    final JPanel fieldPanel = new JPanel(new BorderLayout());
    final JScrollPane fieldScroll = new JScrollPane(fieldTable);
    fieldPanel.add(fieldScroll, BorderLayout.CENTER);
    propertiesPanel.addTab("Fields", fieldPanel);
  }

  protected void createPropertiesPanelSnapping(
    final TabbedValuePanel propertiesPanel) {
    final JPanel panel = new JPanel();
    SwingUtil.setTitledBorder(panel, "Snapping");
    final CheckBox snapToAllLayers = new CheckBox("snapToAllLayers",
      isSnapToAllLayers());
    // TODO on toggle change value and enable/disable list of layers selection
    SwingUtil.addLabel(panel, "Snap To All Layers");
    panel.add(snapToAllLayers);
    GroupLayoutUtil.makeColumns(panel, 2, false);
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

  public UndoableEdit createPropertyEdit(final LayerDataObject object,
    final String propertyName, final Object oldValue, final Object newValue) {
    return new SetObjectProperty(object, propertyName, oldValue, newValue);
  }

  public LayerDataObject createRecord() {
    if (!isReadOnly() && isEditable() && isCanAddRecords()) {
      final LayerDataObject object = createDataObject(getMetaData());
      synchronized (newRecords) {
        this.newRecords.add(object);
      }
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

  public Component createTablePanel() {
    final DataObjectRowTable table = DataObjectLayerTableModel.createTable(this);
    if (table == null) {
      return null;
    } else {
      return new DataObjectLayerTablePanel(this, table);
    }
  }

  @Override
  public void delete() {
    clearChanges();
    setIndex(null);
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

  protected void deleteRecord(final LayerDataObject object) {
    final boolean trackDeletions = true;
    deleteRecord(object, trackDeletions);
  }

  protected void deleteRecord(final LayerDataObject record,
    final boolean trackDeletions) {
    if (isLayerObject(record)) {
      removeSelectedRecords(record);
      clearSelectedRecordsIndex();
      synchronized (newRecords) {
        if (!this.newRecords.remove(record)) {
          this.modifiedRecords.remove(record);
          if (trackDeletions) {
            this.deletedRecords.add(record);
          }
          this.selectedRecords.remove(record);
        }
      }
      record.setState(DataObjectState.Deleted);
    }
  }

  public void deleteRecords(final Collection<? extends LayerDataObject> objects) {
    if (isCanDeleteRecords()) {
      synchronized (this.editSync) {
        unselectRecords(objects);
        for (final LayerDataObject object : objects) {
          deleteRecord(object);
        }
      }
      fireRecordsChanged();
    }
  }

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

  protected List<LayerDataObject> doQueryBackground(
    final BoundingBox boundingBox) {
    return doQuery(boundingBox);
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

  public int getChangeCount() {
    int changeCount = 0;
    changeCount += this.newRecords.size();
    changeCount += this.modifiedRecords.size();
    changeCount += this.deletedRecords.size();
    return changeCount;
  }

  public List<LayerDataObject> getChanges() {
    synchronized (this.editSync) {
      final List<LayerDataObject> objects = new ArrayList<LayerDataObject>();
      synchronized (newRecords) {
        objects.addAll(this.newRecords);
      }
      objects.addAll(this.modifiedRecords);
      objects.addAll(this.deletedRecords);
      return objects;
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

  public LayerDataObject getHighlightedObject() {
    return highlightedObject;
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

  public Set<LayerDataObject> getModifiedRecords() {
    return new LinkedHashSet<LayerDataObject>(this.modifiedRecords);
  }

  public int getNewObjectCount() {
    if (this.newRecords == null) {
      return 0;
    } else {
      return this.newRecords.size();
    }
  }

  public List<LayerDataObject> getNewRecords() {
    synchronized (newRecords) {
      return new ArrayList<LayerDataObject>(this.newRecords);
    }
  }

  protected Geometry getPasteRecordGeometry(final LayerDataObject record,
    final boolean alert) {
    if (record == null) {
      return null;
    } else {
      DataObjectReader reader = ClipboardUtil.getContents(DataObjectReaderTransferable.DATA_OBJECT_READER_FLAVOR);
      if (reader == null) {
        final String string = ClipboardUtil.getContents(DataFlavor.stringFlavor);
        final Resource resource = new ByteArrayResource("t.csv", string);
        reader = AbstractDataObjectReaderFactory.dataObjectReader(resource);
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
    for (final DataObject object : getSelectedRecords()) {
      final Geometry geometry = object.getGeometryValue();
      boundingBox = boundingBox.expandToInclude(geometry);
    }
    return boundingBox;
  }

  public List<LayerDataObject> getSelectedRecords() {
    return new ArrayList<LayerDataObject>(this.selectedRecords);
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

  @SuppressWarnings("unchecked")
  public List<String> getSnapLayerNames() {
    return (List<String>)getProperty("snapLayers");
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

  protected void internalCancelChanges() {
    clearChanges();
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

  public boolean isDeleted(final LayerDataObject object) {
    return this.deletedRecords != null && this.deletedRecords.contains(object);
  }

  @Override
  public boolean isEventsEnabled() {
    return this.eventsEnabled.get() != Boolean.FALSE;
  }

  public boolean isFieldUserReadOnly(final String fieldName) {
    return getUserReadOnlyFieldNames().contains(fieldName);
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

  public boolean isModified(final LayerDataObject object) {
    return this.modifiedRecords.contains(object);
  }

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

  public boolean isSelected(final LayerDataObject object) {
    if (object == null) {
      return false;
    } else {
      return this.selectedRecords.contains(object);
    }
  }

  public boolean isSnapToAllLayers() {
    return snapToAllLayers;
  }

  public boolean isVisible(final LayerDataObject object) {
    if (isVisible()) {
      final AbstractDataObjectLayerRenderer renderer = getRenderer();
      if (renderer == null || renderer.isVisible(object)) {
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

  public void removeSelectedRecords(final LayerDataObject... objects) {
    removeSelectedRecords(Arrays.asList(objects));
  }

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

  public void setHighlightedObject(final LayerDataObject highlightedObject) {
    final Object oldValue = this.highlightedObject;
    this.highlightedObject = highlightedObject;
    firePropertyChange("highlightedObject", oldValue, highlightedObject);
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
    this.query = query;
    firePropertyChange("query", oldValue, query);
  }

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

  public void setSelectedRecords(
    final Collection<LayerDataObject> selectedRecords) {
    synchronized (selectedRecords) {
      clearSelectedRecordsIndex();
      this.selectedRecords.clear();
      this.selectedRecords.addAll(selectedRecords);
    }
    fireSelected();
  }

  public void setSelectedRecords(final LayerDataObject... selectedRecords) {
    setSelectedRecords(Arrays.asList(selectedRecords));
  }

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
            SwingUtil.autoAdjustPosition(window);
            this.forms.put(object, window);
            window.addWindowListener(new WindowAdapter() {

              @Override
              public void windowClosing(final WindowEvent e) {
                removeForm(object);
              }
            });
            window.setVisible(true);
            window.requestFocus();
            return (V)form;
          }
        } else {
          window.setVisible(true);
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

      final DirectionalAttributes property = DirectionalAttributes.getProperty(record);

      final LayerDataObject record2 = copyRecord(record);
      record.setGeometryValue(line1);
      record2.setGeometryValue(line2);

      property.setSplitAttributes(line, coordinates, record);
      property.setSplitAttributes(line, coordinates, record2);

      index.insert(record2);
      removeSelectedRecord(record);
      addSelectedRecords(record, record2);
      return Arrays.asList(record, record2);
    }
    return Arrays.asList(record);
  }

  @Override
  public Map<String, Object> toMap() {
    final Map<String, Object> map = super.toMap();
    if (!super.isReadOnly()) {
      MapSerializerUtil.add(map, "canAddRecords", this.canAddRecords, true);
      MapSerializerUtil.add(map, "canDeleteRecords", this.canDeleteRecords,
        true);
      MapSerializerUtil.add(map, "canEditRecords", this.canEditRecords, true);
      MapSerializerUtil.add(map, "snapToAllLayers", this.snapToAllLayers, false);
    }
    MapSerializerUtil.add(map, "columnNameOrder", this.columnNameOrder);
    map.remove("TableView");
    return map;
  }

  public void unselectRecords(
    final Collection<? extends LayerDataObject> objects) {
    clearSelectedRecordsIndex();
    this.selectedRecords.removeAll(objects);
    fireSelected();
  }

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
