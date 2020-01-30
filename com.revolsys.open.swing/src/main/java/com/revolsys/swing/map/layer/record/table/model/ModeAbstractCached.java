package com.revolsys.swing.map.layer.record.table.model;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;

import com.revolsys.collection.list.ListByIndexIterator;
import com.revolsys.collection.list.Lists;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.record.Record;
import com.revolsys.record.RecordState;
import com.revolsys.record.io.RecordWriter;
import com.revolsys.record.query.Condition;
import com.revolsys.record.query.Query;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.record.schema.RecordDefinitionBuilder;
import com.revolsys.spring.resource.PathResource;
import com.revolsys.spring.resource.Resource;
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

  private Query query;

  private long lastRefreshIndex = Long.MIN_VALUE;

  private LayerRecord currentRecord;

  private int currentRowIndex;

  private ListSelectionModel selectionModel;

  protected Object querySync = new Object();

  public ModeAbstractCached(final String key, final RecordLayerTableModel model) {
    this.key = key;
    this.model = model;
    this.query = model.getFilterQuery();
  }

  @Override
  public void activate() {
    final RecordLayerTableModel model = this.model;
    this.selectionModel = newSelectionModel(model);
    final AbstractRecordLayer layer = getLayer();
    final PropertyChangeListener recordFieldListener = this::recordFieldChanged;
    layer.addPropertyChangeListener(recordFieldListener);
    addListeners( //
      Property.addListenerNewValueSource(layer, AbstractRecordLayer.RECORD_UPDATED,
        this::recordUpdated), //
      recordFieldListener, //
      Property.addListenerNewValue(model, "query", this::setQuery));
    final Query query = model.getFilterQuery();
    setQuery(query);

    for (final String propertyName : new String[] {
      AbstractRecordLayer.RECORDS_CHANGED
    }) {
      addListeners( //
        Property.addListenerRunnable(layer, propertyName, this::refresh));
    }
  }

  protected void addCachedRecord(final LayerRecord record) {
    if (record != null) {
      addCachedRecords(Collections.singletonList(record));
    }
  }

  protected void addCachedRecords(final Iterable<? extends LayerRecord> records) {
    if (records != null) {
      if (SwingUtilities.isEventDispatchThread()) {
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
          setRecordCount(this.records.size());
          this.model.fireTableRowsInserted(fromIndex, fromIndex + addCount - 1);
        }
        repaint();
      } else {
        Invoke.later(() -> addCachedRecords(records));
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
    this.records = new ArrayList<>();
    this.selectionModel = null;
  }

  @Override
  public void exportRecords(final Query query, final Object target,
    final boolean tableColumnsOnly) {
    final AbstractRecordLayer layer = getLayer();
    final List<String> fieldNames;
    if (tableColumnsOnly) {
      fieldNames = this.model.getFieldNames();
    } else {
      fieldNames = layer.getFieldNames();
    }
    final RecordDefinition layerRecordDefinition = layer.getRecordDefinition();
    if (target != null && layerRecordDefinition != null && getRecordCount() > 0) {
      final Resource resource = Resource.getResource(target);
      if (resource instanceof PathResource) {
        final PathResource pathResource = (PathResource)resource;
        pathResource.deleteDirectory();
      }
      final RecordDefinitionBuilder recordDefinitionBuilder = new RecordDefinitionBuilder(
        layerRecordDefinition, fieldNames);

      final boolean showCodeValues = this.model.isShowCodeValues();
      if (showCodeValues) {
        recordDefinitionBuilder.changeCodeFieldsToValues();
      }
      final RecordDefinition recordDefinition = recordDefinitionBuilder.getRecordDefinition();
      try (
        RecordWriter writer = RecordWriter.newRecordWriter(recordDefinition, resource)) {
        if (writer == null) {
          throw new IllegalArgumentException("Unable to create writer " + target);
        } else {
          forEachRecord(query, record -> {
            final Record writeRecord = writer.newRecord();
            if (showCodeValues) {
              final Geometry geometry = record.getGeometry();
              writeRecord.setGeometryValue(geometry);
              for (final String fieldName : recordDefinition.getFieldNames()) {
                final Object value = record.getCodeValue(fieldName);
                writeRecord.setValue(fieldName, value);
              }
            } else {
              writeRecord.setValues(record);
            }
            writer.write(writeRecord);
          });
        }
      }
    }
  }

  protected void fireRecordUpdated(final int index) {
    clearCurrentRecord();
    this.model.fireTableRowsUpdated(index, index);
  }

  protected void fireTableDataChanged() {
    clearCurrentRecord();
    this.model.fireTableDataChanged();
  }

  @Override
  public void forEachRecord(final Query query, final Consumer<? super LayerRecord> action) {
    final Condition filter = query.getWhereCondition();
    final Map<? extends CharSequence, Boolean> orderBy = query.getOrderBy();
    final AbstractRecordLayer layer = getLayer();
    final Iterable<LayerRecord> records = new ListByIndexIterator<>(this.records);
    layer.forEachRecord(records, filter, orderBy, action);
  }

  protected Condition getFilter() {
    return this.query.getWhereCondition();
  }

  @Override
  public String getKey() {
    return this.key;
  }

  public AbstractRecordLayer getLayer() {
    return this.model.getLayer();
  }

  protected Query getQuery() {
    return this.query;
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
    final List<LayerRecord> records = this.records;
    synchronized (records) {
      if (index >= 0 && index < records.size()) {
        try {
          return records.get(index);
        } catch (final ArrayIndexOutOfBoundsException e) {
          return null;
        }
      } else {
        return null;
      }
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

  protected RecordLayerTableModel getTableModel() {
    return this.model;
  }

  private int indexOf(final LayerRecord record) {
    if (record == null) {
      return -1;
    } else {
      return record.indexOf(this.records);
    }
  }

  protected PropertyChangeListener newRecordsDeletedListener(final AbstractRecordLayer layer) {
    return Property.<List<LayerRecord>> addListenerNewValueSource(layer,
      AbstractRecordLayer.RECORDS_DELETED, this::recordsDeleted);
  }

  protected ListSelectionModel newSelectionModel(final RecordLayerTableModel tableModel) {
    return new RecordLayerListSelectionModel(tableModel);
  }

  protected void queryChanged(final Query query) {
  }

  private void recordFieldChanged(final LayerRecord record, final String fieldName,
    final Object value) {
    final int rowIndex = indexOf(record);
    if (rowIndex != -1) {
      final RecordLayerTableModel model = getTableModel();
      final int fieldIndex = model.getColumnFieldIndex(fieldName);
      if (fieldIndex == -1) {
        repaint();
      } else {
        model.fireTableCellUpdated(rowIndex, fieldIndex);
      }
    }
  }

  private void recordFieldChanged(final PropertyChangeEvent event) {
    final Object source = event.getSource();
    if (source instanceof LayerRecord) {
      final String propertyName = event.getPropertyName();
      final Object newValue = event.getNewValue();
      recordFieldChanged((LayerRecord)source, propertyName, newValue);
    }
  }

  protected void recordsDeleted(final List<LayerRecord> records) {
    if (SwingUtilities.isEventDispatchThread()) {
      for (final LayerRecord record : records) {
        if (record.getLayer().isDeleted(record)) {
          removeCachedRecord(record);
        }
      }
      repaint();
    } else {
      Invoke.later(() -> recordsDeleted(records));
    }
  }

  protected void recordUpdated(final LayerRecord record) {
    repaint();
  }

  @Override
  public void refresh() {
    if (SwingUtilities.isEventDispatchThread()) {
      final long refreshIndex = getRefreshIndexNext();
      clearCurrentRecord();
      refresh(refreshIndex);
    } else {
      Invoke.later(this::refresh);
    }
  }

  public void refresh(final long refreshIndex) {
    final Supplier<List<LayerRecord>> backgroundTask = this::getRecordsForCache;

    final Consumer<List<LayerRecord>> doneTask = (records) -> {
      if (canRefreshFinish(refreshIndex)) {
        this.recordCount = 0; // Set to 0 to avoid array index exceptions
        this.records = records;
        this.recordCount = records.size();
        fireTableDataChanged();
      }
    };

    Invoke.background("Refresh table records", backgroundTask, doneTask);
  }

  protected void removeCachedRecord(final LayerRecord record) {
    if (SwingUtilities.isEventDispatchThread()) {
      for (int recordIndex = 0; recordIndex < this.recordCount;) {
        final LayerRecord record2 = this.records.get(recordIndex);
        if (record2.getState() == RecordState.DELETED || record2.isSame(record)) {
          this.records.remove(recordIndex);
          clearCurrentRecord();
          setRecordCount(this.records.size());
          this.model.fireTableRowsDeleted(recordIndex, recordIndex);
        } else {
          recordIndex++;
        }
      }
    } else {
      Invoke.later(() -> removeCachedRecord(record));
    }
  }

  protected void removeCachedRecords(final Iterable<? extends LayerRecord> records) {
    for (final LayerRecord record : records) {
      removeCachedRecord(record);
    }
  }

  public void repaint() {
    this.model.repaint();
  }

  public void setQuery(final Query query) {
    synchronized (this.querySync) {
      if (query != this.query) {
        this.query = query;
        queryChanged(query);
      }
    }
  }

  protected void setRecordCount(final int recordCount) {
    final int oldValue = getRecordCount();
    this.recordCount = recordCount;
    this.model.firePropertyChange("rowCount", oldValue, getRecordCount());
  }

}
