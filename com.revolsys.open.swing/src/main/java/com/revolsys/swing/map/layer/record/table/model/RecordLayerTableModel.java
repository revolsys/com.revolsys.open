package com.revolsys.swing.map.layer.record.table.model;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.function.Function;

import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.SortOrder;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import org.slf4j.LoggerFactory;

import com.revolsys.beans.PropertyChangeSupportProxy;
import com.revolsys.collection.map.LruMap;
import com.revolsys.collection.map.Maps;
import com.revolsys.equals.Equals;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.record.Record;
import com.revolsys.record.Records;
import com.revolsys.record.query.Condition;
import com.revolsys.record.query.Query;
import com.revolsys.record.query.functions.F;
import com.revolsys.record.schema.FieldDefinition;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.swing.EventQueue;
import com.revolsys.swing.action.RunnableAction;
import com.revolsys.swing.listener.EventQueueRunnableListener;
import com.revolsys.swing.map.layer.Project;
import com.revolsys.swing.map.layer.record.AbstractRecordLayer;
import com.revolsys.swing.map.layer.record.LayerRecord;
import com.revolsys.swing.map.layer.record.LayerRecordMenu;
import com.revolsys.swing.map.layer.record.LoadingRecord;
import com.revolsys.swing.map.layer.record.table.RecordLayerTable;
import com.revolsys.swing.map.layer.record.table.predicate.DeletedPredicate;
import com.revolsys.swing.map.layer.record.table.predicate.ModifiedPredicate;
import com.revolsys.swing.map.layer.record.table.predicate.NewPredicate;
import com.revolsys.swing.menu.BaseJPopupMenu;
import com.revolsys.swing.parallel.Invoke;
import com.revolsys.swing.table.SortableTableModel;
import com.revolsys.swing.table.record.RecordRowTable;
import com.revolsys.swing.table.record.filter.RecordRowPredicateRowFilter;
import com.revolsys.swing.table.record.model.RecordRowTableModel;
import com.revolsys.swing.table.selection.NullSelectionModel;
import com.revolsys.util.Property;
import com.revolsys.util.function.Consumer2;

