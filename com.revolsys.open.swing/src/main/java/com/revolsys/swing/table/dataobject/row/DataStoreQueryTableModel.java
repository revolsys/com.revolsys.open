package com.revolsys.swing.table.dataobject.row;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.swing.JPanel;
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
import com.revolsys.swing.table.TablePanel;
import com.revolsys.util.CollectionUtil;

public class DataStoreQueryTableModel extends DataObjectRowTableModel implements
  SortableTableModel {
  private static final long serialVersionUID = 1L;

  public static JPanel createPanel(final DataObjectMetaData metaData) {
    final JTable table = createTable(metaData);
    return new TablePanel(table);
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

  private Integer rowCount;

  private Set<Integer> loadingRowIndexes = new LinkedHashSet<Integer>();

  public DataStoreQueryTableModel(final DataObjectMetaData metaData) {
    this(metaData.getDataObjectStore(), metaData);
  }

  public DataStoreQueryTableModel(final DataObjectMetaData metaData,
    List<String> attributeNames) {
    this(metaData.getDataObjectStore(), metaData);
    setAttributeNames(attributeNames);
    setAttributeTitles(attributeNames);
  }

  public DataStoreQueryTableModel(final DataObjectStore dataStore,
    final DataObjectMetaData metaData) {
    super(metaData);
    this.dataStore = dataStore;
    setEditable(false);
    this.query = new Query(metaData);
  }

  private Query query;

  public void setQuery(Query query) {
    if (query == null) {
      this.query = new Query(getMetaData());
    } else {
      this.query = query.clone();
    }
    createPagerWorker();
  }

  public void createPager() {
    final Query query = this.query.clone();
    for (final Entry<Integer, SortOrder> entry : getSortedColumns().entrySet()) {
      final Integer column = entry.getKey();
      final String name = getAttributeName(column);
      final SortOrder sortOrder = entry.getValue();
      if (sortOrder == SortOrder.ASCENDING) {
        query.addOrderBy(name, true);
      } else if (sortOrder == SortOrder.DESCENDING) {
        query.addOrderBy(name, false);
      }
    }
    final ResultPager<DataObject> pager = dataStore.page(query);
    pager.setPageSize(100);
    synchronized (cache) {
      if (this.pager != null) {
        this.pager.close();
      }
      loadingRowIndexes = new LinkedHashSet<Integer>();
      cache = new LruMap<Integer, DataObject>(100);
      this.pager = pager;
      rowCount = null;
      pagerWorker = null;
      loadObjectsWorker = null;
    }
    fireTableDataChanged();
  }

  private void createPagerWorker() {
    pagerWorker = SwingWorkerManager.execute("Initialize Query "
      + getTypeName(), this, "createPager");
  }

  public DataObjectStore getDataStore() {
    return dataStore;
  }

  @Override
  public DataObject getObject(final int row) {
    synchronized (cache) {
      final DataObject object = cache.get(row);
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

  public String getTypeName() {
    return getMetaData().getPath();
  }

  public void loadRowCount() {
    final ResultPager<DataObject> pager = getPager();
    if (pager != null) {
      rowCount = pager.getNumResults();
      rowCountWorker = null;
      fireTableDataChanged();
    }
  }

  public void loadRows() {
    try {
      ResultPager<DataObject> pager = this.pager;
      if (pager != null) {
        Set<Integer> rowIndexes = this.loadingRowIndexes;
        Map<Integer, DataObject> cache = this.cache;
        while (!rowIndexes.isEmpty()) {
          final int row = CollectionUtil.get(rowIndexes, 0);
          if (row < getRowCount()) {
            int pageNumber = (int)Math.ceil((row + 1) / (double)100);
            if (pageNumber <= 0) {
              pageNumber = 1;
            }
            pager.setPageNumber(pageNumber);
            final List<DataObject> list = pager.getList();
            int i = pager.getStartIndex() - 1;
            synchronized (cache) {
              for (final DataObject result : list) {
                cache.put(i, result);
                rowIndexes.remove(i);
                fireTableRowsUpdated(i, i);
                i++;
              }
            }
          }
        }
      }
    } finally {
      loadObjectsWorker = null;
    }
  }

  @Override
  public SortOrder setSortOrder(final int column) {
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
    final SortOrder sortOrder = super.setSortOrder(column);
    createPagerWorker();
    return sortOrder;
  }

}
