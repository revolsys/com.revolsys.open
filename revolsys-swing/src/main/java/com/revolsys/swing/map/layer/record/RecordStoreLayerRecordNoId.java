package com.revolsys.swing.map.layer.record;

public class RecordStoreLayerRecordNoId extends RecordStoreLayerRecord {

  public RecordStoreLayerRecordNoId(final RecordStoreLayer layer) {
    super(layer);
  }

  @SuppressWarnings("unchecked")
  @Override
  public <R extends LayerRecord> R getRecordProxy() {
    return (R)this;
  }
}
