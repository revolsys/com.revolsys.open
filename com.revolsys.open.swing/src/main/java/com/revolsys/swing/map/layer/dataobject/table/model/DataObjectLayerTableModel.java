package com.revolsys.swing.map.layer.dataobject.table.model;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
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
import com.revolsys.gis.data.query.Function;
import com.revolsys.gis.data.query.Query;
import com.revolsys.gis.data.query.Value;
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
      tableModel.fireTableDataChanged();
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

  private Condition searchCondition;

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

  public DataObjectLayerTableModel(final AbstractDataObjectLayer layer,
    final List<String> attributeNames) {
    super(layer.getMetaData(), attributeNames);
    this.layer = layer;
    layer.addPropertyChangeListener(this);
    setEditable(true);
    setReadOnlyAttributeNames(layer.getUserReadOnlyFieldNames());
  }

  public String getAttributeFilterMode() {
    return this.attributeFilterMode;
  }

  protected int getCachedRowCount() {
    if (!this.countLoaded) {
      if (this.rowCountWorker == null) {
        this.rowCountWorker = Invoke.background(
          "Query row count " + this.layer.getName(), this, "updateRowCount");
      }
      return 0;
    } else {
      return this.rowCount + getLayer().getNewObjectCount();
    }
  }

  protected Query getFilterQuery() {
    Query query = this.layer.getQuery();
    if (query == null) {
      return null;
    } else {
      query = query.clone();
      query.and(this.searchCondition);
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

  private Integer getNextPageNumber() {
    synchronized (getSync()) {
      if (this.loadingPageNumbers.isEmpty()) {
        this.loadObjectsWorker = null;
        return null;
      } else {
        return CollectionUtil.get(this.loadingPageNumbers, 0);
      }
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public <V extends DataObject> V getObject(final int row) {
    if (row < 0) {
      return null;
    } else if (this.attributeFilterMode.equals(MODE_SELECTED)) {
      final List<LayerDataObject> selectedObjects = getSelectedObjects();
      if (row < selectedObjects.size()) {
        return (V)selectedObjects.get(row);
      } else {
        fireTableDataChanged();
        return null;
      }
    } else if (this.attributeFilterMode.equals(MODE_EDITS)) {
      final AbstractDataObjectLayer layer = getLayer();
      final List<LayerDataObject> changes = layer.getChanges();
      if (row < changes.size()) {
        return (V)changes.get(row);
      } else {
        fireTableDataChanged();
        return null;
      }
    } else {
      return (V)loadLayerObjects(row);
    }
  }

  public Map<String, Boolean> getOrderBy() {
    return this.orderBy;
  }

  protected LayerDataObject getPageRecord(final int pageNumber,
    final int recordNumber) {
    synchronized (this.pageCache) {
      final List<LayerDataObject> page = this.pageCache.get(pageNumber);
      if (page == null) {
        this.loadingPageNumbers.add(pageNumber);
        if (this.loadObjectsWorker == null) {
          this.loadObjectsWorker = Invoke.background("Loading records "
            + getTypeName(), this, "loadPages");
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
  public int getRowCount() {
    synchronized (getSync()) {
      if (this.attributeFilterMode.equals(MODE_SELECTED)) {
        return this.layer.getSelectionCount();
      } else if (this.attributeFilterMode.equals(MODE_EDITS)) {
        return this.layer.getChangeCount();
      } else {
        return getCachedRowCount();
      }
    }
  }

  public Condition getSearchCondition() {
    return this.searchCondition;
  }

  protected List<LayerDataObject> getSelectedObjects() {
    final AbstractDataObjectLayer layer = getLayer();
    final List<LayerDataObject> selectedObjects = layer.getSelectedRecords();
    return selectedObjects;
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

  @Override
  public boolean isSelected(final boolean selected, final int rowIndex,
    final int columnIndex) {
    final LayerDataObject object = getObject(rowIndex);
    return layer.isSelected(object);
  }

  protected LayerDataObject loadLayerObjects(int row) {
    final AbstractDataObjectLayer layer = getLayer();
    final int newObjectCount = layer.getNewObjectCount();
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

  protected int loadLayerRowCount() {
    final Query query = getFilterQuery();
    if (query == null) {
      return this.layer.getRowCount();
    } else {
      return this.layer.getRowCount(query);
    }
  }

  protected List<LayerDataObject> loadPage(final int pageNumber) {
    final Query query = getFilterQuery();
    query.setOrderBy(this.orderBy);
    query.setOffset(this.pageSize * pageNumber);
    query.setLimit(this.pageSize);
    final List<LayerDataObject> objects = getLayerObjects(query);
    return objects;
  }

  public void loadPages() {
    while (true) {
      final Integer pageNumber = getNextPageNumber();
      if (pageNumber == null) {
        return;
      } else {
        final Map<Integer, List<LayerDataObject>> pageCache;
        synchronized (getSync()) {
          pageCache = this.pageCache;
        }
        final List<LayerDataObject> records;
        if (getFilterQuery() == null) {
          records = Collections.emptyList();
        } else {
          records = loadPage(pageNumber);
        }
        pageCache.put(pageNumber, records);
        synchronized (getSync()) {
          if (this.pageCache == pageCache) {
            this.loadingPageNumbers.remove(pageNumber);
          }
        }
        fireTableRowsUpdated(pageNumber * this.pageSize,
          Math.min(getRowCount(), (pageNumber + 1) * this.pageSize - 1));
      }
    }
  }

  @Override
  public void propertyChange(final PropertyChangeEvent e) {
    if (e.getSource() == this.layer) {
      final String propertyName = e.getPropertyName();
      if (Arrays.asList("query", "objectsChanged", "editable").contains(
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
    synchronized (getSync()) {
      final SwingWorker<?, ?> worker = this.loadObjectsWorker;
      if (worker != null) {
        worker.cancel(true);
        this.loadObjectsWorker = null;
      }
      this.loadingPageNumbers.clear();
      this.rowCount = 0;
      this.pageCache = new LruMap<Integer, List<LayerDataObject>>(5);
      this.countLoaded = false;
    }
    if (SwingUtilities.isEventDispatchThread()) {
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
      table.setSelectionModel(this.selectionModel);
    }
    if (this.attributeFilterModes.contains(mode)) {
      if (!mode.equals(this.attributeFilterMode)) {
        this.attributeFilterMode = mode;
        refresh();
      }
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

  protected void setRowSorter(final Condition searchCondition) {
    final DataObjectLayerTable table = getTable();
    if (searchCondition == null) {
      table.setRowFilter(null);
    } else {
      if (searchCondition instanceof BinaryCondition) {
        final BinaryCondition binaryCondition = (BinaryCondition)searchCondition;
        final String operator = binaryCondition.getOperator();
        Condition left = binaryCondition.getLeft();
        final Condition right = binaryCondition.getRight();
        int columnIndex = -1;
        while (columnIndex == -1) {
          if (left instanceof Column) {
            final Column column = (Column)left;
            final String columnName = column.getName();
            columnIndex = getMetaData().getAttributeIndex(columnName);
          } else if (left instanceof Function) {
            final Function function = (Function)left;
            left = function.getConditions().get(0);
          } else if (left instanceof Cast) {
            final Cast cast = (Cast)left;
            left = cast.getCondition();
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
            RowFilter<Object, Object> filter;
            if (value instanceof Number) {
              final Number number = (Number)value;
              filter = RowFilter.numberFilter(ComparisonType.EQUAL, number,
                columnIndex);
            } else {
              filter = new EqualFilter(StringConverterRegistry.toString(value),
                columnIndex);
            }
            table.setRowFilter(filter);
          } else if (operator.equals("LIKE")) {
            final RowFilter<Object, Object> filter = new ContainsFilter(
              StringConverterRegistry.toString(value), columnIndex);
            table.setRowFilter(filter);
          }
        }
      }
    }
  }

  public boolean setSearchCondition(final Condition searchCondition) {
    if (EqualsRegistry.equal(searchCondition, this.searchCondition)) {
      return false;
    } else {
      final Object oldValue = this.searchCondition;
      this.searchCondition = searchCondition;
      if (MODE_SELECTED.equals(getAttributeFilterMode())) {
        setRowSorter(searchCondition);
      } else {
        refresh();
      }
      propertyChangeSupport.firePropertyChange("searchCondition", oldValue,
        this.searchCondition);
      return true;
    }
  }

  protected void setSortableModes(final String... sortableModes) {
    this.sortableModes = Arrays.asList(sortableModes);
  }

  @Override
  public SortOrder setSortOrder(final int column) {
    final SortOrder sortOrder = super.setSortOrder(column);
    final String attributeName = getAttributeName(column);

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

  public void updateRowCount() {
    final int rowCount = loadLayerRowCount();
    synchronized (getSync()) {
      this.rowCount = rowCount;
      this.countLoaded = true;
      this.rowCountWorker = null;
    }
    fireTableDataChanged();
  }
}
