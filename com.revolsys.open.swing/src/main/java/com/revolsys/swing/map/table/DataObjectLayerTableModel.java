package com.revolsys.swing.map.table;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.SortOrder;
import javax.swing.SwingWorker;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import com.revolsys.collection.LruMap;
import com.revolsys.gis.cs.BoundingBox;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.gis.data.query.Query;
import com.revolsys.swing.SwingWorkerManager;
import com.revolsys.swing.listener.InvokeMethodPropertyChangeListener;
import com.revolsys.swing.map.layer.Project;
import com.revolsys.swing.map.layer.dataobject.DataObjectLayer;
import com.revolsys.swing.map.table.predicate.DeletedPredicate;
import com.revolsys.swing.map.table.predicate.ModifiedPredicate;
import com.revolsys.swing.map.table.predicate.NewPredicate;
import com.revolsys.swing.table.SortableTableModel;
import com.revolsys.swing.table.dataobject.row.DataObjectRowTable;
import com.revolsys.swing.table.dataobject.row.DataObjectRowTableModel;
import com.revolsys.util.CollectionUtil;

@SuppressWarnings("serial")
public class DataObjectLayerTableModel extends DataObjectRowTableModel
  implements SortableTableModel, PropertyChangeListener {

  public static final String MODE_ALL = "all";

  public static final String MODE_SELECTED = "selected";

  public static final String MODE_EDITS = "edits";

  public static DataObjectRowTable createTable(final DataObjectLayer layer) {
    DataObjectMetaData metaData = layer.getMetaData();
    if (metaData == null) {
      return null;
    } else {
      return createTable(layer, metaData.getAttributeNames());
    }
  }

  public static DataObjectRowTable createTable(final DataObjectLayer layer,
    final List<String> attributeNames) {
    return createTable(layer, attributeNames, attributeNames);
  }

  public static DataObjectRowTable createTable(final DataObjectLayer layer,
    final List<String> attributeNames, final List<String> attributeTitles) {
    final DataObjectLayerTableModel model = new DataObjectLayerTableModel(
      layer, attributeNames, attributeTitles);
    final DataObjectRowTable table = new DataObjectRowTable(model);

    ModifiedPredicate.add(table);
    NewPredicate.add(table);
    DeletedPredicate.add(table);

    final TableCellRenderer cellRenderer = new DataObjectLayerTableCellRenderer(
      model);
    final TableColumnModel columnModel = table.getColumnModel();
    for (int i = 0; i < columnModel.getColumnCount(); i++) {
      final TableColumn column = columnModel.getColumn(i);

      column.setCellRenderer(cellRenderer);
    }
    table.setSelectionModel(new DataObjectLayerListSelectionModel(model));
    layer.addPropertyChangeListener("selected",
      new InvokeMethodPropertyChangeListener(table, "repaint"));
    return table;
  }

  private boolean filterByBoundingBox;

  private List<String> attributeFilterModes = Arrays.asList(MODE_ALL,
    MODE_SELECTED, MODE_EDITS);

  private List<String> sortableModes = Arrays.asList(MODE_SELECTED, MODE_EDITS);

  private SwingWorker<?, ?> loadObjectsWorker;

  private Map<Integer, List<DataObject>> pageCache = new LruMap<Integer, List<DataObject>>(
    5);

  private String attributeFilterMode = MODE_ALL;

  private final int pageSize = 40;

  private final DataObjectLayer layer;

  private boolean countLoaded;

  private int rowCount;

  private SwingWorker<?, ?> rowCountWorker;

  private final Object sync = new Object();

  private final Set<Integer> loadingPageNumbers = new LinkedHashSet<Integer>();

  private Map<String, Boolean> orderBy = new LinkedHashMap<String, Boolean>();

  public DataObjectLayerTableModel(final DataObjectLayer layer) {
    this(layer, layer.getMetaData().getAttributeNames());
  }

  public DataObjectLayerTableModel(final DataObjectLayer layer,
    final List<String> attributeNames) {
    this(layer, attributeNames, attributeNames);
  }

  public DataObjectLayerTableModel(final DataObjectLayer layer,
    final List<String> attributeNames, final List<String> attributeTitles) {
    super(layer.getMetaData(), attributeNames, attributeTitles);
    this.layer = layer;
    layer.addPropertyChangeListener(this);
    setEditable(false);
  }

  public String getAttributeFilterMode() {
    return attributeFilterMode;
  }

  protected int getCachedRowCount() {
    if (!countLoaded) {
      if (rowCountWorker == null) {
        rowCountWorker = SwingWorkerManager.execute(
          "Query row count " + layer.getName(), this, "updateRowCount");
      }
      return 0;
    } else {
      return rowCount + getLayer().getNewObjectCount();
    }
  }

  public DataObjectLayer getLayer() {
    return layer;
  }

  protected List<DataObject> getLayerObjects(final Query query) {
    return layer.query(query);
  }

  private Integer getNextPageNumber() {
    synchronized (getSync()) {
      if (loadingPageNumbers.isEmpty()) {
        loadObjectsWorker = null;
        return null;
      } else {
        return CollectionUtil.get(loadingPageNumbers, 0);
      }
    }
  }

  @Override
  public DataObject getObject(final int row) {
    if (attributeFilterMode.equals(MODE_SELECTED)) {
      final List<DataObject> selectedObjects = getSelectedObjects();
      if (row < selectedObjects.size()) {
        return selectedObjects.get(row);
      } else {
        fireTableDataChanged();
        return null;
      }
    } else if (attributeFilterMode.equals(MODE_EDITS)) {
      final DataObjectLayer layer = getLayer();
      final List<DataObject> changes = layer.getChanges();
      if (row < changes.size()) {
        return changes.get(row);
      } else {
        fireTableDataChanged();
        return null;
      }
    } else {
      return loadLayerObjects(row);
    }
  }

  protected List<DataObject> getSelectedObjects() {
    final DataObjectLayer layer = getLayer();
    final List<DataObject> selectedObjects = layer.getSelectedObjects();
    return selectedObjects;
  }

  public Map<String, Boolean> getOrderBy() {
    return orderBy;
  }

  protected DataObject getPageRecord(final int pageNumber,
    final int recordNumber) {
    synchronized (pageCache) {
      final List<DataObject> page = pageCache.get(pageNumber);
      if (page == null) {
        loadingPageNumbers.add(pageNumber);
        if (loadObjectsWorker == null) {
          loadObjectsWorker = SwingWorkerManager.execute("Loading records "
            + getTypeName(), this, "loadPages");
        }
        return null;
      } else {
        if (recordNumber < page.size()) {
          final DataObject object = page.get(recordNumber);
          return object;
        } else {
          return null;
        }
      }
    }
  }

  public int getPageSize() {
    return pageSize;
  }

  @Override
  public int getRowCount() {
    synchronized (getSync()) {
      if (attributeFilterMode.equals(MODE_SELECTED)) {
        return layer.getSelectionCount();
      } else if (attributeFilterMode.equals(MODE_EDITS)) {
        return layer.getChangeCount();
      } else {
        return getCachedRowCount();
      }
    }
  }

  public List<String> getSortableModes() {
    return sortableModes;
  }

  protected Object getSync() {
    return sync;
  }

  public String getTypeName() {
    return getMetaData().getPath();
  }

  public boolean isFilterByBoundingBox() {
    return filterByBoundingBox;
  }

  protected DataObject loadLayerObjects(int row) {
    final DataObjectLayer layer = getLayer();
    final int newObjectCount = layer.getNewObjectCount();
    if (row < newObjectCount) {
      return layer.getNewObjects().get(row);
    } else {
      row -= newObjectCount;
    }
    final int pageSize = getPageSize();
    final int pageNumber = (row / pageSize);
    final int recordNumber = (row % pageSize);
    return getPageRecord(pageNumber, recordNumber);
  }

  protected int loadLayerRowCount() {
    final Query query = layer.getQuery();
    if (query == null) {
      return 0;
    } else {
      return layer.getRowCount(query);
    }
  }

  protected List<DataObject> loadPage(final int pageNumber) {
    final Query query = getFilterQuery();
    query.setOrderBy(orderBy);
    query.setOffset(pageSize * pageNumber);
    query.setLimit(pageSize);
    final List<DataObject> objects = getLayerObjects(query);
    return objects;
  }

  protected Query getFilterQuery() {
    Query query = layer.getQuery();
    if (query == null) {
      return null;
    } else {
      query = query.clone();
      query.setOrderBy(orderBy);
      if (filterByBoundingBox) {
        Project project = layer.getProject();
        BoundingBox viewBoundingBox = project.getViewBoundingBox();
        query.setBoundingBox(viewBoundingBox);
      }
      return query;
    }
  }

  public void loadPages() {
    while (true) {
      final Integer pageNumber = getNextPageNumber();
      if (pageNumber == null) {
        return;
      } else {
        synchronized (getSync()) {
          final Map<Integer, List<DataObject>> pageCache = this.pageCache;
          final List<DataObject> objects;
          if (layer.getQuery() == null) {
            objects = Collections.emptyList();
          } else {
            objects = loadPage(pageNumber);
          }
          pageCache.put(pageNumber, objects);
          loadingPageNumbers.remove(pageNumber);
        }
        fireTableRowsUpdated(pageNumber * pageSize,
          Math.min(getRowCount(), (pageNumber + 1) * pageSize - 1));
      }
    }
  }

  @Override
  public void propertyChange(final PropertyChangeEvent e) {
    if (e.getSource() == layer) {
      final String propertyName = e.getPropertyName();
      if (propertyName.equals("query")) {
        refresh();
      } else if (propertyName.equals("editable")) {
        refresh();
      }
    }
  }

  protected void refresh() {
    synchronized (getSync()) {
      if (loadObjectsWorker != null) {
        loadObjectsWorker.cancel(true);
        loadObjectsWorker = null;
      }
      loadingPageNumbers.clear();
      rowCount = 0;
      pageCache = new LruMap<Integer, List<DataObject>>(5);
      countLoaded = false;
      fireTableDataChanged();
    }
  }

  public void setAttributeFilterMode(final String mode) {

    if (attributeFilterModes.contains(mode)) {
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

  public void updateRowCount() {
    final int rowCount = loadLayerRowCount();
    synchronized (getSync()) {
      this.rowCount = rowCount;
      countLoaded = true;
      rowCountWorker = null;
      fireTableDataChanged();
    }
  }
}
