package com.revolsys.swing.map.layer.record;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;

import com.revolsys.datatype.DataType;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.impl.BoundingBoxDoubleGf;
import com.revolsys.io.PathName;
import com.revolsys.predicate.Predicates;
import com.revolsys.record.Record;
import com.revolsys.record.RecordState;
import com.revolsys.record.Records;
import com.revolsys.record.query.Condition;
import com.revolsys.record.query.Query;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.record.schema.RecordDefinitionImpl;
import com.revolsys.swing.map.layer.record.table.RecordLayerTable;
import com.revolsys.swing.map.layer.record.table.RecordLayerTablePanel;
import com.revolsys.swing.map.layer.record.table.model.ListRecordLayerTableModel;
import com.revolsys.swing.map.layer.record.table.model.RecordSaveErrorTableModel;

public class ListRecordLayer extends AbstractRecordLayer {

  public static RecordDefinitionImpl createRecordDefinition(final String name,
    final GeometryFactory geometryFactory, final DataType geometryType) {
    final RecordDefinitionImpl recordDefinition = new RecordDefinitionImpl(
      PathName.newPathName(name));
    recordDefinition.addField("GEOMETRY", geometryType, true);
    recordDefinition.setGeometryFactory(geometryFactory);
    return recordDefinition;
  }

  private List<LayerRecord> records = new ArrayList<>();

  public ListRecordLayer() {
  }

  public ListRecordLayer(final Map<String, ? extends Object> properties) {
    super(properties);
  }

  public ListRecordLayer(final RecordDefinition recordDefinition) {
    super(recordDefinition);
    setEditable(true);
  }

  public ListRecordLayer(final String name, final GeometryFactory geometryFactory,
    final DataType geometryType) {
    super(name);
    final RecordDefinitionImpl recordDefinition = createRecordDefinition(name, geometryFactory,
      geometryType);
    setRecordDefinition(recordDefinition);
  }

  protected void clearRecords() {
    this.records.clear();
    fireEmpty();
  }

  @Override
  public ListRecordLayer clone() {
    final ListRecordLayer clone = (ListRecordLayer)super.clone();
    clone.records = new ArrayList<LayerRecord>();
    return clone;
  }

  protected void createRecordInternal(final Map<String, Object> values) {
    final LayerRecord record = newLayerRecord(getRecordDefinition());
    record.setState(RecordState.INITIALIZING);
    try {
      record.setValues(values);
    } finally {
      record.setState(RecordState.PERSISTED);
    }
    synchronized (this.records) {
      this.records.add(record);
      expandBoundingBox(record);
    }
    addToIndex(record);
  }

  @Override
  public RecordLayerTablePanel createTablePanel(final Map<String, Object> config) {
    final RecordLayerTable table = ListRecordLayerTableModel.createTable(this);
    return new RecordLayerTablePanel(this, table, config);
  }

  @Override
  public void deleteRecords(final Collection<? extends LayerRecord> records) {
    if (isCanDeleteRecords()) {
      super.deleteRecords(records);
      synchronized (this.records) {
        this.records.removeAll(records);
      }
      removeFromIndex(records);
      refreshBoundingBox();
      fireRecordsChanged();
    }
  }

  @Override
  protected void doDeleteRecord(final LayerRecord record) {
    this.records.remove(record);
    super.doDeleteRecord(record);
    saveChanges(record);
    refreshBoundingBox();
    fireEmpty();
  }

  @Override
  protected void doRefresh() {
    super.doRefresh();
    setIndexRecords(getRecords());
  }

  @Override
  protected boolean doSaveChanges(final RecordSaveErrorTableModel errors,
    final LayerRecord record) {
    if (record.isDeleted()) {
      return true;
    } else {
      return super.doSaveChanges(errors, record);
    }
  }

  protected void expandBoundingBox(final LayerRecord record) {
    if (record != null) {
      BoundingBox boundingBox = getBoundingBox();
      if (boundingBox.isEmpty()) {
        boundingBox = Records.boundingBox(record);
      } else {
        boundingBox = boundingBox.expandToInclude(record);
      }
      setBoundingBox(boundingBox);
    }
  }

  public void fireEmpty() {
    final boolean empty = isEmpty();
    firePropertyChange("empty", !empty, empty);
  }

  @Override
  protected void fireRecordsChanged() {
    super.fireRecordsChanged();
    fireEmpty();
  }

  @Override
  public void forEach(final Query query, final Consumer<LayerRecord> consumer) {
    final List<LayerRecord> records = getPersistedRecords(query);
    records.forEach(consumer);
  }

  @Override
  public int getNewRecordCount() {
    return 0;
  }

  @Override
  public int getPersistedRecordCount() {
    return this.records.size();
  }

  @Override
  public List<LayerRecord> getPersistedRecords(final Query query) {
    final List<LayerRecord> records = getRecords();
    final Condition filter = query.getWhereCondition();
    final Map<String, Boolean> orderBy = query.getOrderBy();
    Records.filterAndSort(records, filter, orderBy);
    return records;
  }

  @Override
  public LayerRecord getRecord(final int index) {
    if (index >= 0) {
      synchronized (this.records) {
        if (index < this.records.size()) {
          return this.records.get(index);
        }
      }
    }
    return null;
  }

  @Override
  public int getRecordCount() {
    return this.records.size();
  }

  @Override
  public int getRecordCount(final Query query) {
    synchronized (this.records) {
      final Predicate<Record> filter = query.getWhereCondition();
      return Predicates.count(this.records, filter);
    }
  }

  @Override
  public int getRecordCountPersisted(final Query query) {
    final Condition filter = query.getWhereCondition();
    if (filter.isEmpty()) {
      return getPersistedRecordCount();
    } else {
      int count = 0;
      final List<LayerRecord> records = getRecords();
      for (final LayerRecord record : records) {
        if (filter.test(record)) {
          count++;
        }
      }
      return count;
    }
  }

  @Override
  public List<LayerRecord> getRecords() {
    synchronized (this.records) {
      return new ArrayList<>(this.records);
    }
  }

  @Override
  public boolean isEmpty() {
    return getPersistedRecordCount() + super.getNewRecordCount() <= 0;
  }

  @Override
  public LayerRecord newLayerRecord(final Map<String, Object> values) {
    final LayerRecord record = super.newLayerRecord(values);
    addToIndex(record);
    fireEmpty();
    return record;
  }

  protected void refreshBoundingBox() {
    BoundingBox boundingBox = new BoundingBoxDoubleGf(getGeometryFactory());
    for (final LayerRecord record : getRecords()) {
      boundingBox = boundingBox.expandToInclude(record);
    }
    setBoundingBox(boundingBox);
  }

}
