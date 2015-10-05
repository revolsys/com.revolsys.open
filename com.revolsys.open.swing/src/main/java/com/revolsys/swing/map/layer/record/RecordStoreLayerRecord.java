package com.revolsys.swing.map.layer.record;

import java.beans.PropertyChangeEvent;
import java.util.Map;

import com.revolsys.identifier.Identifier;
import com.revolsys.record.Record;
import com.revolsys.swing.map.layer.AbstractLayer;

public class RecordStoreLayerRecord extends ArrayLayerRecord {
  private static final long serialVersionUID = 1L;

  public RecordStoreLayerRecord(final AbstractRecordLayer layer) {
    super(layer);
  }

  public RecordStoreLayerRecord(final AbstractRecordLayer layer,
    final Map<String, ? extends Object> values) {
    super(layer, values);
  }

  @Override
  public void firePropertyChange(final String fieldName, final Object oldValue,
    final Object newValue) {
    final AbstractLayer layer = getLayer();
    if (layer.isEventsEnabled()) {
      final Record proxyLayerRecord = getProxyLayerRecord();
      final PropertyChangeEvent event = new PropertyChangeEvent(proxyLayerRecord, fieldName,
        oldValue, newValue);
      layer.propertyChange(event);
    }
  }

  @Override
  public RecordStoreLayer getLayer() {
    return (RecordStoreLayer)super.getLayer();
  }

  @SuppressWarnings("unchecked")
  protected <V extends Record> V getProxyLayerRecord() {
    final Identifier identifier = getIdentifier();
    if (identifier == null) {
      return null;
    } else {
      final RecordStoreLayer layer = getLayer();
      return (V)layer.createProxyRecord(identifier);
    }
  }
}
