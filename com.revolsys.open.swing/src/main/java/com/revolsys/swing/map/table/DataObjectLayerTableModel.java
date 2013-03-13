package com.revolsys.swing.map.table;

import java.util.Collections;
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
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.gis.data.query.Query;
import com.revolsys.swing.SwingWorkerManager;
import com.revolsys.swing.listener.InvokeMethodPropertyChangeListener;
import com.revolsys.swing.map.layer.dataobject.DataObjectLayer;
import com.revolsys.swing.table.SortableTableModel;
import com.revolsys.swing.table.dataobject.row.DataObjectRowTable;
import com.revolsys.swing.table.dataobject.row.DataObjectRowTableModel;
import com.revolsys.util.CollectionUtil;

@SuppressWarnings("serial")
public class DataObjectLayerTableModel extends DataObjectRowTableModel
  implements SortableTableModel {

  public static final LayerTablePanelFactory FACTORY = new InvokeMethodLayerTablePanelFactory(
    DataObjectLayer.class, DataObjectLayerTableModel.class, "createPanel");

  public static DataObjectLayerTablePanel createPanel(
    final DataObjectLayer layer) {
    final JTable table = createTable(layer);
    return new DataObjectLayerTablePanel(layer, table);
  }

  public static DataObjectRowTable createTable(final DataObjectLayer layer) {
    final DataObjectLayerTableModel model = new DataObjectLayerTableModel(layer);
    final DataObjectRowTable table = new DataObjectRowTable(model);

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

  private SwingWorker<?, ?> loadObjectsWorker;

  private Map<Integer, List<DataObject>> pageCache = new LruMap<Integer, List<DataObject>>(
    5);

  private final int pageSize = 40;

  private final DataObjectLayer layer;

  private boolean countLoaded;

  private int rowCount;

  private SwingWorker<?, ?> rowCountWorker;

  private final Object sync = new Object();

  private final Query query;

  private final Set<Integer> loadingPageNumbers = new LinkedHashSet<Integer>();

  public DataObjectLayerTableModel(DataObjectLayer layer,
    List<String> columnIndexNames) {
    this(layer);
    setColumnIndexNames(columnIndexNames);
  }

  public DataObjectLayerTableModel(final DataObjectLayer layer) {
    super(layer.getMetaData());
    this.layer = layer;
    final DataObjectMetaData metaData = layer.getMetaData();
    query = new Query(metaData);
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
  public DataObject getObject(final int row) {
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

  @Override
  public int getRowCount() {
    synchronized (sync) {
      if (!countLoaded) {
        if (rowCountWorker == null) {
          rowCountWorker = SwingWorkerManager.execute("Query row count "
            + layer.getName(), this, "updateRowCount");
        }
        return 0;
      } else {
        return rowCount;
      }
    }
  }

  public String getTypeName() {
    return getMetaData().getPath();
  }

  public boolean isSelected(final int row) {
    final DataObject object = getObject(row);
    if (object != null) {
      return layer.isSelected(object);
    }
    return false;
  }

  public void loadPages() {
    while (true) {
      final Integer pageNumber = getNextPageNumber();
      if (pageNumber == null) {
        return;
      } else {
        synchronized (sync) {
          final Map<Integer, List<DataObject>> pageCache = this.pageCache;
          final Query query = this.query.clone();
          query.setLimit(pageSize);
          query.setOffset(pageSize * pageNumber);
          final List<DataObject> objects = layer.query(query);
          pageCache.put(pageNumber, objects);
          loadingPageNumbers.remove(pageNumber);
        }
        fireTableRowsUpdated(pageNumber * pageSize,
          Math.min(rowCount, (pageNumber + 1) * pageSize - 1));
      }
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
      query.setOrderBy(orderBy);
      pageCache = new LruMap<Integer, List<DataObject>>(5);
      countLoaded = false;
    }
    return sortOrder;
  }

  public void updateRowCount() {
    final int rowCount = layer.getRowCount(query);
    synchronized (sync) {
      this.rowCount = rowCount;
      countLoaded = true;
      rowCountWorker = null;
      fireTableDataChanged();
    }
  }

}
