package com.revolsys.swing.map.layer.record.table.model;

import java.util.ArrayList;
import java.util.List;

import javax.swing.DefaultListSelectionModel;

import com.revolsys.swing.map.layer.record.AbstractRecordLayer;
import com.revolsys.swing.map.layer.record.LayerRecord;
import com.revolsys.swing.map.layer.record.table.RecordLayerTable;

public class RecordLayerHighlightedListSelectionModel extends DefaultListSelectionModel {
  private static final long serialVersionUID = 1L;

  private final RecordLayerTableModel model;

  private final AbstractRecordLayer layer;

  public RecordLayerHighlightedListSelectionModel(final RecordLayerTableModel model) {
    this.model = model;
    this.layer = model.getLayer();
  }

  @Override
  public void addSelectionInterval(final int rowIndexFrom, final int rowIndexTo) {
    super.addSelectionInterval(rowIndexFrom, rowIndexTo);
    final List<LayerRecord> records = getRecords(rowIndexFrom, rowIndexTo);
    this.layer.addHighlightedRecords(records);
  }

  private LayerRecord getRecord(final int viewRowIndex) {
    final RecordLayerTableModel model = this.model;
    final RecordLayerTable table = model.getTable();
    final int rowIndex = table.convertRowIndexToModel(viewRowIndex);
    return model.getRecord(rowIndex);
  }

  protected List<LayerRecord> getRecords(final int rowIndexFrom, final int rowIndexTo) {
    final List<LayerRecord> records = new ArrayList<>();
    for (int viewRowIndex = rowIndexFrom; viewRowIndex <= rowIndexTo; viewRowIndex++) {
      final LayerRecord record = getRecord(viewRowIndex);
      records.add(record);
    }
    return records;
  }

  @Override
  public boolean isSelectedIndex(final int rowIndex) {
    final LayerRecord record = getRecord(rowIndex);
    if (record != null) {
      return this.layer.isHighlighted(record);
    }
    return false;
  }

  @Override
  public boolean isSelectionEmpty() {
    return this.layer.getHighlightedCount() == 0;
  }

  @Override
  public void removeSelectionInterval(final int rowIndexFrom, final int rowIndexTo) {
    super.removeSelectionInterval(rowIndexFrom, rowIndexTo);
    final List<LayerRecord> records = getRecords(rowIndexFrom, rowIndexTo);
    this.layer.unHighlightRecords(records);
  }

  @Override
  public void setSelectionInterval(final int rowIndexFrom, final int rowIndexTo) {
    final List<LayerRecord> records = getRecords(rowIndexFrom, rowIndexTo);
    this.layer.setHighlightedRecords(records);
    super.setSelectionInterval(rowIndexFrom, rowIndexTo);
  }
}