public class RecordLayerTableModel extends RecordRowTableModel
  implements SortableTableModel, PropertyChangeListener, PropertyChangeSupportProxy {
  public static final String MODE_RECORDS_ALL = "all";

  public static final String MODE_RECORDS_CHANGED = "edits";

  public static final String MODE_RECORDS_SELECTED = "selected";

  private static final long serialVersionUID = 1L;

  public static RecordLayerTable newTable(final AbstractRecordLayer layer) {
    final RecordDefinition recordDefinition = layer.getRecordDefinition();
    if (recordDefinition == null) {
      return null;
    } else {
      final RecordLayerTableModel model = new RecordLayerTableModel(layer);
      final RecordLayerTable table = new RecordLayerTable(model);

      ModifiedPredicate.add(table);
      NewPredicate.add(table);
      DeletedPredicate.add(table);

      model.selectionChangedListener = EventQueue.addPropertyChange(layer, "hasSelectedRecords",
        () -> selectionChanged(table, model));
      return table;
    }
  }

  public static final void selectionChanged(final RecordLayerTable table,
    final RecordLayerTableModel tableModel) {
    table.repaint();
  }

  private List<LayerRecord> cachedRecords = Collections.emptyList();

  private boolean countLoaded;

  private LayerRecord currentRecord;

  private int currentRowIndex;

  private final Consumer<Long> defaultRefreshMethod = (index) -> {
    fireTableDataChanged();
  };

  private Consumer2<Query, Object> exportRecordsMethod;

  private final Map<String, Consumer2<Query, Object>> exportRecordsMethodByFilterMode = new HashMap<>();

  private String fieldFilterMode = MODE_RECORDS_ALL;

  private List<String> fieldFilterModes = new ArrayList<>();

  private Condition filter = Condition.ALL;

  private boolean filterByBoundingBox;

  private final LinkedList<Condition> filterHistory = new LinkedList<>();

  private Function<Integer, LayerRecord> getRecordMethod;

  private final Map<String, Function<Integer, LayerRecord>> getRecordMethodByFilterMode = new HashMap<>();

  private ListSelectionModel highlightedModel = new RecordLayerHighlightedListSelectionModel(this);

  private long lastRefreshIndex = Long.MIN_VALUE;

  private final AbstractRecordLayer layer;

  private final Set<Integer> loadingPageNumbers = new LinkedHashSet<>();

  private final LayerRecord loadingRecord;

  private int notMatchingFilterCount;

  private Map<String, Boolean> orderBy;

  private Comparator<Record> orderByComparatorIdentifier = null;

  private final Map<Integer, List<LayerRecord>> pageCache = new LruMap<>(5);

  private final int pageSize = 40;

  private Consumer<PropertyChangeEvent> propertyChangeMethod;

  private final Map<String, Consumer<PropertyChangeEvent>> propertyChangeMethodByFilterMode = new HashMap<>();

  private int recordCount;

  private Callable<Integer> recordCountMethod;

  private final Map<String, Callable<Integer>> recordCountMethodByFilterMode = new HashMap<>();

  private SwingWorker<?, ?> recordCountWorker;

  private final AtomicLong refreshIndex = new AtomicLong(Long.MIN_VALUE + 1);

  private Consumer<Long> refreshMethod = this.defaultRefreshMethod;

  private final Map<String, Consumer<Long>> refreshMethodByFilterMode = new HashMap<>();

  private RowFilter<? extends RecordRowTableModel, Integer> rowFilterCondition = null;

  private EventQueueRunnableListener selectionChangedListener;

  private ListSelectionModel selectionModel = new RecordLayerListSelectionModel(this);

  private List<String> sortableModes = new ArrayList<>();

  private final Object sync = new Object();

  public RecordLayerTableModel(final AbstractRecordLayer layer) {
    super(layer.getRecordDefinition());
    this.layer = layer;
    setFieldNames(layer.getFieldNamesSet());
    Property.addListener(layer, this);
    setEditable(true);
    setReadOnlyFieldNames(layer.getUserReadOnlyFieldNames());
    this.loadingRecord = new LoadingRecord(layer);

    addFieldFilterMode(MODE_RECORDS_ALL, false, this::refreshRecordsAll,
      this::getRecordCountBackground, this::getRecordPage, layer::exportRecords,
      this::propertyChangeAll);

    addFieldFilterMode(MODE_RECORDS_CHANGED, true, this::refreshRecordsChanged,
      this::getRecordCount, this::getRecordCached, this::exportRecordsCached,
      this::propertyChangeChanged);

    addFieldFilterMode(MODE_RECORDS_SELECTED, true, this::refreshRecordsSelected,
      this::getRecordCount, this::getRecordCached, this::exportRecordsCached,
      this::propertyChangeSelected);
  }

  protected void addCachedRecord(final LayerRecord record) {
    this.cachedRecords.add(record);
    this.recordCount++;
    fireTableDataChanged();
  }

  protected void addFieldFilterMode(final String filterMode, final boolean sortable,
    final Consumer<Long> refresh, final Callable<Integer> rowCount,
    final Function<Integer, LayerRecord> getRecordMethod,
    final Consumer2<Query, Object> exportRecordsMethod,
    final Consumer<PropertyChangeEvent> propertyChangeMethod) {
    if (!this.fieldFilterModes.contains(filterMode)) {
      this.fieldFilterModes.add(filterMode);
    }
    if (sortable) {
      if (!this.sortableModes.contains(filterMode)) {
        this.sortableModes.add(filterMode);
      }
    } else {
      this.sortableModes.remove(filterMode);
    }
    this.refreshMethodByFilterMode.put(filterMode, refresh);
    this.recordCountMethodByFilterMode.put(filterMode, rowCount);
    this.getRecordMethodByFilterMode.put(filterMode, getRecordMethod);
    this.exportRecordsMethodByFilterMode.put(filterMode, exportRecordsMethod);
    this.propertyChangeMethodByFilterMode.put(filterMode, propertyChangeMethod);
    updateMethods();
  }

  protected boolean canRefreshFinish(final long index) {
    if (index >= this.lastRefreshIndex) {
      this.lastRefreshIndex = index;
      return true;
    } else {
      return false;
    }
  }

  protected void clearLoading() {
    synchronized (getSync()) {
      this.loadingPageNumbers.clear();
      this.pageCache.clear();
      this.setRowCount(0);
      this.countLoaded = false;
    }
  }

  @Override
  public void dispose() {
    this.highlightedModel = NullSelectionModel.INSTANCE;
    this.selectionModel = NullSelectionModel.INSTANCE;
    getTable().setSelectionModel(this.selectionModel);
    Property.removeListener(this.layer, this);
    Property.removeListener(this.layer, "hasSelectedRecords", this.selectionChangedListener);
    this.selectionChangedListener = null;
    super.dispose();
  }

  protected void exportRecords(final List<LayerRecord> records, final Object target) {
    final Condition filter = getFilter();
    final Map<String, Boolean> orderBy = getOrderBy();
    final AbstractRecordLayer layer = getLayer();
    layer.exportRecords(records, filter, orderBy, target);
  }

  public void exportRecords(final Object target) {
    final Query query = getFilterQuery();
    this.exportRecordsMethod.accept(query, target);
  }

  protected void exportRecordsCached(final Query query, final Object target) {
    final List<LayerRecord> records = getRecordsCached();
    final Condition filter = query.getWhereCondition();
    final Map<String, Boolean> orderBy = query.getOrderBy();
    final AbstractRecordLayer layer = getLayer();
    layer.exportRecords(records, filter, orderBy, target);

  }

  public String getFieldFilterMode() {
    return this.fieldFilterMode;
  }

  public Condition getFilter() {
    return this.filter;
  }

  public LinkedList<Condition> getFilterHistory() {
    return this.filterHistory;
  }

  protected Query getFilterQuery() {
    final Query query = this.layer.getQuery();
    final Condition filter = getFilter();
    query.and(filter);
    query.setOrderBy(this.orderBy);
    if (this.filterByBoundingBox) {
      final Project project = this.layer.getProject();
      final BoundingBox viewBoundingBox = project.getViewBoundingBox();
      final RecordDefinition recordDefinition = this.layer.getRecordDefinition();
      final FieldDefinition geometryField = recordDefinition.getGeometryField();
      if (geometryField != null) {
        query.and(F.envelopeIntersects(geometryField, viewBoundingBox));
      }
    }
    return query;
  }

  public String getGeometryFilterMode() {
    if (this.filterByBoundingBox) {
      return "boundingBox";
    }
    return "all";
  }

  public final ListSelectionModel getHighlightedModel() {
    return this.highlightedModel;
  }

  public AbstractRecordLayer getLayer() {
    return this.layer;
  }

  @Override
  public BaseJPopupMenu getMenu(final int rowIndex, final int columnIndex) {
    final LayerRecord record = getRecord(rowIndex);
    if (record instanceof LoadingRecord) {
    } else if (record != null) {
      final AbstractRecordLayer layer = getLayer();
      if (layer != null) {
        final LayerRecordMenu menu = record.getMenu();

        final BaseJPopupMenu popupMenu = menu.newJPopupMenu();
        popupMenu.addSeparator();
        final RecordLayerTable table = getTable();
        final boolean editingCurrentCell = isCellEditable(rowIndex, columnIndex);
        if (editingCurrentCell) {
          popupMenu.add(RunnableAction.newMenuItem("Cut Field Value", "cut", table::cutFieldValue));
        }
        popupMenu
          .add(RunnableAction.newMenuItem("Copy Field Value", "page_copy", table::copyFieldValue));
        if (editingCurrentCell) {
          popupMenu.add(
            RunnableAction.newMenuItem("Paste Field Value", "paste_plain", table::pasteFieldValue));
        }
        return popupMenu;
      }
    }
    return null;
  }

  public Map<String, Boolean> getOrderBy() {
    return this.orderBy;
  }

  public int getPageSize() {
    return this.pageSize;
  }

  @SuppressWarnings("unchecked")
  @Override
  public <V extends Record> V getRecord(final int rowIndex) {
    LayerRecord record = null;
    if (rowIndex >= 0) {
      if (rowIndex == this.currentRowIndex) {
        record = this.currentRecord;
      } else {
        record = this.getRecordMethod.apply(rowIndex);
        if (!(record instanceof LoadingRecord)) {
          this.currentRecord = record;
          this.currentRowIndex = rowIndex;
        }
      }
    }
    return (V)record;
  }

  protected LayerRecord getRecordCached(final int index) {
    final List<LayerRecord> records = getRecordsCached();
    if (index < records.size()) {
      return records.get(index);
    } else {
      return null;
    }
  }

  protected final int getRecordCount() {
    return this.recordCount;
  }

  protected int getRecordCountBackground() {
    synchronized (getSync()) {
      if (this.countLoaded) {
        int count = this.recordCount;
        final int newRecordCount = getRecordCountPageChanges();
        count += newRecordCount;
        count -= this.notMatchingFilterCount;
        return count;
      } else {
        if (this.recordCountWorker == null) {
          final long refreshIndex = getRefreshIndexNext();
          this.recordCountWorker = Invoke.background("Query row count " + this.layer.getName(),
            this::getRecordCountLayer, (rowCount) -> {
              if (canRefreshFinish(refreshIndex)) {
                this.recordCount = rowCount;
                this.countLoaded = true;
                this.recordCountWorker = null;
                setRowCount(rowCount);
                fireTableDataChanged();
              }
            });
        }
        return 0;
      }
    }
  }

  protected int getRecordCountLayer() {
    final Query query = getFilterQuery();
    if (query == null) {
      return this.layer.getRecordCount();
    } else {
      return this.layer.getRecordCount(query);
    }
  }

  protected int getRecordCountPageChanges() {
    return this.cachedRecords.size();
  }

  protected LayerRecord getRecordPage(final int row) {
    final int recordCountChanges = getRecordCountPageChanges();
    if (row < recordCountChanges) {
      return getRecordPageChange(row);
    } else {
      final int persistedRow = row - recordCountChanges;
      final int recordCount = getRecordCount() + this.notMatchingFilterCount;
      if (row < recordCount) {
        final int pageSize = getPageSize();
        final int pageNumber = persistedRow / pageSize;
        final int recordNumber = persistedRow % pageSize;
        final LayerRecord record = getRecordPagePersisted(pageNumber, recordNumber);
        return record;
      }
      return null;
    }
  }

  protected LayerRecord getRecordPageChange(final int index) {
    return this.cachedRecords.get(index);
  }

  protected LayerRecord getRecordPagePersisted(final int pageNumber, final int recordIndex) {
    synchronized (getSync()) {
      final List<LayerRecord> page = this.pageCache.get(pageNumber);
      if (page == null) {
        boolean load = false;
        synchronized (this.loadingPageNumbers) {
          if (!this.loadingPageNumbers.contains(pageNumber)) {
            this.loadingPageNumbers.add(pageNumber);
            load = true;
          }
        }
        if (load) {
          final long refreshIndex = this.refreshIndex.get();
          Invoke.background("loadPage" + getTypeName(), 2, "Loading records " + getTypeName(),
            () -> loadPage(pageNumber), //
            (records) -> {
              setRecords(refreshIndex, pageNumber, records);
            });
        }
        return this.loadingRecord;
      } else {
        if (recordIndex < page.size()) {
          final LayerRecord record = page.get(recordIndex);
          if (this.layer.isDeleted(record)) {
            return record;
          } else {
            if (this.filter.test(record)) {
              if (isRecordPageQueryChanged(record)) {
                return null;
              }
            } else {
              return null;
            }
          }
          return record;
        }
        return null;
      }
    }
  }

  protected List<LayerRecord> getRecordsCached() {
    return this.cachedRecords;
  }

  protected List<LayerRecord> getRecordsLayer(final Query query) {
    return this.layer.getRecordsPersisted(query);
  }

  private List<LayerRecord> getRecordsPageChanged() {
    this.notMatchingFilterCount = 0;
    final List<LayerRecord> records = new ArrayList<>();
    for (final LayerRecord record : this.layer.getRecordsChanged()) {
      if (this.layer.isNew(record)) {
        if (this.filter.test(record)) {
          records.add(record);
        }
      } else if (this.layer.isModified(record)) {
        if (isRecordPageQueryChanged(record)) {
          records.add(record);
          this.notMatchingFilterCount++;
        } else {
          if (!this.filter.isEmpty()) {
            final Record originalRecord = record.getOriginalRecord();
            if (this.filter.test(record) && !this.filter.test(originalRecord)) {
              this.notMatchingFilterCount++;
            }
          }
        }
      }

    }
    if (this.orderByComparatorIdentifier != null) {
      records.sort(this.orderByComparatorIdentifier);
    }
    return records;

  }

  protected List<LayerRecord> getRecordsSelected() {
    final AbstractRecordLayer layer = getLayer();
    return layer.getSelectedRecords();
  }

  protected long getRefreshIndexNext() {
    return this.refreshIndex.incrementAndGet();
  }

  @Override
  public final int getRowCount() {
    try {
      return this.recordCountMethod.call();
    } catch (final Exception e) {
      LoggerFactory.getLogger(getClass()).error("Unable to get row count", e);
      return 0;
    }
  }

  public RowFilter<? extends RecordRowTableModel, Integer> getRowFilter() {
    if (isSortable()) {
      return this.rowFilterCondition;
    } else {
      return null;
    }
  }

  public List<String> getSortableModes() {
    return this.sortableModes;
  }

  protected Object getSync() {
    return this.sync;
  }

  @Override
  public RecordLayerTable getTable() {
    return (RecordLayerTable)super.getTable();
  }

  public String getTypeName() {
    return getRecordDefinition().getPath();
  }

  public boolean isDeleted(final int rowIndex) {
    final LayerRecord record = getRecord(rowIndex);
    if (record != null) {
      final AbstractRecordLayer layer = getLayer();
      if (layer != null) {
        return layer.isDeleted(record);
      }
    }
    return false;
  }

  @Override
  public boolean isEditable() {
    return super.isEditable() && this.layer.isEditable() && this.layer.isCanEditRecords();
  }

  public boolean isEditingCell(final int rowIndex, final int columnIndex) {
    final RecordLayerTable table = getTable();
    if (table.isEditing()) {
      if (rowIndex > -1 && rowIndex == table.getEditingRow()) {
        if (columnIndex > -1 && columnIndex == table.getEditingColumn()) {
          return true;
        }
      }
    }
    return false;
  }

  public boolean isFilterByBoundingBox() {
    return this.filterByBoundingBox;
  }

  public boolean isHasFilter() {
    return this.filter != null;
  }

  public boolean isLayerRecord(final LayerRecord record) {
    return getLayer().isLayerRecord(record);
  }

  /**
   * Has the record been changed such that
   * @param record
   * @return
   */
  protected boolean isRecordPageQueryChanged(final LayerRecord record) {
    if (getLayer().isModified(record)) {
      if (this.orderByComparatorIdentifier != null) {
        final Record orginialRecord = record.getOriginalRecord();
        final int compare = this.orderByComparatorIdentifier.compare(record, orginialRecord);
        if (compare != 0) {
          return true;
        }
      }
      if (!this.filter.isEmpty()) {
        if (this.filter.test(record)) {
          final Record orginialRecord = record.getOriginalRecord();
          if (!this.filter.test(orginialRecord)) {
            return true;
          }
        }
      }
    }
    return false;
  }

  @Override
  public boolean isSelected(final boolean selected, final int rowIndex, final int columnIndex) {
    final LayerRecord object = getRecord(rowIndex);
    return this.layer.isSelected(object);
  }

  public boolean isSortable() {
    final String mode = getFieldFilterMode();
    final List<String> sortableModes = getSortableModes();
    return sortableModes.contains(mode);
  }

  protected List<LayerRecord> loadPage(final int pageNumber) {
    try {
      final Query query = getFilterQuery();
      if (query == null) {
        return Collections.emptyList();
      } else {
        query.setOffset(this.pageSize * pageNumber);
        query.setLimit(this.pageSize);
        return getRecordsLayer(query);
      }
    } finally {
      synchronized (this.loadingPageNumbers) {
        this.loadingPageNumbers.remove(pageNumber);
      }
    }
  }

  @Override
  public void propertyChange(final PropertyChangeEvent e) {
    if (this.propertyChangeMethod != null) {
      if (SwingUtilities.isEventDispatchThread()) {
        this.propertyChangeMethod.accept(e);
      } else {
        Invoke.later(() -> {
          this.propertyChangeMethod.accept(e);
        });
      }
      return;
    }
    final Object source = e.getSource();
    if (source == this.layer) {

    } else {
      if (source instanceof LayerRecord) {
        final LayerRecord record = (LayerRecord)source;
        Invoke.later(() -> {
          recordChange(record);
        });
      }
    }
  }

  protected void propertyChangeAll(final PropertyChangeEvent e) {
    final String propertyName = e.getPropertyName();
    if (Arrays.asList("filter", "query", "editable", AbstractRecordLayer.RECORD_INSERTED,
      AbstractRecordLayer.RECORDS_INSERTED, AbstractRecordLayer.RECORD_DELETED,
      AbstractRecordLayer.RECORDS_CHANGED).contains(propertyName)) {
      refresh();
    } else if (AbstractRecordLayer.RECORD_UPDATED.equals(propertyName)) {
      if (isSortable()) {
        fireTableDataChanged();
      } else {
        repaint();
      }
    }
  }

  protected void propertyChangeChanged(final PropertyChangeEvent event) {
    final Object source = event.getSource();
    final String propertyName = event.getPropertyName();
    if (source == getLayer()) {
      if (AbstractRecordLayer.RECORD_CACHE_MODIFIED.equals(propertyName)) {
        final LayerRecord record = (LayerRecord)event.getNewValue();
        addCachedRecord(record);
      } else if (AbstractRecordLayer.RECORD_DELETED.equals(propertyName)) {
        final LayerRecord record = (LayerRecord)event.getNewValue();
        removeCachedRecord(record);
      } else if (AbstractRecordLayer.RECORDS_DELETED.equals(propertyName)) {
        refresh();
      } else if (AbstractRecordLayer.RECORD_UPDATED.equals(propertyName)) {
        repaint();
      }
    }
  }

  protected void propertyChangeSelected(final PropertyChangeEvent e) {
    final String propertyName = e.getPropertyName();
    if (AbstractRecordLayer.RECORD_UPDATED.equals(propertyName)) {
      fireTableDataChanged();
    } else if (propertyName.equals("selectionCount")) {
      refresh();
    }
  }

  protected boolean recordChange(final LayerRecord record) {
    if (recordChange(MODE_RECORDS_SELECTED, false, record)) {
      return true;
    } else if (recordChange(MODE_RECORDS_CHANGED, true, record)) {
      return true;
    } else {
      return false;
    }
  }

  protected boolean recordChange(final String fieldFilterMode, final boolean refreshIfNotFound,
    final LayerRecord record) {
    if (fieldFilterMode.equals(getFieldFilterMode())) {
      final List<LayerRecord> changedRecords = getRecordsCached();
      final int index = record.indexOf(changedRecords);
      if (index == -1) {
        if (refreshIfNotFound) {
          refresh();
        }
      } else {
        fireTableRowsUpdated(index, index);
      }
      return true;
    } else {
      return false;
    }
  }

  public void refresh() {
    Invoke.later(() -> {
      final long refreshIndex = getRefreshIndexNext();
      synchronized (getSync()) {
        this.currentRowIndex = -1;
        this.currentRecord = null;
        if (this.recordCountWorker != null) {
          this.recordCountWorker.cancel(true);
          this.recordCountWorker = null;
        }
        clearLoading();
      }
      this.refreshMethod.accept(refreshIndex);
    });
  }

  protected void refreshRecordsAll(final long index) {
    fireTableDataChanged();
    Invoke.background("Refresh table records", this::getRecordsPageChanged,
      (final List<LayerRecord> records) -> {
        if (canRefreshFinish(index)) {
          setCachedRecords(records);
          fireTableDataChanged();
        }
      });
  }

  protected void refreshRecordsCached(final long index,
    final Callable<List<LayerRecord>> getRecordsFunction) {
    Invoke.background("Refresh table records", getRecordsFunction,
      (final List<LayerRecord> records) -> {
        if (canRefreshFinish(index)) {
          setCachedRecords(records);
          this.setRowCount(records.size());
          fireTableDataChanged();
        }
      });
  }

  protected void refreshRecordsChanged(final long index) {
    refreshRecordsCached(index, this.layer::getRecordsChanged);
  }

  protected void refreshRecordsSelected(final long index) {
    refreshRecordsCached(index, this.layer::getSelectedRecords);
  }

  protected void removeCachedRecord(final LayerRecord record) {
    if (record.removeFrom(this.cachedRecords)) {
      this.recordCount--;
      fireTableDataChanged();
    }
  }

  protected void repaint() {
    getTable().repaint();
  }

  protected void replaceCachedRecord(final LayerRecord oldRecord, final LayerRecord newRecord) {
    synchronized (this.pageCache) {
      for (final List<LayerRecord> records : this.pageCache.values()) {
        for (final ListIterator<LayerRecord> iterator = records.listIterator(); iterator
          .hasNext();) {
          final LayerRecord record = iterator.next();
          if (record == oldRecord) {
            iterator.set(newRecord);
            return;
          }
        }
      }
    }
  }

  protected void setCachedRecords(final List<LayerRecord> records) {
    this.cachedRecords = records;
  }

  public void setFieldFilterMode(final String mode) {
    Invoke.later(() -> {
      if (!mode.equals(this.fieldFilterMode)) {
        this.recordCount = 0;
        this.cachedRecords = Collections.emptyList();
        this.fieldFilterMode = mode;
        getRefreshIndexNext();
        if (this.fieldFilterModes.contains(mode)) {
          final RecordLayerTable table = getTable();
          table.setRowFilter(null);
          ListSelectionModel selectionModel = this.selectionModel;
          if (MODE_RECORDS_SELECTED.equals(mode)) {
            selectionModel = this.highlightedModel;
          }
          table.setSelectionModel(selectionModel);

          updateMethods();

          refresh();
        }
      }
    });
  }

  @Override
  public void setFieldNames(final Collection<String> fieldNames) {
    final List<String> fieldTitles = new ArrayList<>();
    for (final String fieldName : fieldNames) {
      final String fieldTitle = this.layer.getFieldTitle(fieldName);
      fieldTitles.add(fieldTitle);
    }
    super.setFieldNamesAndTitles(fieldNames, fieldTitles);
  }

  public void setFieldNamesSetName(final String fieldNamesSetName) {
    this.layer.setFieldNamesSetName(fieldNamesSetName);
    final List<String> fieldNamesSet = this.layer.getFieldNamesSet();
    setFieldNames(fieldNamesSet);
  }

  public boolean setFilter(Condition filter) {
    if (filter == null) {
      filter = Condition.ALL;
    }
    if (Equals.equal(filter, this.filter)) {
      return false;
    } else {
      final Object oldValue = this.filter;
      this.filter = filter;
      if (filter.isEmpty()) {
        this.rowFilterCondition = null;
      } else {
        this.rowFilterCondition = new RecordRowPredicateRowFilter(filter);
        if (!Equals.equal(oldValue, filter)) {
          this.filterHistory.remove(filter);
          this.filterHistory.addFirst(filter);
          while (this.filterHistory.size() > 20) {
            this.filterHistory.removeLast();
          }
        }
      }
      if (isSortable()) {
        final RecordLayerTable table = getTable();
        table.setRowFilter(this.rowFilterCondition);
      } else {
        refresh();
      }
      firePropertyChange("filter", oldValue, this.filter);
      final boolean hasFilter = isHasFilter();
      firePropertyChange("hasFilter", !hasFilter, hasFilter);
      return true;
    }
  }

  public void setFilterByBoundingBox(final boolean filterByBoundingBox) {
    if (this.filterByBoundingBox != filterByBoundingBox) {
      this.filterByBoundingBox = filterByBoundingBox;
      refresh();
    }
  }

  public void setGeometryFilterMode(final String mode) {
    setFilterByBoundingBox("boundingBox".equals(mode));
  }

  protected void setModes(final String... modes) {
    this.fieldFilterModes = Arrays.asList(modes);
  }

  public void setOrderBy(final Map<String, Boolean> orderBy) {
    setOrderByInternal(orderBy);
    final Map<Integer, SortOrder> sortedColumns = new LinkedHashMap<>();
    for (final Entry<String, Boolean> entry : orderBy.entrySet()) {
      if (orderBy != null) {
        final String fieldName = entry.getKey();
        final Boolean order = entry.getValue();
        final int index = getFieldIndex(fieldName);
        if (index != -1) {
          SortOrder sortOrder;
          if (order) {
            sortOrder = SortOrder.ASCENDING;
          } else {
            sortOrder = SortOrder.DESCENDING;
          }
          sortedColumns.put(index, sortOrder);
        }
      }
    }
    setSortedColumns(sortedColumns);
  }

  protected void setOrderByInternal(final Map<String, Boolean> orderBy) {
    if (Property.hasValue(orderBy)) {
      this.orderBy = orderBy;
      this.orderByComparatorIdentifier = Records.newComparatorOrderByIdentifier(orderBy);
    } else {
      this.orderBy = Collections.emptyMap();
      this.orderByComparatorIdentifier = null;
    }
  }

  protected void setRecords(final long refreshIndex, final int pageNumber,
    final List<LayerRecord> records) {
    synchronized (getSync()) {
      if (canRefreshFinish(refreshIndex)) {
        this.pageCache.put(pageNumber, records);
        fireTableRowsUpdated(pageNumber * this.pageSize,
          Math.min(getRowCount(), (pageNumber + 1) * this.pageSize - 1));
      }
    }
  }

  private void setRowCount(final int rowCount) {
    final int oldValue = this.recordCount;
    this.recordCount = rowCount;
    firePropertyChange("rowCount", oldValue, rowCount);
  }

  protected void setSortableModes(final String... sortableModes) {
    this.sortableModes = Arrays.asList(sortableModes);
  }

  @Override
  public SortOrder setSortOrder(final int column) {
    final SortOrder sortOrder = super.setSortOrder(column);
    final String fieldName = getFieldName(column);

    Map<String, Boolean> orderBy;
    if (sortOrder == SortOrder.ASCENDING) {
      orderBy = Collections.singletonMap(fieldName, true);
    } else if (sortOrder == SortOrder.DESCENDING) {
      orderBy = Collections.singletonMap(fieldName, false);
    } else {
      orderBy = Collections.singletonMap(fieldName, true);
    }
    if (this.sync == null) {
      setOrderByInternal(orderBy);
    } else {
      synchronized (getSync()) {
        setOrderByInternal(orderBy);
        refresh();
      }
    }
    return sortOrder;
  }

  @Override
  public void setTable(final RecordRowTable table) {
    super.setTable(table);
    table.setSelectionModel(this.selectionModel);
  }

  @Override
  public String toDisplayValueInternal(final int rowIndex, final int fieldIndex,
    final Object objectValue) {
    if (objectValue == null) {
      final String fieldName = getFieldName(fieldIndex);
      if (getRecordDefinition().getIdFieldNames().contains(fieldName)) {
        return "NEW";
      }
    }
    return super.toDisplayValueInternal(rowIndex, fieldIndex, objectValue);
  }

  protected void updateMethods() {
    final String fieldFilterMode = getFieldFilterMode();
    this.refreshMethod = Maps.get(this.refreshMethodByFilterMode, fieldFilterMode,
      this.defaultRefreshMethod);

    this.recordCountMethod = Maps.get(this.recordCountMethodByFilterMode, fieldFilterMode,
      this::getRecordCount);

    this.getRecordMethod = Maps.get(this.getRecordMethodByFilterMode, fieldFilterMode,
      this::getRecordPage);

    this.exportRecordsMethod = Maps.get(this.exportRecordsMethodByFilterMode, fieldFilterMode,
      this.layer::exportRecords);

    this.propertyChangeMethod = Maps.get(this.propertyChangeMethodByFilterMode, fieldFilterMode,
      this::propertyChangeAll);
  }
}
