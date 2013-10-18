package com.revolsys.swing.map.layer.dataobject.table.model;

import java.util.ArrayList;
import java.util.List;

import javax.swing.DefaultListSelectionModel;

import com.revolsys.swing.map.layer.dataobject.AbstractDataObjectLayer;
import com.revolsys.swing.map.layer.dataobject.LayerDataObject;

public class DataObjectLayerHighlightedListSelectionModel extends
  DefaultListSelectionModel {
  private static final long serialVersionUID = 1L;

  private final DataObjectLayerTableModel model;

  public DataObjectLayerHighlightedListSelectionModel(
    final DataObjectLayerTableModel model) {
    this.model = model;
  }

  @Override
  public void addSelectionInterval(final int index0, final int index1) {
    super.addSelectionInterval(convertRowIndexToModel(index0),
      convertRowIndexToModel(index1));
    final List<LayerDataObject> records = getObjects(index0, index1);
    final AbstractDataObjectLayer layer = this.model.getLayer();
    layer.addHighlightedRecords(records);
  }

  public int convertRowIndexToModel(final int i) {
    return model.getTable().convertRowIndexToModel(i);
  }

  protected List<LayerDataObject> getObjects(final int index0, final int index1) {
    final List<LayerDataObject> records = new ArrayList<LayerDataObject>();
    for (int i = index0; i <= index1; i++) {
      final int rowIndex = convertRowIndexToModel(i);
      final LayerDataObject record = this.model.getObject(rowIndex);
      records.add(record);
    }
    return records;
  }

  @Override
  public boolean isSelectedIndex(final int index) {
    final int rowIndex = convertRowIndexToModel(index);
    final LayerDataObject record = this.model.getObject(rowIndex);
    if (record != null) {
      final AbstractDataObjectLayer layer = this.model.getLayer();
      return layer.isHighlighted(record);
    }
    return false;
  }

  @Override
  public boolean isSelectionEmpty() {
    final AbstractDataObjectLayer layer = this.model.getLayer();
    return layer.getHighlightedCount() == 0;
  }

  @Override
  public void removeSelectionInterval(final int index0, final int index1) {
    super.removeSelectionInterval(convertRowIndexToModel(index0),
      convertRowIndexToModel(index1));
    final List<LayerDataObject> records = getObjects(index0, index1);
    final AbstractDataObjectLayer layer = this.model.getLayer();
    layer.unHighlightRecords(records);
  }

  @Override
  public void setSelectionInterval(final int index0, final int index1) {
    final List<LayerDataObject> records = getObjects(index0, index1);
    final AbstractDataObjectLayer layer = this.model.getLayer();
    layer.setHighlightedRecords(records);
    super.setSelectionInterval(convertRowIndexToModel(index0),
      convertRowIndexToModel(index1));
  }
}
