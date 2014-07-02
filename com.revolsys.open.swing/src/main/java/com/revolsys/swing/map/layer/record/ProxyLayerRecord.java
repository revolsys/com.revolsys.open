package com.revolsys.swing.map.layer.record;

import com.revolsys.gis.data.model.DataObjectState;
import com.revolsys.gis.data.model.RecordIdentifier;

public class ProxyLayerRecord extends AbstractLayerRecord {

  private final RecordIdentifier recordIdentifier;

  public ProxyLayerRecord(final AbstractDataObjectLayer layer,
    final RecordIdentifier recordIdentifier) {
    super(layer);
    this.recordIdentifier = recordIdentifier;
  }

  /**
   * Internal method to revert the records values to the original
   */
  @Override
  public synchronized void cancelChanges() {
    final LayerRecord layerRecord = getLayerRecord();
    layerRecord.cancelChanges();
  }

  @Override
  public void clearChanges() {
    final LayerRecord layerRecord = getLayerRecord();
    layerRecord.clearChanges();
  }

  @Override
  public void firePropertyChange(final String propertyName,
    final Object oldValue, final Object newValue) {
    final LayerRecord layerRecord = getLayerRecord();
    layerRecord.firePropertyChange(propertyName, oldValue, newValue);
  }

  @Override
  public RecordIdentifier getIdentifier() {
    return this.recordIdentifier;
  }

  protected LayerRecord getLayerRecord() {
    final AbstractDataObjectLayer layer = getLayer();
    final RecordIdentifier identifier = getIdentifier();
    final LayerRecord record = layer.getRecordById(identifier);
    if (record == null) {
      throw new IllegalStateException("Cannot find record " + getTypeName()
        + " #" + identifier);
    }
    return record;
  }

  @Override
  public <T> T getOriginalValue(final String name) {
    final LayerRecord layerRecord = getLayerRecord();
    return layerRecord.getOriginalValue(name);
  }

  @Override
  public DataObjectState getState() {
    final LayerRecord layerRecord = getLayerRecord();
    return layerRecord.getState();
  }

  @Override
  public <T> T getValue(final int index) {
    final LayerRecord layerRecord = getLayerRecord();
    return layerRecord.getValue(index);
  }

  @Override
  public boolean isDeletable() {
    final LayerRecord layerRecord = getLayerRecord();
    return layerRecord.isDeletable();
  }

  @Override
  public boolean isDeleted() {
    final LayerRecord layerRecord = getLayerRecord();
    return layerRecord.isDeleted();
  }

  @Override
  public boolean isGeometryEditable() {
    final LayerRecord layerRecord = getLayerRecord();
    return layerRecord.isGeometryEditable();
  }

  @Override
  public boolean isModified() {
    final LayerRecord layerRecord = getLayerRecord();
    return layerRecord.isModified();
  }

  @Override
  public boolean isModified(final int index) {
    final LayerRecord layerRecord = getLayerRecord();
    return layerRecord.isModified(index);
  }

  @Override
  public boolean isModified(final String name) {
    final LayerRecord layerRecord = getLayerRecord();
    return layerRecord.isModified(name);
  }

  @Override
  public boolean isSame(final LayerRecord record) {
    final LayerRecord layerRecord = getLayerRecord();
    return layerRecord.isSame(record);
  }

  @Override
  public boolean isValid(final int index) {
    final LayerRecord layerRecord = getLayerRecord();
    return layerRecord.isValid(index);
  }

  @Override
  public boolean isValid(final String attributeName) {
    final LayerRecord layerRecord = getLayerRecord();
    return layerRecord.isValid(attributeName);
  }

  @Override
  public LayerRecord revertChanges() {
    final LayerRecord layerRecord = getLayerRecord();
    return layerRecord;
  }

  @Override
  public void revertEmptyFields() {
    final LayerRecord layerRecord = getLayerRecord();
    layerRecord.revertEmptyFields();
  }

  @Override
  public void setState(final DataObjectState state) {
    final LayerRecord layerRecord = getLayerRecord();
    layerRecord.setState(state);
  }

  @Override
  public void setValue(final int index, final Object value) {
    final LayerRecord layerRecord = getLayerRecord();
    layerRecord.setValue(index, value);
  }

}
