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

import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.RowFilter.ComparisonType;
import javax.swing.SortOrder;
import javax.swing.SwingWorker;

import com.revolsys.beans.PropertyChangeSupportProxy;
import com.revolsys.collection.map.LruMap;
import com.revolsys.converter.string.StringConverterRegistry;
import com.revolsys.equals.Equals;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.record.Record;
import com.revolsys.record.io.ListRecordReader;
import com.revolsys.record.io.RecordReader;
import com.revolsys.record.query.BinaryCondition;
import com.revolsys.record.query.Cast;
import com.revolsys.record.query.Column;
import com.revolsys.record.query.Condition;
import com.revolsys.record.query.Query;
import com.revolsys.record.query.QueryValue;
import com.revolsys.record.query.Value;
import com.revolsys.record.query.functions.F;
import com.revolsys.record.query.functions.Function;
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
import com.revolsys.swing.table.filter.ContainsFilter;
import com.revolsys.swing.table.filter.EqualFilter;
import com.revolsys.swing.table.record.model.RecordRowTableModel;
import com.revolsys.swing.table.record.row.RecordRowTable;
import com.revolsys.swing.table.selection.NullSelectionModel;
import com.revolsys.util.Property;

public class RecordLayerTableModel extends RecordRowTableModel
  implements SortableTableModel, PropertyChangeListener, PropertyChangeSupportProxy {

  public static final String MODE_ALL = "all";

  public static final String MODE_EDITS = "edits";

  public static final String MODE_SELECTED = "selected";

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
    final String fieldFilterMode = tableModel.getFieldFilterMode();
    if (MODE_SELECTED.equals(fieldFilterMode)) {
      tableModel.refresh();
    } else {
      table.repaint();
    }
  }

  private boolean countLoaded;

  private String fieldFilterMode = MODE_ALL;

  private List<String> fieldFilterModes = Arrays.asList(MODE_ALL, MODE_SELECTED, MODE_EDITS);

  private Condition filter;

  private boolean filterByBoundingBox;

  private final LinkedList<Condition> filterHistory = new LinkedList<>();

  private ListSelectionModel highlightedModel = new RecordLayerHighlightedListSelectionModel(this);

  private final AbstractRecordLayer layer;

  private final Set<Integer> loadingPageNumbers = new LinkedHashSet<>();

  private final Set<Integer> loadingPageNumbersToProcess = new LinkedHashSet<>();

  private final LayerRecord loadingRecord;

  private SwingWorker<?, ?> loadObjectsWorker;

  private Map<String, Boolean> orderBy;

  private Map<Integer, List<LayerRecord>> pageCache = new LruMap<>(5);

  private final int pageSize = 40;

  private int refreshIndex = 0;

  private int rowCount;

  private SwingWorker<?, ?> rowCountWorker;

  private List<LayerRecord> selectedRecords = Collections.emptyList();

  private final Object selectedSync = new Object();

  private EventQueueRunnableListener selectionChangedListener;

  private ListSelectionModel selectionModel = new RecordLayerListSelectionModel(this);

  private List<String> sortableModes = Arrays.asList(MODE_SELECTED, MODE_EDITS);

  private final Object sync = new Object();

  public RecordLayerTableModel(final AbstractRecordLayer layer) {
    super(layer.getRecordDefinition());
    this.layer = layer;
    setFieldNames(layer.getFieldNamesSet());
    Property.addListener(layer, this);
    setEditable(true);
    setReadOnlyFieldNames(layer.getUserReadOnlyFieldNames());
    this.loadingRecord = new LoadingRecord(layer);
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

  protected void doRefresh() {
    synchronized (getSync()) {
      this.refreshIndex++;
      if (this.loadObjectsWorker != null) {
        this.loadObjectsWorker.cancel(true);
        this.loadObjectsWorker = null;
      }
      if (this.rowCountWorker != null) {
        this.rowCountWorker.cancel(true);
        this.rowCountWorker = null;
      }
      this.loadingPageNumbers.clear();
      this.loadingPageNumbersToProcess.clear();
      this.rowCount = 0;
      this.pageCache = new LruMap<Integer, List<LayerRecord>>(5);
      this.countLoaded = false;
    }
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
    Query query = this.layer.getQuery();
    if (query == null) {
      return null;
    } else {
      query = query.clone();
      query.and(this.filter);
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
      return this.layer.getRowCount();
    } else {
      return this.layer.getRowCount(query);
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

  private Integer getNextPageNumber(final int refreshIndex) {
    synchronized (getSync()) {
      if (this.refreshIndex == refreshIndex) {
        if (this.loadingPageNumbersToProcess.isEmpty()) {
          this.loadObjectsWorker = null;
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
          if (this.loadObjectsWorker == null) {
            this.loadObjectsWorker = Invoke.background("Loading records " + getTypeName(), this,
              "loadPages", this.refreshIndex);
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
    if (this.fieldFilterMode.equals(MODE_SELECTED)) {
      records = getSelectedRecords();
    } else if (this.fieldFilterMode.equals(MODE_EDITS)) {
      final AbstractRecordLayer layer = getLayer();
      records = layer.getChanges();
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
    } else if (this.fieldFilterMode.equals(MODE_SELECTED)) {
      return (V)getSelectedRecord(row);
    } else if (this.fieldFilterMode.equals(MODE_EDITS)) {
      final AbstractRecordLayer layer = getLayer();
      final List<LayerRecord> changes = layer.getChanges();
      if (row < changes.size()) {
        return (V)changes.get(row);
      } else {
        return null;
      }
    } else {
      return (V)loadLayerRecord(row);
    }
  }

  @Override
  public final int getRowCount() {
    synchronized (getSync()) {
      if (this.countLoaded) {
        int count = this.rowCount;
        if (!this.fieldFilterMode.equals(MODE_SELECTED)
          && !this.fieldFilterMode.equals(MODE_EDITS)) {
          final AbstractRecordLayer layer = getLayer();
          final int newRecordCount = layer.getNewRecordCount();
          count += newRecordCount;
        }
        return count;
      } else {
        if (this.rowCountWorker == null) {
          this.rowCountWorker = Invoke.background("Query row count " + this.layer.getName(), () -> {
            final int refreshIndex = this.refreshIndex;
            final int rowCount = getRowCountInternal();
            Invoke.later(() -> {
              boolean updated = false;
              synchronized (getSync()) {
                if (refreshIndex == this.refreshIndex) {
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
  }

  protected int getRowCountInternal() {
    if (this.fieldFilterMode.equals(MODE_SELECTED)) {
      synchronized (this.selectedSync) {
        this.selectedRecords = new ArrayList<>();
        for (final LayerRecord record : getLayerSelectedRecords()) {
          if (!record.isDeleted() && (this.filter == null || this.filter.test(record))) {
            this.selectedRecords.add(record);
          }
        }
        return this.selectedRecords.size();
      }
    } else if (this.fieldFilterMode.equals(MODE_EDITS)) {
      return this.layer.getChangeCount();
    } else {
      return getLayerRowCount();
    }
  }

  protected LayerRecord getSelectedRecord(final int index) {
    synchronized (this.selectedSync) {
      if (index < this.selectedRecords.size()) {
        return this.selectedRecords.get(index);
      } else {
        return null;
      }
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
    final int newObjectCount = layer.getNewRecordCount();
    if (row < newObjectCount) {
      return layer.getNewRecords().get(row);
    } else {
      row -= newObjectCount;
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

  public void loadPages(final int refreshIndex) {
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
        Invoke.later(this, "setRecords", refreshIndex, pageNumber, records);
      }
    }
  }

  @Override
  public void propertyChange(final PropertyChangeEvent e) {
    final String propertyName = e.getPropertyName();
    if (e.getSource() == this.layer) {
      if (Arrays.asList("filter", "query", "editable", "recordInserted", "recordsInserted",
        "recordDeleted", "recordsChanged").contains(propertyName)) {
        refresh();
      } else if ("recordUpdated".equals(propertyName)) {
        repaint();
      } else if (propertyName.equals("selectionCount")) {
        if (MODE_SELECTED.equals(this.fieldFilterMode)) {
          refresh();
        }
      }
    }
  }

  public void refresh() {
    Invoke.later(() -> {
      doRefresh();
      fireTableDataChanged();
    });
  }

  protected void repaint() {
    getTable().repaint();
  }

  protected void replaceCachedObject(final LayerRecord oldObject, final LayerRecord newObject) {
    synchronized (this.pageCache) {
      for (final List<LayerRecord> objects : this.pageCache.values()) {
        for (final ListIterator<LayerRecord> iterator = objects.listIterator(); iterator
          .hasNext();) {
          final LayerRecord object = iterator.next();
          if (object == oldObject) {
            iterator.set(newObject);
            return;
          }
        }
      }
    }
  }

  public void setFieldFilterMode(final String mode) {
    final RecordLayerTable table = getTable();
    if (MODE_SELECTED.equals(mode)) {
      table.setSelectionModel(this.highlightedModel);
    } else {
      this.selectedRecords = Collections.emptyList();
      table.setSelectionModel(this.selectionModel);
    }
    if (this.fieldFilterModes.contains(mode)) {
      if (!mode.equals(this.fieldFilterMode)) {
        this.fieldFilterMode = mode;
        refresh();
      }
    }
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

  public boolean setFilter(final Condition filter) {
    if (Equals.equal(filter, this.filter)) {
      return false;
    } else {
      final Object oldValue = this.filter;
      this.filter = filter;
      if (filter != null && !Equals.equal(oldValue, filter)) {
        this.filterHistory.remove(filter);
        this.filterHistory.addFirst(filter);
        while (this.filterHistory.size() > 20) {
          this.filterHistory.removeLast();
        }
      }
      if (MODE_SELECTED.equals(getFieldFilterMode())) {
        setRowSorter(filter);
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

  public void setRecords(final int refreshIndex, final Integer pageNumber,
    final List<LayerRecord> records) {
    synchronized (getSync()) {
      if (this.refreshIndex == refreshIndex) {
        this.pageCache.put(pageNumber, records);
        this.loadingPageNumbers.remove(pageNumber);
        fireTableRowsUpdated(pageNumber * this.pageSize,
          Math.min(getRowCount(), (pageNumber + 1) * this.pageSize - 1));
      }
    }
  }

  protected void setRowSorter(final Condition filter) {
    final RecordLayerTable table = getTable();
    if (filter == null) {
      table.setRowFilter(null);
    } else {
      if (filter instanceof BinaryCondition) {
        final BinaryCondition binaryCondition = (BinaryCondition)filter;
        final String operator = binaryCondition.getOperator();
        QueryValue left = binaryCondition.getLeft();
        final QueryValue right = binaryCondition.getRight();
        int columnIndex = -1;
        while (columnIndex == -1) {
          if (left instanceof Column) {
            final Column column = (Column)left;
            final String columnName = column.getName();
            columnIndex = getRecordDefinition().getFieldIndex(columnName);
          } else if (left instanceof Function) {
            final Function function = (Function)left;
            left = function.getQueryValues().get(0);
          } else if (left instanceof Cast) {
            final Cast cast = (Cast)left;
            left = cast.getValue();
          } else {
            return;
          }
        }

        if (columnIndex > -1) {
          Object value = null;
          if (right instanceof Value) {
            final Value valueObject = (Value)right;
            value = valueObject.getValue();

          }
          if (operator.equals("=")) {
            RowFilter<Object, Object> rowFilter;
            if (value instanceof Number) {
              final Number number = (Number)value;
              rowFilter = RowFilter.numberFilter(ComparisonType.EQUAL, number, columnIndex);
            } else {
              rowFilter = new EqualFilter(StringConverterRegistry.toString(value), columnIndex);
            }
            table.setRowFilter(rowFilter);
          } else if (operator.equals("LIKE")) {
            final RowFilter<Object, Object> rowFilter = new ContainsFilter(
              StringConverterRegistry.toString(value), columnIndex);
            table.setRowFilter(rowFilter);
          }
        }
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
