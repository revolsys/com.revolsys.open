package com.revolsys.swing.map.layer.record.table.model;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.RowFilter.ComparisonType;
import javax.swing.SortOrder;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import com.revolsys.beans.PropertyChangeSupportProxy;
import com.revolsys.collection.LruMap;
import com.revolsys.converter.string.StringConverterRegistry;
import com.revolsys.data.equals.EqualsRegistry;
import com.revolsys.data.query.BinaryCondition;
import com.revolsys.data.query.Cast;
import com.revolsys.data.query.Column;
import com.revolsys.data.query.Condition;
import com.revolsys.data.query.Query;
import com.revolsys.data.query.QueryValue;
import com.revolsys.data.query.Value;
import com.revolsys.data.query.functions.F;
import com.revolsys.data.query.functions.Function;
import com.revolsys.data.record.Record;
import com.revolsys.data.record.schema.Attribute;
import com.revolsys.data.record.schema.RecordDefinition;
import com.revolsys.jts.geom.BoundingBox;
import com.revolsys.swing.listener.InvokeMethodListener;
import com.revolsys.swing.map.layer.Project;
import com.revolsys.swing.map.layer.record.AbstractRecordLayer;
import com.revolsys.swing.map.layer.record.LayerRecord;
import com.revolsys.swing.map.layer.record.LoadingRecord;
import com.revolsys.swing.map.layer.record.table.RecordLayerTable;
import com.revolsys.swing.map.layer.record.table.predicate.DeletedPredicate;
import com.revolsys.swing.map.layer.record.table.predicate.ModifiedPredicate;
import com.revolsys.swing.map.layer.record.table.predicate.NewPredicate;
import com.revolsys.swing.parallel.Invoke;
import com.revolsys.swing.table.SortableTableModel;
import com.revolsys.swing.table.filter.ContainsFilter;
import com.revolsys.swing.table.filter.EqualFilter;
import com.revolsys.swing.table.record.model.RecordRowTableModel;
import com.revolsys.swing.table.record.row.RecordRowTable;
import com.revolsys.util.Property;

