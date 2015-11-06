package com.revolsys.swing.map.layer.record;

import com.revolsys.record.AbstractRecord;

public abstract class AbstractLayerRecord extends AbstractRecord implements LayerRecord {

  private AbstractRecordLayer layer;

  public AbstractLayerRecord(final AbstractRecordLayer layer) {
    this.layer = layer;
  }

  @Override
  protected void finalize() throws Throwable {
    super.finalize();
    this.layer = null;
  }

  @Override
  public AbstractRecordLayer getLayer() {
    return this.layer;
  }
}
