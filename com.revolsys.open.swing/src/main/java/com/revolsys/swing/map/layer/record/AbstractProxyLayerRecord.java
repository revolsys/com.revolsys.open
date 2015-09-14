package com.revolsys.swing.map.layer.record;

import com.revolsys.identifier.Identifier;
import com.revolsys.record.Record;
import com.revolsys.record.RecordState;

public abstract class AbstractProxyLayerRecord extends AbstractLayerRecord {

  public AbstractProxyLayerRecord(final AbstractRecordLayer layer) {
    super(layer);
    layer.addProxyRecord(this);
  }

  @Override
  public void cancelChanges() {
    final LayerRecord record = getLayerRecord();
    if (record != null) {
      record.cancelChanges();
    }
  }

  @Override
  public void clearChanges() {
    final LayerRecord record = getLayerRecord();
    if (record != null) {
      record.clearChanges();
    }
  }

  @Override
  protected void finalize() throws Throwable {
    final AbstractRecordLayer layer = getLayer();
    layer.removeProxyRecord(this);
  }

  @Override
  public void firePropertyChange(final String propertyName, final Object oldValue,
    final Object newValue) {
    final LayerRecord record = getLayerRecord();
    if (record != null) {
      record.firePropertyChange(propertyName, oldValue, newValue);
    }
  }

  protected LayerRecord getLayerRecord() {
    final AbstractRecordLayer layer = getLayer();
    final Identifier identifier = getIdentifier();
    final LayerRecord record = layer.getCachedRecord(identifier);
    return record;
  }

  @Override
  public <T> T getOriginalValue(final String name) {
    final LayerRecord record = getLayerRecord();
    return record.getOriginalValue(name);
  }

  protected Record getRecord() {
    return getLayerRecord();
  }

  @Override
  public RecordState getState() {
    final Record record = getRecord();
    if (record == null) {
      return RecordState.Deleted;
    } else {
      return record.getState();
    }
  }

  @Override
  public <T> T getValue(final int index) {
    final Record record = getRecord();
    if (record == null) {
      return null;
    } else {
      return record.getValue(index);
    }
  }

  @Override
  public boolean isDeleted() {
    final LayerRecord record = getLayerRecord();
    if (record == null) {
      return true;
    } else {
      return record.isDeleted();
    }
  }

  @Override
  public boolean isGeometryEditable() {
    final LayerRecord record = getLayerRecord();
    return record.isGeometryEditable();
  }

  @Override
  public boolean isModified() {
    final LayerRecord record = getLayerRecord();
    return record.isModified();
  }

  @Override
  public boolean isModified(final String name) {
    final LayerRecord record = getLayerRecord();
    if (record == null) {
      return false;
    } else {
      return record.isModified(name);
    }
  }

  @Override
  public boolean isSame(final Record record) {
    final LayerRecord record2 = getLayerRecord();
    return record2.isSame(record);
  }

  @Override
  public LayerRecord revertChanges() {
    final LayerRecord record = getLayerRecord();
    return record;
  }

  @Override
  public void setState(final RecordState state) {
    final Record record = getRecord();
    if (record != null) {
      record.setState(state);
    }
  }

  @Override
  public boolean setValue(final int index, final Object value) {
    final Record record = getRecord();
    return record.setValue(index, value);
  }

}
