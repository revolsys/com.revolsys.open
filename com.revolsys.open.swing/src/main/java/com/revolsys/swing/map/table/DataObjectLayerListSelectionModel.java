package com.revolsys.swing.map.table;

import java.util.ArrayList;
import java.util.List;

import javax.swing.DefaultListSelectionModel;

import com.revolsys.swing.map.layer.dataobject.DataObjectLayer;
import com.revolsys.swing.map.layer.dataobject.LayerDataObject;

@SuppressWarnings("serial")
public class DataObjectLayerListSelectionModel extends
  DefaultListSelectionModel {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  private final DataObjectLayerTableModel model;

  public DataObjectLayerListSelectionModel(final DataObjectLayerTableModel model) {
    this.model = model;
  }

  @Override
  public void addSelectionInterval(final int index0, final int index1) {
    super.addSelectionInterval(index0, index1);
    final List<LayerDataObject> objects = getObjects(index0, index1);
    final DataObjectLayer layer = this.model.getLayer();
    layer.addSelectedRecords(objects);
  }

  protected List<LayerDataObject> getObjects(final int index0, final int index1) {
    final List<LayerDataObject> objects = new ArrayList<LayerDataObject>();
    for (int i = index0; i <= index1; i++) {
      final LayerDataObject object = this.model.getObject(i);
      objects.add(object);
    }
    return objects;
  }

  @Override
  public boolean isSelectedIndex(final int index) {
    final LayerDataObject object = this.model.getObject(index);
    if (object != null) {
      final DataObjectLayer layer = this.model.getLayer();
      return layer.isSelected(object);
    }
    return false;
  }

  @Override
  public boolean isSelectionEmpty() {
    final DataObjectLayer layer = this.model.getLayer();
    return layer.getSelectionCount() == 0;
  }

  @Override
  public void removeSelectionInterval(final int index0, final int index1) {
    super.removeSelectionInterval(index0, index1);
    final List<LayerDataObject> objects = getObjects(index0, index1);
    final DataObjectLayer layer = this.model.getLayer();
    layer.unselectRecords(objects);
  }

  @Override
  public void setSelectionInterval(final int index0, final int index1) {
    final List<LayerDataObject> objects = getObjects(index0, index1);
    final DataObjectLayer layer = this.model.getLayer();
    layer.setSelectedRecords(objects);
    super.setSelectionInterval(index0, index1);
  }
}
