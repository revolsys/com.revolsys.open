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

import com.revolsys.data.query.BinaryCondition;
import com.revolsys.data.query.Condition;
import com.revolsys.data.query.Q;
import com.revolsys.data.query.Query;
import com.revolsys.data.record.Record;
import com.revolsys.data.record.schema.RecordStore;
import com.revolsys.io.Reader;
import com.revolsys.util.Property;

public class RecordStoreQueryListModel implements ListModel {

  private final RecordStore recordStore;

  private final String displayFieldName;

  protected EventListenerList listDataListeners = new EventListenerList();

  private List<Record> objects = new ArrayList<Record>();

  private final List<Query> queries = new ArrayList<Query>();

  private Record selectedItem;

  private String searchText = "";

  private int maxResults = Integer.MAX_VALUE;

  public RecordStoreQueryListModel(final RecordStore recordStore,
    final String displayFieldName, final List<Query> queries) {
    this.recordStore = recordStore;
    this.queries.addAll(queries);
    this.displayFieldName = displayFieldName;
  }

  public RecordStoreQueryListModel(final RecordStore recordStore,
    final String displayFieldName, final Query... queries) {
    this(recordStore, displayFieldName, Arrays.asList(queries));
  }

  @Override
  public void addListDataListener(final ListDataListener l) {
    this.listDataListeners.add(ListDataListener.class, l);
  }

  protected void fireContentsChanged(final Object source, final int index0,
    final int index1) {
    final Object[] listeners = this.listDataListeners.getListenerList();
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

  public String getDisplayFieldName() {
    return this.displayFieldName;
  }

  @Override
  public Record getElementAt(final int index) {
    return this.objects.get(index);
  }

  public ListDataListener[] getListDataListeners() {
    return this.listDataListeners.getListeners(ListDataListener.class);
  }

  public List<Record> getObjects() {
    return this.objects;
  }

  protected List<Record> getObjects(final String searchParam) {
    if (Property.hasValue(searchParam) && searchParam.length() >= 2) {
      final Map<String, Record> allObjects = new TreeMap<String, Record>();
      for (Query query : this.queries) {
        if (allObjects.size() < this.maxResults) {
          query = query.clone();
          query.addOrderBy(this.displayFieldName, true);
          final Condition whereCondition = query.getWhereCondition();
          if (whereCondition instanceof BinaryCondition) {
            final BinaryCondition binaryCondition = (BinaryCondition)whereCondition;
            if (binaryCondition.getOperator().equalsIgnoreCase("like")) {
              final String likeString = "%"
                  + searchParam.toUpperCase().replaceAll("[^A-Z0-9 ]", "%") + "%";
              Q.setValue(0, binaryCondition, likeString);
            } else {
              Q.setValue(0, binaryCondition, searchParam);
            }
          }
          query.setLimit(this.maxResults);
          final Reader<Record> reader = this.recordStore.query(query);
          try {
            final List<Record> objects = reader.read();
            for (final Record object : objects) {
              if (allObjects.size() < this.maxResults) {
                final String key = object.getString(this.displayFieldName);
                if (!allObjects.containsKey(key)) {
                  if (searchParam.equals(key)) {
                    this.selectedItem = object;
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
      return new ArrayList<Record>(allObjects.values());
    } else {
      return Collections.emptyList();
    }
  }

  public String getSearchText() {
    return this.searchText;
  }

  public Record getSelectedItem() {
    return this.selectedItem;
  }

  @Override
  public int getSize() {
    return this.objects.size();
  }

  @Override
  public void removeListDataListener(final ListDataListener l) {
    this.listDataListeners.remove(ListDataListener.class, l);
  }

  public void setMaxResults(final int maxResults) {
    this.maxResults = maxResults;
  }

  public void setSearchText(final String searchText) {
    if (Property.hasValue(searchText)) {
      if (!this.searchText.equals(searchText)) {
        this.searchText = searchText;
        this.objects = getObjects(this.searchText);
        fireContentsChanged(this, 0, this.objects.size());
      }
    } else {
      this.searchText = "";
      this.objects = Collections.emptyList();
      fireContentsChanged(this, 0, this.objects.size());
      this.selectedItem = null;
    }
  }
}
