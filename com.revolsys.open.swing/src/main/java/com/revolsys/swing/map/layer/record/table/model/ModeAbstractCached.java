package com.revolsys.swing.map.layer.record.table.model;

import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import javax.swing.ListSelectionModel;

import com.revolsys.collection.list.Lists;
import com.revolsys.record.query.Condition;
import com.revolsys.record.query.Query;
import com.revolsys.swing.map.layer.record.AbstractRecordLayer;
import com.revolsys.swing.map.layer.record.LayerRecord;
import com.revolsys.swing.parallel.Invoke;
import com.revolsys.util.Property;

public abstract class ModeAbstractCached implements TableRecordsMode {
  private final String key;

  private final AtomicLong refreshIndex = new AtomicLong(Long.MIN_VALUE + 1);

  private List<LayerRecord> records = new ArrayList<>();

  private int recordCount;

  private final RecordLayerTableModel model;

  private final List<PropertyChangeListener> listeners = new ArrayList<>();

  private long lastRefreshIndex = Long.MIN_VALUE;

  private LayerRecord currentRecord;

  private int currentRowIndex;

  private ListSelectionModel selectionModel;

  public ModeAbstractCached(final String key, final RecordLayerTableModel model) {
    this.key = key;
    this.model = model;
  }

  @Override
  public void activate() {
    this.selectionModel = newSelectionModel(this.model);
  }

  protected void addAndRemoveCachedRecords(final Iterable<? extends LayerRecord> oldRecords,
    final Iterable<? extends LayerRecord> newRecords) {
    if (newRecords == null) {
      removeCachedRecords(oldRecords);
    } else if (newRecords == Collections.emptyList()) {
      this.records = new ArrayList<>();
      setRecordCount(0);
      fireTableDataChanged();
    } else if (oldRecords == null) {
      addCachedRecords(newRecords);
      return;
    } else {
      this.records = Lists.array(newRecords);
      setRecordCount(this.records.size());
      fireTableDataChanged();
      return;
    }
  }

  protected void addCachedRecord(final LayerRecord record) {
    if (record != null) {
      addCachedRecords(Collections.singletonList(record));
    }
  }

  protected void addCachedRecords(final Iterable<? extends LayerRecord> records) {
    if (records != null) {
      final int fromIndex = this.records.size();
      int addCount = 0;
      for (final LayerRecord record : records) {
        if (canAddCachedRecord(record)) {
          final int index = record.addTo(this.records);
          if (index != -1) {
            addCount++;
          }
        }
      }
      if (addCount > 0) {
        clearCurrentRecord();
        setRecordCount(this.recordCount + addCount);
        this.model.fireTableRowsInserted(fromIndex, fromIndex + addCount - 1);
      }
    }
  }

  protected void addListeners(final PropertyChangeListener... listeners) {
    Lists.addAll(this.listeners, listeners);
  }

  protected boolean canAddCachedRecord(final LayerRecord record) {
    return true;
  }

  protected boolean canRefreshFinish(final long index) {
    if (index >= this.lastRefreshIndex) {
      this.lastRefreshIndex = index;
      return true;
    } else {
      return false;
    }
  }

  protected void clearCurrentRecord() {
    this.currentRecord = null;
    this.currentRowIndex = -1;
  }

  @Override
  public void deactivate() {
    for (final Object source : Arrays.asList(this.model, getLayer())) {
      for (final PropertyChangeListener listener : this.listeners) {
        Property.removeListener(source, listener);
      }
    }
    clearCurrentRecord();
    this.listeners.clear();
    this.recordCount = 0;
    this.records.clear();
    this.selectionModel = null;
  }

  @Override
  public void exportRecords(final Query query, final Object target) {
    final Condition filter = query.getWhereCondition();
    final Map<String, Boolean> orderBy = query.getOrderBy();
    final AbstractRecordLayer layer = getLayer();
    layer.exportRecords(this.records, filter, orderBy, target);
  }

  protected void fireTableDataChanged() {
    this.model.fireTableDataChanged();
  }

  protected Condition getFilter() {
    return this.model.getFilter();
  }

  protected Query getFilterQuery() {
    return this.model.getFilterQuery();
  }

  @Override
  public String getKey() {
    return this.key;
  }

  public AbstractRecordLayer getLayer() {
    return this.model.getLayer();
  }

  public RecordLayerTableModel getModel() {
    return this.model;
  }

  @Override
  public final LayerRecord getRecord(final int rowIndex) {
    LayerRecord record = null;
    if (rowIndex >= 0) {
      if (rowIndex == this.currentRowIndex && this.currentRecord != null) {
        record = this.currentRecord;
      } else {
        record = getRecordDo(rowIndex);
        this.currentRecord = record;
        this.currentRowIndex = rowIndex;
      }
    }
    return record;
  }

  @Override
  public int getRecordCount() {
    return this.recordCount;
  }

  protected LayerRecord getRecordDo(final int index) {
    if (index >= 0 && index < this.recordCount) {
      return this.records.get(index);
    } else {
      return null;
    }
  }

  protected List<LayerRecord> getRecordsForCache() {
    return Collections.emptyList();
  }

  protected long getRefreshIndex() {
    return this.refreshIndex.get();
  }

  protected long getRefreshIndexNext() {
    return this.refreshIndex.incrementAndGet();
  }

  @Override
  public final ListSelectionModel getSelectionModel() {
    return this.selectionModel;
  }

  @Override
  public RecordLayerTableModel getTableModel() {
    return this.model;
  }

  protected ListSelectionModel newSelectionModel(final RecordLayerTableModel tableModel) {
    return new RecordLayerListSelectionModel(tableModel);
  }

  protected void recordsDeleted(final List<LayerRecord> records) {
    boolean deleted = false;
    for (final LayerRecord record : records) {
      if (record.getLayer().isDeleted(record)) {
        if (removeCachedRecord(record)) {
          deleted = true;
        }
      }
    }
    if (deleted) {
      repaint();
    }
  }

  protected void recordUpdated(final LayerRecord record) {
    fireTableDataChanged();
  }

  @Override
  public void refresh() {
    Invoke.later(() -> {
      final long refreshIndex = getRefreshIndexNext();
      clearCurrentRecord();
      refresh(refreshIndex);
    });
  }

  public void refresh(final long refreshIndex) {
    Invoke.background("Refresh table records", this::getRecordsForCache, (records) -> {
      if (canRefreshFinish(refreshIndex)) {
        this.recordCount = 0; // Set to 0 to avoid array index exceptions
        this.records = records;
        this.recordCount = records.size();
        fireTableDataChanged();
      }
    });
  }

  protected boolean removeCachedRecord(final LayerRecord record) {
    final int index = record.removeFrom(this.records);
    if (index == -1) {
      return false;
    } else {
      clearCurrentRecord();
      setRecordCount(this.recordCount - 1);
      this.model.fireTableRowsDeleted(index, index);
      return true;
    }
  }

  protected boolean removeCachedRecords(final Iterable<? extends LayerRecord> records) {
    boolean removed = false;
    for (final LayerRecord record : records) {
      removed |= removeCachedRecord(record);
    }
    return removed;
  }

  public void repaint() {
    this.model.repaint();
  }

  protected void setRecordCount(final int recordCount) {
    final int oldValue = getRecordCount();
    this.recordCount = recordCount;
    this.model.firePropertyChange("rowCount", oldValue, getRecordCount());
  }

  protected void setRecords(final List<LayerRecord> records) {
    this.records = records;
  }
}
