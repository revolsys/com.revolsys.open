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

  private DataObjectStore dataStore;

  public DataStoreSearchComboBoxModel(DataObjectStore dataStore,
    String tableName, String whereClause) {
    super(null, false);
    this.dataStore = dataStore;
    this.tableName = tableName;
    this.whereClause = whereClause;
  }

  private String searchText;

  private String tableName;

  public void updateModel(String text) {
    if (!StringUtils.hasText(text)) {
      setPager(null);
    } else if (searchText == null || !searchText.equalsIgnoreCase(text)) {
      searchText = text;
      ResultPager<DataObject> pager = getNameResultPager(text);
      setPager(pager);
    }
  }

  public ResultPager<DataObject> getNameResultPager(String text) {
    Query query = new Query(tableName);
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
    ResultPager<DataObject> pager = dataStore.page(query);
    return pager;
  }

}
