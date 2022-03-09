package com.revolsys.swing.map.layer.record;

import com.revolsys.record.Record;

public class RecordStoreLayerRecordNoId extends RecordStoreLayerRecord {

  public RecordStoreLayerRecordNoId(final RecordStoreLayer layer) {
    super(layer);
  }

  @SuppressWarnings("unchecked")
  @Override
  public <R extends Record> R getRecordProxy() {
    return (R)this;
  }
}
