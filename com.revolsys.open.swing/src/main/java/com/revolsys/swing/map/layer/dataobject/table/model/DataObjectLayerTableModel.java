package com.revolsys.swing.map.layer.dataobject.table.model;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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
import com.revolsys.gis.cs.BoundingBox;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.gis.data.query.BinaryCondition;
import com.revolsys.gis.data.query.Cast;
import com.revolsys.gis.data.query.Column;
import com.revolsys.gis.data.query.Condition;
import com.revolsys.gis.data.query.Query;
import com.revolsys.gis.data.query.QueryValue;
import com.revolsys.gis.data.query.Value;
import com.revolsys.gis.data.query.functions.Function;
import com.revolsys.gis.model.data.equals.EqualsRegistry;
import com.revolsys.swing.listener.InvokeMethodPropertyChangeListener;
import com.revolsys.swing.map.layer.Project;
import com.revolsys.swing.map.layer.dataobject.AbstractDataObjectLayer;
import com.revolsys.swing.map.layer.dataobject.LayerDataObject;
import com.revolsys.swing.map.layer.dataobject.table.DataObjectLayerTable;
import com.revolsys.swing.map.layer.dataobject.table.predicate.DeletedPredicate;
import com.revolsys.swing.map.layer.dataobject.table.predicate.ModifiedPredicate;
import com.revolsys.swing.map.layer.dataobject.table.predicate.NewPredicate;
import com.revolsys.swing.parallel.Invoke;
import com.revolsys.swing.table.SortableTableModel;
import com.revolsys.swing.table.dataobject.model.DataObjectRowTableModel;
import com.revolsys.swing.table.dataobject.row.DataObjectRowTable;
import com.revolsys.swing.table.filter.ContainsFilter;
import com.revolsys.swing.table.filter.EqualFilter;
import com.revolsys.util.CollectionUtil;