public class RecordLayerTableModel extends RecordRowTableModel implements
  SortableTableModel, PropertyChangeListener, PropertyChangeSupportProxy {

  public static RecordLayerTable createTable(final AbstractRecordLayer layer) {
    final RecordDefinition recordDefinition = layer.getRecordDefinition();
    if (recordDefinition == null) {
      return null;
    } else {
      final List<String> columnNames = layer.getFieldNamesSet();
      final RecordLayerTableModel model = new RecordLayerTableModel(layer,
        columnNames);
      final RecordLayerTable table = new RecordLayerTable(model);

      ModifiedPredicate.add(table);
      NewPredicate.add(table);
      DeletedPredicate.add(table);

      Property.addListener(layer, "hasSelectedRecords",
        new InvokeMethodListener(RecordLayerTableModel.class,
          "selectionChanged", table, model));
      return table;
    }
  }

  public static final void selectionChanged(final RecordLayerTable table,
    final RecordLayerTableModel tableModel) {
    final String attributeFilterMode = tableModel.getAttributeFilterMode();
    if (MODE_SELECTED.equals(attributeFilterMode)) {
      tableModel.refresh();
    } else {
      table.repaint();
    }
  }

  private static final long serialVersionUID = 1L;

  public static final String MODE_ALL = "all";

  public static final String MODE_SELECTED = "selected";

  public static final String MODE_EDITS = "edits";

  private final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(
    this);

  private final RecordLayerListSelectionModel selectionModel = new RecordLayerListSelectionModel(
    this);

  private final RecordLayerHighlightedListSelectionModel highlightedModel = new RecordLayerHighlightedListSelectionModel(
    this);

  private Condition filter;

  private boolean filterByBoundingBox;

  private List<String> attributeFilterModes = Arrays.asList(MODE_ALL,
    MODE_SELECTED, MODE_EDITS);

  private List<String> sortableModes = Arrays.asList(MODE_SELECTED, MODE_EDITS);

  private SwingWorker<?, ?> loadObjectsWorker;

  private Map<Integer, List<LayerRecord>> pageCache = new LruMap<Integer, List<LayerRecord>>(
      5);

  private String attributeFilterMode = MODE_ALL;

  private final int pageSize = 40;

  private final AbstractRecordLayer layer;

  private boolean countLoaded;

  private int rowCount;

  private SwingWorker<?, ?> rowCountWorker;

  private final Object sync = new Object();

  private final Set<Integer> loadingPageNumbers = new LinkedHashSet<>();

  private final Set<Integer> loadingPageNumbersToProcess = new LinkedHashSet<>();

  private Map<String, Boolean> orderBy = new LinkedHashMap<String, Boolean>();

  private int refreshIndex = 0;

  private final Object selectedSync = new Object();

  private List<LayerRecord> selectedRecords = Collections.emptyList();

  private final LayerRecord loadingRecord;

  public RecordLayerTableModel(final AbstractRecordLayer layer,
    final List<String> attributeNames) {
    super(layer.getRecordDefinition(), attributeNames);
    this.layer = layer;
    Property.addListener(layer, this);
    setEditable(true);
    setReadOnlyFieldNames(layer.getUserReadOnlyFieldNames());
    this.loadingRecord = new LoadingRecord(layer);
  }

  public String getAttributeFilterMode() {
    return this.attributeFilterMode;
  }

  @Override
  public String getColumnName(final int columnIndex) {
    final String fieldName = getFieldName(columnIndex);
    return this.layer.getFieldTitle(fieldName);
  }

  public Condition getFilter() {
    return this.filter;
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
        final Attribute geometryAttribute = recordDefinition.getGeometryAttribute();
        if (geometryAttribute != null) {
          query.and(F.envelopeIntersects(geometryAttribute, viewBoundingBox));
        }
      }
      return query;
    }
  }

  public final ListSelectionModel getHighlightedModel() {
    return this.highlightedModel;
  }

  public AbstractRecordLayer getLayer() {
    return this.layer;
  }

  protected List<LayerRecord> getLayerObjects(final Query query) {
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

  protected LayerRecord getPageRecord(final int pageNumber,
    final int recordNumber) {
    synchronized (getSync()) {
      final List<LayerRecord> page = this.pageCache.get(pageNumber);
      if (page == null) {
        if (!this.loadingPageNumbers.contains(pageNumber)) {
          this.loadingPageNumbers.add(pageNumber);
          this.loadingPageNumbersToProcess.add(pageNumber);
          if (this.loadObjectsWorker == null) {
            this.loadObjectsWorker = Invoke.background("Loading records "
                + getTypeName(), this, "loadPages", this.refreshIndex);
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

  @Override
  public PropertyChangeSupport getPropertyChangeSupport() {
    return this.propertyChangeSupport;
  }

  @SuppressWarnings("unchecked")
  @Override
  public <V extends Record> V getRecord(final int row) {
    if (row < 0) {
      return null;
    } else if (this.attributeFilterMode.equals(MODE_SELECTED)) {
      return (V)getSelectedRecord(row);
    } else if (this.attributeFilterMode.equals(MODE_EDITS)) {
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
      synchronized (getSync()) {
        if (this.countLoaded) {
          int count = this.rowCount;
          if (!this.attributeFilterMode.equals(MODE_SELECTED)
              && !this.attributeFilterMode.equals(MODE_EDITS)) {
            final AbstractRecordLayer layer = getLayer();
            final int newRecordCount = layer.getNewRecordCount();
            count += newRecordCount;
          }
          return count;

        } else {
          if (this.rowCountWorker == null) {
            this.rowCountWorker = Invoke.background("Query row count "
                + this.layer.getName(), this, "loadRowCount", this.refreshIndex);
          }
          return 0;
        }
      }
    }
  }

  protected int getRowCountInternal() {
    if (this.attributeFilterMode.equals(MODE_SELECTED)) {
      synchronized (this.selectedSync) {
        this.selectedRecords = new ArrayList<LayerRecord>();
        for (final LayerRecord record : getLayerSelectedRecords()) {
          if (!record.isDeleted()) {
            this.selectedRecords.add(record);
          }
        }
        return this.selectedRecords.size();
      }
    } else if (this.attributeFilterMode.equals(MODE_EDITS)) {
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
    return super.isEditable() && this.layer.isEditable();
  }

  public boolean isFilterByBoundingBox() {
    return this.filterByBoundingBox;
  }

  public boolean isHasFilter() {
    return this.filter != null;
  }

  @Override
  public boolean isSelected(final boolean selected, final int rowIndex,
    final int columnIndex) {
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
    query.setOrderBy(this.orderBy);
    query.setOffset(this.pageSize * pageNumber);
    query.setLimit(this.pageSize);
    final List<LayerRecord> objects = getLayerObjects(query);
    return objects;
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

  public void loadRowCount(final int refreshIndex) {
    final int rowCount = getRowCountInternal();
    Invoke.later(this, "setRowCount", refreshIndex, rowCount);
  }

  @Override
  public void propertyChange(final PropertyChangeEvent e) {
    if (e.getSource() == this.layer) {
      final String propertyName = e.getPropertyName();
      if (Arrays.asList("query", "editable", "recordInserted",
        "recordsInserted", "recordDeleted", "recordsChanged").contains(
          propertyName)) {
        refresh();
      } else if ("recordUpdated".equals(propertyName)) {
        repaint();
      } else if (propertyName.equals("selectionCount")) {
        if (MODE_SELECTED.equals(this.attributeFilterMode)) {
          refresh();
        }
      }
    }
  }

  public void refresh() {
    if (SwingUtilities.isEventDispatchThread()) {
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
      fireTableDataChanged();
    } else {
      Invoke.later(this, "refresh");
    }
  }

  protected void repaint() {
    getTable().repaint();
  }

  protected void replaceCachedObject(final LayerRecord oldObject,
    final LayerRecord newObject) {
    synchronized (this.pageCache) {
      for (final List<LayerRecord> objects : this.pageCache.values()) {
        for (final ListIterator<LayerRecord> iterator = objects.listIterator(); iterator.hasNext();) {
          final LayerRecord object = iterator.next();
          if (object == oldObject) {
            iterator.set(newObject);
            return;
          }
        }
      }
    }
  }

  public void setAttributeFilterMode(final String mode) {
    final RecordLayerTable table = getTable();
    if (MODE_SELECTED.equals(mode)) {
      table.setSelectionModel(this.highlightedModel);
    } else {
      this.selectedRecords = Collections.emptyList();
      table.setSelectionModel(this.selectionModel);
    }
    if (this.attributeFilterModes.contains(mode)) {
      if (!mode.equals(this.attributeFilterMode)) {
        this.attributeFilterMode = mode;
        refresh();
      }
    }
  }

  public boolean setFilter(final Condition filter) {
    if (EqualsRegistry.equal(filter, this.filter)) {
      return false;
    } else {
      final Object oldValue = this.filter;
      this.filter = filter;
      if (MODE_SELECTED.equals(getAttributeFilterMode())) {
        setRowSorter(filter);
      } else {
        refresh();
      }
      this.propertyChangeSupport.firePropertyChange("filter", oldValue,
        this.filter);
      final boolean hasFilter = isHasFilter();
      this.propertyChangeSupport.firePropertyChange("hasFilter", !hasFilter,
        hasFilter);
      return true;
    }
  }

  public void setFilterByBoundingBox(final boolean filterByBoundingBox) {
    if (this.filterByBoundingBox != filterByBoundingBox) {
      this.filterByBoundingBox = filterByBoundingBox;
      refresh();
    }
  }

  protected void setModes(final String... modes) {
    this.attributeFilterModes = Arrays.asList(modes);
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

  public void setRowCount(final int refreshIndex, final int rowCount) {
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
            columnIndex = getRecordDefinition().getAttributeIndex(columnName);
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
              rowFilter = RowFilter.numberFilter(ComparisonType.EQUAL, number,
                columnIndex);
            } else {
              rowFilter = new EqualFilter(
                StringConverterRegistry.toString(value), columnIndex);
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
    final String attributeName = getFieldName(column);

    Map<String, Boolean> orderBy;
    if (sortOrder == SortOrder.ASCENDING) {
      orderBy = Collections.singletonMap(attributeName, true);
    } else if (sortOrder == SortOrder.DESCENDING) {
      orderBy = Collections.singletonMap(attributeName, false);
    } else {
      orderBy = Collections.emptyMap();
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
  public String toDisplayValueInternal(final int rowIndex,
    final int attributeIndex, final Object objectValue) {
    if (objectValue == null) {
      if (getRecordDefinition().getIdAttributeIndex() == attributeIndex) {
        return "NEW";
      }
    }
    return super.toDisplayValueInternal(rowIndex, attributeIndex, objectValue);
  }
}
