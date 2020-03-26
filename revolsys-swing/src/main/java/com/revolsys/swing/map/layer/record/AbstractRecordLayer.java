package com.revolsys.swing.map.layer.record;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Window;
import java.awt.datatransfer.DataFlavor;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.CancellationException;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Predicate;

import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.undo.UndoableEdit;

import org.jeometry.common.compare.CompareUtil;
import org.jeometry.common.data.identifier.Identifier;
import org.jeometry.common.data.type.DataType;
import org.jeometry.common.data.type.DataTypes;
import org.jeometry.common.io.PathName;
import org.jeometry.common.logging.Logs;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import com.revolsys.collection.list.Lists;
import com.revolsys.collection.map.LinkedHashMapEx;
import com.revolsys.collection.map.MapEx;
import com.revolsys.collection.map.Maps;
import com.revolsys.collection.set.Sets;
import com.revolsys.geometry.index.RecordSpatialIndex;
import com.revolsys.geometry.index.SpatialIndex;
import com.revolsys.geometry.index.rstartree.RStarTree;
import com.revolsys.geometry.io.GeometryReader;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryDataTypes;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.LineString;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.editor.BoundingBoxEditor;
import com.revolsys.geometry.util.RectangleUtil;
import com.revolsys.io.BaseCloseable;
import com.revolsys.io.FileUtil;
import com.revolsys.io.map.MapObjectFactory;
import com.revolsys.record.ArrayRecord;
import com.revolsys.record.Record;
import com.revolsys.record.RecordFactory;
import com.revolsys.record.RecordState;
import com.revolsys.record.Records;
import com.revolsys.record.code.CodeTable;
import com.revolsys.record.io.RecordIo;
import com.revolsys.record.io.RecordReader;
import com.revolsys.record.io.RecordWriter;
import com.revolsys.record.io.RecordWriterFactory;
import com.revolsys.record.io.format.json.JsonObject;
import com.revolsys.record.property.DirectionalFields;
import com.revolsys.record.query.Condition;
import com.revolsys.record.query.Q;
import com.revolsys.record.query.Query;
import com.revolsys.record.query.QueryValue;
import com.revolsys.record.schema.FieldDefinition;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.spring.resource.ByteArrayResource;
import com.revolsys.spring.resource.PathResource;
import com.revolsys.spring.resource.Resource;
import com.revolsys.swing.Borders;
import com.revolsys.swing.Dialogs;
import com.revolsys.swing.SwingUtil;
import com.revolsys.swing.action.enablecheck.EnableCheck;
import com.revolsys.swing.component.BaseDialog;
import com.revolsys.swing.component.BasePanel;
import com.revolsys.swing.component.ProgressMonitor;
import com.revolsys.swing.component.TabbedValuePanel;
import com.revolsys.swing.component.ValueField;
import com.revolsys.swing.dnd.ClipboardUtil;
import com.revolsys.swing.dnd.transferable.RecordReaderTransferable;
import com.revolsys.swing.dnd.transferable.StringTransferable;
import com.revolsys.swing.field.Field;
import com.revolsys.swing.field.TextField;
import com.revolsys.swing.io.SwingIo;
import com.revolsys.swing.layout.GroupLayouts;
import com.revolsys.swing.logging.LoggingEventPanel;
import com.revolsys.swing.map.MapPanel;
import com.revolsys.swing.map.ViewportCacheBoundingBox;
import com.revolsys.swing.map.form.FieldNamesSetPanel;
import com.revolsys.swing.map.form.LayerRecordForm;
import com.revolsys.swing.map.form.SnapLayersPanel;
import com.revolsys.swing.map.layer.AbstractLayer;
import com.revolsys.swing.map.layer.Layer;
import com.revolsys.swing.map.layer.LayerGroup;
import com.revolsys.swing.map.layer.LayerRenderer;
import com.revolsys.swing.map.layer.Project;
import com.revolsys.swing.map.layer.record.component.RecordLayerFieldUiFactory;
import com.revolsys.swing.map.layer.record.component.recordmerge.MergeRecordsDialog;
import com.revolsys.swing.map.layer.record.renderer.AbstractMultipleRecordLayerRenderer;
import com.revolsys.swing.map.layer.record.renderer.AbstractRecordLayerRenderer;
import com.revolsys.swing.map.layer.record.renderer.GeometryStyleRecordLayerRenderer;
import com.revolsys.swing.map.layer.record.renderer.MultipleRecordRenderer;
import com.revolsys.swing.map.layer.record.style.GeometryStyle;
import com.revolsys.swing.map.layer.record.style.panel.LayerStylePanel;
import com.revolsys.swing.map.layer.record.style.panel.QueryFilterField;
import com.revolsys.swing.map.layer.record.table.RecordLayerTable;
import com.revolsys.swing.map.layer.record.table.RecordLayerTablePanel;
import com.revolsys.swing.map.layer.record.table.model.BlockDeleteRecords;
import com.revolsys.swing.map.layer.record.table.model.RecordDefinitionTableModel;
import com.revolsys.swing.map.layer.record.table.model.RecordLayerErrors;
import com.revolsys.swing.map.layer.record.table.model.RecordLayerTableModel;
import com.revolsys.swing.map.layer.record.table.model.RecordValidationDialog;
import com.revolsys.swing.map.overlay.AbstractOverlay;
import com.revolsys.swing.map.overlay.AddGeometryCompleteAction;
import com.revolsys.swing.map.overlay.CloseLocation;
import com.revolsys.swing.map.overlay.ShortestPathOverlay;
import com.revolsys.swing.map.overlay.record.EditRecordGeometryOverlay;
import com.revolsys.swing.menu.MenuFactory;
import com.revolsys.swing.menu.WrappedMenuFactory;
import com.revolsys.swing.parallel.Invoke;
import com.revolsys.swing.preferences.PreferenceFields;
import com.revolsys.swing.table.BaseJTable;
import com.revolsys.swing.undo.MultipleUndo;
import com.revolsys.swing.undo.SetRecordFieldValueUndo;
import com.revolsys.util.PreferenceKey;
import com.revolsys.util.Preferences;
import com.revolsys.util.Property;

