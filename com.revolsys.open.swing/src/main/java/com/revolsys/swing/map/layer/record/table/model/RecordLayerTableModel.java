package com.revolsys.swing.map.layer.record.table.model;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.SortOrder;
import javax.swing.SwingWorker;

import com.revolsys.beans.PropertyChangeSupportProxy;
import com.revolsys.collection.map.LruMap;
import com.revolsys.equals.Equals;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.record.Record;
import com.revolsys.record.io.ListRecordReader;
import com.revolsys.record.io.RecordReader;
import com.revolsys.record.query.Condition;
import com.revolsys.record.query.Query;
import com.revolsys.record.query.functions.F;
import com.revolsys.record.schema.FieldDefinition;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.swing.EventQueue;
import com.revolsys.swing.listener.EventQueueRunnableListener;
import com.revolsys.swing.map.layer.Project;
import com.revolsys.swing.map.layer.record.AbstractRecordLayer;
import com.revolsys.swing.map.layer.record.LayerRecord;
import com.revolsys.swing.map.layer.record.LoadingRecord;
import com.revolsys.swing.map.layer.record.table.RecordLayerTable;
import com.revolsys.swing.map.layer.record.table.predicate.DeletedPredicate;
import com.revolsys.swing.map.layer.record.table.predicate.ModifiedPredicate;
import com.revolsys.swing.map.layer.record.table.predicate.NewPredicate;
import com.revolsys.swing.menu.MenuFactory;
import com.revolsys.swing.parallel.Invoke;
import com.revolsys.swing.table.SortableTableModel;
import com.revolsys.swing.table.record.RecordRowTable;
import com.revolsys.swing.table.record.filter.RecordRowPredicateRowFilter;
import com.revolsys.swing.table.record.model.RecordRowTableModel;
import com.revolsys.swing.table.selection.NullSelectionModel;
import com.revolsys.util.Property;

