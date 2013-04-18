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

import javax.swing.JTable;
import javax.swing.SortOrder;
import javax.swing.SwingWorker;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import com.revolsys.collection.LruMap;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.query.Query;
import com.revolsys.swing.SwingWorkerManager;
import com.revolsys.swing.listener.InvokeMethodPropertyChangeListener;
import com.revolsys.swing.map.layer.dataobject.DataObjectLayer;
import com.revolsys.swing.map.table.predicate.DeletedPredicate;
import com.revolsys.swing.map.table.predicate.NewPredicate;
import com.revolsys.swing.table.SortableTableModel;
import com.revolsys.swing.table.dataobject.row.DataObjectRowTable;
import com.revolsys.swing.table.dataobject.row.DataObjectRowTableModel;
import com.revolsys.util.CollectionUtil;

@SuppressWarnings("serial")
public class DataObjectLayerTableModel extends DataObjectRowTableModel
  implements SortableTableModel, PropertyChangeListener {

  public static final LayerTablePanelFactory FACTORY = new InvokeMethodLayerTablePanelFactory(
    DataObjectLayer.class, DataObjectLayerTableModel.class, "createPanel");

  public static DataObjectLayerTablePanel createPanel(
    final DataObjectLayer layer) {
    final JTable table = createTable(layer);
    return new DataObjectLayerTablePanel(layer, table);
  }

  public static DataObjectRowTable createTable(final DataObjectLayer layer) {
    return createTable(layer, layer.getMetaData().getAttributeNames());
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

  public String getMode() {
    return mode;
  }

  private SwingWorker<?, ?> loadObjectsWorker;

  private Map<Integer, List<DataObject>> pageCache = new LruMap<Integer, List<DataObject>>(
    5);

  public static final String MODE_ALL = "all";

  public static final String MODE_SELECTED = "selected";

  public static final String MODE_CHANGES = "changes";

  private static final List<String> MODES = Arrays.asList(MODE_ALL,
    MODE_SELECTED, MODE_CHANGES);

  private String mode = MODE_ALL;

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

  public DataObjectLayer getLayer() {
    return layer;
  }

  private Integer getNextPageNumber() {
    synchronized (sync) {
      if (loadingPageNumbers.isEmpty()) {
        loadObjectsWorker = null;
        return null;
      } else {
        return CollectionUtil.get(loadingPageNumbers, 0);
      }
    }
  }

  @Override
  public DataObject getObject(int row) {
    DataObjectLayer layer = getLayer();
    if (mode.equals(MODE_SELECTED)) {
      List<DataObject> selectedObjects = layer.getSelectedObjects();
      if (row < selectedObjects.size()) {
        return selectedObjects.get(row);
      } else {
        fireTableDataChanged();
        return null;
      }
    } else if (mode.equals(MODE_CHANGES)) {
      List<DataObject> changes = layer.getChanges();
      if (row < changes.size()) {
        return changes.get(row);
      } else {
        fireTableDataChanged();
        return null;
      }
    } else {
      int newObjectCount = layer.getNewObjectCount();
      if (row < newObjectCount) {
        return layer.getNewObjects().get(row);
      } else {
        row -= newObjectCount;
      }
      synchronized (pageCache) {
        final int pageNumber = (row / pageSize);
        final int recordNumber = (row % pageSize);
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
  }

  public void setMode(String mode) {
    if (MODES.contains(mode)) {
      if (!mode.equals(this.mode)) {
        this.mode = mode;
        fireTableDataChanged();
      }
    } else {
      throw new IllegalArgumentException("Unsupported mode");
    }
  }

  @Override
  public int getRowCount() {
    synchronized (sync) {
      if (mode.equals(MODE_SELECTED)) {
        return layer.getSelectionCount();
      } else if (mode.equals(MODE_CHANGES)) {
        return layer.getChangeCount();
      } else {
        if (!countLoaded) {
          if (rowCountWorker == null) {
            rowCountWorker = SwingWorkerManager.execute("Query row count "
              + layer.getName(), this, "updateRowCount");
          }
          return 0;
        } else {
          return rowCount + getLayer().getNewObjectCount();
        }
      }
    }
  }

  public String getTypeName() {
    return getMetaData().getPath();
  }

  public void loadPages() {
    while (true) {
      final Integer pageNumber = getNextPageNumber();
      if (pageNumber == null) {
        return;
      } else {
        synchronized (sync) {
          final Map<Integer, List<DataObject>> pageCache = this.pageCache;
          final Query query = layer.getQuery();
          final List<DataObject> objects;
          if (query == null) {
            objects = Collections.emptyList();
          } else {
            query.setOrderBy(orderBy);
            query.setLimit(pageSize);
            query.setOffset(pageSize * pageNumber);
            objects = layer.query(query);
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
    synchronized (sync) {
      pageCache = new LruMap<Integer, List<DataObject>>(5);
      countLoaded = false;
      fireTableDataChanged();
    }
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
    synchronized (sync) {
      this.orderBy = orderBy;
      refresh();
    }
    return sortOrder;
  }

  public void updateRowCount() {
    final Query query = layer.getQuery();
    final int rowCount;
    if (query == null) {
      rowCount = 0;
    } else {
      rowCount = layer.getRowCount(query);
    }
    synchronized (sync) {
      this.rowCount = rowCount;
      countLoaded = true;
      rowCountWorker = null;
      fireTableDataChanged();
    }
  }

}
