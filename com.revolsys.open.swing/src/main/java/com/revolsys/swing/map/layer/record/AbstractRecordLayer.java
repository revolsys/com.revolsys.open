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
import java.util.function.Consumer;
import java.util.function.Predicate;

import javax.swing.Icon;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.undo.UndoableEdit;

import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import com.revolsys.collection.list.Lists;
import com.revolsys.collection.map.MapEx;
import com.revolsys.collection.map.Maps;
import com.revolsys.collection.set.Sets;
import com.revolsys.datatype.DataType;
import com.revolsys.datatype.DataTypes;
import com.revolsys.geometry.cs.CoordinateSystem;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.LineString;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.util.BoundingBoxUtil;
import com.revolsys.identifier.Identifier;
import com.revolsys.io.BaseCloseable;
import com.revolsys.io.FileUtil;
import com.revolsys.io.IoFactory;
import com.revolsys.io.PathName;
import com.revolsys.io.file.FileNameExtensionFilter;
import com.revolsys.io.map.MapObjectFactory;
import com.revolsys.logging.Logs;
import com.revolsys.record.ArrayRecord;
import com.revolsys.record.Record;
import com.revolsys.record.RecordFactory;
import com.revolsys.record.RecordState;
import com.revolsys.record.Records;
import com.revolsys.record.io.ListRecordReader;
import com.revolsys.record.io.RecordIo;
import com.revolsys.record.io.RecordReader;
import com.revolsys.record.io.RecordWriter;
import com.revolsys.record.io.RecordWriterFactory;
import com.revolsys.record.property.DirectionalFields;
import com.revolsys.record.query.Condition;
import com.revolsys.record.query.Q;
import com.revolsys.record.query.Query;
import com.revolsys.record.query.QueryValue;
import com.revolsys.record.schema.FieldDefinition;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.record.schema.RecordDefinitionProxy;
import com.revolsys.spring.resource.ByteArrayResource;
import com.revolsys.spring.resource.Resource;
import com.revolsys.swing.Borders;
import com.revolsys.swing.Icons;
import com.revolsys.swing.SwingUtil;
import com.revolsys.swing.action.enablecheck.EnableCheck;
import com.revolsys.swing.component.BaseDialog;
import com.revolsys.swing.component.BasePanel;
import com.revolsys.swing.component.TabbedValuePanel;
import com.revolsys.swing.component.ValueField;
import com.revolsys.swing.dnd.ClipboardUtil;
import com.revolsys.swing.dnd.transferable.RecordReaderTransferable;
import com.revolsys.swing.dnd.transferable.StringTransferable;
import com.revolsys.swing.layout.GroupLayouts;
import com.revolsys.swing.logging.LoggingEventPanel;
import com.revolsys.swing.map.MapPanel;
import com.revolsys.swing.map.form.FieldNamesSetPanel;
import com.revolsys.swing.map.form.LayerRecordForm;
import com.revolsys.swing.map.form.SnapLayersPanel;
import com.revolsys.swing.map.layer.AbstractLayer;
import com.revolsys.swing.map.layer.Layer;
import com.revolsys.swing.map.layer.LayerGroup;
import com.revolsys.swing.map.layer.LayerRenderer;
import com.revolsys.swing.map.layer.Project;
import com.revolsys.swing.map.layer.record.component.MergeRecordsDialog;
import com.revolsys.swing.map.layer.record.renderer.AbstractMultipleRenderer;
import com.revolsys.swing.map.layer.record.renderer.AbstractRecordLayerRenderer;
import com.revolsys.swing.map.layer.record.renderer.GeometryStyleRenderer;
import com.revolsys.swing.map.layer.record.renderer.MultipleRenderer;
import com.revolsys.swing.map.layer.record.style.GeometryStyle;
import com.revolsys.swing.map.layer.record.style.panel.LayerStylePanel;
import com.revolsys.swing.map.layer.record.style.panel.QueryFilterField;
import com.revolsys.swing.map.layer.record.table.RecordLayerTable;
import com.revolsys.swing.map.layer.record.table.RecordLayerTablePanel;
import com.revolsys.swing.map.layer.record.table.model.RecordDefinitionTableModel;
import com.revolsys.swing.map.layer.record.table.model.RecordLayerTableModel;
import com.revolsys.swing.map.layer.record.table.model.RecordSaveErrors;
import com.revolsys.swing.map.layer.record.table.model.RecordValidationDialog;
import com.revolsys.swing.map.overlay.AbstractOverlay;
import com.revolsys.swing.map.overlay.AddGeometryCompleteAction;
import com.revolsys.swing.map.overlay.CloseLocation;
import com.revolsys.swing.map.overlay.EditRecordGeometryOverlay;
import com.revolsys.swing.menu.MenuFactory;
import com.revolsys.swing.menu.Menus;
import com.revolsys.swing.menu.WrappedMenuFactory;
import com.revolsys.swing.parallel.Invoke;
import com.revolsys.swing.table.BaseJTable;
import com.revolsys.swing.undo.SetRecordFieldValueUndo;
import com.revolsys.util.CompareUtil;
import com.revolsys.util.Label;
import com.revolsys.util.PreferencesUtil;
import com.revolsys.util.Property;
import com.revolsys.util.ShortCounter;

