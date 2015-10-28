package com.revolsys.swing.map.layer.record.table.model;

import java.beans.PropertyChangeListener;

import com.revolsys.record.Record;
import com.revolsys.swing.EventQueue;
import com.revolsys.swing.map.layer.record.AbstractRecordLayer;
import com.revolsys.swing.map.layer.record.ListRecordLayer;
import com.revolsys.swing.map.layer.record.table.RecordLayerTable;

public class ListRecordLayerTableModel extends RecordLayerTableModel
  implements PropertyChangeListener {
  private static final long serialVersionUID = 1L;

  public static RecordLayerTable createTable(final ListRecordLayer layer) {
    final RecordLayerTableModel model = new ListRecordLayerTableModel(layer);
    final RecordLayerTable table = new RecordLayerTable(model);

    EventQueue.addPropertyChange(layer, "hasSelectedRecords", () -> selectionChanged(table, model));

    return table;
  }

  public ListRecordLayerTableModel(final ListRecordLayer layer) {
    super(layer);
    setEditable(false);
    setSortableModes(MODE_SELECTED_RECORDS, MODE_ALL_RECORDS);
    setFilterModeMethods(MODE_ALL_RECORDS, this::refreshAllRecords, this::getRowCountFieldValue,
      this::getRecordCached, this::exportRecordsCached);
  }

  @Override
  protected void refreshAllRecords(final long index) {
    final AbstractRecordLayer layer = getLayer();
    if (layer != null) {
      refreshCachedRecords(index, layer::getRecords);
    }
  }

  @Override
  public void setValueAt(final Object value, final int rowIndex, final int columnIndex) {
    final Record record = getRecord(rowIndex);
    if (record != null) {
      final String name = getFieldName(columnIndex);
      final Object oldValue = record.getValueByPath(name);
      record.setValue(name, value);
      firePropertyChange(record, name, oldValue, value);
    }
  }
}
