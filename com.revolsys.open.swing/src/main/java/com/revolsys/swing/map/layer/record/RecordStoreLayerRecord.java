package com.revolsys.swing.map.layer.record;

import java.util.Map;

import com.revolsys.identifier.Identifier;
import com.revolsys.record.schema.RecordDefinition;

public class RecordStoreLayerRecord extends ArrayLayerRecord {
  private static final long serialVersionUID = 1L;

  public RecordStoreLayerRecord(final RecordStoreLayer layer) {
    super(layer);
  }

  public RecordStoreLayerRecord(final RecordStoreLayer layer,
    final Map<String, ? extends Object> values) {
    super(layer, values);
  }

  protected RecordStoreLayerRecord(final RecordStoreLayer layer,
    final RecordDefinition recordDefinition) {
    super(layer, recordDefinition);
  }

  @Override
  public LayerRecord getEventRecord() {
    return newRecordProxy();
  }

  @Override
  public RecordStoreLayer getLayer() {
    return (RecordStoreLayer)super.getLayer();
  }

  @Override
  public LayerRecord newRecordProxy() {
    final Identifier identifier = getIdentifier();
    if (identifier == null) {
      return this;
    } else {
      final RecordStoreLayer layer = getLayer();
      return layer.newProxyLayerRecord(identifier);
    }
  }
}
