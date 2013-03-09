package com.revolsys.swing.field;

import org.springframework.util.StringUtils;

import com.revolsys.collection.ResultPager;
import com.revolsys.gis.data.io.DataObjectStore;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.query.Query;

public class DataStoreSearchComboBoxModel extends
  ResultPagerComboBoxModel<DataObject> {
  private static final long serialVersionUID = 1L;

  private String whereClause = "UPPER(FULL_NAME) LIKE ?";

  private final DataObjectStore dataStore;

  private String searchText;

  private final String tableName;

  private String orderBy;

  public DataStoreSearchComboBoxModel(final DataObjectStore dataStore,
    final String tableName, final String whereClause, String orderBy) {
    super(null, false);
    this.dataStore = dataStore;
    this.tableName = tableName;
    this.whereClause = whereClause;
    this.orderBy = orderBy;
  }

  public ResultPager<DataObject> getNameResultPager(final String text) {
    final Query query = new Query(tableName);
    if (StringUtils.hasText(orderBy)) {
      query.setOrderByColumns(orderBy);
    }
    if (StringUtils.hasText(text)) {
      query.setWhereClause(whereClause);
      int index = 0;
      do {
        index = whereClause.indexOf('?', index);
        if (index != -1) {
          query.addParameter("%" + text.toUpperCase() + "%");
          index++;
        }
      } while (index != -1);
    }
    final ResultPager<DataObject> pager = dataStore.page(query);
    return pager;
  }

  public void updateModel(final String text) {
    if (!StringUtils.hasText(text)) {
      setPager(null);
    } else if (searchText == null || !searchText.equalsIgnoreCase(text)) {
      searchText = text;
      final ResultPager<DataObject> pager = getNameResultPager(text);
      setPager(pager);
    }
  }

}