public abstract class AbstractRecordLayer extends AbstractLayer
  implements AddGeometryCompleteAction, RecordDefinitionProxy {
  public static final String ALL = "All";

  public static final String FORM_FACTORY_EXPRESSION = "formFactoryExpression";

  public static final String RECORD_CACHE_MODIFIED = "recordCacheModified";

  public static final String RECORD_DELETED_PERSISTED = "recordDeletedPersisted";

  public static final String RECORD_UPDATED = "recordUpdated";

  public static final String RECORDS_CHANGED = "recordsChanged";

  public static final String RECORDS_DELETED = "recordsDeleted";

  public static final String RECORDS_INSERTED = "recordsInserted";

  public static final String RECORDS_SELECTED = "recordsSelected";

  static {
    final MenuFactory menu = MenuFactory.getMenu(AbstractRecordLayer.class);
    menu.setName("Layer");
    menu.addGroup(0, "table");
    menu.addGroup(2, "edit");
    menu.addGroup(3, "dnd");

    final Predicate<AbstractRecordLayer> exists = AbstractRecordLayer::isExists;

    Menus.addMenuItem(menu, "table", "View Records", "table_go", exists,
      AbstractRecordLayer::showRecordsTable, false);

    final Predicate<AbstractRecordLayer> hasSelectedRecords = AbstractRecordLayer::isHasSelectedRecords;
    final Predicate<AbstractRecordLayer> hasSelectedRecordsWithGeometry = AbstractRecordLayer::isHasSelectedRecordsWithGeometry;

    Menus.addMenuItem(menu, "zoom", "Zoom to Selected", "magnifier_zoom_selected",
      hasSelectedRecordsWithGeometry, AbstractRecordLayer::zoomToSelected, true);

    Menus.addMenuItem(menu, "zoom", "Pan to Selected", "pan_selected",
      hasSelectedRecordsWithGeometry, AbstractRecordLayer::panToSelected, true);

    final Predicate<AbstractRecordLayer> notReadOnly = ((Predicate<AbstractRecordLayer>)AbstractRecordLayer::isReadOnly)
      .negate();
    final Predicate<AbstractRecordLayer> canAdd = AbstractRecordLayer::isCanAddRecords;

    Menus.addCheckboxMenuItem(menu, "edit", "Editable", "pencil", notReadOnly,
      AbstractRecordLayer::toggleEditable, AbstractRecordLayer::isEditable, false);

    Menus.addMenuItem(menu, "edit", "Save Changes", "table_save", AbstractLayer::isHasChanges,
      AbstractLayer::saveChanges, true);

    Menus.addMenuItem(menu, "edit", "Cancel Changes", "table_cancel", AbstractLayer::isHasChanges,
      AbstractRecordLayer::cancelChanges, true);

    Menus.addMenuItem(menu, "edit", "Add New Record", "table_row_insert", canAdd,
      AbstractRecordLayer::addNewRecord, false);

    Menus.addMenuItem(menu, "edit", "Delete Selected Records", "table_row_delete",
      hasSelectedRecords.and(AbstractRecordLayer::isCanDeleteRecords),
      AbstractRecordLayer::deleteSelectedRecords, true);

    Menus.addMenuItem(menu, "edit", "Merge Selected Records", "table_row_merge",
      AbstractRecordLayer::isCanMergeRecords, AbstractRecordLayer::mergeSelectedRecords, false);

    Menus.addMenuItem(menu, "dnd", "Copy Selected Records", "page_copy", hasSelectedRecords,
      AbstractRecordLayer::copySelectedRecords, true);

    Menus.addMenuItem(menu, "dnd", "Paste New Records", "paste_plain",
      canAdd.and(AbstractRecordLayer::isCanPasteRecords), AbstractRecordLayer::pasteRecords, true);

    Menus.addMenuItem(menu, "layer", 0, "Layer Style", "palette",
      AbstractRecordLayer::isHasGeometry,
      (final AbstractRecordLayer layer) -> layer.showProperties("Style"), false);
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

  public static void exportRecords(final String title, final boolean hasGeometryField,
    final Consumer<File> exportAction) {
    Invoke.later(() -> {
      final JFileChooser fileChooser = SwingUtil.newFileChooser("Export Records",
        "com.revolsys.swing.map.table.export", "directory");
      final String defaultFileExtension = PreferencesUtil
        .getUserString("com.revolsys.swing.map.table.export", "fileExtension", "tsv");

      final List<FileNameExtensionFilter> recordFileFilters = new ArrayList<>();
      for (final RecordWriterFactory factory : IoFactory.factories(RecordWriterFactory.class)) {
        if (hasGeometryField || factory.isCustomFieldsSupported()) {
          recordFileFilters.add(IoFactory.newFileFilter(factory));
        }
      }
      IoFactory.sortFilters(recordFileFilters);

      fileChooser.setAcceptAllFileFilterUsed(false);
      fileChooser.setSelectedFile(new File(fileChooser.getCurrentDirectory(), title));
      for (final FileNameExtensionFilter fileFilter : recordFileFilters) {
        fileChooser.addChoosableFileFilter(fileFilter);
        if (Arrays.asList(fileFilter.getExtensions()).contains(defaultFileExtension)) {
          fileChooser.setFileFilter(fileFilter);
        }
      }

      fileChooser.setMultiSelectionEnabled(false);
      final int returnVal = fileChooser.showSaveDialog(SwingUtil.getActiveWindow());
      if (returnVal == JFileChooser.APPROVE_OPTION) {
        final FileNameExtensionFilter fileFilter = (FileNameExtensionFilter)fileChooser
          .getFileFilter();
        File file = fileChooser.getSelectedFile();
        if (file != null) {
          final String fileExtension = FileUtil.getFileNameExtension(file);
          final String expectedExtension = fileFilter.getExtensions().get(0);
          if (!fileExtension.equals(expectedExtension)) {
            file = FileUtil.getFileWithExtension(file, expectedExtension);
          }
          final File targetFile = file;
          PreferencesUtil.setUserString("com.revolsys.swing.map.table.export", "fileExtension",
            expectedExtension);
          PreferencesUtil.setUserString("com.revolsys.swing.map.table.export", "directory",
            file.getParent());
          final String description = "Export " + title + " to " + targetFile.getAbsolutePath();
          Invoke.background(description, () -> {
            exportAction.accept(targetFile);
          });
        }
      }
    });
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

  public static List<AbstractRecordLayer> getVisibleLayers(final LayerGroup group,
    final double scale) {
    final List<AbstractRecordLayer> layers = new ArrayList<>();
    addVisibleLayers(layers, group, scale);
    return layers;
  }

  private final Label cacheIdDeleted = new Label("deleted");

  private final Label cacheIdForm = new Label("form");

  private final Label cacheIdHighlighted = new Label("highlighted");

  private final Label cacheIdIndex = new Label("index");

  private final Label cacheIdModified = new Label("modified");

  private final Label cacheIdNew = new Label("new");

  private final Label cacheIdSelected = new Label("selected");

  private boolean canAddRecords = true;

  private boolean canDeleteRecords = true;

  private boolean canEditRecords = true;

  private boolean canPasteRecords = true;

  private Object editSync;

  private List<String> fieldNames = Collections.emptyList();

  private String fieldNamesSetName = ALL;

  private List<String> fieldNamesSetNames = new ArrayList<>();

  private Map<String, List<String>> fieldNamesSets = new HashMap<>();

  private Condition filter = Condition.ALL;

  private final Map<String, Integer> fieldColumnWidths = new HashMap<>();

  private List<Component> formComponents = new LinkedList<>();

  private List<Record> formRecords = new LinkedList<>();

  private List<Window> formWindows = new LinkedList<>();

  private LayerRecordQuadTree index = new LayerRecordQuadTree();

  private final Map<Identifier, ShortCounter> proxiedRecordIdentifiers = new HashMap<>();

  private Set<LayerRecord> proxiedRecords = new HashSet<>();

  private RecordDefinition recordDefinition;

  private RecordFactory<? extends LayerRecord> recordFactory = this::newLayerRecord;

  private LayerRecordMenu recordMenu;

  private Map<Label, Collection<LayerRecord>> recordsByCacheId = new HashMap<>();

  private LayerRecordQuadTree selectedRecordsIndex;

  private boolean snapToAllLayers = true;

  private boolean useFieldTitles = false;

  private Set<String> userReadOnlyFieldNames = new LinkedHashSet<>();

  private String where;

  protected AbstractRecordLayer(final String type) {
    super(type);
    setReadOnly(false);
    setSelectSupported(true);
    setQuerySupported(true);
    setRenderer(new GeometryStyleRenderer(this));
  }

  private void actionFlipFields(final LayerRecord record) {
    final DirectionalFields property = DirectionalFields.getProperty(record);
    property.reverseFieldValues(record);
  }

  private void actionFlipLineOrientation(final LayerRecord record) {
    final DirectionalFields property = DirectionalFields.getProperty(record);
    property.reverseGeometry(record);
  }

  private void actionFlipRecordOrientation(final LayerRecord record) {
    DirectionalFields.reverse(record);
  }

  @Override
  public void activatePanelComponent(final Component component, final Map<String, Object> config) {
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

  protected void addHighlightedRecord(final LayerRecord record) {
    addRecordToCache(this.cacheIdHighlighted, record);
  }

  public void addHighlightedRecords(final Collection<? extends LayerRecord> records) {
    synchronized (getSync()) {
      for (final LayerRecord record : records) {
        addHighlightedRecord(record);
      }
      cleanCachedRecords();
    }
    fireHighlighted();
  }

  public void addHighlightedRecords(final LayerRecord... records) {
    addHighlightedRecords(Arrays.asList(records));
  }

  protected void addModifiedRecord(final LayerRecord record) {
    if (addRecordToCache(this.cacheIdModified, record)) {
      firePropertyChange(RECORD_CACHE_MODIFIED, null, record.newRecordProxy());
      fireHasChangedRecords();
      cleanCachedRecords();
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

  void addProxiedRecordIdentifier(final Identifier identifier) {
    ShortCounter.increment(this.proxiedRecordIdentifiers, identifier);
  }

  protected void addProxiedRecordIdsToCollection(final Collection<Identifier> identifiers) {
    synchronized (this.proxiedRecords) {
      for (final LayerRecord record : this.proxiedRecords) {
        final Identifier identifier = record.getIdentifier();
        if (identifier != null) {
          identifiers.add(identifier);
        }
      }
    }
    synchronized (this.proxiedRecordIdentifiers) {
      identifiers.addAll(this.proxiedRecordIdentifiers.keySet());
    }
  }

  protected void addRecordsToCache(final Label cacheId,
    final Collection<? extends LayerRecord> records) {
    synchronized (getSync()) {
      for (final LayerRecord record : records) {
        addRecordToCache(cacheId, record);
      }
      cleanCachedRecords();
    }
  }

  protected boolean addRecordToCache(final Label cacheId, LayerRecord record) {
    record = getProxiedRecord(record);
    if (isLayerRecord(record)) {
      if (record.getState() == RecordState.DELETED && !isDeleted(record)) {
      } else {
        synchronized (getSync()) {
          Collection<LayerRecord> cachedRecords = this.recordsByCacheId.get(cacheId);
          if (cachedRecords == null) {
            cachedRecords = new ArrayList<>();
            this.recordsByCacheId.put(cacheId, cachedRecords);
          }
          if (!cachedRecords.contains(record)) {
            cachedRecords.add(record);
            return true;
          }
        }
      }
    }
    return false;
  }

  @Override
  public int addRenderer(final LayerRenderer<?> child, final int index) {
    final AbstractRecordLayerRenderer oldRenderer = getRenderer();
    AbstractMultipleRenderer rendererGroup;
    if (oldRenderer instanceof AbstractMultipleRenderer) {
      rendererGroup = (AbstractMultipleRenderer)oldRenderer;
    } else {
      final AbstractRecordLayer layer = oldRenderer.getLayer();
      rendererGroup = new MultipleRenderer(layer);
      rendererGroup.addRenderer(oldRenderer);
      setRenderer(rendererGroup);
    }
    if (index == 0) {
      rendererGroup.addRenderer(0, (AbstractRecordLayerRenderer)child);
      return 0;
    } else {
      rendererGroup.addRenderer((AbstractRecordLayerRenderer)child);
      return rendererGroup.getRenderers().size() - 1;
    }
  }

  protected boolean addSelectedRecord(final LayerRecord record) {
    final boolean added = addRecordToCache(this.cacheIdSelected, record);
    clearSelectedRecordsIndex();
    return added;
  }

  public void addSelectedRecords(final BoundingBox boundingBox) {
    if (isSelectable()) {
      final List<LayerRecord> records = getRecordsVisible(boundingBox);
      addSelectedRecords(records);
      postSelectByBoundingBox(records);
    }
  }

  public void addSelectedRecords(final Collection<? extends LayerRecord> records) {
    final List<LayerRecord> newSelectedRecords = new ArrayList<>();
    synchronized (getSync()) {
      for (final LayerRecord record : records) {
        if (addSelectedRecord(record)) {
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
    for (final LayerRecord record : records) {
      addToIndex(record);
    }
  }

  public void addToIndex(final LayerRecord record) {
    if (record != null) {
      if (record.hasGeometry()) {
        final LayerRecordQuadTree index = getIndex();
        addRecordToCache(this.cacheIdIndex, record);
        final LayerRecord recordProxy = record.newRecordProxy();
        index.addRecord(recordProxy);
      }
    }
  }

  public void addUserReadOnlyFieldNames(final Collection<String> userReadOnlyFieldNames) {
    if (userReadOnlyFieldNames != null) {
      this.userReadOnlyFieldNames.addAll(userReadOnlyFieldNames);
    }
  }

  public void cancelChanges() {
    try {
      synchronized (this.getEditSync()) {
        boolean cancelled = true;
        try (
          BaseCloseable eventsEnabled = eventsDisabled()) {
          cancelled &= internalCancelChanges(this.cacheIdNew);
          cancelled &= internalCancelChanges(this.cacheIdDeleted);
          cancelled &= internalCancelChanges(this.cacheIdModified);
          clearSelectedRecordsIndex();
          cleanCachedRecords();
        } finally {
          fireRecordsChanged();
        }
        if (!cancelled) {
          JOptionPane.showMessageDialog(getMapPanel(),
            "<html><p>There was an error cancelling changes for one or more records.</p>" + "<p>"
              + getPath() + "</p>" + "<p>Check the logging panel for details.</html>",
            "Error Cancelling Changes", JOptionPane.ERROR_MESSAGE);
        }
      }
    } finally {
      fireHasChangedRecords();
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

  protected void cleanCachedRecords() {
    System.gc();
  }

  public void clearCachedRecords(final Label cacheId) {
    synchronized (getSync()) {
      this.recordsByCacheId.remove(cacheId);
    }
  }

  public void clearHighlightedRecords() {
    synchronized (getSync()) {
      clearCachedRecords(this.cacheIdHighlighted);
      cleanCachedRecords();
    }
    fireHighlighted();
  }

  public void clearSelectedRecords() {
    final List<LayerRecord> selectedRecords = getSelectedRecords();
    synchronized (getSync()) {
      clearCachedRecords(this.cacheIdSelected);
      clearCachedRecords(this.cacheIdHighlighted);
      clearSelectedRecordsIndex();
    }
    firePropertyChange(RECORDS_SELECTED, selectedRecords, Collections.emptyList());
    fireSelected();
  }

  protected void clearSelectedRecordsIndex() {
    this.selectedRecordsIndex = null;
  }

  @Override
  public AbstractRecordLayer clone() {
    final AbstractRecordLayer clone = (AbstractRecordLayer)super.clone();
    clone.recordsByCacheId = new HashMap<>();
    clone.fieldNames = new ArrayList<>(this.fieldNames);
    clone.fieldNamesSetNames = new ArrayList<>(this.fieldNamesSetNames);
    clone.fieldNamesSets = new HashMap<>(this.fieldNamesSets);
    clone.formRecords = new LinkedList<>();
    clone.formComponents = new LinkedList<>();
    clone.formWindows = new LinkedList<>();
    clone.index = new LayerRecordQuadTree(getGeometryFactory());
    clone.selectedRecordsIndex = null;
    clone.proxiedRecords = new HashSet<>();
    clone.filter = this.filter.clone();
    clone.editSync = new Object();
    clone.userReadOnlyFieldNames = new LinkedHashSet<>(this.userReadOnlyFieldNames);

    return clone;
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
      final List<Record> copies = new ArrayList<>();
      for (final LayerRecord record : records) {
        final ArrayRecord recordCopy = new ArrayRecord(recordDefinition, record);
        copies.add(recordCopy);
      }
      final RecordReader reader = new ListRecordReader(recordDefinition, copies);
      final RecordReaderTransferable transferable = new RecordReaderTransferable(reader);
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
    this.index.clear();
    this.recordsByCacheId.clear();
    this.selectedRecordsIndex = null;
  }

  public void deleteRecord(final LayerRecord record) {
    final List<LayerRecord> records = Collections.singletonList(record);
    deleteRecords(records);
  }

  public void deleteRecordAndSaveChanges(final LayerRecord record) {
    deleteRecord(record);
    saveChanges(record);
  }

  protected boolean deleteRecordDo(final LayerRecord record) {
    final boolean isNew = isNew(record);
    deleteRecordPre(record);

    if (!isNew) {
      addRecordToCache(this.cacheIdDeleted, record);
    }
    record.setState(RecordState.DELETED);
    deleteRecordPost(record);
    return true;
  }

  protected void deleteRecordPost(final LayerRecord record) {
  }

  protected void deleteRecordPre(final LayerRecord record) {
    removeFromIndex(record);
    removeRecordFromCache(record);
  }

  public void deleteRecords(final Collection<? extends LayerRecord> records) {
    final List<LayerRecord> recordsDeleted = new ArrayList<>();
    final List<LayerRecord> recordsSelected = new ArrayList<>();
    try (
      BaseCloseable eventsEnabled = eventsDisabled()) {
      synchronized (this.getEditSync()) {
        final boolean canDelete = isCanDeleteRecords();
        for (final LayerRecord record : records) {
          final boolean selected = isSelected(record);
          boolean deleted = false;
          if (removeRecordFromCache(this.cacheIdNew, record)) {
            removeRecordFromCache(record);
            record.setState(RecordState.DELETED);
            deleted = true;
          } else if (canDelete) {
            if (deleteRecordDo(record)) {
              deleted = true;
            }
          }
          removeForm(record);
          if (deleted) {
            final LayerRecord recordProxy = record.newRecordProxy();
            recordsDeleted.add(recordProxy);
            if (selected) {
              removeSelectedRecord(recordProxy);
              recordsSelected.add(recordProxy);
            }
          }
        }
      }
    }

    deleteRecordsPost(recordsDeleted, recordsSelected);
  }

  public void deleteRecords(final LayerRecord... records) {
    deleteRecords(Arrays.asList(records));
  }

  protected void deleteRecordsPost(final List<LayerRecord> recordsDeleted,
    final List<LayerRecord> recordsSelected) {
    if (!recordsSelected.isEmpty()) {
      clearSelectedRecordsIndex();
      firePropertyChange(RECORDS_SELECTED, recordsSelected, null);
      fireSelected();
    }
    if (!recordsDeleted.isEmpty()) {
      firePropertyChange(RECORDS_DELETED, null, recordsDeleted);
      fireHasChangedRecords();
    }
  }

  public void deleteSelectedRecords() {
    final List<LayerRecord> selectedRecords = getSelectedRecords();
    deleteRecords(selectedRecords);
  }

  public void exportRecords(final Iterable<LayerRecord> records,
    final Predicate<? super LayerRecord> filter, final Map<String, Boolean> orderBy,
    final Object target) {
    if (Property.hasValue(records) && target != null) {
      final List<LayerRecord> exportRecords = Lists.toArray(records);

      Records.filterAndSort(exportRecords, filter, orderBy);

      if (!exportRecords.isEmpty()) {
        final RecordDefinition recordDefinition = getRecordDefinition();
        RecordIo.copyRecords(recordDefinition, exportRecords, target);
      }
    }
  }

  public void exportRecords(final Query query, final Object target) {
    final RecordDefinition recordDefinition = getRecordDefinition();
    if (recordDefinition != null) {
      try (
        RecordWriter writer = RecordWriter.newRecordWriter(recordDefinition, target)) {
        forEachRecord(query, writer::write);
      }
    }
  }

  protected void fireHasChangedRecords() {
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
    final int selectionCount = getSelectionCount();
    final boolean selected = selectionCount > 0;
    firePropertyChange("hasSelectedRecords", !selected, selected);
    firePropertyChange("selectionCount", -1, selectionCount);
  }

  protected void forEachRecord(final Query query, final Consumer<? super LayerRecord> consumer) {
  }

  public void forEachRecordChanged(final Query query,
    final Consumer<? super LayerRecord> consumer) {
    final List<LayerRecord> records = getRecordsChanged();
    query.forEachRecord(records, consumer);
  }

  protected void forEachRecordProxy(final Query query,
    final Consumer<? super LayerRecord> consumer) {
    forEachRecord(query, (record) -> {
      final LayerRecord proxyRecord = newProxyLayerRecord(record);
      consumer.accept(proxyRecord);
    });
  }

  @SuppressWarnings("unchecked")
  protected <V extends LayerRecord> V getCachedRecord(final Identifier identifier) {
    return (V)getRecordById(identifier);
  }

  @SuppressWarnings("unchecked")
  protected <V extends LayerRecord> V getCachedRecord(final Record record) {
    return (V)record;
  }

  protected final Label getCacheIdDeleted() {
    return this.cacheIdDeleted;
  }

  public Label getCacheIdForm() {
    return this.cacheIdForm;
  }

  protected final Label getCacheIdHighlighted() {
    return this.cacheIdHighlighted;
  }

  public Label getCacheIdIndex() {
    return this.cacheIdIndex;
  }

  protected final Label getCacheIdModified() {
    return this.cacheIdModified;
  }

  protected final Label getCacheIdNew() {
    return this.cacheIdNew;
  }

  public Set<Label> getCacheIds(LayerRecord record) {
    record = getProxiedRecord(record);
    if (isLayerRecord(record)) {
      synchronized (getSync()) {
        return getCacheIdsDo(record);
      }
    } else {
      return Collections.emptySet();
    }
  }

  protected Set<Label> getCacheIdsDo(final LayerRecord record) {
    final Set<Label> cacheIds = new HashSet<>();
    for (final Entry<Label, Collection<LayerRecord>> entry : this.recordsByCacheId.entrySet()) {
      final Collection<LayerRecord> records = entry.getValue();
      if (records.contains(record)) {
        final Label cacheId = entry.getKey();
        cacheIds.add(cacheId);
      }
    }
    return cacheIds;
  }

  protected final Label getCacheIdSelected() {
    return this.cacheIdSelected;
  }

  @Override
  public Collection<Class<?>> getChildClasses() {
    return Collections.<Class<?>> singleton(AbstractRecordLayerRenderer.class);
  }

  @Override
  public CoordinateSystem getCoordinateSystem() {
    final GeometryFactory geometryFactory = getGeometryFactory();
    if (geometryFactory == null) {
      return null;
    } else {
      return geometryFactory.getCoordinateSystem();
    }
  }

  public synchronized Object getEditSync() {
    if (this.editSync == null) {
      this.editSync = new Object();
    }
    return this.editSync;
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
    BoundingBox boundingBox = geometryFactory.newBoundingBoxEmpty();
    for (final Record record : getHighlightedRecords()) {
      final Geometry geometry = record.getGeometry();
      boundingBox = boundingBox.expandToInclude(geometry);
    }
    return boundingBox;
  }

  public int getHighlightedCount() {
    return getRecordCountCached(this.cacheIdHighlighted);
  }

  public Collection<LayerRecord> getHighlightedRecords() {
    return getRecordsCached(this.cacheIdHighlighted);
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

  public LayerRecordQuadTree getIndex() {
    return this.index;
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
      final String sourceIdFieldName = getIdFieldName();
      final Object id1 = record1.getValue(sourceIdFieldName);
      final Object id2 = record2.getValue(sourceIdFieldName);
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
        newValues.remove(getIdFieldName());
        return new ArrayRecord(getRecordDefinition(), newValues);
      }
    }
  }

  public Map<String, Object> getPasteNewValues(final Record sourceRecord) {
    final RecordDefinition recordDefinition = getRecordDefinition();
    final Set<String> ignoreFieldNames = getIgnorePasteFieldNames();
    final Map<String, Object> newValues = new LinkedHashMap<>();
    for (final String fieldName : recordDefinition.getFieldNames()) {
      if (!ignoreFieldNames.contains(fieldName)) {
        final Object value = sourceRecord.getValue(fieldName);
        if (value != null) {
          newValues.put(fieldName, value);
        }
      }
    }
    final FieldDefinition geometryFieldDefinition = recordDefinition.getGeometryField();
    if (geometryFieldDefinition != null) {
      final GeometryFactory geometryFactory = getGeometryFactory();
      Geometry sourceGeometry = sourceRecord.getGeometry();
      final String geometryFieldName = geometryFieldDefinition.getName();
      if (sourceGeometry == null) {
        final Object value = sourceRecord.getValue(geometryFieldName);
        sourceGeometry = geometryFieldDefinition.toFieldValue(value);
      }
      Geometry geometry = geometryFieldDefinition.toFieldValue(sourceGeometry);
      if (geometry == null) {
        if (sourceGeometry != null) {
          newValues.put(geometryFieldName, sourceGeometry);
        }
      } else {
        geometry = geometry.convertGeometry(geometryFactory);
        newValues.put(geometryFieldName, geometry);
      }
    }
    return newValues;
  }

  protected Geometry getPasteRecordGeometry(final LayerRecord record, final boolean alert) {
    try {
      if (record == null) {
        return null;
      } else {
        final RecordDefinition recordDefinition = getRecordDefinition();
        final FieldDefinition geometryField = recordDefinition.getGeometryField();
        if (geometryField != null) {
          final MapPanel parentComponent = getMapPanel();
          Geometry geometry = null;
          DataType geometryDataType = null;
          Class<?> layerGeometryClass = null;
          final GeometryFactory geometryFactory = getGeometryFactory();
          geometryDataType = geometryField.getDataType();
          layerGeometryClass = geometryDataType.getJavaClass();
          RecordReader reader = ClipboardUtil
            .getContents(RecordReaderTransferable.DATA_OBJECT_READER_FLAVOR);
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
                      JOptionPane.showMessageDialog(parentComponent,
                        "Clipboard does not contain a record with a geometry.", "Paste Geometry",
                        JOptionPane.ERROR_MESSAGE);
                    }
                    return null;
                  }
                  geometry = geometryFactory.geometry(layerGeometryClass, sourceGeometry);
                  if (geometry == null) {
                    if (alert) {
                      JOptionPane.showMessageDialog(parentComponent,
                        "Clipboard should contain a record with a " + geometryDataType + " not a "
                          + sourceGeometry.getGeometryType() + ".",
                        "Paste Geometry", JOptionPane.ERROR_MESSAGE);
                    }
                    return null;
                  }
                } else {
                  if (alert) {
                    JOptionPane.showMessageDialog(parentComponent,
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
                JOptionPane.showMessageDialog(parentComponent,
                  "Clipboard does not contain a record with a geometry.", "Paste Geometry",
                  JOptionPane.ERROR_MESSAGE);
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

  @Override
  public PathName getPathName() {
    final RecordDefinition recordDefinition = getRecordDefinition();
    if (recordDefinition == null) {
      return null;
    } else {
      return recordDefinition.getPathName();
    }
  }

  protected LayerRecord getProxiedRecord(LayerRecord record) {
    if (record instanceof AbstractProxyLayerRecord) {
      final AbstractProxyLayerRecord proxy = (AbstractProxyLayerRecord)record;
      record = proxy.getRecordProxied();
    }
    return record;
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

  protected int getRecordCountCached(final Label cacheId) {
    final Collection<LayerRecord> cachedRecords = this.recordsByCacheId.get(cacheId);
    if (cachedRecords == null) {
      return 0;
    } else {
      return cachedRecords.size();
    }
  }

  public int getRecordCountDeleted() {
    return getRecordCountCached(this.cacheIdDeleted);
  }

  public int getRecordCountModified() {
    return getRecordCountCached(this.cacheIdModified);
  }

  public int getRecordCountNew() {
    return getRecordCountCached(this.cacheIdNew);
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

  protected LayerRecord getRecordProxied(final LayerRecord record) {
    if (record instanceof AbstractProxyLayerRecord) {
      final AbstractProxyLayerRecord proxyRecord = (AbstractProxyLayerRecord)record;
      return proxyRecord.getRecordProxied();
    } else {
      return record;
    }
  }

  public List<LayerRecord> getRecords() {
    throw new UnsupportedOperationException();
  }

  @SuppressWarnings({
    "unchecked", "rawtypes"
  })
  public <R extends LayerRecord> List<R> getRecords(BoundingBox boundingBox) {
    if (hasGeometryField()) {
      boundingBox = convertBoundingBox(boundingBox);
      if (Property.hasValue(boundingBox)) {
        final LayerRecordQuadTree index = getIndex();
        final List<R> records = (List)index.queryIntersects(boundingBox);
        return records;
      }
    }
    return Collections.emptyList();
  }

  @SuppressWarnings({
    "unchecked", "rawtypes"
  })
  public <R extends LayerRecord> List<R> getRecords(Geometry geometry, final double distance) {
    if (geometry == null || !hasGeometryField()) {
      return Collections.emptyList();
    } else {
      geometry = convertGeometry(geometry);
      final LayerRecordQuadTree index = getIndex();
      return (List)index.getRecordsDistance(geometry, distance);
    }
  }

  public <R extends LayerRecord> List<R> getRecords(final Map<String, ? extends Object> filter) {
    final RecordDefinition recordDefinition = getRecordDefinition();
    final Query query = Query.and(recordDefinition, filter);
    return getRecords(query);
  }

  public <R extends LayerRecord> List<R> getRecords(final PathName pathName) {
    final Query query = new Query(pathName);
    return getRecords(query);
  }

  public <R extends LayerRecord> List<R> getRecords(final Query query) {
    final List<R> records = new ArrayList<>();
    forEachRecord(query, (final LayerRecord record) -> {
      final R proxyRecord = newProxyLayerRecord(record);
      records.add(proxyRecord);
    });
    return records;
  }

  public List<LayerRecord> getRecordsBackground(final BoundingBox boundingBox) {
    return getRecords(boundingBox);
  }

  public <R extends LayerRecord> List<R> getRecordsCached(final Label cacheId) {
    synchronized (getSync()) {
      final List<R> records = new ArrayList<>();
      final Collection<LayerRecord> cachedRecords = this.recordsByCacheId.get(cacheId);
      if (cachedRecords != null) {
        for (final LayerRecord record : cachedRecords) {
          final R proxyRecord = newProxyLayerRecord(record);
          records.add(proxyRecord);
        }
      }
      return records;
    }
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
    return getRecordsCached(this.cacheIdDeleted);
  }

  public <R extends LayerRecord> Collection<R> getRecordsModified() {
    return getRecordsCached(this.cacheIdModified);
  }

  public <R extends LayerRecord> List<R> getRecordsNew() {
    return getRecordsCached(this.cacheIdNew);
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
    final List<LayerRecord> records = getRecords(boundingBox);
    for (final Iterator<LayerRecord> iterator = records.iterator(); iterator.hasNext();) {
      final LayerRecord layerRecord = iterator.next();
      if (!isVisible(layerRecord) || isDeleted(layerRecord)) {
        iterator.remove();
      }
    }
    return records;
  }

  @Override
  public BoundingBox getSelectedBoundingBox() {
    BoundingBox boundingBox = super.getSelectedBoundingBox();
    for (final Record record : getSelectedRecords()) {
      final Geometry geometry = record.getGeometry();
      boundingBox = boundingBox.expandToInclude(geometry);
    }
    return boundingBox;
  }

  public List<LayerRecord> getSelectedRecords() {
    return getRecordsCached(this.cacheIdSelected);
  }

  public List<LayerRecord> getSelectedRecords(final BoundingBox boundingBox) {
    final LayerRecordQuadTree index = getSelectedRecordsIndex();
    return index.queryIntersects(boundingBox);
  }

  protected LayerRecordQuadTree getSelectedRecordsIndex() {
    if (this.selectedRecordsIndex == null) {
      final List<LayerRecord> selectedRecords = getSelectedRecords();
      final LayerRecordQuadTree index = new LayerRecordQuadTree(getProject().getGeometryFactory(),
        selectedRecords);
      this.selectedRecordsIndex = index;
    }
    return this.selectedRecordsIndex;
  }

  public int getSelectionCount() {
    return getRecordCountCached(this.cacheIdSelected);
  }

  public Collection<String> getSnapLayerPaths() {
    return getProperty("snapLayers", Collections.<String> emptyList());
  }

  public Collection<String> getUserReadOnlyFieldNames() {
    return Collections.unmodifiableSet(this.userReadOnlyFieldNames);
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
        final boolean hasPermission = permissions.contains(permission);
        return hasPermission;
      }
    }
  }

  @Override
  protected boolean initializeDo() {
    initRecordMenu();
    return super.initializeDo();
  }

  protected LayerRecordMenu initRecordMenu() {
    final LayerRecordMenu menu = new LayerRecordMenu(this);
    this.recordMenu = menu;
    if (this.recordDefinition != null) {
      final RecordDefinition recordDefinition = getRecordDefinition();
      final boolean hasGeometry = recordDefinition.hasGeometryField();

      final Predicate<LayerRecord> modified = LayerRecord::isModified;
      final Predicate<LayerRecord> notDeleted = ((Predicate<LayerRecord>)this::isDeleted).negate();
      final Predicate<LayerRecord> modifiedOrDeleted = modified.or(LayerRecord::isDeleted);

      final EnableCheck editableEnableCheck = this::isEditable;

      menu.addGroup(0, "default");
      menu.addGroup(1, "record");
      menu.addGroup(2, "dnd");

      final MenuFactory layerMenuFactory = MenuFactory.findMenu(this);
      if (layerMenuFactory != null) {
        menu.addComponentFactory("default", 0, new WrappedMenuFactory("Layer", layerMenuFactory));
      }

      menu.addMenuItem("record", "View/Edit Record", "table_edit", notDeleted, this::showForm);

      if (hasGeometry) {
        menu.addMenuItem("record", "Zoom to Record", "magnifier_zoom_selected", notDeleted,
          this::zoomToRecord);
        menu.addMenuItem("record", "Pan to Record", "pan_selected", notDeleted, (record) -> {
          final MapPanel mapPanel = getMapPanel();
          if (mapPanel != null) {
            mapPanel.panToRecord(record);
          }
        });
        final MenuFactory editMenu = new MenuFactory("Edit Record Operations");
        editMenu.setEnableCheck(LayerRecordMenu.enableCheck(notDeleted));
        final DataType geometryDataType = recordDefinition.getGeometryField().getDataType();
        if (geometryDataType == DataTypes.LINE_STRING
          || geometryDataType == DataTypes.MULTI_LINE_STRING) {
          if (DirectionalFields.getProperty(recordDefinition).hasDirectionalFields()) {
            LayerRecordMenu.addMenuItem(editMenu, "geometry", LayerRecordForm.FLIP_RECORD_NAME,
              LayerRecordForm.FLIP_RECORD_ICON, editableEnableCheck,
              this::actionFlipRecordOrientation);

            LayerRecordMenu.addMenuItem(editMenu, "geometry",
              LayerRecordForm.FLIP_LINE_ORIENTATION_NAME,
              LayerRecordForm.FLIP_LINE_ORIENTATION_ICON, editableEnableCheck,
              this::actionFlipLineOrientation);

            LayerRecordMenu.addMenuItem(editMenu, "geometry", LayerRecordForm.FLIP_FIELDS_NAME,
              LayerRecordForm.FLIP_FIELDS_ICON, editableEnableCheck, this::actionFlipFields);
          } else {
            LayerRecordMenu.addMenuItem(editMenu, "geometry", "Flip Line Orientation", "flip_line",
              editableEnableCheck, this::actionFlipLineOrientation);
          }
        }
        menu.addComponentFactory("record", editMenu);
      }
      menu.addMenuItem("record", "Delete Record", "table_row_delete", LayerRecord::isDeletable,
        this::deleteRecord);

      menu.addMenuItem("record", "Revert Record", "arrow_revert", modifiedOrDeleted,
        LayerRecord::revertChanges);

      final Predicate<LayerRecord> hasModifiedEmptyFields = LayerRecord::isHasModifiedEmptyFields;
      menu.addMenuItem("record", "Revert Empty Fields", "field_empty_revert",
        hasModifiedEmptyFields, LayerRecord::revertEmptyFields);

      menu.addMenuItem("dnd", "Copy Record", "page_copy", this::copyRecordToClipboard);

      if (hasGeometry) {
        menu.addMenuItem("dnd", "Paste Geometry", "geometry_paste", this::canPasteRecordGeometry,
          this::pasteRecordGeometry);
      }
    }
    return menu;
  }

  /**
   * Cancel changes for one of the lists of changes {@link #deletedRecords},
   * {@link #newRecords}, {@link #modifiedRecords}.
   *
   * @param cacheId
   * @param records
   */
  private boolean internalCancelChanges(final Label cacheId) {
    boolean cancelled = true;
    for (final LayerRecord record : getRecordsCached(cacheId)) {
      removeFromIndex(record);
      try {
        removeRecordFromCache(cacheId, record);
        if (cacheId == this.cacheIdNew) {
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

  protected boolean internalIsDeleted(final LayerRecord record) {
    if (record == null) {
      return false;
    } else if (record.getState() == RecordState.DELETED) {
      return true;
    } else {
      return isRecordCached(this.cacheIdDeleted, record);
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

  protected boolean internalSaveChanges(final RecordSaveErrors errors, final LayerRecord record) {
    final RecordState originalState = record.getState();
    final LayerRecord layerRecord = getProxiedRecord(record);
    final boolean saved = saveChangesDo(errors, layerRecord);
    if (saved) {
      postSaveChanges(originalState, layerRecord);
    }
    return saved;
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
        if (DataTypes.POINT.equals(geometryType)) {
          return false;
        } else if (DataTypes.MULTI_POINT.equals(geometryType)) {
          return false;
        } else if (DataTypes.POLYGON.equals(geometryType)) {
          return false;
        } else if (DataTypes.MULTI_POLYGON.equals(geometryType)) {
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
        .isDataFlavorAvailable(RecordReaderTransferable.DATA_OBJECT_READER_FLAVOR)) {
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

  public boolean isDeleted(final LayerRecord record) {
    return internalIsDeleted(record);
  }

  public boolean isFieldUserReadOnly(final String fieldName) {
    return getUserReadOnlyFieldNames().contains(fieldName);
  }

  public boolean isHasCachedRecords(final Label cacheId) {
    return getRecordCountCached(cacheId) > 0;
  }

  public boolean isHasChangedRecords() {
    return isHasChanges();
  }

  @Override
  public boolean isHasChanges() {
    if (isEditable()) {
      if (isHasCachedRecords(this.cacheIdNew)) {
        return true;
      } else if (isHasCachedRecords(this.cacheIdModified)) {
        return true;
      } else if (isHasCachedRecords(this.cacheIdDeleted)) {
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
    return isExists() && isHasCachedRecords(this.cacheIdSelected);
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

    return isRecordCached(this.cacheIdHighlighted, record);
  }

  public boolean isLayerRecord(final Record record) {
    if (record == null) {
      return false;
    } else if (record.getRecordDefinition() == getRecordDefinition()) {
      return true;
    } else {
      return false;
    }
  }

  public boolean isModified(final LayerRecord record) {
    return isRecordCached(this.cacheIdModified, record);
  }

  public boolean isNew(final LayerRecord record) {
    return isRecordCached(this.cacheIdNew, record);
  }

  protected boolean isPostSaveRemoveCacheId(final Label cacheId) {
    if (cacheId == this.cacheIdDeleted || cacheId == this.cacheIdNew
      || cacheId == this.cacheIdModified) {
      return true;
    } else {
      return false;
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

  public boolean isRecordCached(final Label cacheId, LayerRecord record) {
    record = getProxiedRecord(record);
    if (isLayerRecord(record)) {
      synchronized (getSync()) {
        final Collection<LayerRecord> cachedRecords = this.recordsByCacheId.get(cacheId);
        if (cachedRecords != null) {
          return cachedRecords.contains(record);
        }
      }
    }
    return false;
  }

  public boolean isSelected(final LayerRecord record) {
    return isRecordCached(this.cacheIdSelected, record);
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
      addRecordToCache(this.cacheIdNew, record);
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

  @SuppressWarnings("unchecked")
  protected <R extends LayerRecord> R newProxyLayerRecord(final LayerRecord record) {
    return (R)record;
  }

  protected <R extends LayerRecord> List<R> newProxyLayerRecords(
    final Iterable<? extends LayerRecord> records) {
    final List<R> proxyRecords = new ArrayList<>();
    for (final LayerRecord record : records) {
      final R proxyRecord = newProxyLayerRecord(record);
      proxyRecords.add(proxyRecord);
    }
    return proxyRecords;
  }

  public UndoableEdit newSetFieldUndo(final LayerRecord record, final String fieldName,
    final Object oldValue, final Object newValue) {
    return new SetRecordFieldValueUndo(record, fieldName, oldValue, newValue);
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
        .getContents(RecordReaderTransferable.DATA_OBJECT_READER_FLAVOR);
      if (reader == null) {
        final String string = ClipboardUtil.getContents(DataFlavor.stringFlavor);
        if (Property.hasValue(string)) {
          if (string.contains("\t")) {
            final Resource tsvResource = new ByteArrayResource("t.tsv", string);
            reader = RecordReader.newRecordReader(tsvResource);
          } else {
            final Resource resource = new ByteArrayResource("t.csv", string);
            reader = RecordReader.newRecordReader(resource);
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
      LoggingEventPanel.showDialog(getMapPanel(), "Unexpected error pasting records", e);
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
        showRecordsTable(RecordLayerTableModel.MODE_RECORDS_SELECTED);
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

  protected void postSaveChanges(final RecordState originalState, final LayerRecord record) {
    postSaveDeletedRecord(record);
    postSaveModifiedRecord(record);
    postSaveNewRecord(record);
  }

  protected boolean postSaveDeletedRecord(final LayerRecord record) {
    final boolean deleted;
    synchronized (getSync()) {
      deleted = removeRecordFromCache(this.cacheIdDeleted, record);
    }
    if (deleted) {
      removeRecordFromCache(record);
      removeFromIndex(record);
      return true;
    } else {
      return false;
    }
  }

  protected boolean postSaveModifiedRecord(final LayerRecord record) {
    boolean removed;
    synchronized (getSync()) {
      removed = removeRecordFromCache(this.cacheIdModified, record);
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
      isNew = removeRecordFromCache(this.cacheIdNew, record);
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
      showRecordsTable(RecordLayerTableModel.MODE_RECORDS_SELECTED);
    }
    if (!records.isEmpty()) {
      firePropertyChange("selectedRecordsByBoundingBox", false, true);
    }
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
            if (DataType.equal(propertyName, getGeometryFieldName())) {
              final Geometry oldGeometry = (Geometry)event.getOldValue();
              updateSpatialIndex(record, oldGeometry);
              clearSelectedRecordsIndex();
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
    cleanCachedRecords();
    fireRecordsChanged();
  }

  protected void removeForm(final LayerRecord record) {
    LayerRecordForm form;
    Window window;
    synchronized (this.formRecords) {
      final LayerRecord proxiedRecord = getRecordProxied(record);
      final int index = proxiedRecord.indexOf(this.formRecords);
      if (index == -1) {
        return;
      } else {
        removeRecordFromCache(this.cacheIdForm, proxiedRecord);
        this.formRecords.remove(index);
        final Component component = this.formComponents.remove(index);
        if (component instanceof LayerRecordForm) {
          form = (LayerRecordForm)component;
        } else {
          form = null;
        }
        window = this.formWindows.remove(index);
      }
    }
    if (form != null || window != null) {
      Invoke.later(() -> {
        if (form != null) {
          form.destroy();
        }
        if (window != null) {
          SwingUtil.dispose(window);
        }
      });
    }
    cleanCachedRecords();
  }

  public boolean removeFromIndex(final BoundingBox boundingBox, final LayerRecord record) {
    final LayerRecordQuadTree index = getIndex();
    return index.removeRecord(record);
  }

  public void removeFromIndex(final Collection<? extends LayerRecord> records) {
    for (final LayerRecord record : records) {
      removeFromIndex(record);
    }
  }

  public void removeFromIndex(final LayerRecord record) {
    final Geometry geometry = record.getGeometry();
    if (geometry != null && !geometry.isEmpty()) {
      final BoundingBox boundingBox = geometry.getBoundingBox();
      removeFromIndex(boundingBox, record);
      removeRecordFromCache(this.cacheIdIndex, record);
    }
  }

  protected void removeHighlightedRecord(final LayerRecord record) {
    removeRecordFromCache(this.cacheIdHighlighted, record);
  }

  void removeProxiedRecord(final LayerRecord proxyRecord) {
    if (proxyRecord != null) {
      synchronized (proxyRecord) {
        this.proxiedRecords.remove(proxyRecord);
      }
    }
  }

  void removeProxiedRecordIdentifier(final Identifier identifier) {
    ShortCounter.deccrement(this.proxiedRecordIdentifiers, identifier);
  }

  protected boolean removeRecordFromCache(final Label cacheId, LayerRecord record) {
    record = getProxiedRecord(record);
    if (isLayerRecord(record)) {
      synchronized (getSync()) {
        return Maps.removeFromCollection(this.recordsByCacheId, cacheId, record);
      }
    }
    return false;
  }

  protected boolean removeRecordFromCache(LayerRecord record) {
    boolean removed = false;
    record = getProxiedRecord(record);
    synchronized (getSync()) {
      if (isLayerRecord(record)) {
        for (final Label cacheId : new ArrayList<>(this.recordsByCacheId.keySet())) {
          removed |= removeRecordFromCache(cacheId, record);
        }
      }
    }
    return removed;
  }

  public int removeRecordsFromCache(final Label cacheId,
    final Collection<? extends LayerRecord> records) {
    synchronized (getSync()) {
      int count = 0;
      for (final LayerRecord record : records) {
        if (removeRecordFromCache(cacheId, record)) {
          count++;
        }
      }
      cleanCachedRecords();
      return count;
    }
  }

  protected boolean removeSelectedRecord(final LayerRecord record) {
    final boolean removed = removeRecordFromCache(this.cacheIdSelected, record);
    removeHighlightedRecord(record);
    clearSelectedRecordsIndex();
    return removed;
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
        if (removeRecordFromCache(this.cacheIdDeleted, record)) {
          record.setState(RecordState.PERSISTED);
        }
        removeRecordFromCache(record);
        setSelectedHighlighted(record, selected, highlighted);
        cleanCachedRecords();
      }
    }
  }

  @Override
  public boolean saveChanges() {
    if (isExists()) {
      final List<LayerRecord> allRecords = new ArrayList<>();
      for (final Label cacheId : Arrays.asList(this.cacheIdDeleted, this.cacheIdModified,
        this.cacheIdNew)) {
        final List<LayerRecord> records = getRecordsCached(cacheId);
        allRecords.addAll(records);
      }
      return saveChanges(allRecords);
    } else {
      return false;
    }
  }

  public final boolean saveChanges(final Collection<? extends LayerRecord> records) {
    try {
      if (records.isEmpty()) {
        return true;
      } else {
        synchronized (this.getEditSync()) {
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
                final RecordSaveErrors errors = new RecordSaveErrors(this);
                try (
                  BaseCloseable eventsEnabled = eventsDisabled()) {
                  for (final LayerRecord record : validRecords) {
                    try {
                      final boolean saved = internalSaveChanges(errors, record);
                      if (!saved) {
                        errors.addRecord(record, "Unable to save record");
                      }
                    } catch (final Throwable t) {
                      errors.addRecord(record, t);
                    }
                  }
                  cleanCachedRecords();
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
    synchronized (this.getEditSync()) {
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
            final RecordSaveErrors errors = new RecordSaveErrors(this);
            try (
              BaseCloseable eventsEnabled = eventsDisabled()) {
              try {
                final boolean saved = internalSaveChanges(errors, record);
                if (!saved) {
                  errors.addRecord(record, "Unable to save record");
                }
              } catch (final Throwable t) {
                errors.addRecord(record, t);
              }
              cleanCachedRecords();
              record.fireRecordUpdated();
            } finally {
              if (!errors.showErrorDialog()) {
                allSaved.add(false);
              }
            }
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
  }

  @Override
  protected boolean saveChangesDo() {
    throw new UnsupportedOperationException();
  }

  protected boolean saveChangesDo(final RecordSaveErrors errors, final LayerRecord record) {
    return true;
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

  @Override
  public void setEditable(final boolean editable) {
    Invoke.background("Set Editable " + this, () -> {
      if (editable == false) {
        firePropertyChange("preEditable", false, true);
        final boolean hasChanges = isHasChanges();
        if (hasChanges) {
          final Integer result = Invoke.andWait(() -> {
            return JOptionPane.showConfirmDialog(JOptionPane.getRootFrame(),
              "The layer has unsaved changes. Click Yes to save changes. Click No to discard changes. Click Cancel to continue editing.",
              "Save Changes", JOptionPane.YES_NO_CANCEL_OPTION);
          });
          synchronized (getEditSync()) {
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
      synchronized (this.getEditSync()) {
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
  protected boolean setGeometryFactoryDo(final GeometryFactory geometryFactory) {
    this.index.setGeometryFactory(geometryFactory);
    return super.setGeometryFactoryDo(geometryFactory);
  }

  public void setHighlightedRecords(final Collection<LayerRecord> highlightedRecords) {
    synchronized (getSync()) {
      clearCachedRecords(this.cacheIdHighlighted);
      addHighlightedRecords(highlightedRecords);
    }
  }

  protected void setIndexRecords(final List<LayerRecord> records) {
    synchronized (getSync()) {
      if (hasGeometryField()) {
        final GeometryFactory geometryFactory = getGeometryFactory();
        final LayerRecordQuadTree index = new LayerRecordQuadTree(geometryFactory);
        final Label cacheIdIndex = getCacheIdIndex();
        clearCachedRecords(cacheIdIndex);
        if (records != null) {
          for (final LayerRecord record : records) {
            if (record.hasGeometry()) {
              addRecordToCache(cacheIdIndex, record);
              index.addRecord(record.newRecordProxy());
            }
          }
        }
        cleanCachedRecords();
        final List<LayerRecord> newRecords = getRecordsNew();
        index.addRecords(newRecords);
        this.index = index;
      }
    }
  }

  @Override
  public void setProperties(final Map<String, ? extends Object> properties) {
    if (!properties.containsKey("style")) {
      final GeometryStyleRenderer renderer = getRenderer();
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
      final String iconName = recordDefinition.getIconName();
      final Icon icon = Icons.getIcon(iconName);
      setIcon(icon);
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

  protected void setSelectedHighlighted(final LayerRecord record, final boolean selected,
    final boolean highlighted) {
    if (selected) {
      addSelectedRecord(record);
      if (highlighted) {
        addHighlightedRecord(record);
      }
    }
  }

  public void setSelectedRecords(final BoundingBox boundingBox) {
    if (isSelectable()) {
      final List<LayerRecord> records = getRecordsVisible(boundingBox);
      setSelectedRecords(records);
      postSelectByBoundingBox(records);
    }
  }

  public void setSelectedRecords(final Collection<LayerRecord> selectedRecords) {
    final List<LayerRecord> oldSelectedRecords = getSelectedRecords();
    synchronized (getSync()) {
      clearCachedRecords(this.cacheIdSelected);
      for (final LayerRecord record : selectedRecords) {
        addSelectedRecord(record);
      }
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
      final String idFieldName = recordDefinition.getIdFieldName();
      if (idFieldName == null) {
        clearSelectedRecords();
      } else {
        final Query query = Query.equal(recordDefinition, idFieldName, id);
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
      final Window window = SwingUtil.getActiveWindow();
      JOptionPane.showMessageDialog(window,
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
        final LayerRecord proxiedRecord = getRecordProxied(record);
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
            final Window parent = SwingUtil.getActiveWindow();
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
              this.formRecords.add(proxiedRecord);
              this.formComponents.add(form);
              this.formWindows.add(window);
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
    showRecordsTable(null);
  }

  public void showRecordsTable(final String fieldFilterMode) {
    Invoke.later(() -> {
      final Map<String, Object> config = Maps.newLinkedHash("fieldFilterMode", fieldFilterMode);
      showTableView(config);
    });
  }

  public List<LayerRecord> splitRecord(final LayerRecord record,
    final CloseLocation mouseLocation) {

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
          return Collections.singletonList(record);
        } else if (vertexCount - pointIndex < 2) {
          return Collections.singletonList(record);
        } else {
          line1 = line.subLine(pointIndex + 1);
          line2 = line.subLine(null, pointIndex, vertexCount - pointIndex, null);
        }

      }
      if (line1 == null || line2 == null) {
        return Collections.singletonList(record);
      }

      return splitRecord(record, line, convertedPoint, line1, line2);
    }
    return Arrays.asList(record);
  }

  /** Perform the actual split. */
  protected List<LayerRecord> splitRecord(final LayerRecord record, final LineString line,
    final Point point, final LineString line1, final LineString line2) {
    final Map<String, Object> values1 = newSplitValues(record, line, point, line1);
    final LayerRecord record1 = newLayerRecord(values1);

    final Map<String, Object> values2 = newSplitValues(record, line, point, line2);
    final LayerRecord record2 = newLayerRecord(values2);

    deleteRecord(record);

    saveChanges(record, record1, record2);

    addSelectedRecords(record1, record2);
    return Arrays.asList(record1, record2);
  }

  public List<LayerRecord> splitRecord(final LayerRecord record, final Point point) {
    final LineString line = record.getGeometry();
    final List<LineString> lines = line.split(point);
    if (lines.size() == 2) {
      final LineString line1 = lines.get(0);
      final LineString line2 = lines.get(1);
      return splitRecord(record, line, point, line1, line2);
    } else {
      return Collections.singletonList(record);
    }
  }

  @Override
  public MapEx toMap() {
    final MapEx map = super.toMap();
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
    addToMap(map, "useFieldTitles", this.useFieldTitles);
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
    removeRecordsFromCache(this.cacheIdHighlighted, records);
    fireHighlighted();
  }

  public void unHighlightRecords(final LayerRecord... records) {
    unHighlightRecords(Arrays.asList(records));
  }

  public void unSelectRecords(final BoundingBox boundingBox) {
    if (isSelectable()) {
      final List<LayerRecord> records = getRecordsVisible(boundingBox);
      unSelectRecords(records);
      if (isHasSelectedRecordsWithGeometry()) {
        showRecordsTable(RecordLayerTableModel.MODE_RECORDS_SELECTED);
      }
    }
  }

  public void unSelectRecords(final Collection<? extends LayerRecord> records) {
    final List<LayerRecord> removedRecords = new ArrayList<>();
    synchronized (getSync()) {
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

  protected void updateRecordState(final LayerRecord record) {
    final RecordState state = record.getState();
    if (state == RecordState.MODIFIED) {
      addModifiedRecord(record);
    } else if (state == RecordState.PERSISTED) {
      postSaveModifiedRecord(record);
      fireHasChangedRecords();
    }
  }

  protected void updateSpatialIndex(final LayerRecord record, final Geometry oldGeometry) {
    if (oldGeometry != null) {
      final BoundingBox oldBoundingBox = oldGeometry.getBoundingBox();
      if (removeFromIndex(oldBoundingBox, record)) {
        addToIndex(record);
      }
    }

  }

  public void zoomToBoundingBox(BoundingBox boundingBox) {
    if (!BoundingBoxUtil.isEmpty(boundingBox)) {
      final Project project = getProject();
      final GeometryFactory geometryFactory = project.getGeometryFactory();
      boundingBox = boundingBox.convert(geometryFactory);
      boundingBox = boundingBox.expandPercent(0.1);
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
    BoundingBox boundingBox = BoundingBox.empty();
    for (final Record record : records) {
      boundingBox = boundingBox.expandToInclude(record);
    }
    zoomToBoundingBox(boundingBox);
  }

  public void zoomToSelected() {
    final BoundingBox selectedBoundingBox = getSelectedBoundingBox();
    zoomToBoundingBox(selectedBoundingBox);
  }
}
