package com.revolsys.swing.map.layer.record;

import com.revolsys.data.identifier.Identifier;
import com.revolsys.data.record.Record;
import com.revolsys.data.record.RecordState;

public class ProxyLayerRecord extends AbstractLayerRecord {

  private final Identifier recordIdentifier;

  public ProxyLayerRecord(final AbstractDataObjectLayer layer,
    final Identifier recordIdentifier) {
    super(layer);
    this.recordIdentifier = recordIdentifier;
  }

  @Override
  public synchronized void cancelChanges() {
    final LayerRecord record = getLayerRecord();
    record.cancelChanges();
  }

  @Override
  public void clearChanges() {
    final LayerRecord record = getLayerRecord();
    record.clearChanges();
  }

  @Override
  public void firePropertyChange(final String propertyName,
    final Object oldValue, final Object newValue) {
    final LayerRecord record = getLayerRecord();
    record.firePropertyChange(propertyName, oldValue, newValue);
  }

  @Override
  public Identifier getIdentifier() {
    return this.recordIdentifier;
  }

  protected LayerRecord getLayerRecord() {
    final AbstractDataObjectLayer layer = getLayer();
    final Identifier identifier = getIdentifier();
    final LayerRecord record = layer.getCachedRecord(identifier);
    if (record == null) {
      throw new IllegalStateException("Cannot find record " + getTypeName()
        + " #" + identifier);
    }
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
    return record.getState();
  }

  @Override
  public <T> T getValue(final int index) {
    final Record record = getRecord();
    return record.getValue(index);
  }

  @Override
  public boolean isDeleted() {
    final LayerRecord record = getLayerRecord();
    return record.isDeleted();
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
    return record.isModified(name);
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
    record.setState(state);
  }

  @Override
  public void setValue(final int index, final Object value) {
    final Record record = getRecord();
    record.setValue(index, value);
  }

}
