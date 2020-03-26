package com.revolsys.swing.map.layer.record;

import java.util.Collection;
import java.util.Map;

import org.jeometry.common.data.identifier.Identifier;

import com.revolsys.record.Record;
import com.revolsys.record.RecordState;
import com.revolsys.record.schema.FieldDefinition;

public abstract class AbstractProxyLayerRecord extends AbstractLayerRecord {
  public AbstractProxyLayerRecord(final AbstractRecordLayer layer) {
    super(layer);
  }

  protected <R extends LayerRecord> R addProxiedRecord(final R record) {
    if (record != null) {
      final AbstractRecordLayer layer = getLayer();
      layer.addProxiedRecord(record);
    }
    return record;
  }

  @Override
  public boolean cancelChanges() {
    final LayerRecord layerRecord = getRecordProxied();
    if (layerRecord != null) {
      return layerRecord.cancelChanges();
    }
    return false;
  }

  @Override
  public void clearChanges() {
    final LayerRecord layerRecord = getRecordProxied();
    if (layerRecord != null) {
      layerRecord.clearChanges();
    }
  }

  @Override
  public boolean equals(final Object o) {
    final LayerRecord recordProxied = getRecordProxied();
    if (recordProxied == null) {
      return super.equals(o);
    } else {
      return recordProxied.equals(o);
    }
  }

  @Override
  public void firePropertyChange(final String propertyName, final Object oldValue,
    final Object newValue) {
    final LayerRecord layerRecord = getRecordProxied();
    if (layerRecord != null) {
      layerRecord.firePropertyChange(propertyName, oldValue, newValue);
    }
  }

  @Override
  public <T> T getOriginalValue(final String name) {
    final LayerRecord layerRecord = getRecordProxied();
    if (layerRecord == null) {
      return null;
    } else {
      return layerRecord.getOriginalValue(name);
    }
  }

  protected Record getRecord() {
    return getRecordProxied();
  }

  protected LayerRecord getRecordProxied() {
    final AbstractRecordLayer layer = getLayer();
    final Identifier identifier = getIdentifier();
    final LayerRecord layerRecord = layer.getCachedRecord(identifier);
    return layerRecord;
  }

  @Override
  public RecordState getState() {
    final Record record = getRecord();
    if (record == null) {
      return RecordState.DELETED;
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
  public int hashCode() {
    final LayerRecord recordProxied = getRecordProxied();
    if (recordProxied == null) {
      return super.hashCode();
    } else {
      return recordProxied.hashCode();
    }
  }

  @Override
  public boolean isDeleted() {
    final LayerRecord record = getRecordProxied();
    if (record == null) {
      return true;
    } else {
      return record.isDeleted();
    }
  }

  @Override
  public boolean isGeometryEditable() {
    final LayerRecord record = getRecordProxied();
    return record.isGeometryEditable();
  }

  @Override
  public boolean isModified() {
    final LayerRecord record = getRecordProxied();
    if (record == null) {
      return false;
    } else {
      return record.isModified();
    }
  }

  @Override
  public boolean isModified(final String name) {
    final LayerRecord record = getRecordProxied();
    if (record == null) {
      return false;
    } else {
      return record.isModified(name);
    }
  }

  @Override
  public boolean isProxyRecord() {
    return true;
  }

  @Override
  public boolean isSame(Record record) {
    if (record == null) {
      return false;
    } else if (record == this) {
      return true;
    } else {
      if (record instanceof AbstractProxyLayerRecord) {
        final AbstractProxyLayerRecord proxyRecord = (AbstractProxyLayerRecord)record;
        record = proxyRecord.getRecordProxied();
      }
      final LayerRecord layerRecord = getRecordProxied();
      if (layerRecord == null) {
        return false;
      } else {
        return layerRecord.isSame(record);
      }
    }
  }

  @Override
  public boolean isState(final RecordState state) {
    final Record record = getRecord();
    if (record == null) {
      return state == RecordState.DELETED;
    } else {
      return record.isState(state);
    }
  }

  protected <R extends LayerRecord> R removeProxiedRecord(final R record) {
    final AbstractRecordLayer layer = getLayer();
    layer.removeProxiedRecord(record);
    return null;
  }

  @Override
  public LayerRecord revertChanges() {
    final LayerRecord layerRecord = getRecordProxied();
    return layerRecord.revertChanges();
  }

  @Override
  public RecordState setState(final RecordState state) {
    final Record record = getRecord();
    if (record == null) {
      return RecordState.DELETED;
    } else {
      return record.setState(state);
    }
  }

  public boolean setStateDeleted() {
    // TODO might need some work
    final Record record = getRecord();
    if (record != null) {
      record.setState(RecordState.DELETED);
    }
    return true;
  }

  @Override
  protected boolean setValue(final FieldDefinition fieldDefinition, final Object value) {
    final int index = fieldDefinition.getIndex();
    final Record record = getRecord();
    return record.setValue(index, value);
  }

  @Override
  public void setValues(final Map<? extends CharSequence, ? extends Object> values,
    final Collection<? extends CharSequence> fieldNames) {
    final Record record = getRecord();
    record.setValues(values, fieldNames);
  }
}
