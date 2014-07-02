package com.revolsys.swing.map.layer.record;

import com.revolsys.gis.data.model.DataObjectState;

public class LoadingRecord extends AbstractLayerRecord {

  public LoadingRecord(final AbstractDataObjectLayer layer) {
    super(layer);
  }

  @Override
  public DataObjectState getState() {
    return DataObjectState.Initalizing;
  }

  @Override
  public <T> T getValue(final int index) {
    throw new UnsupportedOperationException(
        "Cannot get values for a loading record");
  }

  @Override
  public void setState(final DataObjectState state) {
    throw new UnsupportedOperationException("Cannot modify a loading record");
  }

  @Override
  public void setValue(final int index, final Object value) {
    throw new UnsupportedOperationException("Cannot modify a loading record");
  }

}
