package com.revolsys.swing.map.layer.record;

import java.util.Map;

import com.revolsys.data.identifier.Identifier;
import com.revolsys.data.record.RecordState;

public class NewProxyLayerRecord extends AbstractProxyLayerRecord {

  private Identifier identifier;

  private LayerRecord record;

  public NewProxyLayerRecord(final RecordStoreLayer layer) {
    super(layer);
    this.record = new ArrayLayerRecord(layer);
  }

  public NewProxyLayerRecord(final RecordStoreLayer layer,
    final Map<String, Object> values) {
    super(layer);
    this.record = new ArrayLayerRecord(layer, values);
    this.record.setState(RecordState.Initalizing);
    try {
      this.record.setIdValue(null);
    } finally {
      this.record.setState(RecordState.New);
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
    if (state == RecordState.Persisted) {
      if (this.identifier == null) {
        this.identifier = super.getIdentifier();
        if (this.identifier != null) {
          this.record = null;
        }
      }
    }
  }
}
