package com.revolsys.swing.map.layer.record;

public class RecordStoreLayerRecordWithId extends RecordStoreLayerRecord {

  private RecordStoreLayerRecordWithIdProxy proxyRecord;

  public RecordStoreLayerRecordWithId(final RecordStoreLayer layer) {
    super(layer);
  }

  @SuppressWarnings("unchecked")
  @Override
  public <R extends LayerRecord> R getRecordProxy() {
    if (this.proxyRecord == null) {
      final RecordStoreLayer layer = getLayer();
      synchronized (layer.getRecordCacheSync()) {
        if (this.proxyRecord == null) {
          this.proxyRecord = new RecordStoreLayerRecordWithIdProxy(this.layer, this);
        }

      }
    }
    return (R)this.proxyRecord;
  }
}
