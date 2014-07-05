package com.revolsys.swing.map.layer.record;

import java.awt.Component;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.revolsys.data.query.Condition;
import com.revolsys.data.query.Query;
import com.revolsys.data.record.RecordState;
import com.revolsys.data.record.schema.RecordDefinition;
import com.revolsys.data.record.schema.RecordDefinitionImpl;
import com.revolsys.data.types.DataType;
import com.revolsys.jts.geom.BoundingBox;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.jts.geom.impl.BoundingBoxDoubleGf;
import com.revolsys.swing.map.layer.record.table.RecordLayerTable;
import com.revolsys.swing.map.layer.record.table.RecordLayerTablePanel;
import com.revolsys.swing.map.layer.record.table.model.RecordListLayerTableModel;

public class ListRecordLayer extends AbstractRecordLayer {

  public static RecordDefinitionImpl createMetaData(final String name,
    final GeometryFactory geometryFactory, final DataType geometryType) {
    final RecordDefinitionImpl metaData = new RecordDefinitionImpl(name);
    metaData.addAttribute("GEOMETRY", geometryType, true);
    metaData.setGeometryFactory(geometryFactory);
    return metaData;
  }

  private final List<LayerRecord> records = new ArrayList<LayerRecord>();

  public ListRecordLayer() {
  }

  public ListRecordLayer(final RecordDefinition metaData) {
    super(metaData);
    setEditable(true);
  }

  public ListRecordLayer(final Map<String, ? extends Object> properties) {
    super(properties);
  }

  public ListRecordLayer(final String name,
    final GeometryFactory geometryFactory, final DataType geometryType) {
    super(name);
    final RecordDefinitionImpl metaData = createMetaData(name,
      geometryFactory, geometryType);
    setMetaData(metaData);
  }

  @Override
  public LayerRecord createRecord(final Map<String, Object> values) {
    final LayerRecord record = super.createRecord(values);
    addToIndex(record);
    fireEmpty();
    return record;
  }

  protected void createRecordInternal(final Map<String, Object> values) {
    final LayerRecord record = createRecord(getMetaData());
    record.setState(RecordState.Initalizing);
    try {
      record.setValues(values);
    } finally {
      record.setState(RecordState.Persisted);
    }
    synchronized (records) {
      this.records.add(record);
    }
    addToIndex(record);
  }

  @Override
  public Component createTablePanel() {
    final RecordLayerTable table = RecordListLayerTableModel.createTable(this);
    return new RecordLayerTablePanel(this, table);
  }

  @Override
  protected void doDeleteRecord(final LayerRecord record) {
    this.records.remove(record);
    super.doDeleteRecord(record);
    saveChanges(record);
    fireEmpty();
  }

  @Override
  public void deleteRecords(final Collection<? extends LayerRecord> records) {
    if (isCanDeleteRecords()) {
      super.deleteRecords(records);
      synchronized (this.records) {
        this.records.removeAll(records);
      }
      removeFromIndex(records);
      fireRecordsChanged();
    }
  }

  @SuppressWarnings({
    "unchecked", "rawtypes"
  })
  @Override
  public List<LayerRecord> doQuery(final BoundingBox boundingBox) {
    final double width = boundingBox.getWidth();
    final double height = boundingBox.getHeight();
    if (boundingBox.isEmpty() || width == 0 || height == 0) {
      return Collections.emptyList();
    } else {
      final GeometryFactory geometryFactory = getGeometryFactory();
      final BoundingBox convertedBoundingBox = boundingBox.convert(geometryFactory);
      final List<LayerRecord> records = (List)getIndex().queryIntersects(
        convertedBoundingBox);
      return records;
    }
  }

  @SuppressWarnings({
    "unchecked", "rawtypes"
  })
  @Override
  public List<LayerRecord> doQuery(Geometry geometry, final double distance) {
    geometry = getGeometryFactory().geometry(geometry);
    return (List)getIndex().queryDistance(geometry, distance);
  }

  @Override
  protected List<LayerRecord> doQuery(final Query query) {
    final Condition whereCondition = query.getWhereCondition();
    if (whereCondition == null) {
      return new ArrayList<LayerRecord>(records);
    } else {
      final List<LayerRecord> records = new ArrayList<LayerRecord>();
      for (final LayerRecord record : new ArrayList<LayerRecord>(
        this.records)) {
        if (whereCondition.accept(record)) {
          records.add(record);
        }
      }
      return records;
    }
  }

  @Override
  protected boolean doSaveChanges(final LayerRecord record) {
    if (record.isDeleted()) {
      return true;
    } else {
      return super.doSaveChanges(record);
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
  public BoundingBox getBoundingBox() {
    BoundingBox boundingBox = new BoundingBoxDoubleGf(getGeometryFactory());
    for (final LayerRecord record : getRecords()) {
      boundingBox = boundingBox.expandToInclude(record);
    }
    return boundingBox;
  }

  @Override
  public int getNewRecordCount() {
    return 0;
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
  public List<LayerRecord> getRecords() {
    synchronized (this.records) {
      final ArrayList<LayerRecord> records = new ArrayList<>(
        this.records);
      records.addAll(getNewRecords());
      return records;
    }
  }

  @Override
  public int getRowCount() {
    synchronized (this.records) {
      return this.records.size();
    }
  }

  @Override
  public int getRowCount(final Query query) {
    final List<LayerRecord> results = query(query);
    return results.size();
  }

  @Override
  public boolean isEmpty() {
    return getRowCount() + super.getNewRecordCount() <= 0;
  }

}
