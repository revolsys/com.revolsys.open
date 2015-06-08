package com.revolsys.swing.map.layer.record.table.model;

import java.util.ArrayList;
import java.util.List;

import javax.swing.DefaultListSelectionModel;

import com.revolsys.swing.map.layer.record.AbstractRecordLayer;
import com.revolsys.swing.map.layer.record.LayerRecord;

public class RecordLayerHighlightedListSelectionModel extends DefaultListSelectionModel {
  private static final long serialVersionUID = 1L;

  private final RecordLayerTableModel model;

  public RecordLayerHighlightedListSelectionModel(final RecordLayerTableModel model) {
    this.model = model;
  }

  @Override
  public void addSelectionInterval(final int index0, final int index1) {
    super.addSelectionInterval(convertRowIndexToModel(index0), convertRowIndexToModel(index1));
    final List<LayerRecord> records = getObjects(index0, index1);
    final AbstractRecordLayer layer = this.model.getLayer();
    layer.addHighlightedRecords(records);
  }

  public int convertRowIndexToModel(final int i) {
    return this.model.getTable().convertRowIndexToModel(i);
  }

  protected List<LayerRecord> getObjects(final int index0, final int index1) {
    final List<LayerRecord> records = new ArrayList<LayerRecord>();
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
      return layer.isHighlighted(record);
    }
    return false;
  }

  @Override
  public boolean isSelectionEmpty() {
    final AbstractRecordLayer layer = this.model.getLayer();

    return layer.getHighlightedCount() == 0;
  }

  @Override
  public void removeSelectionInterval(final int index0, final int index1) {
    super.removeSelectionInterval(convertRowIndexToModel(index0), convertRowIndexToModel(index1));
    final List<LayerRecord> records = getObjects(index0, index1);
    final AbstractRecordLayer layer = this.model.getLayer();
    layer.unHighlightRecords(records);
  }

  @Override
  public void setSelectionInterval(final int index0, final int index1) {
    final List<LayerRecord> records = getObjects(index0, index1);
    final AbstractRecordLayer layer = this.model.getLayer();
    layer.setHighlightedRecords(records);
    super.setSelectionInterval(convertRowIndexToModel(index0), convertRowIndexToModel(index1));
  }
}
