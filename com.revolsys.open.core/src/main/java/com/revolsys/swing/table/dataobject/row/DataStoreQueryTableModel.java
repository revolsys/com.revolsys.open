package com.revolsys.swing.table.dataobject.row;

import java.awt.BorderLayout;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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

  ResultPager<DataObject> pager;

  SwingWorker<?, ?> pagerWorker;

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

  @Override
  public int getRowCount() {
    final ResultPager<DataObject> pager = getPager();
    if (pager == null) {
      return 0;
    } else {
      return pager.getNumResults();
    }
  }

  @Override
  public SortOrder setSortOrder(int column) {
    synchronized (cache) {
      if (pagerWorker != null) {
        pagerWorker.cancel(true);
        pagerWorker = null;
      }
    }
    SortOrder sortOrder = super.setSortOrder(column);
    createPagerWorker();
    return sortOrder;
  }

  public DataObject getObject(final int row) {
    synchronized (cache) {
      DataObject object = cache.get(row);
      if (object == null) {
        ResultPager<DataObject> pager = getPager();
        if (pager != null) {
          if (row < pager.getNumResults()) {
            pager.setPageNumber(row + 1);
            List<DataObject> list = pager.getList();
            object = list.get(0);
            cache.put(row, object);
          }
        }
      }
      return object;
    }
  }

  ResultPager<DataObject> getPager() {
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
    pagerWorker = SwingWorkerManager.execute("Load data", this, "loadData");
  }

  public void loadData() {
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
    pager.setPageSize(1);
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
