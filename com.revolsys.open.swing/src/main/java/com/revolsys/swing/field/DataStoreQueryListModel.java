package com.revolsys.swing.field;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.ListModel;
import javax.swing.event.EventListenerList;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import org.springframework.util.StringUtils;

import com.revolsys.gis.data.io.DataObjectStore;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.query.Query;
import com.revolsys.io.Reader;

public class DataStoreQueryListModel implements ListModel {

  private final DataObjectStore dataStore;

  private final String displayAttributeName;

  protected EventListenerList listDataListeners = new EventListenerList();

  private List<DataObject> objects = new ArrayList<DataObject>();

  private final List<Query> queries = new ArrayList<Query>();

  public DataStoreQueryListModel(final DataObjectStore dataStore,
    final String displayAttributeName, final List<Query> queries) {
    this.dataStore = dataStore;
    this.queries.addAll(queries);
    this.displayAttributeName = displayAttributeName;
  }

  public DataStoreQueryListModel(final DataObjectStore dataStore,
    final String displayAttributeName, final Query... queries) {
    this(dataStore, displayAttributeName, Arrays.asList(queries));
  }

  @Override
  public void addListDataListener(final ListDataListener l) {
    listDataListeners.add(ListDataListener.class, l);
  }

  protected void fireContentsChanged(final Object source, final int index0,
    final int index1) {
    final Object[] listeners = listDataListeners.getListenerList();
    ListDataEvent e = null;

    for (int i = listeners.length - 2; i >= 0; i -= 2) {
      if (listeners[i] == ListDataListener.class) {
        if (e == null) {
          e = new ListDataEvent(source, ListDataEvent.CONTENTS_CHANGED, index0,
            index1);
        }
        ((ListDataListener)listeners[i + 1]).contentsChanged(e);
      }
    }
  }

  @Override
  public DataObject getElementAt(final int index) {
    return objects.get(index);
  }

  public ListDataListener[] getListDataListeners() {
    return listDataListeners.getListeners(ListDataListener.class);
  }

  private DataObject selectedItem;

  public List<DataObject> getObjects() {
    return objects;
  }

  protected List<DataObject> getObjects(final String searchParam) {
    final Map<String, DataObject> allObjects = new TreeMap<String, DataObject>();
    for (Query query : queries) {
      if (allObjects.size() < 10) {
        query = query.clone();
        query.addOrderBy(displayAttributeName, true);
        final String whereClause = query.getWhereClause();
        if (whereClause.indexOf("LIKE") == -1) {
          query.addParameter(searchParam);
        } else {
          query.addParameter(searchParam + "%");
        }
        query.setLimit(10);
        final Reader<DataObject> reader = dataStore.query(query);
        try {
          final List<DataObject> objects = reader.read();
          for (final DataObject object : objects) {
            if (allObjects.size() < 10) {
              final String key = object.getString(displayAttributeName);
              if (!allObjects.containsKey(key)) {
                if (searchParam.equalsIgnoreCase(key)) {
                  selectedItem = object;
                }
                allObjects.put(key, object);
              }
            }
          }
        } finally {
          reader.close();
        }
      }
    }
    return new ArrayList<DataObject>(allObjects.values());
  }

  public DataObject getSelectedItem() {
    return selectedItem;
  }

  @Override
  public int getSize() {
    return objects.size();
  }

  @Override
  public void removeListDataListener(final ListDataListener l) {
    listDataListeners.remove(ListDataListener.class, l);
  }

  private String searchText = "";

  public void setSearchText(final String searchText) {
    if (StringUtils.hasText(searchText)) {
      if (!this.searchText.equalsIgnoreCase(searchText)) {
        this.searchText = searchText.toUpperCase();
        objects = getObjects(this.searchText);
        fireContentsChanged(this, 0, objects.size());
      }
    } else {
      this.searchText = "";
      objects = Collections.emptyList();
      fireContentsChanged(this, 0, objects.size());
      selectedItem = null;
    }
  }

  public String getSearchText() {
    return searchText;
  }
}
