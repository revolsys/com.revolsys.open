package com.revolsys.swing.map.layer.record.table.model;

import java.util.ArrayList;
import java.util.List;

import javax.swing.DefaultListSelectionModel;

import com.revolsys.swing.map.layer.record.AbstractRecordLayer;
import com.revolsys.swing.map.layer.record.LayerRecord;
import com.revolsys.swing.map.layer.record.table.RecordLayerTable;

public class RecordLayerListSelectionModel extends DefaultListSelectionModel {
  private static final long serialVersionUID = 1L;

  private final RecordLayerTableModel model;

  public RecordLayerListSelectionModel(final RecordLayerTableModel model) {
    this.model = model;
  }

  @Override
  public void addSelectionInterval(final int index0, final int index1) {
    final List<LayerRecord> records = getRecords(index0, index1);
    final AbstractRecordLayer layer = this.model.getLayer();
    layer.addSelectedRecords(records);
    super.addSelectionInterval(index0, index1);
  }

  public int convertRowIndexToModel(final int i) {
    final RecordLayerTable table = this.model.getTable();
    return table.convertRowIndexToModel(i);
  }

  protected List<LayerRecord> getRecords(final int index0, final int index1) {
    final List<LayerRecord> records = new ArrayList<>();
    for (int i = index0; i <= index1; i++) {
      final int rowIndex = convertRowIndexToModel(i);
      final LayerRecord record = this.model.getRecord(rowIndex);
      records.add(record);
    }
    return records;
  }

  @Override
  public boolean isSelectedIndex(final int index) {
    final int rowIndex = convertRowIndexToModel(index);
    final LayerRecord record = this.model.getRecord(rowIndex);
    if (record != null) {
      final AbstractRecordLayer layer = this.model.getLayer();

      final boolean selected = layer.isSelected(record);
      return selected;
    }
    return false;
  }

  @Override
  public boolean isSelectionEmpty() {
    final AbstractRecordLayer layer = this.model.getLayer();
    return layer.getSelectedRecordsCount() == 0;
  }

  @Override
  public void removeSelectionInterval(final int index0, final int index1) {
    final List<LayerRecord> records = getRecords(index0, index1);
    final AbstractRecordLayer layer = this.model.getLayer();
    layer.unSelectRecords(records);
    super.removeSelectionInterval(index0, index1);
  }

  @Override
  public void setSelectionInterval(int index0, final int index1) {
    if (index0 == -1 || index1 == -1) {
      return;
    } else if (getSelectionMode() == SINGLE_SELECTION) {
      index0 = index1;
    }
    final boolean valueIsAdjusting = getValueIsAdjusting();
    try {
      setValueIsAdjusting(true);
      final List<LayerRecord> records = getRecords(index0, index1);
      final AbstractRecordLayer layer = this.model.getLayer();
      layer.setSelectedRecords(records);
      super.setSelectionInterval(index0, index1);
    } finally {
      setValueIsAdjusting(valueIsAdjusting);
    }
  }
}