public abstract class AbstractRecordLayer extends AbstractLayer
  implements AddGeometryCompleteAction, RecordLayerProxy, RecordLayerFieldUiFactory {
  private class RecordCacheIndex extends RecordCacheDelegating {
    private RecordSpatialIndex<LayerRecord> index;

    public RecordCacheIndex(final String cacheId) {
      super(newRecordCacheDo(cacheId));
      addRecordCache(this);
    }

    @Override
    public boolean addRecord(final LayerRecord record) {
      synchronized (getRecordCacheSync()) {
        addRecordIndex(record);
      }
      return false;
    }

    private boolean addRecordIndex(final LayerRecord record) {
      if (record != null && record.hasGeometry()) {
        if (super.addRecord(record)) {
          final RecordSpatialIndex<LayerRecord> index = this.index;
          if (index != null) {
            index.addRecord(record.getRecordProxy());
          }
          return true;
        }
      }
      return false;
    }

    @Override
    public void addRecords(final Iterable<? extends LayerRecord> records) {
      synchronized (getRecordCacheSync()) {
        for (final LayerRecord record : records) {
          addRecordIndex(record);
        }
      }
    }

    public void clearIndex() {
      this.index = null;
    }

    @Override
    public void clearRecords() {
      synchronized (getRecordCacheSync()) {
        super.clearRecords();
        clearIndex();
      }
    }

    private RecordSpatialIndex<LayerRecord> getIndex() {
      synchronized (getRecordCacheSync()) {
        RecordSpatialIndex<LayerRecord> index = this.index;
        if (index == null) {
          final RecordSpatialIndex<LayerRecord> newIndex = newSpatialIndex();
          this.index = index = newIndex;
          final Consumer<LayerRecord> action = record -> {
            if (!isDeleted(record)) {
              newIndex.addRecord(record);
            }
          };
          forEachRecord(action);
        }
        return index;
      }
    }

    @SuppressWarnings({
      "unchecked", "rawtypes"
    })
    public <R extends LayerRecord> List<R> getRecords(final BoundingBox boundingBox) {
      final RecordSpatialIndex<LayerRecord> index = getIndex();
      if (index != null) {
        synchronized (index) {
          return (List)index.queryIntersects(boundingBox);
        }
      }
      return Collections.emptyList();
    }

    @SuppressWarnings({
      "unchecked", "rawtypes"
    })
    public <R extends LayerRecord> List<R> getRecordsDistance(final Geometry geometry,
      final double distance) {
      final RecordSpatialIndex<LayerRecord> index = getIndex();
      if (index != null) {
        synchronized (index) {
          return (List)index.getRecordsDistance(geometry, distance);
        }
      }
      return Collections.emptyList();
    }

    @Override
    public boolean removeRecord(final LayerRecord record) {
      synchronized (getRecordCacheSync()) {
        try {
          super.removeRecord(record);
        } finally {
          clearIndex();
        }
      }
      return true;
    }

    @Override
    public boolean replaceRecord(final LayerRecord record) {
      synchronized (getRecordCacheSync()) {
        try {
          if (super.replaceRecord(record)) {
            return true;
          } else {
            return false;
          }
        } finally {
          clearIndex();
        }
      }
    }

    private void setGeometryFactory(final GeometryFactory geometryFactory) {
      synchronized (getRecordCacheSync()) {
        final RecordSpatialIndex<LayerRecord> index = this.index;
        if (index != null) {
          index.setGeometryFactory(geometryFactory);
        }
      }
    }

    @Override
    public void setRecords(final Iterable<? extends LayerRecord> records) {
      synchronized (getRecordCacheSync()) {
        clearIndex();
        super.setRecords(records);
      }
    }
  }

  public static final String ALL = "All";

  public static final String FORM_FACTORY_EXPRESSION = "formFactoryExpression";

  public static final String PREFERENCE_PATH = "/com/revolsys/gis/layer/record";

  public static final PreferenceKey PREFERENCE_CONFIRM_DELETE_RECORDS = new PreferenceKey(
    PREFERENCE_PATH, "confirmDeleteRecords", DataTypes.BOOLEAN, false)//
      .setCategoryTitle("Layers");

  public static final PreferenceKey PREFERENCE_SHOW_ALL_RECORDS_ON_FILTER = new PreferenceKey(
    PREFERENCE_PATH, "showAllRecordViewOnFilter", DataTypes.BOOLEAN, true)//
      .setCategoryTitle("Layers");

  public static final PreferenceKey PREFERENCE_GENERALIZE_GEOMETRY_TOLERANCE = new PreferenceKey(
    PREFERENCE_PATH, "generalizeGeometryTolerance", DataTypes.DOUBLE, 0.2)//
      .setCategoryTitle("Layers");

  public static final String RECORD_CACHE_MODIFIED = "recordCacheModified";

  public static final String RECORD_DELETED_PERSISTED = "recordDeletedPersisted";

  public static final String RECORD_UPDATED = "recordUpdated";

  public static final String RECORDS_CHANGED = "recordsChanged";

  public static final String RECORDS_DELETED = "recordsDeleted";

  public static final String RECORDS_INSERTED = "recordsInserted";

  public static final String RECORDS_SELECTED = "recordsSelected";

  static {
    MenuFactory.addMenuInitializer(AbstractRecordLayer.class, (menu) -> {
      menu.setName("Layer");
      menu.addGroup(0, "table");
      menu.addGroup(2, "edit");
      menu.addGroup(3, "tools");
      menu.addGroup(4, "dnd");

      final Predicate<AbstractRecordLayer> exists = AbstractRecordLayer::isExists;

      menu.addMenuItem("table", -1, "View Records", "table_go", exists,
        AbstractRecordLayer::showRecordsTable, false);

      final Predicate<AbstractRecordLayer> hasSelectedRecords = AbstractRecordLayer::isHasSelectedRecords;
      final Predicate<AbstractRecordLayer> hasSelectedRecordsWithGeometry = AbstractRecordLayer::isHasSelectedRecordsWithGeometry;

      menu.addMenuItem("zoom", -1, "Zoom to Selected", "magnifier_zoom_selected",
        hasSelectedRecordsWithGeometry, AbstractRecordLayer::zoomToSelected, true);

      menu.addMenuItem("zoom", -1, "Pan to Selected", "pan_selected",
        hasSelectedRecordsWithGeometry, AbstractRecordLayer::panToSelected, true);

      final Predicate<AbstractRecordLayer> notReadOnly = ((Predicate<AbstractRecordLayer>)AbstractRecordLayer::isReadOnly)
        .negate();
      final Predicate<AbstractRecordLayer> canAdd = AbstractRecordLayer::isCanAddRecords;

      menu.addCheckboxMenuItem("edit", "Editable", "pencil", notReadOnly,
        AbstractRecordLayer::toggleEditable, AbstractRecordLayer::isEditable, false);

      menu.addMenuItem("edit", -1, "Save Changes", "table:save", AbstractLayer::isHasChanges,
        AbstractLayer::saveChanges, true);

      menu.addMenuItem("edit", -1, "Cancel Changes", "table_cancel", AbstractLayer::isHasChanges,
        AbstractRecordLayer::cancelChanges, true);

      menu.addMenuItem("edit", -1, "Add New Record", "table_row_insert", canAdd,
        AbstractRecordLayer::addNewRecord, false);

      menu.addComponentFactory("edit", EditRecordMenu.newSelectedRecords());

      menu.addMenuItem("edit", -1, "Delete Selected Records", "table_row_delete",
        hasSelectedRecords.and(AbstractRecordLayer::isCanDeleteRecords), layer -> {
          final List<LayerRecord> selectedRecords = layer.getSelectedRecords();
          layer.deleteRecordsWithConfirm(selectedRecords);
        }, true);

      menu.addMenuItem("edit", -1, "Merge Selected Records", "table_row_merge",
        AbstractRecordLayer::isCanMergeRecords, AbstractRecordLayer::mergeSelectedRecords, false);

      menu.addComponentFactory("tools", new RecordShortestPathMenu());

      menu.addMenuItem("dnd", -1, "Copy Selected Records", "page_copy", hasSelectedRecords,
        AbstractRecordLayer::copySelectedRecords, true);

      menu.addMenuItem("dnd", -1, "Paste New Records", "paste_plain",
        canAdd.and(AbstractRecordLayer::isCanPasteRecords), AbstractRecordLayer::pasteRecords,
        true);

      menu.addMenuItem("layer", 0, "Layer Style", "palette", AbstractRecordLayer::isHasGeometry,
        (final AbstractRecordLayer layer) -> layer.showProperties("Style"), false);

      PreferenceFields.addField("com.revolsys.gis", PREFERENCE_SHOW_ALL_RECORDS_ON_FILTER);
      PreferenceFields.addField("com.revolsys.gis", PREFERENCE_CONFIRM_DELETE_RECORDS);
      PreferenceFields.addField("com.revolsys.gis", PREFERENCE_GENERALIZE_GEOMETRY_TOLERANCE);
    });
  }

  public static void addVisibleLayers(final List<AbstractRecordLayer> layers,
    final LayerGroup group, final double scale) {
    if (group.isExists() && group.isVisible(scale)) {
      for (final Layer layer : group) {
        if (layer instanceof LayerGroup) {
          final LayerGroup layerGroup = (LayerGroup)layer;
          addVisibleLayers(layers, layerGroup, scale);
        } else if (layer instanceof AbstractRecordLayer) {
          if (layer.isExists() && layer.isVisible(scale)) {
            final AbstractRecordLayer recordLayer = (AbstractRecordLayer)layer;
            layers.add(recordLayer);
          }
        }
      }
    }
  }

  public static void exportRecords(final String baseName, final boolean hasGeometryField,
    final Consumer<File> exportAction) {
    final Predicate<RecordWriterFactory> filter = factory -> hasGeometryField
      || factory.isCustomFieldsSupported();
    SwingIo.exportToFile("Records", "com.revolsys.swing.map.table.export",
      RecordWriterFactory.class, filter, "tsv", true, baseName, exportAction);
  }

  public static void forEachSelectedRecords(final Layer layer,
    final Consumer<List<LayerRecord>> action) {
    if (layer instanceof LayerGroup) {
      final LayerGroup group = (LayerGroup)layer;
      for (final Layer childLayer : group) {
        forEachSelectedRecords(childLayer, action);
      }
    } else if (layer instanceof AbstractRecordLayer) {
      final AbstractRecordLayer recordLayer = (AbstractRecordLayer)layer;
      final List<LayerRecord> records = recordLayer.getSelectedRecords();
      if (!records.isEmpty()) {
        action.accept(records);
      }
    }
  }

  public static double getDefaultGeneralizeGeometryTolerance() {
    return Preferences.getValue("com.revolsys.gis", PREFERENCE_GENERALIZE_GEOMETRY_TOLERANCE);
  }

  public static List<AbstractRecordLayer> getVisibleLayers(final LayerGroup group,
    final double scale) {
    final List<AbstractRecordLayer> layers = new ArrayList<>();
    addVisibleLayers(layers, group, scale);
    return layers;
  }

  private boolean canAddRecords = true;

  private boolean canDeleteRecords = true;

  private boolean canEditRecords = true;

  private boolean canPasteRecords = true;

  private boolean confirmDeleteRecords;

  private final Map<String, Integer> fieldColumnWidths = new HashMap<>();

  private List<String> fieldNames = Collections.emptyList();

  private String fieldNamesSetName = ALL;

  private final List<String> fieldNamesSetNames = new ArrayList<>();

  private final Map<String, List<String>> fieldNamesSets = new HashMap<>();

  private Condition filter = Condition.ALL;

  private final List<Component> formComponents = new LinkedList<>();

  private final List<Record> formRecords = new LinkedList<>();

  private final List<Window> formWindows = new LinkedList<>();

  private final Set<LayerRecord> proxiedRecords = new HashSet<>();

  protected final List<RecordCache> recordCaches = new ArrayList<>();

  protected final RecordCache recordCacheDeletedInternal = newRecordCacheDo("deleted");

  protected final RecordCache recordCacheDeleted = addRecordCache(
    new RecordCacheDelegating(this.recordCacheDeletedInternal) {
      @Override
      public boolean removeContainsRecord(final LayerRecord record) {
        return false;
      }

      @Override
      public boolean removeRecord(final LayerRecord record) {
        return !super.containsRecord(record);
      }
    });

  protected final RecordCache recordCacheForm = newRecordCache("form");

  private final RecordCache recordCacheHighlighted = newRecordCache("highlighted");

  private final RecordCacheIndex recordCacheIndex = new RecordCacheIndex("index");

  private final RecordCache recordCacheModified = newRecordCache("modified");

  protected final RecordCache recordCacheNew = newRecordCache("new");

  private final RecordCacheIndex recordCacheSelected = new RecordCacheIndex("selected");

  private RecordDefinition recordDefinition;

  private RecordFactory<? extends LayerRecord> recordFactory = this::newLayerRecord;

  private LayerRecordMenu recordMenu;

  private boolean snapToAllLayers = true;

  private boolean useFieldTitles = true;

  private Set<String> userReadOnlyFieldNames = new LinkedHashSet<>();

  private String where;

  private Map<String, Condition> deleteRecordsBlockFilterByFieldName;

  protected AbstractRecordLayer(final String type) {
    super(type);
    setReadOnly(false);
    setSelectSupported(true);
    setQuerySupported(true);
    setRenderer(new GeometryStyleRecordLayerRenderer(this));
  }

  @Override
  public void activatePanelComponent(final Component component, final Map<String, Object> config) {
    super.activatePanelComponent(component, config);
    if (component instanceof RecordLayerTablePanel) {
      final RecordLayerTablePanel panel = (RecordLayerTablePanel)component;

      final String fieldFilterMode = Maps.getString(config, "fieldFilterMode");
      panel.setFieldFilterMode(fieldFilterMode);
    }
  }

  @Override
  public void addComplete(final AbstractOverlay overlay, final Geometry geometry) {
    if (geometry != null) {
      final RecordDefinition recordDefinition = getRecordDefinition();
      final String geometryFieldName = recordDefinition.getGeometryFieldName();
      final Map<String, Object> parameters = new HashMap<>();
      parameters.put(geometryFieldName, geometry);
      showAddForm(parameters);
    }
  }

  public void addDeleteRecordsBlockFilterByFieldName(final String fieldName, final String filter) {
    if (this.recordDefinition.hasField(fieldName)) {
      final String conditionSql = fieldName + " " + filter;
      final Condition condition = QueryValue.parseWhere(this.recordDefinition, conditionSql);
      final Map<String, Condition> conditionsByFieldName = getDeleteRecordsBlockFilterByFieldName();
      conditionsByFieldName.put(fieldName, condition);
    }
  }

  public void addHighlightedRecords(final Collection<? extends LayerRecord> records) {
    synchronized (this.recordCacheSelected.getRecordCacheSync()) {
      this.recordCacheHighlighted.addRecords(records);
    }
    fireHighlighted();
  }

  public void addHighlightedRecords(final LayerRecord... records) {
    addHighlightedRecords(Arrays.asList(records));
  }

  protected void addModifiedRecord(final LayerRecord record) {
    if (this.recordCacheModified.addRecord(record)) {
      firePropertyChange(RECORD_CACHE_MODIFIED, null, record.getRecordProxy());
      fireHasChangedRecords();
    }
  }

  public void addNewRecord() {
    final RecordDefinition recordDefinition = getRecordDefinition();
    final FieldDefinition geometryField = recordDefinition.getGeometryField();
    if (geometryField == null) {
      showAddForm(null);
    } else {
      final MapPanel map = getMapPanel();
      if (map != null) {
        final EditRecordGeometryOverlay addGeometryOverlay = map
          .getMapOverlay(EditRecordGeometryOverlay.class);
        synchronized (addGeometryOverlay) {
          clearSelectedRecords();
          addGeometryOverlay.addRecord(this, this);
        }
      }
    }
  }

  void addProxiedRecord(final LayerRecord record) {
    synchronized (this.proxiedRecords) {
      this.proxiedRecords.add(record);
    }
  }

  protected <RC extends RecordCache> RC addRecordCache(final RC recordCache) {
    this.recordCaches.add(recordCache);
    return recordCache;
  }

  @Override
  public int addRenderer(final LayerRenderer<?> child, final int index) {
    final AbstractRecordLayerRenderer oldRenderer = getRenderer();
    AbstractMultipleRecordLayerRenderer rendererGroup;
    if (oldRenderer instanceof AbstractMultipleRecordLayerRenderer) {
      rendererGroup = (AbstractMultipleRecordLayerRenderer)oldRenderer;
    } else {
      final AbstractRecordLayer layer = oldRenderer.getLayer();
      rendererGroup = new MultipleRecordRenderer(layer);
      rendererGroup.addRenderer(oldRenderer);
      setRenderer(rendererGroup);
    }
    if (index == 0) {
      rendererGroup.addRenderer(0, (AbstractRecordLayerRenderer)child);
      return 0;
    } else {
      rendererGroup.addRenderer((AbstractRecordLayerRenderer)child);
      return rendererGroup.getRendererCount() - 1;
    }
  }

  public boolean addSelectedRecords(final BoundingBox boundingBox) {
    if (isSelectable()) {
      final List<LayerRecord> records = getRecordsVisible(boundingBox);
      addSelectedRecords(records);
      postSelectByBoundingBox(records);
      return isHasSelectedRecordsWithGeometry();
    } else {
      return false;
    }
  }

  public void addSelectedRecords(final Collection<? extends LayerRecord> records) {
    final List<LayerRecord> newSelectedRecords = new ArrayList<>();
    synchronized (this.recordCacheSelected.getRecordCacheSync()) {
      for (final LayerRecord record : records) {
        if (this.recordCacheSelected.addRecord(record)) {
          newSelectedRecords.add(record);
        }
      }
    }
    firePropertyChange(RECORDS_SELECTED, null, newSelectedRecords);
    fireSelected();
  }

  public void addSelectedRecords(final LayerRecord... records) {
    addSelectedRecords(Arrays.asList(records));
  }

  public void addToIndex(final Collection<? extends LayerRecord> records) {
    this.recordCacheIndex.addRecords(records);
  }

  public void addToIndex(final LayerRecord record) {
    this.recordCacheIndex.addRecord(record);
  }

  public void addUserReadOnlyFieldNames(final Collection<String> userReadOnlyFieldNames) {
    if (userReadOnlyFieldNames != null) {
      this.userReadOnlyFieldNames.addAll(userReadOnlyFieldNames);
    }
  }

  public void cancelChanges() {
    try {
      synchronized (this.getSync()) {
        boolean cancelled = true;
        try (
          BaseCloseable eventsEnabled = eventsDisabled()) {
          cancelled &= internalCancelChanges(this.recordCacheNew);
          cancelled &= internalCancelChanges(this.recordCacheDeletedInternal);
          cancelled &= internalCancelChanges(this.recordCacheModified);
        } finally {
          fireRecordsChanged();
        }
        if (!cancelled) {
          Dialogs.showMessageDialog(
            "<html><p>There was an error cancelling changes for one or more records.</p>" + "<p>"
              + getPath() + "</p>" + "<p>Check the logging panel for details.</html>",
            "Error Cancelling Changes", JOptionPane.ERROR_MESSAGE);
        }
      }
    } finally {
      fireHasChangedRecords();
    }
  }

  public boolean canPasteGeometry() {
    if (isEditable()) {
      final Geometry geometry = getPasteGeometry(false);
      return geometry != null;
    } else {
      return false;
    }
  }

  public boolean canPasteRecordGeometry(final LayerRecord record) {
    if (isEditable()) {
      final Geometry geometry = getPasteRecordGeometry(record, false);
      return geometry != null;
    } else {
      return false;
    }
  }

  protected <LR extends LayerRecord> boolean checkBlockDeleteRecord(final LR record) {
    return !isDeleted(record);
  }

  public void clearHighlightedRecords() {
    this.recordCacheHighlighted.clearRecords();
    fireHighlighted();
  }

  protected void clearIndex() {
    this.recordCacheIndex.clearRecords();
  }

  public void clearSelectedRecords() {
    final List<LayerRecord> selectedRecords = getSelectedRecords();
    synchronized (this.recordCacheSelected.getRecordCacheSync()) {
      this.recordCacheSelected.clearRecords();
      this.recordCacheHighlighted.clearRecords();
    }
    firePropertyChange(RECORDS_SELECTED, selectedRecords, Collections.emptyList());
    fireSelected();
  }

  @Override
  public AbstractRecordLayer clone() {
    final MapEx config = toMap();
    return MapObjectFactory.toObject(config);
  }

  protected <LR extends LayerRecord> boolean confirmDeleteRecords(final List<LR> records,
    final Consumer<Collection<LR>> deleteAction) {
    return confirmDeleteRecords("", records, deleteAction);
  }

  protected <LR extends LayerRecord> boolean confirmDeleteRecords(final String suffix,
    final List<LR> records, final Consumer<Collection<LR>> deleteAction) {

    final Map<String, Condition> deleteRecordsBlockFilterByFieldName = getDeleteRecordsBlockFilterByFieldName();
    if (!deleteRecordsBlockFilterByFieldName.isEmpty()) {
      List<LR> blockedRecords = null;
      List<LR> otherRecords = null;
      int i = 0;
      for (final LR record : records) {
        if (isDeleteBlocked(suffix, record)) {
          if (blockedRecords == null) {
            blockedRecords = new ArrayList<>();
            otherRecords = new ArrayList<>();

            for (int j = 0; j < i; j++) {
              final LR otherRecord = records.get(j);
              otherRecords.add(otherRecord);
            }
          }
          blockedRecords.add(record);
        } else if (otherRecords != null) {
          otherRecords.add(record);
        }
        i++;
      }
      if (blockedRecords != null) {
        BlockDeleteRecords.showErrorDialog(this, blockedRecords, otherRecords, deleteAction);
        return false;
      }
    }
    boolean delete;
    final int recordCount = records.size();
    final boolean globalConfirmDeleteRecords = Preferences.getValue("com.revolsys.gis",
      PREFERENCE_CONFIRM_DELETE_RECORDS);
    if (globalConfirmDeleteRecords || this.confirmDeleteRecords) {
      final String message = "Delete " + recordCount + " records" + suffix
        + "? This action cannot be undone.";
      final String title = "Delete Records" + suffix;
      final int confirm = Dialogs.showConfirmDialog(message, title, JOptionPane.YES_NO_OPTION,
        JOptionPane.ERROR_MESSAGE);
      delete = confirm == JOptionPane.YES_OPTION;
    } else {
      delete = true;
    }
    if (delete) {
      deleteAction.accept(records);
    }
    return delete;
  }

  public void copyRecordGeometry(final LayerRecord record) {
    final Geometry geometry = record.getGeometry();
    if (geometry != null) {
      final StringTransferable transferable = new StringTransferable(DataFlavor.stringFlavor,
        geometry.toString());
      ClipboardUtil.setContents(transferable);
    }
  }

  public void copyRecordsToClipboard(final List<LayerRecord> records) {
    if (!records.isEmpty()) {
      final RecordDefinition recordDefinition = getRecordDefinition();
      final RecordReaderTransferable transferable = new RecordReaderTransferable(recordDefinition,
        records);
      ClipboardUtil.setContents(transferable);
    }
  }

  public void copyRecordToClipboard(final LayerRecord record) {
    copyRecordsToClipboard(Collections.singletonList(record));
  }

  public void copySelectedRecords() {
    final List<LayerRecord> selectedRecords = getSelectedRecords();
    copyRecordsToClipboard(selectedRecords);
  }

  @Override
  public void delete() {
    super.delete();
    for (final Window window : this.formWindows) {
      SwingUtil.dispose(window);

    }
    for (final Component form : this.formComponents) {
      if (form != null) {
        if (form instanceof LayerRecordForm) {
          final LayerRecordForm recordForm = (LayerRecordForm)form;
          Invoke.later(recordForm::destroy);
        }
      }
    }
    this.fieldNamesSetNames.clear();
    this.fieldNamesSets.clear();
    this.formRecords.clear();
    this.formComponents.clear();
    this.formWindows.clear();
    for (final RecordCache cache : this.recordCaches) {
      cache.clearRecords();
    }
    this.recordCaches.clear();
  }

  public boolean deleteRecord(final LayerRecord record) {
    if (record.getState() == RecordState.DELETED) {
      return false;
    } else {
      removeForms(Collections.singletonList(record));
      final List<LayerRecord> recordsDeleted = new ArrayList<>();
      final boolean deleted = deleteSingleRecordDo(record);
      if (deleted) {
        final LayerRecord recordProxy = record.getRecordProxy();
        recordsDeleted.add(recordProxy);
        deleteRecordsPost(recordsDeleted);
        return true;
      }
      return false;
    }
  }

  public void deleteRecordAndSaveChanges(final LayerRecord record) {
    deleteRecord(record);
    saveChanges(record);
  }

  protected boolean deleteRecordDo(final LayerRecord record) {
    final boolean isNew = isNew(record);
    if (!isNew) {
      this.recordCacheDeleted.addRecord(record);
    }
    record.setStateDeleted();
    return true;
  }

  public void deleteRecords(final Collection<? extends LayerRecord> records) {
    removeForms(records);
    final List<LayerRecord> recordsDeleted = new ArrayList<>();
    processTasks("Delete Records", records, (final LayerRecord record) -> {
      final boolean deleted = deleteSingleRecordDo(record);
      if (deleted) {
        final LayerRecord recordProxy = record.getRecordProxy();
        recordsDeleted.add(recordProxy);
      }
    }, monitor -> {
      deleteRecordsPost(recordsDeleted);
    });
  }

  protected void deleteRecordsPost(final List<LayerRecord> recordsDeleted) {
    if (!recordsDeleted.isEmpty()) {
      firePropertyChange(RECORDS_DELETED, null, recordsDeleted);
      fireHasChangedRecords();
    }
  }

  public boolean deleteRecordsWithConfirm(final List<? extends LayerRecord> records) {
    return confirmDeleteRecords(records, this::deleteRecords);
  }

  public boolean deleteRecordWithConfirm(final LayerRecord record) {
    final List<LayerRecord> records = Collections.singletonList(record);
    return deleteRecordsWithConfirm(records);
  }

  protected boolean deleteSingleRecordDo(final LayerRecord record) {
    final boolean isNewRecord = this.recordCacheNew.removeContainsRecord(record);
    removeRecordFromCache(record);
    if (isNewRecord) {
      record.setState(RecordState.DELETED);
      return true;
    } else if (isCanDeleteRecords()) {
      if (deleteRecordDo(record)) {
        return true;
      }
    }
    return false;
  }

  public void exportRecords(final Iterable<LayerRecord> records,
    final Predicate<? super LayerRecord> filter, final Collection<String> fieldNames,
    final Map<? extends CharSequence, Boolean> orderBy, final Object target) {
    if (Property.hasValue(records) && target != null) {
      final List<LayerRecord> exportRecords = Lists.toArray(records);

      Records.filterAndSort(exportRecords, filter, orderBy);

      if (!exportRecords.isEmpty()) {
        final RecordDefinition recordDefinition = newRecordDefinition(fieldNames);
        if (recordDefinition != null) {
          RecordIo.copyRecords(recordDefinition, exportRecords, target);
        }
      }
    }
  }

  public void exportRecords(final Query query, final Collection<String> fieldNames,
    final Object target) {
    final RecordDefinition recordDefinition = newRecordDefinition(fieldNames);
    if (recordDefinition != null) {
      final Resource resource = Resource.getResource(target);
      if (resource instanceof PathResource) {
        final PathResource pathResource = (PathResource)resource;
        pathResource.deleteDirectory();
      }
      try (
        RecordWriter writer = RecordWriter.newRecordWriter(recordDefinition, resource)) {
        forEachRecordInternal(query, writer::write);
      }
    }
  }

  public boolean filterTestModified(final Condition filter, final LayerRecord modifiedRecord) {
    boolean accept = false;
    if (filter.test(modifiedRecord)) {
      if (!filter.test(modifiedRecord.getOriginalRecord())) {
        accept = true;
      }
    }
    return accept;
  }

  public void fireHasChangedRecords() {
    final boolean hasChangedRecords = isHasChangedRecords();
    firePropertyChange("hasChangedRecords", !hasChangedRecords, hasChangedRecords);
  }

  protected void fireHighlighted() {
    final int highlightedCount = getHighlightedCount();
    final boolean highlighted = highlightedCount > 0;
    firePropertyChange("hasHighlightedRecords", !highlighted, highlighted);
    firePropertyChange("highlightedCount", -1, highlightedCount);
  }

  protected void fireRecordInserted(final LayerRecord record) {
    final List<LayerRecord> records = Collections.singletonList(record);
    firePropertyChange(RECORDS_INSERTED, null, records);
  }

  public void fireRecordsChanged() {
    firePropertyChange(RECORDS_CHANGED, false, true);
  }

  protected void fireSelected() {
    final int selectionCount = getSelectedRecordsCount();
    final boolean selected = selectionCount > 0;
    firePropertyChange("hasSelectedRecords", !selected, selected);
    firePropertyChange("selectionCount", -1, selectionCount);
  }

  public void forEachRecord(final Iterable<LayerRecord> records,
    final Predicate<? super LayerRecord> filter, final Map<? extends CharSequence, Boolean> orderBy,
    final Consumer<? super LayerRecord> action) {
    try {
      if (Property.hasValue(records) && action != null) {
        final List<LayerRecord> exportRecords = Lists.toArray(records);

        Records.filterAndSort(exportRecords, filter, orderBy);

        if (!exportRecords.isEmpty()) {
          exportRecords.forEach(action);
        }
      }
    } catch (final CancellationException e) {
    }
  }

  public void forEachRecord(final Query query, final Consumer<? super LayerRecord> consumer) {
    forEachRecordInternal(query, (record) -> {
      final LayerRecord proxyRecord = record.getRecordProxy();
      consumer.accept(proxyRecord);
    });
  }

  protected void forEachRecordCache(final Consumer<RecordCache> action) {
    synchronized (getSync()) {
      for (final RecordCache recordCache : this.recordCaches) {
        action.accept(recordCache);
      }
    }
  }

  public void forEachRecordChanged(final Query query,
    final Consumer<? super LayerRecord> consumer) {
    final List<LayerRecord> records = getRecordsChanged();
    query.forEachRecord(records, consumer);
  }

  protected void forEachRecordInternal(final Query query,
    final Consumer<? super LayerRecord> consumer) {
  }

  public <R extends Record> void forEachSelectedRecord(final Consumer<R> action) {
    this.recordCacheSelected.forEachRecord(action);
  }

  @SuppressWarnings("unchecked")
  protected <V extends LayerRecord> V getCachedRecord(final Identifier identifier) {
    return (V)getRecordById(identifier);
  }

  @SuppressWarnings("unchecked")
  protected <V extends LayerRecord> V getCachedRecord(final Record record) {
    return (V)record;
  }

  @Override
  public Collection<Class<?>> getChildClasses() {
    return Collections.<Class<?>> singleton(AbstractRecordLayerRenderer.class);
  }

  public Comparator<?> getComparator(final String fieldName) {
    final FieldDefinition field = getFieldDefinition(fieldName);
    if (field == null) {
      return CompareUtil.INSTANCE;
    } else {
      final Class<?> typeClass = field.getTypeClass();
      return CompareUtil.getComparator(typeClass);
    }
  }

  public Set<String> getDeleteRecordsBlockFieldNames() {
    final Map<String, Condition> conditionsByFieldName = getDeleteRecordsBlockFilterByFieldName();
    return conditionsByFieldName.keySet();
  }

  public Map<String, Condition> getDeleteRecordsBlockFilterByFieldName() {
    synchronized (getSync()) {

      if (this.deleteRecordsBlockFilterByFieldName == null) {
        this.deleteRecordsBlockFilterByFieldName = new LinkedHashMap<>();
        final Map<String, String> filterByFieldName = getProperty("deleteRecordsBlockFieldFilters");
        if (filterByFieldName != null) {
          for (final String fieldName : filterByFieldName.keySet()) {
            final String filter = filterByFieldName.get(fieldName);
            addDeleteRecordsBlockFilterByFieldName(fieldName, filter);
          }
        }
      }
      return this.deleteRecordsBlockFilterByFieldName;
    }
  }

  public int getFieldColumnWidth(final String fieldName) {
    return Maps.get(this.fieldColumnWidths, fieldName, -1);
  }

  public Map<String, Integer> getFieldColumnWidths() {
    return this.fieldColumnWidths;
  }

  @Override
  public List<String> getFieldNames() {
    return new ArrayList<>(this.fieldNames);
  }

  public List<String> getFieldNamesSet() {
    return getFieldNamesSet(this.fieldNamesSetName);
  }

  public List<String> getFieldNamesSet(final String fieldNamesSetName) {
    if (Property.hasValue(fieldNamesSetName)) {
      List<String> fieldNames = this.fieldNamesSets.get(fieldNamesSetName.toUpperCase());
      if (Property.hasValue(fieldNames)) {
        fieldNames = new ArrayList<>(fieldNames);
        if (Property.hasValue(this.fieldNames)) {
          fieldNames.retainAll(this.fieldNames);
        }
        return fieldNames;
      }
    }
    return getFieldNames();
  }

  public String getFieldNamesSetName() {
    return this.fieldNamesSetName;
  }

  public List<String> getFieldNamesSetNames() {
    return new ArrayList<>(this.fieldNamesSetNames);
  }

  public Map<String, List<String>> getFieldNamesSets() {
    final Map<String, List<String>> fieldNamesSets = new LinkedHashMap<>();
    for (final String fieldNamesSetName : getFieldNamesSetNames()) {
      final List<String> fieldNames = getFieldNamesSet(fieldNamesSetName);
      fieldNamesSets.put(fieldNamesSetName, fieldNames);
    }
    return fieldNamesSets;
  }

  public Condition getFilter() {
    if (Property.hasValue(this.where)) {
      final RecordDefinition recordDefinition = getRecordDefinition();
      if (recordDefinition == null) {
        return Condition.ALL;
      } else {
        this.filter = QueryValue.parseWhere(recordDefinition, this.where);
        this.where = null;
      }
    }
    return this.filter;
  }

  public double getGeneralizeGeometryTolerance() {
    return getDefaultGeneralizeGeometryTolerance();
  }

  public DataType getGeometryType() {
    final RecordDefinition recordDefinition = getRecordDefinition();
    if (recordDefinition == null) {
      return null;
    } else {
      final FieldDefinition geometryField = recordDefinition.getGeometryField();
      if (geometryField == null) {
        return null;
      } else {
        return geometryField.getDataType();
      }
    }
  }

  public BoundingBox getHighlightedBoundingBox() {
    final GeometryFactory geometryFactory = getGeometryFactory();
    if (geometryFactory == null) {
      return BoundingBox.empty();
    } else {
      final Collection<LayerRecord> records = getHighlightedRecords();
      return BoundingBox.bboxNew(records);
    }
  }

  public int getHighlightedCount() {
    return getRecordCountCached(this.recordCacheHighlighted);
  }

  public Collection<LayerRecord> getHighlightedRecords() {
    return this.recordCacheHighlighted.getRecords();
  }

  @Override
  public String getIdFieldName() {
    return getRecordDefinition().getIdFieldName();
  }

  @SuppressWarnings("unchecked")
  public Set<String> getIgnorePasteFieldNames() {
    final Set<String> ignoreFieldNames = Sets
      .newHash((Collection<String>)getProperty("ignorePasteFields"));
    ignoreFieldNames.addAll(getRecordDefinition().getIdFieldNames());
    return ignoreFieldNames;
  }

  public MenuFactory getMenuFactory(final LayerRecord record) {
    return null;
  }

  public List<LayerRecord> getMergeableSelectedRecords() {
    final GeometryFactory geometryFactory = getGeometryFactory();
    if (geometryFactory == null) {
      return new ArrayList<>();
    } else {
      final List<LayerRecord> selectedRecords = getSelectedRecords();
      for (final ListIterator<LayerRecord> iterator = selectedRecords.listIterator(); iterator
        .hasNext();) {
        final LayerRecord record = iterator.next();
        if (record == null || isDeleted(record)) {
          iterator.remove();
        } else {
          Geometry geometry = record.getGeometry();
          geometry = geometryFactory.geometry(LineString.class, geometry);
          if (!(geometry instanceof LineString)) {
            iterator.remove();
          }
        }
      }
      return selectedRecords;
    }
  }

  /**
   * Get a record containing the values of the two records if they can be
   * merged. The new record is not a layer data object so would need to be
   * added, likewise the old records are not removed so they would need to be
   * deleted.
   *
   * @param point
   * @param record1
   * @param record2
   * @return
   */
  public Record getMergedRecord(final Point point, final Record record1, final Record record2) {
    if (record1 == record2) {
      return record1;
    } else {
      int compare = 0;
      for (final String idFieldName : getIdFieldNames()) {
        final Object id1 = record1.getValue(idFieldName);
        final Object id2 = record2.getValue(idFieldName);
        if (id1 == null) {
          if (id2 != null) {
            compare = 1;
          }
        } else if (id2 == null) {
          compare = -1;
        } else {
          compare = CompareUtil.compare(id1, id2);
        }
        if (compare != 0) {
          break;
        }
      }
      if (compare == 0) {
        final Geometry geometry1 = record1.getGeometry();
        final Geometry geometry2 = record2.getGeometry();
        final double length1 = geometry1.getLength();
        final double length2 = geometry2.getLength();
        if (length1 == length2) {
          compare = Integer.compare(System.identityHashCode(record1),
            System.identityHashCode(record2));
        } else if (length1 > length2) {
          compare = -1;
        } else {
          compare = 1;
        }
      }
      if (compare > 0) {
        return getMergedRecord(point, record2, record1);
      } else {
        final DirectionalFields property = DirectionalFields.getProperty(getRecordDefinition());
        final Map<String, Object> newValues = property.getMergedMap(point, record1, record2);
        for (final String idFieldName : getIdFieldNames()) {
          newValues.remove(idFieldName);
        }
        return new ArrayRecord(getRecordDefinition(), newValues);
      }
    }
  }

  public Geometry getPasteGeometry(final boolean alert) {
    try {
      final RecordDefinition recordDefinition = getRecordDefinition();
      final FieldDefinition geometryField = recordDefinition.getGeometryField();
      if (geometryField != null) {
        Geometry geometry = null;
        DataType geometryDataType = null;
        Class<?> layerGeometryClass = null;
        final GeometryFactory geometryFactory = getGeometryFactory();
        geometryDataType = geometryField.getDataType();
        layerGeometryClass = geometryDataType.getJavaClass();
        RecordReader reader = ClipboardUtil
          .getContents(RecordReaderTransferable.RECORD_READER_FLAVOR);
        if (reader == null) {
          final String string = ClipboardUtil.getContents(DataFlavor.stringFlavor);
          if (Property.hasValue(string)) {
            try {
              geometry = geometryFactory.geometry(string);
              geometry = geometryFactory.geometry(layerGeometryClass, geometry);
              if (geometry != null) {
                return geometry;
              }
            } catch (final Throwable e) {
            }
            final Resource resource = new ByteArrayResource("t.csv", string);
            reader = RecordReader.newRecordReader(resource);
          } else {
            return null;
          }
        }
        if (reader != null) {
          try {

            for (final Record sourceRecord : reader) {
              if (geometry == null) {
                final Geometry sourceGeometry = sourceRecord.getGeometry();
                if (sourceGeometry == null) {
                  if (alert) {
                    Dialogs.showMessageDialog(
                      "Clipboard does not contain a record with a geometry.", "Paste Geometry",
                      JOptionPane.ERROR_MESSAGE);
                  }
                  return null;
                }
                geometry = geometryFactory.geometry(layerGeometryClass, sourceGeometry);
                if (geometry == null) {
                  if (alert) {
                    Dialogs.showMessageDialog(
                      "Clipboard should contain a record with a " + geometryDataType + " not a "
                        + sourceGeometry.getGeometryType() + ".",
                      "Paste Geometry", JOptionPane.ERROR_MESSAGE);
                  }
                  return null;
                }
              } else {
                if (alert) {
                  Dialogs.showMessageDialog(
                    "Clipboard contains more than one record. Copy a single record.",
                    "Paste Geometry", JOptionPane.ERROR_MESSAGE);
                }
                return null;
              }
            }
          } finally {
            FileUtil.closeSilent(reader);
          }
          if (geometry == null) {
            if (alert) {
              Dialogs.showMessageDialog("Clipboard does not contain a record with a geometry.",
                "Paste Geometry", JOptionPane.ERROR_MESSAGE);
            }
          } else if (geometry.isEmpty()) {
            if (alert) {
              Dialogs.showMessageDialog("Clipboard contains an empty geometry.", "Paste Geometry",
                JOptionPane.ERROR_MESSAGE);
            }
            return null;
          } else {
            return geometry;
          }
        }
      }
      return null;
    } catch (final Throwable t) {
      return null;
    }
  }

  public MapEx getPasteNewValues(final Record sourceRecord) {
    final RecordDefinition recordDefinition = getRecordDefinition();
    final Set<String> ignoreFieldNames = getIgnorePasteFieldNames();
    final MapEx newValues = new LinkedHashMapEx();
    for (final FieldDefinition field : recordDefinition.getFields()) {
      final String fieldName = field.getName();
      if (!ignoreFieldNames.contains(fieldName)) {
        final Object value = sourceRecord.getValue(fieldName);
        if (value != null) {
          final Object newValue = field.toFieldValue(value);

          newValues.put(fieldName, newValue);
        }
      }
    }
    final GeometryFactory geometryFactory = getGeometryFactory();
    boolean first = true;
    for (final FieldDefinition geometryField : recordDefinition.getGeometryFields()) {
      final String name = geometryField.getName();
      final Object value = newValues.get(name);
      if (value == null) {
        if (first) {
          Geometry geometry = sourceRecord.getGeometry();
          geometry = geometryField.toFieldValue(geometry);
          if (geometry != null) {
            geometry = geometry.convertGeometry(geometryFactory);
            newValues.put(name, geometry);
          }
        }
      } else if (value instanceof Geometry) {
        Geometry geometry = (Geometry)value;
        geometry = geometry.convertGeometry(geometryFactory);
        newValues.put(name, geometry);
      }
      first = false;
    }
    return newValues;
  }

  protected Geometry getPasteRecordGeometry(final LayerRecord record, final boolean alert) {
    if (record == null) {
      return null;
    } else {
      return getPasteGeometry(alert);
    }
  }

  public List<Geometry> getPasteWktGeometries() {
    try {
      final RecordDefinition recordDefinition = getRecordDefinition();
      final FieldDefinition geometryField = recordDefinition.getGeometryField();
      if (geometryField != null) {
        final String string = ClipboardUtil.getContents(DataFlavor.stringFlavor);
        if (Property.hasValue(string)) {
          final Resource wktResource = new ByteArrayResource("t.wkt", string);
          final GeometryFactory geometryFactory = getGeometryFactory();
          try (
            GeometryReader geometryReader = GeometryReader.newGeometryReader(wktResource,
              geometryFactory)) {
            final DataType geometryDataType = geometryField.getDataType();
            final Class<?> layerGeometryClass = geometryDataType.getJavaClass();
            final List<Geometry> geometries = new ArrayList<>();
            for (Geometry geometry : geometryReader) {
              geometry = geometryFactory.geometry(layerGeometryClass, geometry);
              if (geometry != null && !geometry.isEmpty()) {
                geometries.add(geometry);
              }
            }
            return geometries;
          } catch (final Exception e) {
          }
        }

      }
    } catch (final Throwable t) {
    }
    return Collections.emptyList();
  }

  @Override
  public PathName getPathName() {
    final RecordDefinition recordDefinition = getRecordDefinition();
    if (recordDefinition == null) {
      return null;
    } else {
      return recordDefinition.getPathName();
    }
  }

  public LayerRecord getProxiedRecord(LayerRecord record) {
    if (record != null && record.getLayer() == this) {
      if (record instanceof AbstractProxyLayerRecord) {
        final AbstractProxyLayerRecord proxy = (AbstractProxyLayerRecord)record;
        record = proxy.getRecordProxied();
      }
      return record;
    } else {
      return null;
    }
  }

  public List<LayerRecord> getProxiedRecords() {
    return new ArrayList<>(this.proxiedRecords);
  }

  public final Query getQuery() {
    final RecordDefinition recordDefinition = getRecordDefinition();
    final Condition whereCondition = getFilter();
    return new Query(recordDefinition, whereCondition);
  }

  public LayerRecord getRecord(final Identifier identifier) {
    final RecordDefinition recordDefinition = getRecordDefinition();
    if (recordDefinition != null) {
      final List<FieldDefinition> idFieldDefinitions = recordDefinition.getIdFields();
      if (idFieldDefinitions.isEmpty()) {
        final Query query = new Query(recordDefinition, Q.equalId(idFieldDefinitions, identifier));
        for (final LayerRecord record : getRecords(query)) {
          return record;
        }
      }
    }
    return null;
  }

  public LayerRecord getRecord(final int row) {
    throw new UnsupportedOperationException();
  }

  public LayerRecord getRecordById(final Identifier identifier) {
    final RecordDefinition recordDefinition = getRecordDefinition();
    if (recordDefinition != null) {
      final List<FieldDefinition> idFieldDefinitions = recordDefinition.getIdFields();
      if (!idFieldDefinitions.isEmpty()) {
        final Query query = new Query(recordDefinition, Q.equalId(idFieldDefinitions, identifier));
        for (final LayerRecord record : getRecords(query)) {
          return record;
        }
      }
    }
    return null;
  }

  /**
   * Get the record count including any pending changes.
   *
   * @return
   */
  public int getRecordCount() {
    return getRecordCountPersisted() + getRecordCountNew() - getRecordCountDeleted();
  }

  public int getRecordCount(final Query query) {
    return 0;
  }

  protected int getRecordCountCached(final RecordCache recordCache) {
    return recordCache.getSize();
  }

  public int getRecordCountDeleted() {
    return getRecordCountCached(this.recordCacheDeleted);
  }

  public int getRecordCountModified() {
    return getRecordCountCached(this.recordCacheModified);
  }

  public int getRecordCountNew() {
    return getRecordCountCached(this.recordCacheNew);
  }

  public int getRecordCountPersisted() {
    return 0;
  }

  public int getRecordCountPersisted(final Query query) {
    return getRecordCount(query);
  }

  @Override
  public RecordDefinition getRecordDefinition() {
    return this.recordDefinition;
  }

  @SuppressWarnings({
    "unchecked"
  })
  @Override
  public <R extends Record> RecordFactory<R> getRecordFactory() {
    return (RecordFactory<R>)this.recordFactory;
  }

  @SuppressWarnings("unchecked")
  @Override
  public <L extends AbstractRecordLayer> L getRecordLayer() {
    return (L)this;
  }

  public LayerRecordMenu getRecordMenu() {
    return this.recordMenu;
  }

  public LayerRecordMenu getRecordMenu(final LayerRecord record) {
    if (isLayerRecord(record)) {
      LayerRecordMenu.setEventRecord(record);
      return this.recordMenu;
    }
    return null;
  }

  public List<LayerRecord> getRecords() {
    throw new UnsupportedOperationException();
  }

  public <R extends LayerRecord> List<R> getRecords(BoundingBox boundingBox) {
    if (hasGeometryField()) {
      boundingBox = convertBoundingBox(boundingBox);
      if (Property.hasValue(boundingBox)) {
        final List<R> records = getRecordsIndex(boundingBox);
        return records;
      }
    }
    return Collections.emptyList();
  }

  @SuppressWarnings("unchecked")
  public <R extends LayerRecord> List<R> getRecords(Geometry geometry, final double distance) {
    if (geometry == null || !hasGeometryField()) {
      return new ArrayList<>();
    } else {
      geometry = convertGeometry(geometry);
      return (List<R>)this.recordCacheIndex.getRecordsDistance(geometry, distance);
    }
  }

  @SuppressWarnings("unchecked")
  public <R extends LayerRecord> List<R> getRecords(final Query query) {
    final List<R> records = new ArrayList<>();
    final Consumer<LayerRecord> action = (Consumer<LayerRecord>)(Consumer<R>)records::add;
    forEachRecord(query, action);
    return records;
  }

  public List<LayerRecord> getRecordsBackground(final ViewportCacheBoundingBox cache,
    final BoundingBox boundingBox) {
    return getRecords(boundingBox);
  }

  public <R extends LayerRecord> List<R> getRecordsChanged() {
    synchronized (getSync()) {
      final List<R> records = new ArrayList<>();
      records.addAll(getRecordsNew());
      records.addAll(getRecordsModified());
      records.addAll(getRecordsDeleted());
      return records;
    }
  }

  public <R extends LayerRecord> List<R> getRecordsDeleted() {
    return this.recordCacheDeleted.getRecords();
  }

  protected <R extends LayerRecord> List<R> getRecordsIndex(final BoundingBox boundingBox) {
    return this.recordCacheIndex.getRecords(boundingBox);
  }

  public <R extends LayerRecord> Collection<R> getRecordsModified() {
    return this.recordCacheModified.getRecords();
  }

  public <R extends LayerRecord> List<R> getRecordsNew() {
    return this.recordCacheNew.getRecords();
  }

  /**
   * Query the underlying record store to return those records that have been
   * saved that match the query.
   *
   * @param query
   * @return The records.
   */
  public <R extends LayerRecord> List<R> getRecordsPersisted(final Query query) {
    return getRecords(query);
  }

  protected List<LayerRecord> getRecordsVisible(final BoundingBox boundingBox) {
    final List<LayerRecord> records = getRecordsVisibleDo(boundingBox);
    for (final Iterator<LayerRecord> iterator = records.iterator(); iterator.hasNext();) {
      final LayerRecord layerRecord = iterator.next();
      if (!isVisible(layerRecord) || isDeleted(layerRecord)
        || !layerRecord.getGeometry().intersectsBbox(boundingBox)) {
        iterator.remove();
      }
    }
    return records;
  }

  protected List<LayerRecord> getRecordsVisibleDo(final BoundingBox boundingBox) {
    return getRecords(boundingBox);
  }

  @Override
  public BoundingBox getSelectedBoundingBox() {
    final BoundingBoxEditor boundingBox = super.getSelectedBoundingBox().bboxEditor();
    forEachSelectedRecord(boundingBox::addBbox);
    return boundingBox.getBoundingBox();
  }

  public List<LayerRecord> getSelectedRecords() {
    return this.recordCacheSelected.getRecords();
  }

  public List<LayerRecord> getSelectedRecords(final BoundingBox boundingBox) {
    return this.recordCacheSelected.getRecords(boundingBox);
  }

  public int getSelectedRecordsCount() {
    return getRecordCountCached(this.recordCacheSelected);
  }

  public Collection<String> getSnapLayerPaths() {
    return getProperty("snapLayers", Collections.<String> emptyList());
  }

  public Collection<String> getUserReadOnlyFieldNames() {
    return Collections.unmodifiableSet(this.userReadOnlyFieldNames);
  }

  public Object getValidSearchValue(final FieldDefinition field, final Object fieldValue) {
    try {
      return field.toFieldValueException(fieldValue);
    } catch (final Throwable t) {
      return null;
    }
  }

  public String getWhere() {
    if (Property.isEmpty(this.filter)) {
      return this.where;
    } else {
      return this.filter.toFormattedString();
    }
  }

  public boolean hasFieldNamesSet(final String fieldNamesSetName) {
    if (Property.hasValue(fieldNamesSetName)) {
      final List<String> fieldNames = this.fieldNamesSets.get(fieldNamesSetName.toUpperCase());
      if (Property.hasValue(fieldNames)) {
        return true;
      }
    }
    return false;
  }

  public boolean hasGeometryField() {
    final RecordDefinition recordDefinition = getRecordDefinition();
    if (recordDefinition == null) {
      return false;
    } else {
      return recordDefinition.getGeometryField() != null;
    }
  }

  protected boolean hasPermission(final String permission) {
    if (this.recordDefinition == null) {
      return true;
    } else {
      final Collection<String> permissions = this.recordDefinition.getProperty("permissions");
      if (permissions == null) {
        return true;
      } else {
        return permissions.contains(permission);
      }
    }
  }

  protected void initEditRecordsMenu(final EditRecordMenu editMenu) {
    final RecordDefinition recordDefinition = getRecordDefinition();

    if (recordDefinition.hasGeometryField()) {
      final EnableCheck editableEnableCheck = this::isEditable;

      final DataType geometryDataType = recordDefinition.getGeometryField().getDataType();
      if (geometryDataType == GeometryDataTypes.LINE_STRING
        || geometryDataType == GeometryDataTypes.MULTI_LINE_STRING) {
        final Consumer<Record> reverseGeometryConsumer = DirectionalFields::reverseGeometryRecord;
        if (DirectionalFields.getProperty(recordDefinition).hasDirectionalFields()) {
          final Consumer<Record> reverse = DirectionalFields::reverseRecord;
          editMenu.addMenuItemRecord("geometry", LayerRecordForm.FLIP_RECORD_NAME,
            LayerRecordForm.FLIP_RECORD_ICON, editableEnableCheck, reverse);

          editMenu.addMenuItemRecord("geometry", LayerRecordForm.FLIP_LINE_ORIENTATION_NAME,
            LayerRecordForm.FLIP_LINE_ORIENTATION_ICON, editableEnableCheck,
            reverseGeometryConsumer);

          final Consumer<Record> reverseFieldValues = DirectionalFields::reverseFieldValuesRecord;
          editMenu.addMenuItemRecord("geometry", LayerRecordForm.FLIP_FIELDS_NAME,
            LayerRecordForm.FLIP_FIELDS_ICON, editableEnableCheck, reverseFieldValues);
        } else {
          editMenu.addMenuItemRecord("geometry", "Flip Line Orientation", "flip_line",
            editableEnableCheck, reverseGeometryConsumer);
        }
      }
      if (!(geometryDataType == GeometryDataTypes.POINT
        || geometryDataType == GeometryDataTypes.MULTI_POINT)) {
        editMenu.addMenuItemRecords("geometry", "Generalize Vertices", "generalize_line",
          editableEnableCheck, RecordLayerActions::generalize);
      }
    }
  }

  @Override
  protected void initializeMenuExpressions(final List<String> menuInitializerExpressions) {
    if (this.recordDefinition != null) {
      for (final String menuInitializerExpression : this.recordDefinition
        .getProperty("menuInitializerExpressions", Collections.<String> emptyList())) {
        if (Property.hasValue(menuInitializerExpression)) {
          if (!menuInitializerExpressions.contains(menuInitializerExpression)) {
            menuInitializerExpressions.add(menuInitializerExpression);
          }
        }
      }
    }
    super.initializeMenuExpressions(menuInitializerExpressions);
  }

  @Override
  protected void initializeMenus() {
    final LayerRecordMenu recordMenu = new LayerRecordMenu(this);
    this.recordMenu = recordMenu;
    initRecordMenu(recordMenu, this.recordDefinition);
    super.initializeMenus();
  }

  @Override
  protected EvaluationContext initializeMenusContext(final MenuFactory layerMenu) {
    final EvaluationContext context = super.initializeMenusContext(layerMenu);
    context.setVariable("recordMenu", this.recordMenu);
    context.setVariable("recordDefinition", this.recordDefinition);
    return context;
  }

  protected void initRecordMenu(final LayerRecordMenu menu,
    final RecordDefinition recordDefinition) {
    if (recordDefinition != null) {
      final boolean hasGeometry = recordDefinition.hasGeometryField();

      final Predicate<LayerRecord> modified = LayerRecord::isModified;
      final Predicate<LayerRecord> notDeleted = ((Predicate<LayerRecord>)this::isDeleted).negate();
      final Predicate<LayerRecord> modifiedOrDeleted = modified.or(LayerRecord::isDeleted);

      menu.addGroup(0, "default");
      menu.addGroup(1, "record");
      menu.addGroup(2, "dnd");

      final MenuFactory layerMenuFactory = MenuFactory.findMenu(this);
      if (layerMenuFactory != null) {
        menu.addComponentFactory("default", 0, new WrappedMenuFactory("Layer", this::getMenu));
      }

      menu.addMenuItem("record", "View/Edit Record", "table:edit", notDeleted, this::showForm);

      if (hasGeometry) {
        menu.addMenuItem("record", "Zoom to Record", "magnifier_zoom_selected", notDeleted,
          this::zoomToRecord);
        menu.addMenuItem("record", "Pan to Record", "pan_selected", notDeleted, (record) -> {
          final MapPanel mapPanel = getMapPanel();
          if (mapPanel != null) {
            mapPanel.panToRecord(record);
          }
        });
        final EditRecordMenu editRecordMenu = EditRecordMenu.newSingleRecord();
        menu.addComponentFactory("record", editRecordMenu);
      }
      menu.addMenuItem("record", "Delete Record", "table_row_delete", LayerRecord::isDeletable,
        this::deleteRecordWithConfirm);

      menu.addMenuItem("record", "Revert Record", "arrow_revert", modifiedOrDeleted,
        LayerRecord::revertChanges);

      final Predicate<LayerRecord> hasModifiedEmptyFields = LayerRecord::isHasModifiedEmptyFields;
      menu.addMenuItem("record", "Revert Empty Fields", "field_empty_revert",
        hasModifiedEmptyFields, LayerRecord::revertEmptyFields);

      ShortestPathOverlay.initMenuItems(this, menu);

      menu.addMenuItem("dnd", "Copy Record", "page_copy", this::copyRecordToClipboard);

      if (hasGeometry) {
        menu.addMenuItem("dnd", "Paste Geometry", "geometry_paste", this::canPasteRecordGeometry,
          this::pasteRecordGeometry);
      }
    }
  }

  /**
   * Revert the values of the record to the last values loaded from the database
   *
   * @param record
   */
  protected void internalCancelChanges(final LayerRecord record) {
    if (record != null) {
      record.cancelChanges();
    }
  }

  /**
   * Cancel changes for one of the lists of changes {@link #deletedRecords},
   * {@link #newRecords}, {@link #modifiedRecords}.
   *
   * @param cache
   */
  private boolean internalCancelChanges(final RecordCache cache) {
    boolean cancelled = true;
    for (final LayerRecord record : cache.getRecords()) {
      try {
        cache.removeRecord(record);
        if (cache == this.recordCacheNew) {
          removeRecordFromCache(record);
          record.setState(RecordState.DELETED);
        } else {
          internalCancelChanges(record);
          addToIndex(record);
        }
      } catch (final Throwable e) {
        Logs.error(this, "Unable to cancel changes.\n" + record, e);
        cancelled = false;
      }
    }
    return cancelled;
  }

  protected boolean internalIsDeleted(final LayerRecord record) {
    if (record == null) {
      return false;
    } else if (record.getState() == RecordState.DELETED) {
      return true;
    } else {
      return this.recordCacheDeleted.containsRecord(record);
    }
  }

  /**
   * Revert the values of the record to the last values loaded from the database
   *
   * @param record
   */
  protected LayerRecord internalPostSaveChanges(final LayerRecord record) {
    if (record != null) {
      record.clearChanges();
      return record;
    }
    return null;
  }

  protected boolean internalSaveChanges(final RecordLayerErrors errors, final LayerRecord record) {
    final RecordState originalState = record.getState();
    final LayerRecord layerRecord = getProxiedRecord(record);
    if (layerRecord == null) {
      return true;
    } else {
      final boolean saved = saveChangesDo(errors, layerRecord);
      if (saved) {
        postSaveChanges(originalState, layerRecord);
      }
      return saved;
    }
  }

  public boolean isCanAddRecords() {
    return !super.isReadOnly() && isEditable() && this.canAddRecords && hasPermission("INSERT");
  }

  public boolean isCanDeleteRecords() {
    return !super.isReadOnly() && isEditable() && this.canDeleteRecords && hasPermission("DELETE");
  }

  public boolean isCanEditRecords() {
    return !super.isReadOnly() && isEditable() && this.canEditRecords && hasPermission("UPDATE");
  }

  public boolean isCanMergeRecords() {
    if (isCanAddRecords()) {
      if (isCanDeleteRecords()) {
        final DataType geometryType = getGeometryType();
        if (GeometryDataTypes.POINT.equals(geometryType)) {
          return false;
        } else if (GeometryDataTypes.MULTI_POINT.equals(geometryType)) {
          return false;
        } else if (GeometryDataTypes.POLYGON.equals(geometryType)) {
          return false;
        } else if (GeometryDataTypes.MULTI_POLYGON.equals(geometryType)) {
          return false;
        }
        final List<LayerRecord> mergeableSelectedRecords = getMergeableSelectedRecords();
        if (mergeableSelectedRecords.size() > 1) {
          return true;
        }
      }
    }
    return false;
  }

  public boolean isCanPasteRecords() {
    if (isExists()) {
      if (!this.canPasteRecords) {
        return false;
      } else if (super.isReadOnly()) {
        return false;
      } else if (!super.isEditable()) {
        return false;
      } else if (ClipboardUtil
        .isDataFlavorAvailable(RecordReaderTransferable.RECORD_READER_FLAVOR)) {
        return true;
      } else {
        if (ClipboardUtil.isDataFlavorAvailable(DataFlavor.stringFlavor)) {
          final String string = ClipboardUtil.getContents(DataFlavor.stringFlavor);
          if (Property.hasValue(string)) {
            int lineIndex = string.indexOf('\n');
            if (lineIndex == -1) {
              lineIndex = string.indexOf('\r');
            }
            if (lineIndex != -1) {
              final String line = string.substring(0, lineIndex).trim();
              String fieldName;
              final int tabIndex = line.indexOf('\t');
              if (tabIndex != -1) {
                fieldName = line.substring(0, tabIndex);

              } else {
                final int commaIndex = line.indexOf(',');
                if (commaIndex != -1) {
                  fieldName = line.substring(0, commaIndex);
                } else {
                  fieldName = line;
                }
              }
              if (fieldName.startsWith("\"")) {
                fieldName = fieldName.substring(1);
              }
              if (fieldName.endsWith("\"")) {
                fieldName = fieldName.substring(0, fieldName.length() - 1);
              }
              if (getRecordDefinition().hasField(fieldName)) {
                return true;
              }
            }
            if (canPasteGeometry()) {
              return true;
            }
          }
        }
      }
    }
    return false;
  }

  @Override
  public boolean isClonable() {
    return true;
  }

  public boolean isConfirmDeleteRecords() {
    return this.confirmDeleteRecords;
  }

  protected boolean isDeleteBlocked(final String suffix, final LayerRecord record) {

    if (checkBlockDeleteRecord(record)) {
      final Map<String, Condition> deleteRecordsBlockFilterByFieldName = getDeleteRecordsBlockFilterByFieldName();
      for (final String fieldName : deleteRecordsBlockFilterByFieldName.keySet()) {
        final boolean fieldBlocked = isDeletedRecordFieldBlocked(record, fieldName);
        if (fieldBlocked) {
          return true;
        }
      }

    }
    return false;
  }

  public boolean isDeleted(final LayerRecord record) {
    return internalIsDeleted(record);
  }

  public <LR extends LayerRecord> boolean isDeletedRecordFieldBlocked(final Record record,
    final String fieldName) {
    final Map<String, Condition> conditionsByFieldName = getDeleteRecordsBlockFilterByFieldName();
    final Condition condition = conditionsByFieldName.get(fieldName);
    final boolean fieldBlocked = condition.test(record);
    return fieldBlocked;
  }

  public boolean isFieldEditable(final String fieldName) {
    return !this.userReadOnlyFieldNames.contains(fieldName);
  }

  public boolean isFieldUserReadOnly(final String fieldName) {
    return getUserReadOnlyFieldNames().contains(fieldName);
  }

  public boolean isHasChangedRecords() {
    return isHasChanges();
  }

  @Override
  public boolean isHasChanges() {
    if (isEditable()) {
      if (this.recordCacheNew.hasRecords()) {
        return true;
      } else if (this.recordCacheModified.hasRecords()) {
        return true;
      } else if (this.recordCacheDeleted.hasRecords()) {
        return true;
      } else {
        return false;
      }
    } else {
      return false;
    }
  }

  @Override
  public boolean isHasGeometry() {
    return isExists() && getGeometryFieldName() != null;
  }

  @Override
  public boolean isHasSelectedRecords() {
    return isExists() && this.recordCacheSelected.hasRecords();
  }

  public boolean isHasSelectedRecordsWithGeometry() {
    return isHasGeometry() && isHasSelectedRecords();
  }

  public boolean isHidden(final LayerRecord record) {
    if (isCanDeleteRecords() && isDeleted(record)) {
      return true;
    } else if (isSelectable() && isSelected(record)) {
      return true;
    } else {
      return false;
    }
  }

  public boolean isHighlighted(final LayerRecord record) {

    return this.recordCacheHighlighted.containsRecord(record);
  }

  public boolean isLayerRecord(final LayerRecord record) {
    if (record == null) {
      return false;
    } else {
      return record.getLayer() == this;
    }
  }

  public boolean isLayerRecord(final Record record) {
    if (record == null) {
      return false;
    } else if (record instanceof LayerRecord) {
      final LayerRecord layerRecord = (LayerRecord)record;
      return isLayerRecord(layerRecord);
    } else if (record.getRecordDefinition() == getRecordDefinition()) {
      return true;
    } else {
      return false;
    }
  }

  public boolean isModified(final LayerRecord record) {
    return this.recordCacheModified.containsRecord(record);
  }

  public boolean isNew(final LayerRecord record) {
    return this.recordCacheNew.containsRecord(record);
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

  public boolean isSelected(final LayerRecord record) {
    return this.recordCacheSelected.containsRecord(record);
  }

  public boolean isShowAllRecordsOnFilter() {
    return Preferences.getValue("com.revolsys.gis", PREFERENCE_SHOW_ALL_RECORDS_ON_FILTER);
  }

  public boolean isSnapToAllLayers() {
    return this.snapToAllLayers;
  }

  public boolean isUseFieldTitles() {
    return this.useFieldTitles;
  }

  public boolean isVisible(final LayerRecord record) {
    if (isExists() && isVisible()) {
      final AbstractRecordLayerRenderer renderer = getRenderer();
      if (renderer == null || renderer.isVisible(record)) {
        return true;
      }
    }
    return false;
  }

  private void mergeSelectedRecords() {
    if (isCanMergeRecords()) {
      MergeRecordsDialog.showDialog(this);
    }
  }

  protected LayerRecordForm newDefaultForm(final LayerRecord record) {
    return new LayerRecordForm(this, record);
  }

  public LayerRecordForm newForm(final LayerRecord record) {
    final String formFactoryExpression = getProperty(FORM_FACTORY_EXPRESSION);
    if (Property.hasValue(formFactoryExpression)) {
      try {
        final SpelExpressionParser parser = new SpelExpressionParser();
        final Expression expression = parser.parseExpression(formFactoryExpression);
        final EvaluationContext context = new StandardEvaluationContext(this);
        context.setVariable("object", record);
        return expression.getValue(context, LayerRecordForm.class);
      } catch (final Throwable e) {
        Logs.error(this, "Unable to create form for " + this, e);
        return null;
      }
    } else {
      return newDefaultForm(record);
    }
  }

  public LayerRecord newLayerRecord(final Map<String, ? extends Object> values) {
    if (!isReadOnly() && isEditable() && isCanAddRecords()) {
      final RecordFactory<LayerRecord> recordFactory = getRecordFactory();
      final LayerRecord record = recordFactory.newRecord(getRecordDefinition());
      record.setState(RecordState.INITIALIZING);
      try {
        if (values != null && !values.isEmpty()) {
          record.setValuesByPath(values);
          record.setIdentifier(null);
        }
      } finally {
        record.setState(RecordState.NEW);
      }
      this.recordCacheNew.addRecord(record);
      fireRecordInserted(record);
      fireHasChangedRecords();
      return record;
    } else {
      return null;
    }
  }

  protected LayerRecord newLayerRecord(final RecordDefinition recordDefinition) {
    if (recordDefinition.equals(getRecordDefinition())) {
      return new ArrayLayerRecord(this);
    } else {
      throw new IllegalArgumentException("Cannot create records for " + recordDefinition);
    }
  }

  public MultipleUndo newMultipleUndo() {
    return new MultipleUndo();
  }

  @Override
  public TabbedValuePanel newPropertiesPanel() {
    final TabbedValuePanel propertiesPanel = super.newPropertiesPanel();
    newPropertiesPanelFields(propertiesPanel);
    newPropertiesPanelFieldNamesSet(propertiesPanel);
    newPropertiesPanelStyle(propertiesPanel);
    newPropertiesPanelSnapping(propertiesPanel);
    return propertiesPanel;
  }

  protected void newPropertiesPanelFieldNamesSet(final TabbedValuePanel propertiesPanel) {
    final FieldNamesSetPanel panel = new FieldNamesSetPanel(this);
    propertiesPanel.addTab("Field Sets", panel);
  }

  protected void newPropertiesPanelFields(final TabbedValuePanel propertiesPanel) {
    final RecordDefinition recordDefinition = getRecordDefinition();
    final BaseJTable fieldTable = RecordDefinitionTableModel.newTable(recordDefinition);

    final BasePanel fieldPanel = new BasePanel(new BorderLayout());
    fieldPanel.setPreferredSize(new Dimension(500, 400));
    final JScrollPane fieldScroll = new JScrollPane(fieldTable);
    fieldPanel.add(fieldScroll, BorderLayout.CENTER);
    propertiesPanel.addTab("Fields", fieldPanel);
  }

  protected void newPropertiesPanelSnapping(final TabbedValuePanel propertiesPanel) {
    final SnapLayersPanel panel = new SnapLayersPanel(this);
    propertiesPanel.addTab("Snapping", panel);
  }

  protected void newPropertiesPanelStyle(final TabbedValuePanel propertiesPanel) {
    if (getRenderer() != null) {
      final LayerStylePanel stylePanel = new LayerStylePanel(this);
      propertiesPanel.addTab("Style", "palette", stylePanel);
    }
  }

  @Override
  protected BasePanel newPropertiesTabGeneral(final TabbedValuePanel tabPanel) {
    final BasePanel generalPanel = super.newPropertiesTabGeneral(tabPanel);
    newPropertiesTabGeneralPanelFilter(generalPanel);
    return generalPanel;
  }

  protected ValueField newPropertiesTabGeneralPanelFilter(final BasePanel parent) {
    final ValueField filterPanel = new ValueField(this);
    Borders.titled(filterPanel, "Filter");

    final QueryFilterField field = new QueryFilterField(this, "where", getWhere());
    filterPanel.add(field);
    Property.addListener(field, "where", getBeanPropertyListener());

    GroupLayouts.makeColumns(filterPanel, 1, true);

    parent.add(filterPanel);
    return filterPanel;
  }

  @Override
  protected ValueField newPropertiesTabGeneralPanelGeneral(final BasePanel parent) {
    final ValueField panel = super.newPropertiesTabGeneralPanelGeneral(parent);

    final Field confirmDeleteField = (Field)SwingUtil.addObjectField(panel, this,
      "confirmDeleteRecords", DataTypes.BOOLEAN);
    Property.addListener(confirmDeleteField, "confirmDeleteRecords", this.beanPropertyListener);

    GroupLayouts.makeColumns(panel, 2, true);

    return panel;
  }

  protected <R extends LayerRecord> List<R> newProxyLayerRecords(
    final Iterable<? extends LayerRecord> records) {
    final List<R> proxyRecords = new ArrayList<>();
    for (final LayerRecord record : records) {
      final R proxyRecord = record.getRecordProxy();
      proxyRecords.add(proxyRecord);
    }
    return proxyRecords;
  }

  public Query newQuery() {
    final RecordDefinition recordDefinition = getRecordDefinition();
    return new Query(recordDefinition);
  }

  protected final RecordCache newRecordCache(final String cacheId) {
    final RecordCache recordCache = newRecordCacheDo(cacheId);
    addRecordCache(recordCache);
    return recordCache;
  }

  protected Collection<LayerRecord> newRecordCacheCollection() {
    return new HashSet<>();
  }

  protected final RecordCacheCollection newRecordCacheCollection(final String cacheId) {
    return new RecordCacheCollection(cacheId, this);
  }

  protected RecordCache newRecordCacheDo(final String cacheId) {
    return newRecordCacheCollection(cacheId);
  }

  public JComponent newSearchField(final FieldDefinition fieldDefinition,
    final CodeTable codeTable) {
    if (fieldDefinition == null) {
      return new TextField(20);
    } else {
      final String fieldName = fieldDefinition.getName();
      return newCompactField(fieldName, true);
    }
  }

  public UndoableEdit newSetFieldUndo(final LayerRecord record, final String fieldName,
    final Object oldValue, final Object newValue) {
    return new SetRecordFieldValueUndo(record, fieldName, oldValue, newValue);
  }

  protected RecordSpatialIndex<LayerRecord> newSpatialIndex() {
    return newSpatialIndex(this);
  }

  protected RecordSpatialIndex<LayerRecord> newSpatialIndex(final AbstractRecordLayer layer) {
    final GeometryFactory geometryFactory = layer.getGeometryFactory();
    final BiPredicate<LayerRecord, LayerRecord> equalsItemFunction = LayerRecord::isSame;
    final SpatialIndex<LayerRecord> spatialIndex = new RStarTree<LayerRecord>(geometryFactory)
      .setEqualsItemFunction(equalsItemFunction);
    return new RecordSpatialIndex<>(spatialIndex);
  }

  protected Map<String, Object> newSplitValues(final LayerRecord oldRecord,
    final LineString oldLine, final Point splitPoint, final LineString newLine) {
    final DirectionalFields directionalFields = DirectionalFields.getProperty(oldRecord);
    final Map<String, Object> values1 = directionalFields.newSplitValues(oldRecord, oldLine,
      splitPoint, newLine);
    return values1;
  }

  public RecordLayerTablePanel newTablePanel(final Map<String, Object> config) {
    final RecordLayerTable table = RecordLayerTableModel.newTable(this);
    if (table == null) {
      return null;
    } else {
      return new RecordLayerTablePanel(this, table, config);
    }
  }

  @Override
  protected Component newTableViewComponent(final Map<String, Object> config) {
    return newTablePanel(config);
  }

  public void panToBoundingBox(final BoundingBox boundingBox) {
    final MapPanel mapPanel = getMapPanel();
    mapPanel.panToBoundingBox(boundingBox);
  }

  public void panToSelected() {
    final BoundingBox selectedBoundingBox = getSelectedBoundingBox();
    panToBoundingBox(selectedBoundingBox);
  }

  public void pasteRecordGeometry(final LayerRecord record) {
    final Geometry geometry = getPasteRecordGeometry(record, true);
    if (geometry != null) {
      record.setGeometryValue(geometry);
    }
  }

  public void pasteRecords() {
    final List<LayerRecord> newRecords = new ArrayList<>();
    try (
      BaseCloseable eventsEnabled = eventsDisabled()) {
      RecordReader reader = ClipboardUtil
        .getContents(RecordReaderTransferable.RECORD_READER_FLAVOR);
      if (reader == null) {
        final List<Geometry> geometries = getPasteWktGeometries();
        if (geometries.isEmpty()) {
          final String string = ClipboardUtil.getContents(DataFlavor.stringFlavor);
          if (Property.hasValue(string)) {
            if (string.contains("\n") || string.contains("\r")) {
              if (string.contains("\t")) {
                final Resource tsvResource = new ByteArrayResource("t.tsv", string);
                reader = RecordReader.newRecordReader(tsvResource);
              } else {
                final Resource resource = new ByteArrayResource("t.csv", string);
                reader = RecordReader.newRecordReader(resource);
              }
            }
          }
        } else {
          final String geometryFieldName = getGeometryFieldName();
          for (final Geometry geometry : geometries) {
            final Map<String, Object> values = Collections.singletonMap(geometryFieldName,
              geometry);
            final LayerRecord newRecord = newLayerRecord(values);
            if (newRecord != null) {
              newRecords.add(newRecord);
            }
          }
        }
      }
      if (reader != null) {
        for (final Record sourceRecord : reader) {
          final Map<String, Object> newValues = getPasteNewValues(sourceRecord);

          if (!newValues.isEmpty()) {
            final LayerRecord newRecord = newLayerRecord(newValues);
            if (newRecord != null) {
              newRecords.add(newRecord);
            }
          }
        }
      }
    } catch (final Throwable e) {
      LoggingEventPanel.showDialog("Unexpected error pasting records", e);
      return;
    }
    RecordValidationDialog.validateRecords("Pasting Records", this, newRecords, (validator) -> {
      // Success
      // Save the valid records
      final List<LayerRecord> validRecords = validator.getValidRecords();
      if (!validRecords.isEmpty()) {
        saveChanges(validRecords);
        addSelectedRecords(validRecords);
        zoomToRecords(validRecords);
        showRecordsTable(RecordLayerTableModel.MODE_RECORDS_SELECTED, true);
        firePropertyChange(RECORDS_INSERTED, null, validRecords);
      }
      // Delete any invalid records
      final List<LayerRecord> invalidRecords = validator.getInvalidRecords();
      if (!invalidRecords.isEmpty()) {
        deleteRecords(invalidRecords);
      }
    }, (validator) -> {
      // Cancel, delete all the records
      deleteRecords(newRecords);
    });
  }

  protected void postProcess(final LayerRecord layerRecord) {
  }

  protected void postSaveChanges(final RecordState originalState, final LayerRecord record) {
    postSaveDeletedRecord(record);
    postSaveModifiedRecord(record);
    postSaveNewRecord(record);
  }

  protected boolean postSaveDeletedRecord(final LayerRecord record) {
    final boolean deleted;
    synchronized (getSync()) {
      deleted = this.recordCacheDeletedInternal.removeContainsRecord(record);
    }
    if (deleted) {
      removeRecordFromCache(record);
      return true;
    } else {
      return false;
    }
  }

  protected boolean postSaveModifiedRecord(final LayerRecord record) {
    boolean removed;
    synchronized (getSync()) {
      removed = this.recordCacheModified.removeContainsRecord(record);
    }
    if (removed) {
      record.postSaveModified();
      return true;
    } else {
      return false;
    }
  }

  protected boolean postSaveNewRecord(final LayerRecord record) {
    boolean isNew;
    final boolean selected;
    final boolean highlighted;
    synchronized (getSync()) {
      selected = isSelected(record);
      highlighted = isHighlighted(record);
      isNew = this.recordCacheNew.removeContainsRecord(record);
    }
    if (isNew) {
      removeRecordFromCache(record);
      record.postSaveNew();
      addToIndex(record);
      setSelectedHighlighted(record, selected, highlighted);
    }
    return isNew;
  }

  protected void postSelectByBoundingBox(final List<LayerRecord> records) {
    if (isHasSelectedRecordsWithGeometry()) {
      showRecordsTable(RecordLayerTableModel.MODE_RECORDS_SELECTED, false);
    }
    if (!records.isEmpty()) {
      firePropertyChange("selectedRecordsByBoundingBox", false, true);
    }
  }

  public <A extends B, B> void processTasks(final CharSequence title, final Collection<A> records,
    final Consumer<B> action) {
    processTasks(title, records, action, null);
  }

  public <A extends B, B> void processTasks(final CharSequence title, final Collection<A> records,
    final Consumer<B> action, final Consumer<ProgressMonitor> afterAction) {
    final int recordCount = records.size();
    final Consumer<Consumer<A>> forEachAction = records::forEach;
    processTasks(title, recordCount, forEachAction, action, afterAction);
  }

  public <A extends B, B> void processTasks(final CharSequence title, final Collection<A> records,
    final Consumer<B> action, final Consumer<ProgressMonitor> afterAction,
    final Runnable doneTask) {
    final int recordCount = records.size();
    final Consumer<Consumer<A>> forEachAction = records::forEach;
    processTasks(title, recordCount, forEachAction, action, afterAction, doneTask);
  }

  public <A extends B, B> void processTasks(final CharSequence title, final int taskCount,
    final Consumer<Consumer<A>> forEachAction, final Consumer<B> action) {
    processTasks(title, taskCount, forEachAction, action, null, null);
  }

  public <A extends B, B> void processTasks(final CharSequence title, final int taskCount,
    final Consumer<Consumer<A>> forEachAction, final Consumer<B> action,
    final Consumer<ProgressMonitor> afterAction) {
    processTasks(title, taskCount, forEachAction, action, afterAction, null);
  }

  public <A extends B, B> void processTasks(final CharSequence title, final int taskCount,
    final Consumer<Consumer<A>> forEachAction, final Consumer<B> action,
    final Consumer<ProgressMonitor> afterAction, final Runnable doneTask) {
    final Consumer<ProgressMonitor> task = progressMonitor -> processTasksDo(title, progressMonitor,
      forEachAction, action, afterAction);
    ProgressMonitor.background(title, null, task, taskCount, doneTask);
  }

  protected <A extends B, B> void processTasksDo(final CharSequence title,
    final ProgressMonitor progressMonitor, final Consumer<Consumer<A>> forEachAction,
    final Consumer<B> action, final Consumer<ProgressMonitor> afterAction) {
    final Consumer<A> monitorAction = value -> {
      if (progressMonitor.isCancelled()) {
        throw new CancellationException();
      } else {
        action.accept(value);
        progressMonitor.addProgress();
      }
    };

    try (
      BaseCloseable eventsDisabled = eventsDisabled()) {
      processTasksDo(forEachAction, monitorAction);
    } catch (final CancellationException e) {
    } finally {
      if (afterAction != null) {
        try {
          afterAction.accept(progressMonitor);
        } catch (final Exception e) {
          Logs.error(this, "Unable to post-process " + title, e);
        }
      }
      fireRecordsChanged();
    }
  }

  protected <A> void processTasksDo(final Consumer<Consumer<A>> forEachAction,
    final Consumer<A> monitorAction) {
    forEachAction.accept(monitorAction);
  }

  @Override
  public void propertyChange(final PropertyChangeEvent event) {
    super.propertyChange(event);
    if (isExists()) {
      final Object source = event.getSource();
      final String propertyName = event.getPropertyName();
      if (!"qaMessagesUpdated".equals(propertyName)) {
        if (source instanceof LayerRecord) {
          final LayerRecord record = (LayerRecord)source;
          if (record.getLayer() == this) {
            final String geometryFieldName = getGeometryFieldName();
            if (propertyName.equals(geometryFieldName)) {
              this.recordCacheIndex.clearIndex();
              this.recordCacheSelected.clearIndex();
            }
          }
        }
      }
    }
  }

  protected void recordPasted(final LayerRecord newRecord) {
  }

  @Override
  protected void refreshDo() {
    setIndexRecords(null);
  }

  @Override
  protected void refreshPostDo() {
    super.refreshPostDo();
    fireRecordsChanged();
  }

  protected void removeForm(final LayerRecord record) {
    final List<LayerRecord> records = Collections.singletonList(record);
    removeForms(records);
  }

  protected void removeForms(final Iterable<? extends LayerRecord> records) {
    final List<LayerRecordForm> forms = new ArrayList<>();
    final List<Window> windows = new ArrayList<>();
    synchronized (this.formRecords) {
      for (final LayerRecord record : records) {
        final LayerRecord proxiedRecord = getProxiedRecord(record);
        if (proxiedRecord != null) {
          final int index = proxiedRecord.indexOf(this.formRecords);
          if (index == -1) {
          } else {
            this.recordCacheForm.removeRecord(proxiedRecord);
            this.formRecords.remove(index);
            final Component component = this.formComponents.remove(index);
            if (component instanceof LayerRecordForm) {
              final LayerRecordForm form = (LayerRecordForm)component;
              forms.add(form);
            }
            final Window window = this.formWindows.remove(index);
            if (window != null) {
              windows.add(window);
            }
          }
        }
      }
    }
    if (!forms.isEmpty() && !windows.isEmpty()) {
      Invoke.later(() -> {
        for (final LayerRecordForm form : forms) {
          form.destroy();
        }
        for (final Window window : windows) {
          SwingUtil.dispose(window);
        }
      });
    }
  }

  protected void removeHighlightedRecord(final LayerRecord record) {
    this.recordCacheHighlighted.removeRecord(record);
  }

  void removeProxiedRecord(final LayerRecord proxyRecord) {
    if (proxyRecord != null) {
      synchronized (proxyRecord) {
        this.proxiedRecords.remove(proxyRecord);
      }
    }
  }

  protected boolean removeRecordFromCache(final LayerRecord record) {
    boolean removed = true;
    synchronized (getSync()) {
      if (isLayerRecord(record)) {
        for (final RecordCache cache : this.recordCaches) {
          if (!cache.removeRecord(record)) {
            removed = false;
          }
        }
      }
    }
    return removed;
  }

  protected boolean removeSelectedRecord(final LayerRecord record) {
    final boolean removed = this.recordCacheSelected.removeContainsRecord(record);
    removeHighlightedRecord(record);
    return removed;
  }

  protected void replaceCachedRecord(final LayerRecord record) {
    if (isLayerRecord(record)) {
      forEachRecordCache(cache -> cache.replaceRecord(record));
    }
  }

  public void replaceValues(final LayerRecord record, final Map<String, Object> values) {
    record.setValues(values);
  }

  public void revertChanges(final LayerRecord record) {
    synchronized (getSync()) {
      if (isLayerRecord(record)) {
        final boolean selected = isSelected(record);
        final boolean highlighted = isHighlighted(record);
        postSaveModifiedRecord(record);
        if (this.recordCacheDeletedInternal.removeContainsRecord(record)) {
          record.setState(RecordState.PERSISTED);
        }
        removeRecordFromCache(record);
        setSelectedHighlighted(record, selected, highlighted);
      }
    }
  }

  @Override
  public boolean saveChanges() {
    if (isExists()) {
      final List<LayerRecord> allRecords = new ArrayList<>();
      for (final RecordCache recordCache : Arrays.asList(this.recordCacheDeleted,
        this.recordCacheModified, this.recordCacheNew)) {
        final Consumer<LayerRecord> action = allRecords::add;
        recordCache.forEachRecord(action);
      }
      return saveChanges(allRecords);
    } else {
      return false;
    }
  }

  public boolean saveChanges(final Collection<? extends LayerRecord> records) {
    try {
      if (records.isEmpty()) {
        return true;
      } else {
        // Includes two types of validation of record. The first checks field
        // types before interacting with the record store. The second is any
        // errors on the actual saving of data.
        final Set<Boolean> allSaved = new HashSet<>();
        RecordValidationDialog.validateRecords("Save Changes", //
          this, //
          records, (validator) -> {
            // Success
            // Save the valid records
            final List<LayerRecord> validRecords = validator.getValidRecords();
            if (!validRecords.isEmpty()) {
              final RecordLayerErrors errors = new RecordLayerErrors("Saving Changes", this);
              try (
                BaseCloseable eventsEnabled = eventsDisabled()) {
                saveChangesDo(errors, validRecords);
              } finally {
                if (!errors.showErrorDialog()) {
                  allSaved.add(false);
                }
              }
              fireRecordsChanged();
            }
            final List<LayerRecord> invalidRecords = validator.getInvalidRecords();
            if (!invalidRecords.isEmpty()) {
              allSaved.add(false);
            }
          }, (validator) -> {
            allSaved.add(false);
          });
        return allSaved.isEmpty();
      }
    } finally {
      fireSelected();
      fireHasChangedRecords();
    }
  }

  public final boolean saveChanges(final LayerRecord... records) {
    final List<LayerRecord> list = Arrays.asList(records);
    return saveChanges(list);
  }

  public final boolean saveChanges(final LayerRecord record) {
    // Includes two types of validation of record. The first checks field
    // types before interacting with the record store. The second is any
    // errors on the actual saving of data.
    final Set<Boolean> allSaved = new HashSet<>();
    RecordValidationDialog.validateRecords("Save Changes", //
      this, //
      record, (validator) -> {
        // Success
        // Save the valid records
        final List<LayerRecord> validRecords = validator.getValidRecords();
        if (!validRecords.isEmpty()) {
          final RecordLayerErrors errors = new RecordLayerErrors("Saving Changes", this);
          try (
            BaseCloseable eventsEnabled = eventsDisabled()) {
            synchronized (getSync()) {
              try {
                final boolean saved = saveSingleRecordDo(errors, record);
                if (!saved) {
                  errors.addRecord(record, "Unknown error");
                }
              } catch (final Throwable t) {
                errors.addRecord(record, t);
              }
            }
            record.fireRecordUpdated();
          } finally {
            if (!errors.showErrorDialog()) {
              allSaved.add(false);
            }
          }
        }
        fireHasChangedRecords();
        final List<LayerRecord> invalidRecords = validator.getInvalidRecords();
        if (!invalidRecords.isEmpty()) {
          allSaved.add(false);
        }
      }, (validator) -> {
        allSaved.add(false);
      });
    return allSaved.isEmpty();
  }

  @Override
  protected boolean saveChangesDo() {
    throw new UnsupportedOperationException();
  }

  protected boolean saveChangesDo(final RecordLayerErrors errors, final LayerRecord record) {
    return true;
  }

  protected void saveChangesDo(final RecordLayerErrors errors, final List<LayerRecord> records) {
    for (final LayerRecord record : records) {
      synchronized (getSync()) {
        try {
          final boolean saved = saveSingleRecordDo(errors, record);
          if (!saved) {
            errors.addRecord(record, "Unknown error");
          }
        } catch (final Throwable t) {
          errors.addRecord(record, t);
        }
      }
    }
  }

  protected boolean saveSingleRecordDo(final RecordLayerErrors errors, final LayerRecord record) {
    return internalSaveChanges(errors, record);
  }

  public void setCanAddRecords(final boolean canAddRecords) {
    this.canAddRecords = canAddRecords;
    firePropertyChange("canAddRecords", !isCanAddRecords(), isCanAddRecords());
  }

  public void setCanDeleteRecords(final boolean canDeleteRecords) {
    if (this.canDeleteRecords != canDeleteRecords) {
      this.canDeleteRecords = canDeleteRecords;
      firePropertyChange("canDeleteRecords", !isCanDeleteRecords(), isCanDeleteRecords());
    }
  }

  public void setCanEditRecords(final boolean canEditRecords) {
    this.canEditRecords = canEditRecords;
    firePropertyChange("canEditRecords", !isCanEditRecords(), isCanEditRecords());
  }

  public void setCanPasteRecords(final boolean canPasteRecords) {
    this.canPasteRecords = canPasteRecords;
  }

  public void setConfirmDeleteRecords(final boolean confirmDeleteRecords) {
    this.confirmDeleteRecords = confirmDeleteRecords;
  }

  @Override
  public void setEditable(final boolean editable) {
    Invoke.background("Set Editable " + this, () -> {
      if (editable == false) {
        firePropertyChange("preEditable", false, true);
        final boolean hasChanges = isHasChanges();
        if (hasChanges) {
          final Integer result = Invoke.andWait(() -> {
            return Dialogs.showConfirmDialog(
              "The layer has unsaved changes. Click Yes to save changes. Click No to discard changes. Click Cancel to continue editing.",
              "Save Changes", JOptionPane.YES_NO_CANCEL_OPTION);
          });
          synchronized (getSync()) {
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
      }
      synchronized (this.getSync()) {
        super.setEditable(editable);
        setCanAddRecords(this.canAddRecords);
        setCanDeleteRecords(this.canDeleteRecords);
        setCanEditRecords(this.canEditRecords);
      }
    });
  }

  public void setFieldColumnWidth(final String fieldName, final int columnWidth) {
    this.fieldColumnWidths.put(fieldName, columnWidth);
  }

  public void setFieldColumnWidths(final Map<String, ? extends Number> fieldColumnWidths) {
    this.fieldColumnWidths.clear();
    for (final Entry<String, ? extends Number> entry : fieldColumnWidths.entrySet()) {
      final String fieldName = entry.getKey();
      final Number widthNumber = entry.getValue();
      if (Property.hasValuesAll(fieldName, widthNumber)) {
        final int width = widthNumber.intValue();
        this.fieldColumnWidths.put(fieldName, width);
      }
    }
  }

  public void setFieldNamesSetName(final String fieldNamesSetName) {
    final String oldValue = this.fieldNamesSetName;
    if (Property.hasValue(fieldNamesSetName)) {
      this.fieldNamesSetName = fieldNamesSetName;
    } else {
      this.fieldNamesSetName = ALL;
    }
    firePropertyChange("fieldNamesSetName", oldValue, this.fieldNamesSetName);
  }

  public void setFieldNamesSets(final Map<String, List<String>> fieldNamesSets) {
    final List<String> allFieldNames = this.fieldNames;
    this.fieldNamesSetNames.clear();
    this.fieldNamesSetNames.add("All");
    this.fieldNamesSets.clear();
    if (fieldNamesSets != null) {
      for (final Entry<String, List<String>> entry : fieldNamesSets.entrySet()) {
        final String name = entry.getKey();
        if (Property.hasValue(name)) {
          final String upperName = name.toUpperCase();
          final Collection<String> names = entry.getValue();
          if (Property.hasValue(names)) {
            final Set<String> fieldNames = new LinkedHashSet<>(names);
            if (ALL.equalsIgnoreCase(name)) {
              if (Property.hasValue(allFieldNames)) {
                fieldNames.addAll(allFieldNames);
              }
            } else {
              boolean found = false;
              for (final String name2 : this.fieldNamesSetNames) {
                if (name.equalsIgnoreCase(name2)) {
                  found = true;
                  Logs.error(this,
                    "Duplicate field set name " + name + "=" + name2 + " for layer " + getPath());
                }
              }
              if (!found) {
                this.fieldNamesSetNames.add(name);
              }
            }
            if (Property.hasValue(allFieldNames)) {
              fieldNames.retainAll(allFieldNames);
            }
            this.fieldNamesSets.put(upperName, new ArrayList<>(fieldNames));
          }
        }
      }
    }
    getFieldNamesSet(ALL);
    firePropertyChange("fieldNamesSets", null, this.fieldNamesSets);
  }

  public void setFilter(final Condition filter) {
    final Object oldValue = this.filter;
    this.where = null;
    this.filter = filter;
    firePropertyChange("filter", oldValue, this.filter);
  }

  @Override
  protected GeometryFactory setGeometryFactoryDo(final GeometryFactory geometryFactory) {
    this.recordCacheIndex.setGeometryFactory(geometryFactory);
    this.recordCacheSelected.setGeometryFactory(geometryFactory);
    return super.setGeometryFactoryDo(geometryFactory);
  }

  public void setHighlightedRecords(final Collection<LayerRecord> highlightedRecords) {
    this.recordCacheHighlighted.setRecords(highlightedRecords);
    fireHighlighted();
  }

  protected void setIndexRecords(final List<LayerRecord> records) {
    synchronized (getSync()) {
      if (hasGeometryField()) {
        this.recordCacheIndex.setRecords(records);
        final List<LayerRecord> newRecords = getRecordsNew();
        for (final LayerRecord newRecord : newRecords) {
          if (newRecord.getState().isNew()) {
            this.recordCacheIndex.addRecord(newRecord);
          }
        }
      }
    }
  }

  @Override
  public void setProperties(final Map<String, ? extends Object> properties) {
    if (!properties.containsKey("style")) {
      final GeometryStyleRecordLayerRenderer renderer = getRenderer();
      if (renderer != null) {
        renderer.setStyle(GeometryStyle.newStyle());
      }
    }
    super.setProperties(properties);
    final Predicate<Record> predicate = AbstractRecordLayerRenderer.getFilter(this, properties);
    if (predicate instanceof RecordDefinitionSqlFilter) {
      final RecordDefinitionSqlFilter sqlFilter = (RecordDefinitionSqlFilter)predicate;
      setWhere(sqlFilter.getQuery());
    }
    if (this.fieldNamesSets.isEmpty()) {
      setFieldNamesSets(null);
    }
  }

  protected void setRecordDefinition(final RecordDefinition recordDefinition) {
    this.recordDefinition = recordDefinition;
    if (recordDefinition != null) {
      final FieldDefinition geometryField = recordDefinition.getGeometryField();
      GeometryFactory geometryFactory;
      if (geometryField == null) {
        geometryFactory = null;
        setVisible(false);
        setSelectSupported(false);
        setRenderer(null);
      } else {
        geometryFactory = recordDefinition.getGeometryFactory();
      }
      setGeometryFactory(geometryFactory);
      setBoundingBox(recordDefinition.getBoundingBox());
      final String iconName = recordDefinition.getIconName();
      setIcon(iconName);
      this.fieldNames = recordDefinition.getFieldNames();
      List<String> allFieldNames = this.fieldNamesSets.get(ALL.toUpperCase());
      if (Property.hasValue(allFieldNames)) {
        final Set<String> mergedFieldNames = new LinkedHashSet<>(allFieldNames);
        mergedFieldNames.addAll(this.fieldNames);
        mergedFieldNames.retainAll(this.fieldNames);
        allFieldNames = new ArrayList<>(mergedFieldNames);
      } else {
        allFieldNames = new ArrayList<>(this.fieldNames);
      }
      this.fieldNamesSets.put(ALL.toUpperCase(), allFieldNames);
      setWhere(this.where);
    }
  }

  protected void setRecordFactory(final RecordFactory<? extends LayerRecord> recordFactory) {
    this.recordFactory = recordFactory;
  }

  public boolean setRecordValue(final LayerRecord record, final CharSequence fieldName,
    final Object value) {
    return record.setValue(fieldName, value);
  }

  protected void setSelectedHighlighted(final LayerRecord record, final boolean selected,
    final boolean highlighted) {
    if (selected) {
      this.recordCacheSelected.addRecord(record);
      if (highlighted) {
        this.recordCacheHighlighted.addRecord(record);
      }
    }
  }

  public boolean setSelectedRecords(final BoundingBox boundingBox) {
    if (isSelectable()) {
      final List<LayerRecord> records = getRecordsVisible(boundingBox);
      setSelectedRecords(records);
      postSelectByBoundingBox(records);
      return !records.isEmpty();
    } else {
      return false;
    }
  }

  public void setSelectedRecords(final Collection<LayerRecord> selectedRecords) {
    final List<LayerRecord> oldSelectedRecords = getSelectedRecords();
    synchronized (this.recordCacheSelected.getRecordCacheSync()) {
      this.recordCacheSelected.clearRecords();
      this.recordCacheSelected.addRecords(selectedRecords);
      for (final LayerRecord record : getHighlightedRecords()) {
        if (!isSelected(record)) {
          removeHighlightedRecord(record);
        }
      }
    }
    final List<LayerRecord> newSelectedRecords = getSelectedRecords();
    firePropertyChange(RECORDS_SELECTED, oldSelectedRecords, newSelectedRecords);
    fireSelected();
  }

  public void setSelectedRecords(final LayerRecord... selectedRecords) {
    setSelectedRecords(Arrays.asList(selectedRecords));
  }

  public void setSelectedRecords(final Query query) {
    final List<LayerRecord> records = getRecords(query);
    setSelectedRecords(records);
  }

  public void setSelectedRecordsById(final Identifier id) {
    final RecordDefinition recordDefinition = getRecordDefinition();
    if (recordDefinition != null) {
      final FieldDefinition idField = recordDefinition.getIdField();
      if (idField == null) {
        clearSelectedRecords();
      } else {
        final Query query = Query.where(Q::equal, idField, id);
        setSelectedRecords(query);
      }
    }
  }

  public void setSnapLayerPaths(final Collection<String> snapLayerPaths) {
    if (snapLayerPaths == null || snapLayerPaths.isEmpty()) {
      removeProperty("snapLayers");
    } else {
      setProperty("snapLayers", new TreeSet<>(snapLayerPaths));
    }
  }

  public void setSnapToAllLayers(final boolean snapToAllLayers) {
    this.snapToAllLayers = snapToAllLayers;
  }

  @SuppressWarnings("unchecked")
  public void setStyle(Object style) {
    if (style instanceof Map) {
      final Map<String, Object> map = (Map<String, Object>)style;
      style = MapObjectFactory.toObject(map);
    }
    if (style instanceof AbstractRecordLayerRenderer) {
      final AbstractRecordLayerRenderer renderer = (AbstractRecordLayerRenderer)style;
      setRenderer(renderer);
    } else {
      Logs.error(this, "Cannot create renderer for: " + style);
    }
  }

  public void setUseFieldTitles(final boolean useFieldTitles) {
    this.useFieldTitles = useFieldTitles;
  }

  public void setUserReadOnlyFieldNames(final Collection<String> userReadOnlyFieldNames) {
    this.userReadOnlyFieldNames = new LinkedHashSet<>(userReadOnlyFieldNames);
  }

  public void setWhere(final String where) {
    final RecordDefinition recordDefinition = getRecordDefinition();
    this.where = where;
    if (recordDefinition != null) {
      final Object oldValue = this.filter;
      this.filter = QueryValue.parseWhere(recordDefinition, where);
      firePropertyChange("filter", oldValue, this.filter);
    }
  }

  public LayerRecord showAddForm(final Map<String, Object> values) {
    if (isCanAddRecords()) {
      final LayerRecord newRecord = newLayerRecord(values);
      final LayerRecordForm form = newForm(newRecord);
      if (form == null) {
        return null;
      } else {
        try {
          form.setAddRecord(newRecord);
          if (form.showAddDialog()) {
            return form.getAddRecord();
          } else {
            return null;
          }
        } finally {
          form.setAddRecord(null);
        }
      }
    } else {
      Dialogs.showMessageDialog(
        "Adding records is not enabled for the " + getPath()
          + " layer. If possible make the layer editable",
        "Cannot Add Record", JOptionPane.ERROR_MESSAGE);
      return null;
    }

  }

  public void showForm(final LayerRecord record) {
    showForm(record, null);
  }

  public void showForm(final LayerRecord record, final String fieldName) {
    Invoke.later(() -> {
      if (record != null && !record.isDeleted()) {
        final LayerRecord proxiedRecord = getProxiedRecord(record);
        final int index;
        Window window;
        synchronized (this.formRecords) {
          index = proxiedRecord.indexOf(this.formRecords);
          if (index == -1) {
            window = null;
          } else {
            window = this.formWindows.get(index);
          }
        }
        Component form = null;
        if (window == null) {
          form = newForm(record);
          if (form != null) {
            final String title = LayerRecordForm.getTitle(record);
            final Window parent = getMapPanel().getProjectFrame();
            window = new BaseDialog(parent, title);
            window.add(form);
            window.pack();
            if (form instanceof LayerRecordForm) {
              final LayerRecordForm recordForm = (LayerRecordForm)form;
              window.addWindowListener(recordForm);
              if (record.getState() != RecordState.NEW) {
                if (!isCanEditRecords()) {
                  recordForm.setEditable(false);
                }
              }
            }
            SwingUtil.autoAdjustPosition(window);
            synchronized (this.formRecords) {
              if (proxiedRecord.isDeleted()) {
                window.dispose();
                return;
              } else {
                this.formRecords.add(proxiedRecord);
                this.formComponents.add(form);
                this.formWindows.add(window);
              }
            }
            window.addWindowListener(new WindowAdapter() {

              @Override
              public void windowClosed(final WindowEvent e) {
                removeForm(record);
              }

              @Override
              public void windowClosing(final WindowEvent e) {
                removeForm(record);
              }
            });
            SwingUtil.setVisible(window, true);

            window.requestFocus();
            if (proxiedRecord.isDeleted()) {
              window.setVisible(false);
            }
          }
        } else {
          SwingUtil.setVisible(window, true);

          window.requestFocus();
          final Component component = window.getComponent(0);
          if (component instanceof JScrollPane) {
            final JScrollPane scrollPane = (JScrollPane)component;
            form = scrollPane.getComponent(0);
          }
        }
        if (form instanceof LayerRecordForm) {
          final LayerRecordForm recordForm = (LayerRecordForm)form;
          recordForm.setFieldFocussed(fieldName);
        }
      }
    });
  }

  public void showRecordsTable() {
    showRecordsTable(null, true);
  }

  public void showRecordsTable(final String fieldFilterMode, final boolean selectTab) {
    final MapEx config = new LinkedHashMapEx("fieldFilterMode", fieldFilterMode);
    if (!selectTab) {
      config.put("selectTab", false);
    }
    showTableView(config);
  }

  public void splitRecord(final LayerRecord record, final CloseLocation mouseLocation) {
    final Geometry geometry = mouseLocation.getGeometry();
    if (geometry instanceof LineString) {
      final LineString line = (LineString)geometry;
      final int[] vertexId = mouseLocation.getVertexId();
      final Point point = mouseLocation.getViewportPoint();
      final Point convertedPoint = point.newGeometry(getGeometryFactory());
      final LineString line1;
      final LineString line2;

      final int vertexCount = line.getVertexCount();
      if (vertexId == null) {
        final int vertexIndex = mouseLocation.getSegmentId()[0];
        line1 = line.subLine(null, 0, vertexIndex + 1, convertedPoint);
        line2 = line.subLine(convertedPoint, vertexIndex + 1, vertexCount - vertexIndex - 1, null);
      } else {
        final int pointIndex = vertexId[0];
        if (pointIndex == 0) {
          return;
        } else if (vertexCount - pointIndex < 2) {
          return;
        } else {
          line1 = line.subLine(pointIndex + 1);
          line2 = line.subLine(null, pointIndex, vertexCount - pointIndex, null);
        }

      }
      if (line1 == null || line2 == null) {
        return;
      }

      splitRecord(record, line, convertedPoint, line1, line2);
    }
  }

  /** Perform the actual split. */
  public List<LayerRecord> splitRecord(final LayerRecord record, final LineString line,
    final Point point, final LineString line1, final LineString line2) {
    if (line1.getLength() == 0) {
      if (line2.getLength() == 0) {
        return Collections.singletonList(record);
      } else {
        record.setGeometryValue(line2);
        saveChanges(record);
        return Collections.singletonList(record);
      }
    } else if (line2.getLength() == 0) {
      record.setGeometryValue(line1);
      saveChanges(record);
      return Collections.singletonList(record);
    } else {
      final Map<String, Object> values1 = newSplitValues(record, line, point, line1);
      final LayerRecord record1 = newLayerRecord(values1);

      final Map<String, Object> values2 = newSplitValues(record, line, point, line2);
      final LayerRecord record2 = newLayerRecord(values2);

      addSelectedRecords(record1, record2);

      deleteRecord(record);

      saveChanges(record, record1, record2);

      return Arrays.asList(record1, record2);
    }
  }

  public List<LayerRecord> splitRecord(final LayerRecord record, final Point point) {
    final Geometry geometry = record.getGeometry();
    if (geometry instanceof LineString) {
      final LineString line = (LineString)geometry;
      final List<LineString> lines = line.split(point);
      if (lines.size() == 2) {
        final LineString line1 = lines.get(0);
        final LineString line2 = lines.get(1);
        return splitRecord(record, line, point, line1, line2);
      } else {
        return Collections.singletonList(record);
      }
    } else {
      return Collections.singletonList(record);
    }
  }

  @Override
  public JsonObject toMap() {
    final JsonObject map = super.toMap();
    if (!super.isReadOnly()) {
      addToMap(map, "canAddRecords", this.canAddRecords);
      addToMap(map, "canDeleteRecords", this.canDeleteRecords);
      addToMap(map, "canEditRecords", this.canEditRecords);
      addToMap(map, "canPasteRecords", this.canPasteRecords);
      addToMap(map, "snapToAllLayers", this.snapToAllLayers);
    }
    addToMap(map, "fieldNamesSetName", this.fieldNamesSetName, ALL);
    addToMap(map, "fieldNamesSets", getFieldNamesSets());
    addToMap(map, "fieldColumnWidths", getFieldColumnWidths());
    addToMap(map, "useFieldTitles", this.useFieldTitles, true);
    addToMap(map, "confirmDeleteRecords", this.confirmDeleteRecords);
    map.remove("filter");
    String where;
    if (Property.isEmpty(this.filter)) {
      where = this.where;
    } else {
      where = this.filter.toFormattedString();
    }
    if (Property.hasValue(where)) {
      final RecordDefinitionSqlFilter filter = new RecordDefinitionSqlFilter(this, where);
      addToMap(map, "filter", filter);
    }
    return map;
  }

  public void unHighlightRecords(final Collection<? extends LayerRecord> records) {
    this.recordCacheHighlighted.removeRecords(records);
    fireHighlighted();
  }

  public void unHighlightRecords(final LayerRecord... records) {
    unHighlightRecords(Arrays.asList(records));
  }

  public void unSelectRecords(final BoundingBox boundingBox) {
    if (isSelectable()) {
      final List<LayerRecord> records = getRecordsVisible(boundingBox);
      unSelectRecords(records);
    }
  }

  public void unSelectRecords(final Collection<? extends LayerRecord> records) {
    final List<LayerRecord> removedRecords = new ArrayList<>();
    synchronized (this.recordCacheSelected.getRecordCacheSync()) {
      for (final LayerRecord record : records) {
        if (removeSelectedRecord(record)) {
          removedRecords.add(record);
        }
      }
      unHighlightRecords(records);
    }
    if (!removedRecords.isEmpty()) {
      firePropertyChange(RECORDS_SELECTED, removedRecords, null);
      fireSelected();
    }
  }

  public void unSelectRecords(final LayerRecord... records) {
    unSelectRecords(Arrays.asList(records));
  }

  public void updateRecordState(final LayerRecord record) {
    final RecordState state = record.getState();
    if (state == RecordState.MODIFIED) {
      addModifiedRecord(record);
    } else if (state == RecordState.PERSISTED) {
      postSaveModifiedRecord(record);
      fireHasChangedRecords();
    }
  }

  public void zoomToBoundingBox(BoundingBox boundingBox) {
    if (!RectangleUtil.isEmpty(boundingBox)) {
      final Project project = getProject();
      final GeometryFactory geometryFactory = project.getGeometryFactory();
      boundingBox = boundingBox //
        .bboxEditor() //
        .setGeometryFactory(geometryFactory) //
        .expandPercent(0.1) //
        .newBoundingBox();
      project.setViewBoundingBox(boundingBox);
    }
  }

  public void zoomToGeometry(final Geometry geometry) {
    if (geometry != null) {
      final BoundingBox boundingBox = geometry.getBoundingBox();
      zoomToBoundingBox(boundingBox);
    }
  }

  public void zoomToHighlighted() {
    final BoundingBox boundingBox = getHighlightedBoundingBox();
    zoomToBoundingBox(boundingBox);
  }

  public void zoomToRecord(final Record record) {
    if (record != null) {
      final Geometry geometry = record.getGeometry();
      zoomToGeometry(geometry);
    }
  }

  public void zoomToRecords(final List<? extends LayerRecord> records) {
    final BoundingBox boundingBox = BoundingBox.bboxNew(this, records);
    zoomToBoundingBox(boundingBox);
  }

  public void zoomToSelected() {
    final BoundingBox selectedBoundingBox = getSelectedBoundingBox();
    zoomToBoundingBox(selectedBoundingBox);
  }
}
