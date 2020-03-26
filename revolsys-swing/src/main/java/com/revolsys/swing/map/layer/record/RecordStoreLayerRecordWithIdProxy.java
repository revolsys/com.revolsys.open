package com.revolsys.swing.map.layer.record;

import org.jeometry.common.data.identifier.Identifier;

public class RecordStoreLayerRecordWithIdProxy extends AbstractProxyLayerRecord {

  private final RecordStoreLayerRecordWithId record;

  public RecordStoreLayerRecordWithIdProxy(final AbstractRecordLayer layer,
    final RecordStoreLayerRecordWithId record) {
    super(layer);
    this.record = record;
  }

  @Override
  public Identifier getIdentifier() {
    return this.record.getIdentifier();
  }

  @Override
  protected LayerRecord getRecordProxied() {
    return this.record;
  }
}
