package com.revolsys.swing.map.layer.record;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
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
import com.revolsys.record.query.Query;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.record.schema.RecordDefinitionImpl;
import com.revolsys.swing.map.layer.record.table.RecordLayerTable;
import com.revolsys.swing.map.layer.record.table.RecordLayerTablePanel;
import com.revolsys.swing.map.layer.record.table.model.RecordListLayerTableModel;
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
    final LayerRecord record = newRecord(getRecordDefinition());
    record.setState(RecordState.Initializing);
    try {
      record.setValues(values);
    } finally {
      record.setState(RecordState.Persisted);
    }
    synchronized (this.records) {
      this.records.add(record);
      expandBoundingBox(record);
    }
    addToIndex(record);
  }

  @Override
  public RecordLayerTablePanel createTablePanel(final Map<String, Object> config) {
    final RecordLayerTable table = RecordListLayerTableModel.createTable(this);
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
  public int getNewRecordCount() {
    return 0;
  }

  @Override
  public int getPersistedRecordCount() {
    synchronized (this.records) {
      return this.records.size();
    }
  }

  @Override
  public int getPersistedRecordCount(final Query query) {
    final List<LayerRecord> results = query(query);
    return results.size();
  }

  @Override
  public LayerRecord getRecord(final int index) {
    if (index < 0) {
      return null;
    } else {
      synchronized (this.records) {
        return this.records.get(index);
      }
    }
  }

  @Override
  public int getRecordCount(final Query query) {
    final Predicate<Record> filter = query.getWhereCondition();
    return Predicates.count(this.records, filter);
  }

  @Override
  public List<LayerRecord> getRecords() {
    synchronized (this.records) {
      final ArrayList<LayerRecord> records = new ArrayList<>(this.records);
      records.addAll(getNewRecords());
      return records;
    }
  }

  @Override
  public boolean isEmpty() {
    return getPersistedRecordCount() + super.getNewRecordCount() <= 0;
  }

  @Override
  public LayerRecord newRecord(final Map<String, Object> values) {
    final LayerRecord record = super.newRecord(values);
    addToIndex(record);
    fireEmpty();
    return record;
  }

  @Override
  public List<LayerRecord> query(final Query query) {
    final List<LayerRecord> records = new ArrayList<>();
    final Predicate<Record> filter = query.getWhereCondition();
    for (final LayerRecord record : new ArrayList<>(this.records)) {
      if (filter.test(record)) {
        records.add(record);
      }
    }
    return records;
  }

  protected void refreshBoundingBox() {
    BoundingBox boundingBox = new BoundingBoxDoubleGf(getGeometryFactory());
    for (final LayerRecord record : getRecords()) {
      boundingBox = boundingBox.expandToInclude(record);
    }
    setBoundingBox(boundingBox);
  }

}
