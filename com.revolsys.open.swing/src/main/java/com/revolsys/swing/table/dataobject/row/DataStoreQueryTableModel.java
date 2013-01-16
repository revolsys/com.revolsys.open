package com.revolsys.swing.table.dataobject.row;

import java.awt.BorderLayout;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SortOrder;
import javax.swing.SwingWorker;

import com.revolsys.collection.LruMap;
import com.revolsys.collection.ResultPager;
import com.revolsys.gis.data.io.DataObjectStore;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.gis.data.query.Query;
import com.revolsys.swing.SwingWorkerManager;
import com.revolsys.swing.table.SortableTableModel;
import com.revolsys.util.CollectionUtil;

public class DataStoreQueryTableModel extends DataObjectRowTableModel implements
  SortableTableModel {
  private static final long serialVersionUID = 1L;

  public static JPanel createPanel(DataObjectMetaData metaData) {
    JTable table = createTable(metaData);
    final JScrollPane scrollPane = new JScrollPane(table);
    JPanel panel = new JPanel(new BorderLayout());
    panel.add(scrollPane, BorderLayout.CENTER);
    return panel;
  }

  public static DataObjectRowTable createTable(final DataObjectMetaData metaData) {
    final DataStoreQueryTableModel model = new DataStoreQueryTableModel(
      metaData);
    return new DataObjectRowTable(model);
  }

  private final DataObjectStore dataStore;

  private ResultPager<DataObject> pager;

  private SwingWorker<?, ?> pagerWorker;

  private SwingWorker<?, ?> loadObjectsWorker;

  private SwingWorker<?, ?> rowCountWorker;

  private Map<Integer, DataObject> cache = new LruMap<Integer, DataObject>(100);

  public DataStoreQueryTableModel(final DataObjectMetaData metaData) {
    this(metaData.getDataObjectStore(), metaData);
  }

  public DataStoreQueryTableModel(final DataObjectStore dataStore,
    final DataObjectMetaData metaData) {
    super(metaData);
    this.dataStore = dataStore;
    setEditable(false);
  }

  public DataObjectStore getDataStore() {
    return dataStore;
  }

  private Integer rowCount;

  @Override
  public int getRowCount() {
    synchronized (cache) {
      final ResultPager<DataObject> pager = getPager();
      if (pager == null) {
        return 0;
      } else {
        if (rowCount == null) {
          if (rowCountWorker == null) {
            rowCountWorker = SwingWorkerManager.execute("Load row count "
              + getTypeName(), this, "loadRowCount");
          }
          return 0;
        } else {
          return rowCount;
        }
      }
    }
  }

  public void loadRowCount() {
    ResultPager<DataObject> pager = getPager();
    if (pager != null) {
      rowCount = pager.getNumResults();
      rowCountWorker = null;
      fireTableDataChanged();
    }
  }

  public void loadRows() {
    if (pager != null) {
      while (!loadingRowIndexes.isEmpty()) {
        int row = CollectionUtil.get(loadingRowIndexes,0);
        if (row < getRowCount()) {
          int pageNumber = (int)Math.ceil((row + 1) / (double)100);
          if (pageNumber <= 0) {
            pageNumber = 1;
          }
          pager.setPageNumber(pageNumber);
          List<DataObject> list = pager.getList();
          int i = pager.getStartIndex()-1;
          synchronized (cache) {
            for (DataObject result : list) {
              cache.put(i, result);
              loadingRowIndexes.remove(i);
              fireTableRowsUpdated(i, i);
              i++;
            }
          }
        }
      }
    }
    loadObjectsWorker = null;
  }

  @Override
  public SortOrder setSortOrder(int column) {
    synchronized (cache) {
      if (pagerWorker != null) {
        pagerWorker.cancel(true);
        pagerWorker = null;
      }
      if (rowCountWorker != null) {
        rowCountWorker.cancel(true);
        rowCountWorker = null;
      }
    }
    SortOrder sortOrder = super.setSortOrder(column);
    createPagerWorker();
    return sortOrder;
  }

  private Set<Integer> loadingRowIndexes = new LinkedHashSet<Integer>();

  public DataObject getObject(final int row) {
    synchronized (cache) {
      DataObject object = cache.get(row);
      if (object == null) {
        loadingRowIndexes.add(row);
        if (loadObjectsWorker == null) {
          loadObjectsWorker = SwingWorkerManager.execute("Loading records "
            + getTypeName(), this, "loadRows");
        }
      }
      return object;
    }
  }

  private ResultPager<DataObject> getPager() {
    synchronized (cache) {
      if (pager == null) {
        if (pagerWorker == null) {
          createPagerWorker();
        }
      }
      return pager;
    }
  }

  private void createPagerWorker() {
    pagerWorker = SwingWorkerManager.execute("Initialize Query "
      + getTypeName(), this, "createPager");
    rowCount = null;
  }

  public String getTypeName() {
    return getMetaData().getPath();
  }

  public void createPager() {
    DataObjectMetaData metaData = getMetaData();
    final Query query = new Query(metaData);
    for (Entry<Integer, SortOrder> entry : getSortedColumns().entrySet()) {
      Integer column = entry.getKey();
      String name = getAttributeName(column);
      SortOrder sortOrder = entry.getValue();
      if (sortOrder == SortOrder.ASCENDING) {
        query.addOrderBy(name, true);
      } else if (sortOrder == SortOrder.DESCENDING) {
        query.addOrderBy(name, false);
      }
    }
    ResultPager<DataObject> pager = dataStore.page(query);
    pager.setPageSize(100);
    synchronized (cache) {
      if (this.pager != null) {
        this.pager.close();
      }
      this.pager = pager;
      cache.clear();
      pagerWorker = null;
    }
    fireTableDataChanged();
  }

}
