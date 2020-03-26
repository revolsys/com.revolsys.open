package com.revolsys.swing.map.layer.record;

import org.jeometry.common.data.identifier.Identifier;

import com.revolsys.record.RecordState;

public class NewProxyLayerRecord extends AbstractProxyLayerRecord {
  private Identifier identifier;

  private LayerRecord record;

  public NewProxyLayerRecord(final RecordStoreLayer layer, final LayerRecord record) {
    super(layer);
    this.record = record;
    addProxiedRecord(record);
  }

  @Override
  public Identifier getIdentifier() {
    return this.identifier;
  }

  @Override
  protected LayerRecord getRecordProxied() {
    final AbstractRecordLayer layer = getLayer();
    synchronized (layer.getSync()) {
      if (this.record != null) {
        final RecordState state = this.record.getState();
        if (state == RecordState.PERSISTED) {
          this.identifier = this.record.getIdentifier();
          if (this.identifier != null) {
            this.record = removeProxiedRecord(this.record);
          }
        } else {
          return this.record;
        }
      }
      return super.getRecordProxied();
    }
  }

  @Override
  public int hashCode() {
    if (this.identifier == null) {
      return 0;
    } else {
      return this.identifier.hashCode();
    }
  }
}
