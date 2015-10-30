package com.revolsys.swing.map.layer.record;

import java.util.Map;

import com.revolsys.identifier.Identifier;

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
  public LayerRecord getEventRecord() {
    final Identifier identifier = getIdentifier();
    if (identifier == null) {
      return this;
    } else {
      final RecordStoreLayer layer = getLayer();
      return layer.newProxyRecord(identifier);
    }
  }

  @Override
  public RecordStoreLayer getLayer() {
    return (RecordStoreLayer)super.getLayer();
  }
}
