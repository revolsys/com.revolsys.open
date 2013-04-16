package com.revolsys.swing.map.layer.dataobject;

import java.beans.PropertyChangeEvent;

import com.revolsys.gis.data.model.ArrayDataObject;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.gis.data.model.DataObjectState;
import com.revolsys.gis.model.data.equals.EqualsRegistry;

public class LayerDataObject extends ArrayDataObject {
  private static final long serialVersionUID = 1L;

  private DataObjectLayer layer;

  public LayerDataObject(DataObjectLayer layer) {
    super(layer.getMetaData());
    this.layer = layer;
  }

  public DataObjectLayer getLayer() {
    return layer;
  }

  @Override
  public void setValue(int index, Object value) {
    DataObjectLayer layer = getLayer();
    DataObjectState state = getState();
    if (state == DataObjectState.Initalizing) {
      // Allow modification on initialization
    } else if (state == DataObjectState.New) {
      if (!layer.isCanAddObjects()) {
        throw new IllegalStateException(
          "Adding new objects is not supported for layer " + layer);
      }
    } else if (state == DataObjectState.Deleted) {
      throw new IllegalStateException("Cannot edit a deleted object for layer "
        + layer);
    } else if (!layer.isCanEditObjects()) {
      throw new IllegalStateException(
        "Editing objects is not supported for layer " + layer);
    }
    final Object oldValue = getValue(index);
    if (!EqualsRegistry.INSTANCE.equals(oldValue, value)) {
      super.setValue(index, value);
      DataObjectMetaData metaData = getMetaData();
      String attributeName = metaData.getAttributeName(index);
      PropertyChangeEvent event = new PropertyChangeEvent(this, attributeName,
        oldValue, value);
      layer.propertyChange(event);
    }

  }

}
