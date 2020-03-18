package com.revolsys.swing.map.layer.record.table.model;

import java.util.Arrays;
import java.util.Collection;

import com.revolsys.record.Record;
import com.revolsys.swing.EventQueue;
import com.revolsys.swing.map.layer.record.ListRecordLayer;
import com.revolsys.swing.map.layer.record.table.RecordLayerTable;

public class ListRecordLayerTableModel extends RecordLayerTableModel {
  private static final long serialVersionUID = 1L;

  public static RecordLayerTable newTable(final ListRecordLayer layer) {
    return newTable(layer, layer.getFieldNamesSet());
  }

  public static RecordLayerTable newTable(final ListRecordLayer layer,
    final Collection<String> fieldNames) {
    final RecordLayerTableModel model = new ListRecordLayerTableModel(layer, fieldNames);
    final RecordLayerTable table = new RecordLayerTable(model);
    table.setSortable(true);

    EventQueue.addPropertyChange(layer, "hasSelectedRecords", () -> selectionChanged(table, model));

    return table;
  }

  public static RecordLayerTable newTable(final ListRecordLayer layer, final String... fieldNames) {
    return newTable(layer, Arrays.asList(fieldNames));
  }

  public ListRecordLayerTableModel(final ListRecordLayer layer,
    final Collection<String> fieldNames) {
    super(layer, fieldNames);
    addFieldFilterMode(new ModeAllList(this));
  }

  @Override
  public void setValueAt(final Object value, final int rowIndex, final int columnIndex) {
    final Record record = getRecord(rowIndex);
    if (record != null) {
      final String name = getColumnFieldName(columnIndex);
      final Object oldValue = record.getValueByPath(name);
      record.setValue(name, value);
      firePropertyChange(record, name, oldValue, value);
    }
  }
}
