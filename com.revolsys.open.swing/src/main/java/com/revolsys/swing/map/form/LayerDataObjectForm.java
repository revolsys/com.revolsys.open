package com.revolsys.swing.map.form;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.swing.map.layer.dataobject.DataObjectLayer;

@SuppressWarnings("serial")
public class LayerDataObjectForm extends DataObjectForm implements
  PropertyChangeListener {
  private final DataObjectLayer layer;


  public LayerDataObjectForm(final DataObjectLayer layer) {
    super(layer.getMetaData());
    this.layer = layer;
    DataObjectMetaData metaData = layer.getMetaData();
    addTabAllAttributes();
    getAllAttributes().setEditable(layer.isEditable());
    if (metaData.getGeometryAttributeName() != null) {
      addTabGeometry();
    }
  }

  public DataObjectLayer getLayer() {
    return layer;
  }

  @Override
  public void propertyChange(PropertyChangeEvent event) {
    if (event.getSource() == layer) {
      DataObject object = getObject();
    }
  }

}