public class DataObjectLayerTableModel extends DataObjectRowTableModel
  implements SortableTableModel, PropertyChangeListener,
  PropertyChangeSupportProxy {

  private static final long serialVersionUID = 1L;

  public static final String MODE_ALL = "all";

  public static final String MODE_SELECTED = "selected";

  public static final String MODE_EDITS = "edits";

  public static DataObjectLayerTable createTable(
    final AbstractDataObjectLayer layer) {
    final DataObjectMetaData metaData = layer.getMetaData();
    if (metaData == null) {
      return null;
    } else {
      final List<String> columnNames = layer.getColumnNames();
      final DataObjectLayerTableModel model = new DataObjectLayerTableModel(
        layer, columnNames);
      final DataObjectLayerTable table = new DataObjectLayerTable(model);

      ModifiedPredicate.add(table);
      NewPredicate.add(table);
      DeletedPredicate.add(table);

      layer.addPropertyChangeListener("hasSelectedRecords",
        new InvokeMethodPropertyChangeListener(DataObjectLayerTableModel.class,
          "selectionChanged", table, model));
      return table;
    }
  }

  public static final void selectionChanged(final DataObjectLayerTable table,
    final DataObjectLayerTableModel tableModel) {
    final String attributeFilterMode = tableModel.getAttributeFilterMode();
    if (MODE_SELECTED.equals(attributeFilterMode)) {
      tableModel.refresh();
    } else {
      table.repaint();
    }
  }

  private final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(
    this);

  private final DataObjectLayerListSelectionModel selectionModel = new DataObjectLayerListSelectionModel(
    this);

  private final DataObjectLayerHighlightedListSelectionModel highlightedModel = new DataObjectLayerHighlightedListSelectionModel(
    this);

  private Condition filter;

  private boolean filterByBoundingBox;

  private List<String> attributeFilterModes = Arrays.asList(MODE_ALL,
    MODE_SELECTED, MODE_EDITS);

  private List<String> sortableModes = Arrays.asList(MODE_SELECTED, MODE_EDITS);

  private SwingWorker<?, ?> loadObjectsWorker;

  private Map<Integer, List<LayerDataObject>> pageCache = new LruMap<Integer, List<LayerDataObject>>(
    5);

  private String attributeFilterMode = MODE_ALL;

  private final int pageSize = 40;

  private final AbstractDataObjectLayer layer;

  private boolean countLoaded;

  private int rowCount;

  private SwingWorker<?, ?> rowCountWorker;

  private final Object sync = new Object();

  private final Set<Integer> loadingPageNumbers = new LinkedHashSet<Integer>();

  private Map<String, Boolean> orderBy = new LinkedHashMap<String, Boolean>();

  private int refreshIndex = 0;

  private final Object selectedSync = new Object();

  private List<LayerDataObject> selectedRecords = Collections.emptyList();

  public DataObjectLayerTableModel(final AbstractDataObjectLayer layer,
    final List<String> attributeNames) {
    super(layer.getMetaData(), attributeNames);
    this.layer = layer;
    layer.addPropertyChangeListener(this);
    setEditable(true);
    setReadOnlyFieldNames(layer.getUserReadOnlyFieldNames());
  }

  public String getAttributeFilterMode() {
    return this.attributeFilterMode;
  }

  @Override
  public String getColumnName(final int columnIndex) {
    final String fieldName = getFieldName(columnIndex);
    return layer.getFieldTitle(fieldName);
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
        query.setBoundingBox(viewBoundingBox);
      }
      return query;
    }
  }

  public final ListSelectionModel getHighlightedModel() {
    return highlightedModel;
  }

  public AbstractDataObjectLayer getLayer() {
    return this.layer;
  }

  protected List<LayerDataObject> getLayerObjects(final Query query) {
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

  protected List<LayerDataObject> getLayerSelectedRecords() {
    final AbstractDataObjectLayer layer = getLayer();
    return layer.getSelectedRecords();
  }

  private Integer getNextPageNumber(final int refreshIndex) {
    synchronized (getSync()) {
      if (this.refreshIndex == refreshIndex) {
        if (this.loadingPageNumbers.isEmpty()) {
          this.loadObjectsWorker = null;
          return null;
        } else {
          return CollectionUtil.get(this.loadingPageNumbers, 0);
        }
      } else {
        return null;
      }
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public <V extends DataObject> V getObject(final int row) {
    if (row < 0) {
      return null;
    } else if (this.attributeFilterMode.equals(MODE_SELECTED)) {
      return (V)getSelectedRecord(row);
    } else if (this.attributeFilterMode.equals(MODE_EDITS)) {
      final AbstractDataObjectLayer layer = getLayer();
      final List<LayerDataObject> changes = layer.getChanges();
      if (row < changes.size()) {
        return (V)changes.get(row);
      } else {
        return null;
      }
    } else {
      return (V)loadLayerRecord(row);
    }
  }

  public Map<String, Boolean> getOrderBy() {
    return this.orderBy;
  }

  protected LayerDataObject getPageRecord(final int pageNumber,
    final int recordNumber) {
    synchronized (getSync()) {
      final List<LayerDataObject> page = this.pageCache.get(pageNumber);
      if (page == null) {
        this.loadingPageNumbers.add(pageNumber);
        synchronized (getSync()) {
          if (this.loadObjectsWorker == null) {
            this.loadObjectsWorker = Invoke.background("Loading records "
              + getTypeName(), this, "loadPages", refreshIndex);
          }
        }
        return null;
      } else {
        if (recordNumber < page.size()) {
          final LayerDataObject object = page.get(recordNumber);
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
    return propertyChangeSupport;
  }

  @Override
  public final int getRowCount() {
    synchronized (getSync()) {
      synchronized (getSync()) {
        if (this.countLoaded) {
          final AbstractDataObjectLayer layer = getLayer();
          final int newRecordCount = layer.getNewRecordCount();
          final int count = this.rowCount + newRecordCount;
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
      synchronized (selectedSync) {
        this.selectedRecords = new ArrayList<LayerDataObject>(
          getLayerSelectedRecords());
        return this.selectedRecords.size();
      }
    } else if (this.attributeFilterMode.equals(MODE_EDITS)) {
      return this.layer.getChangeCount();
    } else {
      return getLayerRowCount();
    }
  }

  protected LayerDataObject getSelectedRecord(final int index) {
    synchronized (selectedSync) {
      if (index < selectedRecords.size()) {
        return selectedRecords.get(index);
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
  public DataObjectLayerTable getTable() {
    return (DataObjectLayerTable)super.getTable();
  }

  public String getTypeName() {
    return getMetaData().getPath();
  }

  @Override
  public boolean isEditable() {
    return super.isEditable() && layer.isEditable();
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
    final LayerDataObject object = getObject(rowIndex);
    return layer.isSelected(object);
  }

  protected LayerDataObject loadLayerRecord(int row) {
    final AbstractDataObjectLayer layer = getLayer();
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

  protected List<LayerDataObject> loadPage(final int pageNumber) {
    final Query query = getFilterQuery();
    query.setOrderBy(this.orderBy);
    query.setOffset(this.pageSize * pageNumber);
    query.setLimit(this.pageSize);
    final List<LayerDataObject> objects = getLayerObjects(query);
    return objects;
  }

  public void loadPages(final int refreshIndex) {
    while (true) {
      final Integer pageNumber = getNextPageNumber(refreshIndex);
      if (pageNumber == null) {
        return;
      } else {
        final List<LayerDataObject> records;
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
      if (Arrays.asList("query", "recordsChanged", "editable").contains(
        propertyName)) {
        refresh();
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
        this.rowCount = 0;
        this.pageCache = new LruMap<Integer, List<LayerDataObject>>(5);
        this.countLoaded = false;

      }
      fireTableDataChanged();
    } else {
      Invoke.later(this, "refresh");
    }
  }

  protected void replaceCachedObject(final LayerDataObject oldObject,
    final LayerDataObject newObject) {
    synchronized (this.pageCache) {
      for (final List<LayerDataObject> objects : this.pageCache.values()) {
        for (final ListIterator<LayerDataObject> iterator = objects.listIterator(); iterator.hasNext();) {
          final LayerDataObject object = iterator.next();
          if (object == oldObject) {
            iterator.set(newObject);
            return;
          }
        }
      }
    }
  }

  public void setAttributeFilterMode(final String mode) {
    final DataObjectLayerTable table = getTable();
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
      propertyChangeSupport.firePropertyChange("filter", oldValue, this.filter);
      final boolean hasFilter = isHasFilter();
      propertyChangeSupport.firePropertyChange("hasFilter", !hasFilter,
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
    final List<LayerDataObject> records) {
    synchronized (getSync()) {
      if (this.refreshIndex == refreshIndex) {
        pageCache.put(pageNumber, records);
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
    final DataObjectLayerTable table = getTable();
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
            columnIndex = getMetaData().getAttributeIndex(columnName);
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
    synchronized (getSync()) {
      this.orderBy = orderBy;
      refresh();
    }
    return sortOrder;
  }

  @Override
  public void setTable(final DataObjectRowTable table) {
    super.setTable(table);
    table.setSelectionModel(this.selectionModel);
  }

  @Override
  public String toDisplayValueInternal(final int rowIndex,
    final int attributeIndex, final Object objectValue) {
    if (objectValue == null) {
      if (getMetaData().getIdAttributeIndex() == attributeIndex) {
        return "NEW";
      }
    }
    return super.toDisplayValueInternal(rowIndex, attributeIndex, objectValue);
  }
}
