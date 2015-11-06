package com.revolsys.swing.map.layer.record;

import com.revolsys.identifier.Identifier;
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
  protected void finalize() throws Throwable {
    this.identifier = removeProxiedRecordIdentifier(this.identifier);
    super.finalize();
  }

  @Override
  public Identifier getIdentifier() {
    return this.identifier;
  }

  @Override
  protected synchronized LayerRecord getProxiedRecord() {
    if (this.record != null) {
      final RecordState state = this.record.getState();
      if (state == RecordState.PERSISTED) {
        this.identifier = this.record.getIdentifier();
        if (this.identifier != null) {
          addProxiedRecordIdentifier(this.identifier);
          this.record = removeProxiedRecord(this.record);
        }
      } else {
        return this.record;
      }
    }
    return super.getProxiedRecord();
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
