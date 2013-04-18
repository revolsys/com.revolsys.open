package com.revolsys.swing.map.table;

import java.util.ArrayList;
import java.util.List;

import javax.swing.DefaultListSelectionModel;

import com.revolsys.gis.data.model.DataObject;
import com.revolsys.swing.map.layer.dataobject.DataObjectLayer;

@SuppressWarnings("serial")
public class DataObjectLayerListSelectionModel extends
  DefaultListSelectionModel {

  private final DataObjectLayerTableModel model;

  public DataObjectLayerListSelectionModel(final DataObjectLayerTableModel model) {
    this.model = model;
  }

  @Override
  public void addSelectionInterval(final int index0, final int index1) {
    super.addSelectionInterval(index0, index1);
    final List<DataObject> objects = getObjects(index0, index1);
    final DataObjectLayer layer = model.getLayer();
    layer.addSelectedObjects(objects);
  }

  protected List<DataObject> getObjects(final int index0, final int index1) {
    final List<DataObject> objects = new ArrayList<DataObject>();
    for (int i = index0; i <= index1; i++) {
      final DataObject object = model.getObject(i);
      objects.add(object);
    }
    return objects;
  }

  @Override
  public boolean isSelectedIndex(final int index) {
    final DataObject object = model.getObject(index);
    if (object != null) {
      DataObjectLayer layer = model.getLayer();
      return layer.isSelected(object);
    }
    return false;
  }

  @Override
  public boolean isSelectionEmpty() {
    final DataObjectLayer layer = model.getLayer();
    return layer.getSelectionCount() == 0;
  }

  @Override
  public void removeSelectionInterval(final int index0, final int index1) {
    super.removeSelectionInterval(index0, index1);
    final List<DataObject> objects = getObjects(index0, index1);
    final DataObjectLayer layer = model.getLayer();
    layer.unselectObjects(objects);
  }

  @Override
  public void setSelectionInterval(final int index0, final int index1) {
    super.setSelectionInterval(index0, index1);
    final List<DataObject> objects = getObjects(index0, index1);
    final DataObjectLayer layer = model.getLayer();
    layer.setSelectedObjects(objects);
  }
}
