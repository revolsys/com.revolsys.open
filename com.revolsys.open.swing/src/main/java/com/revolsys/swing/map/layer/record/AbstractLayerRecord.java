package com.revolsys.swing.map.layer.record;

import com.revolsys.record.AbstractRecord;

public abstract class AbstractLayerRecord extends AbstractRecord implements LayerRecord {

  private final AbstractRecordLayer layer;

  public AbstractLayerRecord(final AbstractRecordLayer layer) {
    this.layer = layer;
  }

  @Override
  public AbstractRecordLayer getLayer() {
    return this.layer;
  }

}