public class RecordLayerTableModel extends RecordRowTableModel
  implements SortableTableModel, PropertyChangeListener, PropertyChangeSupportProxy {

  public static final String MODE_ALL = "all";

  public static final String MODE_CHANGED_RECORDS = "edits";

  public static final String MODE_SELECTED_RECORDS = "selected";

  private static final long serialVersionUID = 1L;

  public static RecordLayerTable createTable(final AbstractRecordLayer layer) {
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

  private boolean countLoaded;

  private String fieldFilterMode = MODE_ALL;

  private List<String> fieldFilterModes = Arrays.asList(MODE_ALL, MODE_SELECTED_RECORDS,
    MODE_CHANGED_RECORDS);

  private Condition filter = Condition.ALL;

  private RowFilter<RecordRowTableModel, Integer> rowFilter = null;

  private boolean filterByBoundingBox;

  private final LinkedList<Condition> filterHistory = new LinkedList<>();

  private ListSelectionModel highlightedModel = new RecordLayerHighlightedListSelectionModel(this);

  private final AbstractRecordLayer layer;

  private final Set<Integer> loadingPageNumbers = new LinkedHashSet<>();

  private final Set<Integer> loadingPageNumbersToProcess = new LinkedHashSet<>();

  private final LayerRecord loadingRecord;

  private SwingWorker<?, ?> loadRecordsWorker;

  private Map<String, Boolean> orderBy;

  private final Map<Integer, List<LayerRecord>> pageCache = new LruMap<>(5);

  private final int pageSize = 40;

  private final AtomicLong refreshIndex = new AtomicLong(Long.MIN_VALUE);

  private int rowCount;

  private SwingWorker<?, ?> rowCountWorker;

  private List<LayerRecord> selectedRecords = Collections.emptyList();

  private List<LayerRecord> changedRecords = Collections.emptyList();

  private EventQueueRunnableListener selectionChangedListener;

  private ListSelectionModel selectionModel = new RecordLayerListSelectionModel(this);

  private List<String> sortableModes = Arrays.asList(MODE_SELECTED_RECORDS, MODE_CHANGED_RECORDS);

  private final Object sync = new Object();

  private boolean loadInBackground = true;

  public RecordLayerTableModel(final AbstractRecordLayer layer) {
    super(layer.getRecordDefinition());
    this.layer = layer;
    setFieldNames(layer.getFieldNamesSet());
    Property.addListener(layer, this);
    setEditable(true);
    setReadOnlyFieldNames(layer.getUserReadOnlyFieldNames());
    this.loadingRecord = new LoadingRecord(layer);
  }

  protected void clearLoading() {
    synchronized (getSync()) {
      this.loadingPageNumbers.clear();
      this.loadingPageNumbersToProcess.clear();
      this.pageCache.clear();// = new LruMap<>(5);
      this.rowCount = 0;
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

  protected LayerRecord getChangedRecord(final int index) {
    final List<LayerRecord> records = getChangedRecords();
    if (index < records.size()) {
      return records.get(index);
    } else {
      return null;
    }
  }

  public List<LayerRecord> getChangedRecords() {
    return this.changedRecords;
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

  protected List<LayerRecord> getLayerRecords(final Query query) {
    query.setOrderBy(this.orderBy);
    return this.layer.query(query);
  }

  protected int getLayerRowCount() {
    final Query query = getFilterQuery();
    if (query == null) {
      return this.layer.getRecordCount();
    } else {
      return this.layer.getRecordCount(query);
    }
  }

  protected List<LayerRecord> getLayerSelectedRecords() {
    final AbstractRecordLayer layer = getLayer();
    return layer.getSelectedRecords();
  }

  @Override
  public MenuFactory getMenu(final int rowIndex, final int columnIndex) {
    final Record record = getRecord(rowIndex);
    if (record instanceof LoadingRecord) {
      return null;
    } else {
      return super.getMenu(rowIndex, columnIndex);
    }
  }

  private Integer getNextPageNumber(final long refreshIndex) {
    synchronized (getSync()) {
      if (this.refreshIndex.get() == refreshIndex) {
        if (this.loadingPageNumbersToProcess.isEmpty()) {
          this.loadRecordsWorker = null;
        } else {
          final Iterator<Integer> iterator = this.loadingPageNumbersToProcess.iterator();
          if (iterator.hasNext()) {
            final Integer pageNumber = iterator.next();
            iterator.remove();
            return pageNumber;
          }
        }
      }
    }
    return null;
  }

  public Map<String, Boolean> getOrderBy() {
    return this.orderBy;
  }

  protected LayerRecord getPageRecord(final int pageNumber, final int recordNumber) {
    synchronized (getSync()) {
      final List<LayerRecord> page = this.pageCache.get(pageNumber);
      if (page == null) {
        if (!this.loadingPageNumbers.contains(pageNumber)) {
          this.loadingPageNumbers.add(pageNumber);
          this.loadingPageNumbersToProcess.add(pageNumber);
          if (this.loadRecordsWorker == null) {
            this.loadRecordsWorker = Invoke.background("Loading records " + getTypeName(),
              () -> loadPages(this.refreshIndex.get()));
          }
        }
        return this.loadingRecord;
      } else {
        if (recordNumber < page.size()) {
          final LayerRecord object = page.get(recordNumber);
          return object;
        } else {
          return null;
        }
      }
    }
  }

  public int getPageSize() {
    return this.pageSize;
  }

  public RecordReader getReader() {
    final RecordDefinition recordDefinition = getRecordDefinition();
    final List<LayerRecord> records;
    if (this.fieldFilterMode.equals(MODE_SELECTED_RECORDS)) {
      records = getSelectedRecords();
    } else if (this.fieldFilterMode.equals(MODE_CHANGED_RECORDS)) {
      records = getChangedRecords();
    } else {
      final Query query = getFilterQuery();
      records = getLayerRecords(query);
    }
    return new ListRecordReader(recordDefinition, records);
  }

  @SuppressWarnings("unchecked")
  @Override
  public <V extends Record> V getRecord(final int row) {
    if (row < 0) {
      return null;
    } else if (this.fieldFilterMode.equals(MODE_SELECTED_RECORDS)) {
      return (V)getSelectedRecord(row);
    } else if (this.fieldFilterMode.equals(MODE_CHANGED_RECORDS)) {
      return (V)getChangedRecord(row);
    } else {
      return (V)loadLayerRecord(row);
    }
  }

  @Override
  public final int getRowCount() {
    if (this.loadInBackground) {
      synchronized (getSync()) {
        if (this.countLoaded) {
          int count = this.rowCount;
          if (!this.fieldFilterMode.equals(MODE_SELECTED_RECORDS)
            && !this.fieldFilterMode.equals(MODE_CHANGED_RECORDS)) {
            final AbstractRecordLayer layer = getLayer();
            final int newRecordCount = layer.getNewRecordCount();
            count += newRecordCount;
          }
          return count;
        } else {
          if (this.rowCountWorker == null) {
            this.rowCountWorker = Invoke.background("Query row count " + this.layer.getName(),
              () -> {
                final long refreshIndex = this.refreshIndex.get();
                final int rowCount = getRowCountInternal();
                Invoke.later(() -> {
                  boolean updated = false;
                  synchronized (getSync()) {
                    if (refreshIndex == this.refreshIndex.get()) {
                      this.rowCount = rowCount;
                      this.countLoaded = true;
                      this.rowCountWorker = null;
                      updated = true;
                    }
                  }
                  if (updated) {
                    fireTableDataChanged();
                  }
                });
              });
          }
          return 0;
        }
      }
    } else {
      return this.rowCount;
    }
  }

  protected int getRowCountInternal() {
    if (this.fieldFilterMode.equals(MODE_CHANGED_RECORDS)) {
      return this.layer.getChangeCount();
    } else {
      return getLayerRowCount();
    }
  }

  protected LayerRecord getSelectedRecord(final int index) {
    final List<LayerRecord> records = getSelectedRecords();
    if (index < records.size()) {
      return records.get(index);
    } else {
      return null;
    }
  }

  protected List<LayerRecord> getSelectedRecords() {
    return this.selectedRecords;
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

  @Override
  public boolean isEditable() {
    return super.isEditable() && this.layer.isEditable() && this.layer.isCanEditRecords();
  }

  public boolean isFilterByBoundingBox() {
    return this.filterByBoundingBox;
  }

  public boolean isHasFilter() {
    return this.filter != null;
  }

  @Override
  public boolean isSelected(final boolean selected, final int rowIndex, final int columnIndex) {
    final LayerRecord object = getRecord(rowIndex);
    return this.layer.isSelected(object);
  }

  protected LayerRecord loadLayerRecord(int row) {
    final AbstractRecordLayer layer = getLayer();
    final int newRecordCount = layer.getNewRecordCount();
    if (row < newRecordCount) {
      return layer.getNewRecords().get(row);
    } else {
      row -= newRecordCount;
    }
    final int pageSize = getPageSize();
    final int pageNumber = row / pageSize;
    final int recordNumber = row % pageSize;
    return getPageRecord(pageNumber, recordNumber);
  }

  protected List<LayerRecord> loadPage(final int pageNumber) {
    final Query query = getFilterQuery();
    query.setOffset(this.pageSize * pageNumber);
    query.setLimit(this.pageSize);
    final List<LayerRecord> records = getLayerRecords(query);
    return records;
  }

  public void loadPages(final long refreshIndex) {
    while (true) {
      final Integer pageNumber = getNextPageNumber(refreshIndex);
      if (pageNumber == null) {
        return;
      } else {
        final List<LayerRecord> records;
        if (getFilterQuery() == null) {
          records = Collections.emptyList();
        } else {
          records = loadPage(pageNumber);
        }
        Invoke.later(() -> setRecords(refreshIndex, pageNumber, records));
      }
    }
  }

  @Override
  public void propertyChange(final PropertyChangeEvent e) {
    final String propertyName = e.getPropertyName();
    final Object source = e.getSource();
    if (source == this.layer) {
      if (Arrays.asList("filter", "query", "editable", "recordInserted", "recordsInserted",
        "recordDeleted", "recordsChanged").contains(propertyName)) {
        refresh();
      } else if ("recordUpdated".equals(propertyName)) {
        if (MODE_SELECTED_RECORDS.equals(getFieldFilterMode())) {
          fireTableDataChanged();
        } else {
          repaint();
        }
      } else if (propertyName.equals("selectionCount")) {
        refresh();
      }
    } else {
      if (source instanceof LayerRecord) {
        final LayerRecord record = (LayerRecord)source;
        Invoke.later(() -> {
          recordChange(record);
        });
      }
    }
  }

  protected boolean recordChange(final LayerRecord record) {
    if (recordChangeSelectedRecords(record)) {
      return true;
    } else if (recordChangeChangedRecords(record)) {
      return true;
    } else {
      return false;
    }
  }

  protected boolean recordChangeChangedRecords(final LayerRecord record) {
    if (MODE_CHANGED_RECORDS.equals(getFieldFilterMode())) {
      final List<LayerRecord> changedRecords = getChangedRecords();
      final int index = changedRecords.indexOf(record);
      if (index == -1) {
        refresh();
      } else {
        fireTableRowsUpdated(index, index);
      }
      return true;
    } else {
      return false;
    }
  }

  protected boolean recordChangeSelectedRecords(final LayerRecord record) {
    if (MODE_SELECTED_RECORDS.equals(getFieldFilterMode())) {
      final List<LayerRecord> selectedRecords = getSelectedRecords();
      int index = 0;
      for (final LayerRecord selectedRecord : selectedRecords) {
        if (selectedRecord.isSame(record)) {
          fireTableRowsUpdated(index, index);
          return true;
        }
        index++;
      }
      return true;
    } else {
      return false;
    }
  }

  public void refresh() {
    Invoke.later(() -> {
      this.refreshIndex.incrementAndGet();
      refreshDo();
    });
  }

  protected boolean refreshChangedRecords() {
    if (this.fieldFilterMode.equals(MODE_CHANGED_RECORDS)) {
      final long index = this.refreshIndex.incrementAndGet();

      Invoke.background("Table set changed records", this.layer::getChangedRecords,
        (final List<LayerRecord> changedRecords) -> {
          if (index == this.refreshIndex.get()) {
            final RecordLayerTable table = getTable();
            this.changedRecords = changedRecords;
            this.rowCount = changedRecords.size();
            fireTableDataChanged();
            table.setRowFilter(this.rowFilter);
          }
        });
      return true;
    } else {
      this.changedRecords = Collections.emptyList();
      return false;
    }
  }

  protected void refreshDo() {
    synchronized (getSync()) {
      if (this.loadRecordsWorker != null) {
        this.loadRecordsWorker.cancel(true);
        this.loadRecordsWorker = null;
      }
      if (this.rowCountWorker != null) {
        this.rowCountWorker.cancel(true);
        this.rowCountWorker = null;
      }
      clearLoading();
    }
    if (refreshSelectedRecords()) {
    } else if (refreshChangedRecords()) {
    } else {
      fireTableDataChanged();
    }
  }

  protected boolean refreshSelectedRecords() {
    if (this.fieldFilterMode.equals(MODE_SELECTED_RECORDS)) {
      final long index = this.refreshIndex.get();

      Invoke.background("Table set selected records", this.layer::getSelectedRecords,
        (final List<LayerRecord> selectedRecords) -> {
          if (index == this.refreshIndex.get()) {
            final RecordLayerTable table = getTable();
            this.selectedRecords = selectedRecords;
            this.rowCount = selectedRecords.size();
            fireTableDataChanged();
            table.setRowFilter(this.rowFilter);
          }
        });
      return true;
    } else {
      this.selectedRecords = Collections.emptyList();
      return false;
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

  public void setFieldFilterMode(final String mode) {
    Invoke.later(() -> {
      if (!mode.equals(this.fieldFilterMode)) {
        if (this.fieldFilterModes.contains(mode)) {
          final RecordLayerTable table = getTable();
          if (MODE_SELECTED_RECORDS.equals(mode)) {
            table.setSelectionModel(this.highlightedModel);
            this.loadInBackground = false;
          } else {
            table.setSelectionModel(this.selectionModel);
            if (MODE_CHANGED_RECORDS.equals(mode)) {
              this.loadInBackground = false;
            } else {
              this.loadInBackground = true;
            }
          }
          this.fieldFilterMode = mode;
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
        this.rowFilter = null;
      } else {
        this.rowFilter = new RecordRowPredicateRowFilter(filter);
        if (!Equals.equal(oldValue, filter)) {
          this.filterHistory.remove(filter);
          this.filterHistory.addFirst(filter);
          while (this.filterHistory.size() > 20) {
            this.filterHistory.removeLast();
          }
        }
      }
      if (MODE_SELECTED_RECORDS.equals(getFieldFilterMode())) {
        getTable().setRowFilter(this.rowFilter);
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
    this.orderBy = orderBy;
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

  public void setRecords(final long refreshIndex, final Integer pageNumber,
    final List<LayerRecord> records) {
    synchronized (getSync()) {
      if (this.refreshIndex.get() == refreshIndex) {
        this.pageCache.put(pageNumber, records);
        this.loadingPageNumbers.remove(pageNumber);
        fireTableRowsUpdated(pageNumber * this.pageSize,
          Math.min(getRowCount(), (pageNumber + 1) * this.pageSize - 1));
      }
    }
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
      this.orderBy = orderBy;
    } else {
      synchronized (getSync()) {
        this.orderBy = orderBy;
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
}
