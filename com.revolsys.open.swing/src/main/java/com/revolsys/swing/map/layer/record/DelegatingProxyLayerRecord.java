package com.revolsys.swing.map.layer.record;

import java.util.Map;

import com.revolsys.identifier.Identifier;
import com.revolsys.record.RecordState;

public class DelegatingProxyLayerRecord extends AbstractProxyLayerRecord {
  private Identifier identifier;

  private LayerRecord record;

  public DelegatingProxyLayerRecord(final RecordStoreLayer layer) {
    super(layer);
    this.record = new ArrayLayerRecord(layer);
  }

  public DelegatingProxyLayerRecord(final RecordStoreLayer layer,
    final Map<String, Object> values) {
    super(layer);
    this.record = new ArrayLayerRecord(layer, values);
    this.record.setState(RecordState.INITIALIZING);
    try {
      this.record.setIdentifier(null);
    } finally {
      this.record.setState(RecordState.NEW);
    }
  }

  @Override
  public Identifier getIdentifier() {
    return this.identifier;
  }

  @Override
  protected LayerRecord getLayerRecord() {
    if (this.identifier == null) {
      return this.record;
    } else {
      return super.getLayerRecord();
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

  @Override
  public void postSaveNew() {
    final RecordState state = getState();
    if (state == RecordState.PERSISTED) {
      if (this.identifier == null) {
        this.identifier = super.getIdentifier();
        if (this.identifier != null) {
          this.record = null;
        }
      }
    }
  }
}
